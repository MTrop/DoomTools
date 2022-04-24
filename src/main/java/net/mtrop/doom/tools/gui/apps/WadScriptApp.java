package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel.ActionNames;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel.EditorHandle;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * The WadScript application.
 * @author Matthew Tropiano
 */
public class WadScriptApp extends DoomToolsApplicationInstance
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(WadScriptApp.class); 

    private static final String LASTPATH_KEY = "wadscript.editor"; 

	private static final FileFilter[] TYPES = new FileFilter[] { fileExtensionFilter("WadScript Files (*.script)", "script") };
	
	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	private static final String[] NO_ARGUMENTS = new String[0];
	
	private static final String EMPTY_SCRIPT = (new StringBuilder())
		.append("entry main(args) {\n")
		.append("\n")
		.append("}\n")
	.toString();
	
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsTaskManager tasks;
	private DoomToolsIconManager icons;
	private DoomToolsLanguageManager language;
	
	// Components
	
	private MultiFileEditorPanel editorPanel;
	private JFormField<File> workingDirFileField;
	private JFormField<String> entryPointField;
	private JButton runScriptButton;
	private JButton popupButton;
	private JPopupMenu runPopupMenu;
	private DoomToolsStatusPanel statusPanel;
	
	// Fields
    
	private File defaultWorkingDirectory;
	private File currentWorkingDirectory;
	private String entryPoint;

	// State
	
	private EditorHandle currentHandle;
	
	// ...

	/**
	 * Create a new WadScript application.
	 * The default working directory for new files is the application working directory.
	 */
	public WadScriptApp() 
	{
		this(new File(OSUtils.getWorkingDirectoryPath()).getAbsoluteFile());
	}
	
	/**
	 * Create a new WadScript application.
	 * @param defaultWorkingDirectory the working directory for new files.
	 */
	public WadScriptApp(final File defaultWorkingDirectory) 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.tasks = DoomToolsTaskManager.get();
		this.icons = DoomToolsIconManager.get();
		this.language = DoomToolsLanguageManager.get();
		
		this.defaultWorkingDirectory = defaultWorkingDirectory;
		this.workingDirFileField = fileField(
			currentWorkingDirectory, 
			(current) -> chooseDirectory(
				receiver.getApplicationContainer(),
				language.getText("wadscript.workdir.browse.title"), 
				current, 
				language.getText("wadscript.workdir.browse.accept"), 
				FileFilters.DIRECTORIES
			), 
			(selected) -> {
				currentWorkingDirectory = selected;
			}
		);
		
		this.entryPoint = "main";
		this.entryPointField = stringTextField(entryPoint, (value) -> entryPoint = value);
		this.runScriptButton = utils.createButtonFromLanguageKey(
			icons.getImage("script-run.png"), 
			"wadscript.entrypoint.action", 
			(b, e) -> onExecuteScript(false, IOUtils.getNullInputStream(), NO_ARGUMENTS)
		);
		this.runPopupMenu = popupMenu(
			utils.createItemFromLanguageKey("wadscript.run.popup.item.log", (c, e) -> onExecuteScript(true, IOUtils.getNullInputStream(), NO_ARGUMENTS)),
			utils.createItemFromLanguageKey("wadscript.run.popup.item.args", (c, e) -> onExecuteScriptArgs())
		);
		this.popupButton = button("\u2228", this.runPopupMenu);
		
		this.editorPanel = new WadScriptEditorPanel(new WadScriptEditorPanel.Listener()
		{
			@Override
			public void onCurrentEditorChange(EditorHandle handle) 
			{
				currentHandle = handle;
				onHandleChange();
			}

			@Override
			public void onSave(EditorHandle handle) 
			{
				File sourceFile = handle.getContentSourceFile();
				statusPanel.setSuccessMessage(language.getText("wadscript.status.message.saved", sourceFile.getName()));
				setWorkingDirectoryField(sourceFile.getParentFile());
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
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		this.currentHandle = null;
	}
	
	@Override
	public String getTitle() 
	{
		return "WadScript";
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(650, 500), createEmptyBorder(4, 4, 4, 4), new BorderLayout(0, 4), 
			node(BorderLayout.CENTER, editorPanel),
			node(BorderLayout.SOUTH, containerOf(new BorderLayout(0, 4),
				node(BorderLayout.CENTER, containerOf(new BorderLayout(0, 4),
					node(BorderLayout.NORTH, utils.createTitlePanel(language.getText("wadscript.workdir.title"), workingDirFileField)),
					node(BorderLayout.SOUTH, utils.createTitlePanel(language.getText("wadscript.entrypoint.title"), containerOf(new BorderLayout(4, 0),
						node(BorderLayout.CENTER, entryPointField),
						node(BorderLayout.LINE_END, containerOf(
							node(BorderLayout.CENTER, runScriptButton),
							node(BorderLayout.LINE_END, popupButton)
						))
					)))
				)),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		// TODO Finish me.
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file",
				utils.createItemFromLanguageKey("wadscript.menu.file.item.new", (c, e) -> onNewEditor()),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.open", (c, e) -> onOpenEditor()),
				separator(),
				utils.createItemFromLanguageKey("texteditor.action.close", editorPanel.getActionFor(ActionNames.ACTION_CLOSE)),
				utils.createItemFromLanguageKey("texteditor.action.closeallbutcurrent", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL_BUT_CURRENT)),
				utils.createItemFromLanguageKey("texteditor.action.closeall", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL)),
				separator(),
				utils.createItemFromLanguageKey("texteditor.action.save", editorPanel.getActionFor(ActionNames.ACTION_SAVE)),
				utils.createItemFromLanguageKey("texteditor.action.saveas", editorPanel.getActionFor(ActionNames.ACTION_SAVE_AS)),
				utils.createItemFromLanguageKey("texteditor.action.saveall", editorPanel.getActionFor(ActionNames.ACTION_SAVE_ALL)),
				separator(),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (c, e) -> receiver.attemptClose())
			),
			utils.createMenuFromLanguageKey("wadscript.menu.edit",
				utils.createItemFromLanguageKey("texteditor.action.undo", editorPanel.getActionFor(ActionNames.ACTION_UNDO)),
				utils.createItemFromLanguageKey("texteditor.action.redo", editorPanel.getActionFor(ActionNames.ACTION_REDO)),
				separator(),
				utils.createItemFromLanguageKey("texteditor.action.cut", editorPanel.getActionFor(ActionNames.ACTION_CUT)),
				utils.createItemFromLanguageKey("texteditor.action.copy", editorPanel.getActionFor(ActionNames.ACTION_COPY)),
				utils.createItemFromLanguageKey("texteditor.action.paste", editorPanel.getActionFor(ActionNames.ACTION_PASTE)),
				separator(),
				utils.createItemFromLanguageKey("texteditor.action.delete", editorPanel.getActionFor(ActionNames.ACTION_DELETE)),
				utils.createItemFromLanguageKey("texteditor.action.selectall", editorPanel.getActionFor(ActionNames.ACTION_SELECT_ALL))
			)
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		// TODO Finish me.
		return super.createInternalMenuBar();
	}

	@Override
	public void onOpen() 
	{
		statusPanel.setSuccessMessage(language.getText("wadscript.status.message.ready"));
		if (editorPanel.getOpenEditorCount() == 0)
			onNewEditor();
	}

	@Override
	public boolean shouldClose() 
	{
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors();
		return true;
	}
	
	// ====================================================================

	private void onHandleChange()
	{
		if (currentHandle != null)
		{
			File sourceFile = currentHandle.getContentSourceFile();
			setWorkingDirectoryField(sourceFile == null ? defaultWorkingDirectory : sourceFile.getParentFile());
			workingDirFileField.setEnabled(true);
			entryPointField.setEnabled(true);
			runScriptButton.setEnabled(sourceFile != null);
			popupButton.setEnabled(sourceFile != null);
		}
		else
		{
			workingDirFileField.setEnabled(false);
			entryPointField.setEnabled(false);
			runScriptButton.setEnabled(false);
			popupButton.setEnabled(false);
		}

	}
	
	private void onNewEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, EMPTY_SCRIPT);
	}
	
	private void onOpenEditor()
	{
		final Container parent = receiver.getApplicationContainer();
		
		File file = utils.chooseFile(
			parent, 
			LASTPATH_KEY, 
			language.getText("wadscript.open.title"), 
			language.getText("wadscript.open.accept"), 
			TYPES
		);
		
		if (file != null)
		{
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
	}
	
	private void setWorkingDirectoryField(File directory)
	{
		workingDirFileField.setValue(directory);
	}
	
	private void onExecuteScriptArgs()
	{
		// TODO: Finish this with args.
	}
	
	private void onExecuteScript(boolean showLog, final InputStream standardIn, final String[] args)
	{
		final Container parent = receiver.getApplicationContainer();

		if (currentHandle.needsToSave())
		{
			Boolean saveChoice = modal(parent, utils.getWindowIcons(), 
				language.getText("wadscript.run.save.modal.title"),
				containerOf(label(language.getText("wadscript.run.save.modal.message", currentHandle.getEditorTabName()))), 
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
				utils.createChoiceFromLanguageKey("doomtools.cancel", null)
			).openThenDispose();
			
			if (saveChoice == null)
				return;
			else if (saveChoice == true)
			{
				if (!editorPanel.saveCurrentEditor())
					return;
			}
		}
		
		File scriptFile = currentHandle.getContentSourceFile();
		
		if (showLog)
		{
			utils.createProcessModal(
				receiver.getApplicationContainer(), 
				language.getText("wadscript.run.message.title"), 
				language.getText("wadscript.run.message.running", entryPoint), 
				language.getText("wadscript.run.message.success"), 
				language.getText("wadscript.run.message.error"), 
				(stream) -> execute(scriptFile, currentWorkingDirectory, entryPoint, args, stream, stream, standardIn)
			).start(tasks);
		}
		else
		{
			execute(scriptFile, currentWorkingDirectory, entryPoint, args, null, null, standardIn);
		}
	}

	private InstancedFuture<Integer> execute(final File scriptFile, final File workingDirectory, String entryPoint, String[] args, final PrintStream out, final PrintStream err, final InputStream in)
	{
		return tasks.spawn(() -> {
			Integer result = null;
			try {
				statusPanel.setActivityMessage(language.getText("wadscript.run.message.running", scriptFile.getName()));
				result = callWadScript(scriptFile, workingDirectory, entryPoint, args, out, err, in).get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.run.message.success"));
				}
				else
				{
					LOG.errorf("Error on WadScript invoke (%s) result was %d: %s", entryPoint, result, scriptFile.getAbsolutePath());
					statusPanel.setErrorMessage(language.getText("wadscript.run.message.error"));
				}
			} catch (InterruptedException e) {
				LOG.warnf("Call to WadScript invoke interrupted (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.interrupt"));
			} catch (ExecutionException e) {
				LOG.errorf(e, "Error on WadScript invoke (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.error"));
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
	
	private static class WadScriptEditorPanel extends MultiFileEditorPanel
	{
		private static final long serialVersionUID = -2590465129796097892L;

		public WadScriptEditorPanel(Listener listener)
		{
			super(listener);
		}

		@Override
		protected String getLastPathKey() 
		{
			return LASTPATH_KEY;
		}
		
		@Override
		protected FileFilter[] getSaveFileTypes() 
		{
			return TYPES;
		}
	
	}
	
}
