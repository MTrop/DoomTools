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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WSwAnTablesSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DefSwAniExportPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;
import net.mtrop.doom.tools.struct.util.FileUtils.TempFile;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The WSwAnTbl application.
 * @author Matthew Tropiano
 */
public class WSwAnTablesEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(WSwAnTablesEditorApp.class); 

	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

    // Singletons

	private WSwAnTablesSettingsManager settings;

	// Referenced Components
	
	private DefSwAniEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;

	private Action exportAction;
	
	// State
	
	private File fileToOpenFirst;
	private EditorHandle currentHandle;
	private Map<EditorHandle, DefSwAniExportSettings> handleToSettingsMap;

	// ...

	/**
	 * Create a new WSwAnTbl application.
	 */
	public WSwAnTablesEditorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WSwAnTbl application.
	 * @param fileToOpenFirst if not null, open this file on create.
	 */
	public WSwAnTablesEditorApp(File fileToOpenFirst) 
	{
		this.settings = WSwAnTablesSettingsManager.get();
		
		this.editorPanel = new DefSwAniEditorPanel(new EditorMultiFilePanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return true;
			}

			@Override
			public boolean hideTreeActions()
			{
				return true;
			}
		}, 
		new DefSwAniEditorPanel.Listener()
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
				statusPanel.setSuccessMessage(language.getText("wswantbl.status.message.saved", sourceFile.getName()));
				onHandleChange();
			}

			@Override
			public void onOpen(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wswantbl.status.message.editor.open", handle.getEditorTabName()));
			}

			@Override
			public void onClose(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wswantbl.status.message.editor.close", handle.getEditorTabName()));
				handleToSettingsMap.remove(handle);
			}

			@Override
			public void onTreeDirectoryRequest(EditorHandle handle)
			{
				// Do nothing.
			}

			@Override
			public void onTreeRevealRequest(EditorHandle handle)
			{
				// Do nothing.
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.exportAction = utils.createActionFromLanguageKey("wswantbl.menu.patch.item.export", (e) -> onExport());
		
		this.currentHandle = null;
		this.handleToSettingsMap = new HashMap<>();
		this.fileToOpenFirst = fileToOpenFirst;
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wswantbl.editor.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(650, 500), borderLayout(0, 8), 
			node(BorderLayout.CENTER, editorPanel),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	private MenuNode[] createCommonFileMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("wswantbl.menu.file.item.new",
				utils.createItemFromLanguageKey("wswantbl.menu.file.item.new.item.main", (i) -> onNewEditor()),
				utils.createItemFromLanguageKey("wswantbl.menu.file.item.new.item.blank", (i) -> onNewBlankEditor())
			),
			utils.createItemFromLanguageKey("wswantbl.menu.file.item.open", (i) -> onOpenEditor()),
			utils.createItemFromLanguageKey("wswantbl.menu.file.item.open.wad", (i) -> onOpenEditorFromWAD()),
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

	private MenuNode[] createCommonPatchMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("wswantbl.menu.patch.item.export", exportAction)
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
			editorPanel.getEditorPreferencesMenuItem()
		);
	}
	
	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wswantbl.menu.file", ArrayUtils.joinArrays(
				createCommonFileMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("wswantbl.menu.file.item.exit", (i) -> attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("wswantbl.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wswantbl.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("wswantbl.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wswantbl.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("wswantbl.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wswantbl.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("wswantbl.menu.editor", createCommonEditorMenuItems()),
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
		}
	}
	
	@Override
	public void onOpen(Object frame) 
	{
		statusPanel.setSuccessMessage(language.getText("wswantbl.status.message.ready"));
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
		editorPanel.saveState("wswantbl", state);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			if (currentHandle == handle)
				state.put("editor.selected", String.valueOf(i));
			
			String settingPrefix = "export." + i;
			DefSwAniExportSettings settings = handleToSettingsMap.get(handle);
			if (settings != null)
			{
				state.put(settingPrefix + ".enabled", String.valueOf(true));
				if (settings.getOutputWAD() != null)
					state.put(settingPrefix + ".outwad", settings.getOutputWAD().getAbsolutePath());
				state.put(settingPrefix + ".outputsource", String.valueOf(settings.isOutputSource()));
			}
		}
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		handleToSettingsMap.clear();
		editorPanel.loadState("wswantbl", state);
		
		int selectedIndex = ValueUtils.parseInt(state.get("editor.selected"), -1);
		if (selectedIndex >= 0)
			editorPanel.setEditorByIndex(selectedIndex);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			String settingPrefix = "export." + i;
			
			boolean enabled = ValueUtils.parseBoolean(state.get(settingPrefix + ".enabled"), false);
			if (enabled)
			{
				DefSwAniExportSettings settings = new DefSwAniExportSettings();
				Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
				settings.setOutputWAD(ValueUtils.parse(state.get(settingPrefix + ".outwad"), parseFile));
				settings.setOutputSource(ValueUtils.parseBoolean(state.get(settingPrefix + ".outputsource"), false));
				handleToSettingsMap.put(handle, settings);
			}
		}
	}
	
	
	// ====================================================================

	private void onHandleChange()
	{
		exportAction.setEnabled(currentHandle != null);
	}
	
	private void onNewEditor()
	{
		StringWriter writer = new StringWriter();
		try (Reader reader = new InputStreamReader(IOUtils.openResource("gui/apps/defswani.txt"), StandardCharsets.UTF_8))
		{
			IOUtils.relay(reader, writer);
		} 
		catch (IOException e) 
		{
			LOG.error(e, "Could not get DEFSWANI data from resources.");
		}
		
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, writer.toString(), Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEFSWANI, 0);
	}

	private void onNewBlankEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, "", Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEFSWANI, 0);
	}

	private void onOpenEditor()
	{
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wswantbl.open.title"), 
			language.getText("wswantbl.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.createDEFSWANIFileFilter()
		);
		
		if (file != null)
			onOpenFile(file);
	}
	
	private void onOpenEditorFromWAD()
	{
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wswantbl.open.wad.title"), 
			language.getText("wswantbl.open.wad.accept"),
			settings::getLastOpenedWAD,
			settings::setLastOpenedWAD,
			utils.createWADFileFilter()
		);
		
		if (file == null)
			return;
		
		boolean isWad;
		try {
			isWad = Wad.isWAD(file);
		} catch (FileNotFoundException e) {
			SwingUtils.error(language.getText("wswantbl.open.wad.error.notfound", file.getAbsolutePath()));
			return;
		} catch (IOException e) {
			SwingUtils.error(language.getText("wswantbl.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("wswantbl.open.wad.error.security", file.getAbsolutePath()));
			return;
		}
	
		if (!isWad)
		{
			SwingUtils.error(language.getText("wswantbl.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		
		String content;
		try (WadFile wad = new WadFile(file))
		{
			int animIndex = wad.indexOf("ANIMATED");
			int switIndex = wad.indexOf("SWITCHES");
			
			if (animIndex < 0 || switIndex < 0)
			{
				SwingUtils.error(language.getText("wswantbl.open.wad.error.nodata", file.getAbsolutePath()));
				return;
			}
			
			Animated animated = wad.getDataAs(animIndex, Animated.class);
			Switches switches = wad.getDataAs(switIndex, Switches.class);
			
			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			Utility.writeSwitchAnimatedTables(switches, animated, WSwAnTablesMain.SWANTBLS_OUTPUT_HEADER, writer);
			content = sw.toString();
		} 
		catch (WadException e) 
		{
			SwingUtils.error(language.getText("wswantbl.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		catch (IOException e) 
		{
			SwingUtils.error(language.getText("wswantbl.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(language.getText("wswantbl.open.wad.error.security", file.getAbsolutePath()));
			return;
		}
		
		String editorName = "Wad " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, content, Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEFSWANI, 0);
	}

	private void onOpenFile(File file)
	{
		try {
			editorPanel.openFileEditor(file, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			LOG.errorf(e, "Selected file could not be found: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wswantbl.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("wswantbl.open.error.notfound", file.getAbsolutePath()));
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wswantbl.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("wswantbl.open.error.ioerror", file.getAbsolutePath()));
		} catch (SecurityException e) {
			LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wswantbl.status.message.editor.error.security", file.getName()));
			SwingUtils.error(language.getText("wswantbl.open.error.security", file.getAbsolutePath()));
		}
	}
	
	private boolean saveBeforeExecute()
	{
		if (currentHandle.getContentSourceFile() != null && currentHandle.needsToSave())
		{
			Boolean saveChoice = modal(
				getApplicationContainer(), 
				utils.getWindowIcons(), 
				language.getText("wswantbl.save.modal.title"),
				containerOf(label(language.getText("wswantbl.save.modal.message", currentHandle.getEditorTabName()))), 
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
	
	private void onExport()
	{
		if (!saveBeforeExecute())
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wswantbl.error.mustsave"));
			return;
		}
		
		// Should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for export.

		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			exportWithSettings(scriptFile);
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				exportWithSettings(scriptFile);
			}
		}
	}

	private void exportWithSettings(final File scriptFile) 
	{
		DefSwAniExportSettings existingSettings = handleToSettingsMap.get(currentHandle);
		final DefSwAniExportSettings processSettings = createExportSettings(scriptFile, existingSettings != null ? existingSettings : new DefSwAniExportSettings());
		
		if (processSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, processSettings);
		appCommon.onExecuteWSwAnTbl(getApplicationContainer(), statusPanel, scriptFile, processSettings);
	}

	private DefSwAniExportSettings createExportSettings(File sourceFile, final DefSwAniExportSettings initSettings) 
	{
		final DefSwAniExportPanel argsPanel = new DefSwAniExportPanel(initSettings);
		argsPanel.setPreferredSize(dimension(400, 100));
		DefSwAniExportSettings settings = utils.createSettingsModal(
			language.getText("wswantbl.export.title"),
			argsPanel,
			(panel) -> {
				DefSwAniExportSettings out = new DefSwAniExportSettings();
				out.setOutputWAD(panel.getOutputWAD());
				out.setOutputSource(panel.getOutputSource());
				return out;
			},
			utils.createChoiceFromLanguageKey("wswantbl.export.choice.export", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wswantbl.md")).open();
	}

	private class DefSwAniEditorPanel extends EditorMultiFilePanel
	{
		private static final long serialVersionUID = -9024669807749249148L;
		
		private FileFilter[] TYPES = null;
		
		public DefSwAniEditorPanel(Options options, Listener listener)
		{
			super(options, listener);
		}

		@Override
		protected String getDefaultStyleName() 
		{
			return DoomToolsEditorProvider.SYNTAX_STYLE_DEFSWANI;
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
			return TYPES == null ? TYPES = new FileFilter[]{utils.createDEFSWANIFileFilter()} : TYPES;
		}
	
		@Override
		protected File transformSaveFile(FileFilter selectedFilter, File selectedFile) 
		{
			return selectedFilter == getSaveFileTypes()[0] ? FileUtils.addMissingExtension(selectedFile, "txt") : selectedFile;
		}
		
	}
	
}
