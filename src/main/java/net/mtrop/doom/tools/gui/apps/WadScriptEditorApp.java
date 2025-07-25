/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadScriptSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorDirectoryTreePanel;
import net.mtrop.doom.tools.gui.swing.panels.WadScriptExecuteWithArgsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DirectoryTreePanel.DirectoryTreeListener;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.FileUtils.TempFile;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The WadScript editor application.
 * @author Matthew Tropiano
 */
public class WadScriptEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(WadScriptEditorApp.class); 

	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	private static final String EMPTY_SCRIPT = (new StringBuilder())
		.append("/*****************************************************************************\n")
		.append(" * WadScript Script File\n")
		.append(" *****************************************************************************/\n")
		.append("\n")
		.append("/**\n")
		.append(" * Main entry point.\n")
		.append(" */\n")
		.append("entry main(args) {\n")
		.append("\n")
		.append("}\n")
	.toString();
	
	// Singletons

	private WadScriptSettingsManager settings;
	
	// Referenced Components
	
	private JSplitPane splitPaneHorizontal;
	private EditorDirectoryTreePanel treePanel;
	private WadScriptEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;

	private Action runAction;
	private Action runParametersAction;
	
	// State
	
	private File fileToOpenFirst;
	private EditorHandle currentHandle;
	private Map<EditorHandle, ScriptExecutionSettings> handleToSettingsMap;
	
	// ...

	/**
	 * Create a new WadScript application.
	 */
	public WadScriptEditorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WadScript application.
	 * @param fileToOpenFirst if not null, open this file on create.
	 */
	public WadScriptEditorApp(File fileToOpenFirst) 
	{
		this.settings = WadScriptSettingsManager.get();
		
		this.editorPanel = new WadScriptEditorPanel(new EditorMultiFilePanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return true;
			}

			@Override
			public boolean hideTreeActions()
			{
				return false;
			}
		}, 
		new WadScriptEditorPanel.Listener()
		{
			@Override
			public void onCurrentEditorChange(EditorHandle previous, EditorHandle next) 
			{
				currentHandle = next;
				onHandleChange();
			}

			@Override
			public void onSave(EditorHandle handle) 
			{
				File sourceFile = handle.getContentSourceFile();
				statusPanel.setSuccessMessage(language.getText("wadscript.status.message.saved", sourceFile.getName()));
				onHandleChange();
			}

			@Override
			public void onOpen(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wadscript.status.message.editor.open", handle.getEditorTabName()));
			}

			@Override
			public void onClose(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wadscript.status.message.editor.close", handle.getEditorTabName()));
				handleToSettingsMap.remove(handle);
			}

			@Override
			public void onTreeDirectoryRequest(EditorHandle handle)
			{
				File dir = handle.getContentSourceFile();
				if (dir != null)
					onOpenDirectory(dir.getParentFile());
			}

			@Override
			public void onTreeRevealRequest(EditorHandle handle) 
			{
				if (handle.getContentSourceFile() != null)
					treePanel.setSelectedFile(handle.getContentSourceFile());
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.treePanel = new WadScriptTreePanel();
		onOpenDirectory(new File(OSUtils.getWorkingDirectoryPath()));
		
		splitPaneHorizontal = split(
			containerOf(dimension(215, 500), 
				node(BorderLayout.CENTER, treePanel)
			),
			containerOf(dimension(610, 500),
				node(BorderLayout.CENTER, editorPanel)
			)
		);
		splitPaneHorizontal.setDividerLocation(215);

		this.runAction = utils.createActionFromLanguageKey("wadscript.menu.run.item.run", (e) -> onRunAgain());
		this.runParametersAction = utils.createActionFromLanguageKey("wadscript.menu.run.item.params", (e) -> onRunWithArgs());
		
		this.currentHandle = null;
		this.handleToSettingsMap = new HashMap<>();
		this.fileToOpenFirst = fileToOpenFirst;
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wadscript.editor.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(borderLayout(0, 8), 
			node(BorderLayout.CENTER, splitPaneHorizontal),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	private MenuNode[] createCommonFileMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("wadscript.menu.file.item.new",
				utils.createItemFromLanguageKey("wadscript.menu.file.item.new.item.main", (i) -> onNewEditor()),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.new.item.blank", (i) -> onNewBlankEditor())
			),
			utils.createItemFromLanguageKey("wadscript.menu.file.item.open", (i) -> onOpenEditor()),
			utils.createItemFromLanguageKey("wadscript.menu.file.item.open.directory", (i) -> onOpenDirectory()),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.close", editorPanel.getActionFor(ActionNames.ACTION_CLOSE)),
			utils.createItemFromLanguageKey("texteditor.action.closeallbutcurrent", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL_BUT_CURRENT)),
			utils.createItemFromLanguageKey("texteditor.action.closeall", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.save", editorPanel.getActionFor(ActionNames.ACTION_SAVE)),
			utils.createItemFromLanguageKey("texteditor.action.saveas", editorPanel.getActionFor(ActionNames.ACTION_SAVE_AS)),
			utils.createItemFromLanguageKey("texteditor.action.saveall", editorPanel.getActionFor(ActionNames.ACTION_SAVE_ALL))
		);
	}
	
	private MenuNode[] createCommonEditMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.undo", editorPanel.getActionFor(ActionNames.ACTION_UNDO)),
			utils.createItemFromLanguageKey("texteditor.action.redo", editorPanel.getActionFor(ActionNames.ACTION_REDO)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.cut", editorPanel.getActionFor(ActionNames.ACTION_CUT)),
			utils.createItemFromLanguageKey("texteditor.action.copy", editorPanel.getActionFor(ActionNames.ACTION_COPY)),
			utils.createItemFromLanguageKey("texteditor.action.paste", editorPanel.getActionFor(ActionNames.ACTION_PASTE)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.delete", editorPanel.getActionFor(ActionNames.ACTION_DELETE)),
			utils.createItemFromLanguageKey("texteditor.action.selectall", editorPanel.getActionFor(ActionNames.ACTION_SELECT_ALL))
		);
	}

	private MenuNode[] createWadScriptRunMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("wadscript.menu.run.item.run", runAction),
			utils.createItemFromLanguageKey("wadscript.menu.run.item.params", runParametersAction)
		);
	}

	private MenuNode[] createCommonEditorMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.goto", editorPanel.getActionFor(ActionNames.ACTION_GOTO)),
			utils.createItemFromLanguageKey("texteditor.action.find", editorPanel.getActionFor(ActionNames.ACTION_FIND)),
			separator(),
			editorPanel.getToggleLineWrapMenuItem(),
			editorPanel.getChangeEncodingMenuItem(),
			editorPanel.getChangeSpacingMenuItem(),
			editorPanel.getChangeLineEndingMenuItem(),
			separator(),
			utils.createItemFromLanguageKey("texteditor.transformer.menu.name", editorPanel.createTextTransformerMenuItems()),
			separator(),
			editorPanel.getEditorPreferencesMenuItem()
		);
	}
	
	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog()),
			separator(),
			utils.createItemFromLanguageKey("wadscript.menu.help.item.rookscript", (i) -> onRookScriptReference()),
			utils.createItemFromLanguageKey("wadscript.menu.help.item.functions", (i) -> onFunctionReference())
		); 
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file", ArrayUtils.joinArrays(
				createCommonFileMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (i) -> attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("wadscript.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.run", createWadScriptRunMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.run", createWadScriptRunMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}

	@Override
	public void onCreate(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			Rectangle bounds = settings.getBounds();
			boolean maximized = settings.getBoundsMaximized();
			f.setBounds(bounds);
			if (maximized)
				f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			splitPaneHorizontal.setDividerLocation(settings.getTreeWidth());
		}
	}
	
	@Override
	public void onOpen(Object frame) 
	{
		statusPanel.setSuccessMessage(language.getText("wadscript.status.message.ready"));
		if (editorPanel.getOpenEditorCount() == 0)
		{
			if (fileToOpenFirst != null && fileToOpenFirst.exists() && !fileToOpenFirst.isDirectory())
				onOpenFile(fileToOpenFirst);
			else
				onNewEditor();
		}
	}

	@Override
	public void onClose(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			settings.setBounds(f);
			settings.setTreeWidth(splitPaneHorizontal.getDividerLocation());
		}
	}
	
	@Override
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		if (!fromWorkspaceClear && !SwingUtils.yesTo(language.getText("doomtools.application.close")))
			return false;
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors(false);
		return true;
	}
	
	@Override
	public Map<String, String> getApplicationState() 
	{
		Map<String, String> state = super.getApplicationState();
		editorPanel.saveState("wadscript", state);
		
		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			if (currentHandle == handle)
				state.put("editor.selected", String.valueOf(i));
			
			String settingPrefix = "execution." + i;
			ScriptExecutionSettings settings = handleToSettingsMap.get(handle);
			if (settings != null)
			{
				state.put(settingPrefix + ".enabled", String.valueOf(true));
				if (settings.getWorkingDirectory() != null)
					state.put(settingPrefix + ".workdir", settings.getWorkingDirectory().getAbsolutePath());
				if (settings.getStandardInPath() != null)
					state.put(settingPrefix + ".stdin", settings.getStandardInPath().getAbsolutePath());
				state.put(settingPrefix + ".entryPoint", settings.getEntryPoint());
				state.put(settingPrefix + ".args", String.valueOf(settings.getArgs().length));
				for (int a = 0; a < settings.getArgs().length; a++)
					state.put(settingPrefix + ".args." + a, settings.getArgs()[a]);
			}
		}
		
		state.put("tree.width", String.valueOf(treePanel.getWidth()));
		state.put("tree.dir", treePanel.getRootDirectory().getAbsolutePath());
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));

		handleToSettingsMap.clear();
		editorPanel.loadState("wadscript", state);
		
		int selectedIndex = ValueUtils.parseInt(state.get("editor.selected"), -1);
		if (selectedIndex >= 0)
			editorPanel.setEditorByIndex(selectedIndex);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			String settingPrefix = "execution." + i;
			
			boolean enabled = ValueUtils.parseBoolean(state.get(settingPrefix + ".enabled"), false);
			if (enabled)
			{
				ScriptExecutionSettings settings = new ScriptExecutionSettings();
				settings.setWorkingDirectory(ValueUtils.parse(state.get(settingPrefix + ".workdir"), parseFile));
				settings.setStandardInPath(ValueUtils.parse(state.get(settingPrefix + ".stdin"), parseFile));
				settings.setEntryPoint(ValueUtils.parse(state.get(settingPrefix + ".entryPoint"), (input) -> 
					ObjectUtils.isEmpty(input) ? "" : input
				));
				
				String[] args = new String[ValueUtils.parseInt(state.get(settingPrefix + ".args"))];
				for (int a = 0; a < settings.getArgs().length; a++)
					args[a] = state.get(settingPrefix + ".args." + a);
				
				settings.setArgs(args);
				handleToSettingsMap.put(handle, settings);
			}
		}
	}
	
	// ====================================================================

	private void onHandleChange()
	{
		runAction.setEnabled(currentHandle != null);
		runParametersAction.setEnabled(currentHandle != null);
	}
	
	private void onNewEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, EMPTY_SCRIPT, Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT);
	}
	
	private void onNewBlankEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, "", Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT, 0);
	}
	
	private void onOpenEditor()
	{
		final Container parent = getApplicationContainer();
		
		File file = utils.chooseFile(
			parent, 
			language.getText("wadscript.open.title"), 
			language.getText("wadscript.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.createWadScriptFileFilter()
		);
		
		if (file != null)
			onOpenFile(file);
	}
	
	private void onOpenFile(File file)
	{
		final Container parent = getApplicationContainer();
		try {
			editorPanel.openFileEditor(file, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			LOG.errorf(e, "Selected file could not be found: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error", file.getName()));
			SwingUtils.error(parent, language.getText("wadscript.open.error.notfound", file.getAbsolutePath()));
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error", file.getName()));
			SwingUtils.error(parent, language.getText("wadscript.open.error.ioerror", file.getAbsolutePath()));
		} catch (SecurityException e) {
			LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error.security", file.getName()));
			SwingUtils.error(parent, language.getText("wadscript.open.error.security", file.getAbsolutePath()));
		}
	}
	
	private void onOpenDirectory()
	{
		File dir = utils.chooseDirectory(
			getApplicationContainer(), 
			language.getText("decohack.open.title"), 
			language.getText("decohack.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile
		);
		
		if (dir != null)
			onOpenDirectory(dir);
	}
	
	private void onOpenDirectory(File directory)
	{
		treePanel.setRootDirectory(directory);
		treePanel.setLabel(directory.getName());
	}
		
	private boolean saveBeforeExecute()
	{
		final Container parent = getApplicationContainer();

		if (currentHandle.getContentSourceFile() != null && currentHandle.needsToSave())
		{
			Boolean saveChoice = modal(parent, utils.getWindowIcons(), 
				language.getText("wadscript.run.save.modal.title"),
				containerOf(label(language.getText("wadscript.run.save.modal.message", currentHandle.getEditorTabName()))), 
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
				utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
			).openThenDispose();
			
			if (saveChoice == null)
				return false;
			else if (saveChoice == true)
			{
				if (!editorPanel.saveCurrentEditor())
					return false;
			}
			// else, continue on.
		}
		
		return true;
	}

	private void onRunWithArgs()
	{
		if (!saveBeforeExecute())
			return;
		
		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeWithArgs(scriptFile, currentHandle.getContentCharset(), scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeWithArgs(scriptFile, currentHandle.getContentCharset(), treePanel.getRootDirectory());
			}
		}
	}

	private void executeWithArgs(File scriptFile, Charset encoding, File workDir) 
	{
		ScriptExecutionSettings executionSettings;
		executionSettings = handleToSettingsMap.get(currentHandle);
		executionSettings = createExecutionSettings(executionSettings != null ? executionSettings : new ScriptExecutionSettings(workDir));

		if (executionSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, executionSettings);
		appCommon.onExecuteWadScript(getApplicationContainer(), statusPanel, scriptFile, encoding, executionSettings);
	}
	
	private void onRunAgain()
	{
		if (!saveBeforeExecute())
			return;

		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeAgain(scriptFile, scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeAgain(scriptFile, new File(OSUtils.getWorkingDirectoryPath()));
			}
		}
	}

	private void executeAgain(File scriptFile, File workDir)
	{
		ScriptExecutionSettings executionSettings;
		if ((executionSettings = handleToSettingsMap.get(currentHandle)) == null)
			executionSettings = createExecutionSettings(new ScriptExecutionSettings(workDir));
		
		if (executionSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, executionSettings);
		appCommon.onExecuteWadScript(getApplicationContainer(), statusPanel, scriptFile, currentHandle.getContentCharset(), executionSettings);
	}

	private ScriptExecutionSettings createExecutionSettings(ScriptExecutionSettings initSettings) 
	{
		final WadScriptExecuteWithArgsPanel argsPanel = new WadScriptExecuteWithArgsPanel(initSettings);
		ScriptExecutionSettings settings = utils.createSettingsModal(
			language.getText("wadscript.run.withargs.title"),
			argsPanel,
			(panel) -> {
				ScriptExecutionSettings out = new ScriptExecutionSettings();
				out.setWorkingDirectory(panel.getWorkingDirectory());
				out.setStandardInPath(panel.getStandardInPath());
				out.setEntryPoint(panel.getEntryPoint());
				out.setArgs(panel.getArgs());
				return out;
			},
			utils.createChoiceFromLanguageKey("wadscript.run.withargs.choice.run", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wadscript.md")).open();
	}
	
	private void onRookScriptReference()
	{
		utils.createHelpModal(utils.helpResource("docs/RookScript Quick Guide.md")).open();
	}

	private void onFunctionReference()
	{
		utils.createHelpModal(utils.helpProcess(WadScriptMain.class, "--function-help")).open();
	}

	private class WadScriptTreePanel extends EditorDirectoryTreePanel
	{
		private static final long serialVersionUID = 6075047699092987406L;

		public WadScriptTreePanel()
		{
			super();
			setListener(new DirectoryTreeListener() 
			{
				@Override
				public void onFileSelectionChange(File[] selectedFiles)
				{
					// Do nothing.
				}

				@Override
				public void onFilesDeleted(File[] deletedFiles) 
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.dirtree.delete.result", deletedFiles.length));
				}
				
				@Override
				public void onFilesCopied(File[] copiedFiles) 
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.dirtree.copy.result", copiedFiles.length));
				}
				
				@Override
				public void onFileRename(File changedFile, String newName)
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.dirtree.rename.result", changedFile.getName(), newName));
				}
				
				@Override
				public void onFileConfirmed(File confirmedFile)
				{
					onOpenFile(confirmedFile);
				}
			});
		}
	}
	
	private class WadScriptEditorPanel extends EditorMultiFilePanel
	{
		private static final long serialVersionUID = -2590465129796097892L;

		private FileFilter[] TYPES = null;
		
		public WadScriptEditorPanel(Options options, Listener listener)
		{
			super(options, listener);
		}

		@Override
		protected String getDefaultStyleName() 
		{
			return DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT;
		}
		
		@Override
		protected File getLastPathTouched() 
		{
			return settings.getLastTouchedFile();
		}
		
		@Override
		protected void setLastPathTouched(File saved) 
		{
			settings.setLastTouchedFile(saved);
		}
		
		@Override
		protected FileFilter[] getSaveFileTypes() 
		{
			return TYPES == null ? TYPES = new FileFilter[]{utils.createWadScriptFileFilter()} : TYPES;
		}
	
		@Override
		protected File transformSaveFile(FileFilter selectedFilter, File selectedFile) 
		{
			return selectedFilter == getSaveFileTypes()[0] ? FileUtils.addMissingExtension(selectedFile, "wscript") : selectedFile;
		}
		
	}
	
}
