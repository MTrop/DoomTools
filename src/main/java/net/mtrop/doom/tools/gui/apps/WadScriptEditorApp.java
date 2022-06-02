package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.managers.WadScriptSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadScriptExecuteWithArgsPanel;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorHandle;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WadScript application.
 * @author Matthew Tropiano
 */
public class WadScriptEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(WadScriptEditorApp.class); 

	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	private static final String EMPTY_SCRIPT = (new StringBuilder())
		.append("entry main(args) {\n")
		.append("\n")
		.append("}\n")
	.toString();
	
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsTaskManager tasks;
	private DoomToolsLanguageManager language;
	private WadScriptSettingsManager settings;
	
	// Referenced Components
	
	private WadScriptEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;

	private Action runAction;
	private Action runParametersAction;
	
	// State
	
	private File fileToOpenFirst;
	private EditorHandle currentHandle;
	private Map<EditorHandle, ExecutionSettings> handleToSettingsMap;
	
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
		this.utils = DoomToolsGUIUtils.get();
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = WadScriptSettingsManager.get();
		
		this.editorPanel = new WadScriptEditorPanel(new MultiFileEditorPanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return true;
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
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
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
		return containerOf(dimension(650, 500), createEmptyBorder(8, 8, 8, 8), borderLayout(0, 8), 
			node(BorderLayout.CENTER, editorPanel),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	private MenuNode[] createCommonFileMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("wadscript.menu.file.item.new", (c, e) -> onNewEditor()),
			utils.createItemFromLanguageKey("wadscript.menu.file.item.open", (c, e) -> onOpenEditor()),
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
			editorPanel.getEditorPreferencesMenuItem()
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
					utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (c, e) -> receiver.attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("wadscript.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.run", createWadScriptRunMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.editor", createCommonEditorMenuItems())
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.run", createWadScriptRunMenuItems()),
			utils.createMenuFromLanguageKey("wadscript.menu.editor", createCommonEditorMenuItems())
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
		}
	}
	
	@Override
	public boolean shouldClose() 
	{
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors();
		return true;
	}
	
	@Override
	public Map<String, String> getApplicationState() 
	{
		Map<String, String> state = new HashMap<>();
		// TODO Finish this.
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		// TODO Finish this.
	}
	
	// ====================================================================

	private void onHandleChange()
	{
		if (currentHandle != null)
		{
			boolean hasFile = currentHandle.getContentSourceFile() != null;
			runAction.setEnabled(hasFile);
			runParametersAction.setEnabled(hasFile);
		}
		else
		{
			runAction.setEnabled(false);
			runParametersAction.setEnabled(false);
		}

	}
	
	private void onNewEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, EMPTY_SCRIPT, Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT);
	}
	
	private void onOpenEditor()
	{
		final Container parent = receiver.getApplicationContainer();
		
		File file = utils.chooseFile(
			parent, 
			language.getText("wadscript.open.title"), 
			language.getText("wadscript.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.getWadScriptFileFilter()
		);
		
		if (file != null)
			onOpenFile(file);
	}
	
	private void onOpenFile(File file)
	{
		final Container parent = receiver.getApplicationContainer();
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
	
	private boolean saveBeforeExecute()
	{
		final Container parent = receiver.getApplicationContainer();

		if (currentHandle.needsToSave())
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
		}

		return true;
	}

	private void onRunWithArgs()
	{
		if (!saveBeforeExecute())
			return;

		// Should be set if saveBeforeExecute() succeeds.
		File scriptFile = currentHandle.getContentSourceFile();
		
		ExecutionSettings executionSettings = handleToSettingsMap.get(currentHandle);
		executionSettings = createExecutionSettings(executionSettings != null ? executionSettings : new ExecutionSettings(scriptFile.getParentFile()));
		
		if (executionSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, executionSettings);
		onExecuteWithSettings(scriptFile, executionSettings);
	}
	
	private void onRunAgain()
	{
		if (!saveBeforeExecute())
			return;

		// Should be set if saveBeforeExecute() succeeds.
		File scriptFile = currentHandle.getContentSourceFile();
		
		ExecutionSettings executionSettings;
		if ((executionSettings = handleToSettingsMap.get(currentHandle)) == null)
			executionSettings = createExecutionSettings(new ExecutionSettings(scriptFile.getParentFile()));
		
		if (executionSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, executionSettings);
		onExecuteWithSettings(scriptFile, executionSettings);
	}

	private void onExecuteWithSettings(File scriptFile, ExecutionSettings executionSettings)
	{
		final File workingDirectory = executionSettings.workingDirectory;
		final File standardInPath = executionSettings.standardInPath;
		final String entryPoint = executionSettings.entryPoint;
		final String[] args = executionSettings.args;
		
		utils.createProcessModal(
			receiver.getApplicationContainer(), 
			language.getText("wadscript.run.message.title"), 
			language.getText("wadscript.run.message.running", entryPoint), 
			language.getText("wadscript.run.message.success"), 
			language.getText("wadscript.run.message.error"), 
			(stream, errstream) -> execute(scriptFile, workingDirectory, entryPoint, args, stream, errstream, standardInPath)
		).start(tasks);
	}

	private ExecutionSettings createExecutionSettings(ExecutionSettings initSettings) 
	{
		final WadScriptExecuteWithArgsPanel argsPanel = new WadScriptExecuteWithArgsPanel(initSettings);
		ExecutionSettings settings = utils.createSettingsModal(
			language.getText("wadscript.run.withargs.title"),
			argsPanel,
			(panel) -> {
				ExecutionSettings out = new ExecutionSettings();
				out.workingDirectory = panel.getWorkingDirectory();
				out.standardInPath = panel.getStandardInPath();
				out.entryPoint = panel.getEntryPoint();
				out.args = panel.getArgs();
				return out;
			},
			utils.createChoiceFromLanguageKey("wadscript.run.withargs.choice.run", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	private InstancedFuture<Integer> execute(final File scriptFile, final File workingDirectory, String entryPoint, String[] args, final PrintStream out, final PrintStream err, final File input)
	{
		return tasks.spawn(() -> {
			Integer result = null;
			InputStream stdin = null;
			try
			{
				stdin = input != null ? new FileInputStream(input) : IOUtils.getNullInputStream();
				statusPanel.setActivityMessage(language.getText("wadscript.run.message.running", scriptFile.getName()));
				result = callWadScript(scriptFile, workingDirectory, entryPoint, args, out, err, stdin).get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.run.message.success"));
				}
				else
				{
					LOG.errorf("Error on WadScript invoke (%s) result was %d: %s", entryPoint, result, scriptFile.getAbsolutePath());
					statusPanel.setErrorMessage(language.getText("wadscript.run.message.error.result", result));
				}
			} catch (InterruptedException e) {
				LOG.warnf("Call to WadScript invoke interrupted (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.interrupt"));
			} catch (ExecutionException e) {
				LOG.errorf(e, "Error on WadScript invoke (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.error"));
			} finally {
				IOUtils.close(stdin);
			}
			return result;
		});
	}
	
	private static InstancedFuture<Integer> callWadScript(final File scriptFile, final File workingDirectory, String entryPoint, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		ProcessCallable callable = Common.spawnJava(WadScriptMain.class).setWorkingDirectory(workingDirectory);
		callable.arg("--entry").arg(entryPoint)
			.arg(scriptFile.getAbsolutePath())
			.args(args)
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDERR."))
			.setInListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDIN."));
		
		LOG.infof("Calling WadScript (%s:%s).", scriptFile, entryPoint);
		return InstancedFuture.instance(callable).spawn();
	}
	
	private class WadScriptEditorPanel extends MultiFileEditorPanel
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
			return TYPES == null ? TYPES = new FileFilter[]{utils.getWadScriptFileFilter()} : TYPES;
		}
	
		@Override
		protected File transformSaveFile(FileFilter selectedFilter, File selectedFile) 
		{
			return selectedFilter == getSaveFileTypes()[0] ? FileUtils.addMissingExtension(selectedFile, "wscript") : selectedFile;
		}
		
	}
	
	public static class ExecutionSettings
	{
		private File workingDirectory;
		private File standardInPath;
		private String entryPoint;
		private String[] args;
		
		private ExecutionSettings()
		{
			this(null);
		}
		
		private ExecutionSettings(File workingDirectory)
		{
			this.workingDirectory = workingDirectory;
			this.standardInPath = null;
			this.entryPoint = "main";
			this.args = new String[0];
		}
		
		public File getWorkingDirectory() 
		{
			return workingDirectory;
		}
		
		public File getStandardInPath() 
		{
			return standardInPath;
		}
		
		public String getEntryPoint() 
		{
			return entryPoint;
		}
		
		public String[] getArgs() 
		{
			return args;
		}
		
	}
	
}
