package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel.ActionNames;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel.EditorHandle;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
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
	
	private static final String EMPTY_SCRIPT = (new StringBuilder())
		.append("entry main(args) {\n")
		.append("\n")
		.append("}\n")
	.toString();
	
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsIconManager icons;
	private DoomToolsLanguageManager language;
	
	// Components
	
	private MultiFileEditorPanel editorPanel;
	private JFormField<File> workingDirFileField;
	private JFormField<String> entryPointField;
	private DoomToolsStatusPanel statusPanel;
	
	// Fields
    
	private File currentWorkingDirectory;
	private String entryPoint;

	// State
	
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
		this.icons = DoomToolsIconManager.get();
		this.language = DoomToolsLanguageManager.get();
		
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
		
		this.editorPanel = new WadScriptEditorPanel(new WadScriptEditorPanel.Listener()
		{
			@Override
			public void onCurrentEditorChange(EditorHandle handle) 
			{
				if (handle != null)
				{
					File sourceFile = handle.getContentSourceFile();
					setWorkingDirectoryField(sourceFile == null ? defaultWorkingDirectory : sourceFile.getParentFile());
					workingDirFileField.setEnabled(true);
					entryPointField.setEnabled(true);
				}
				else
				{
					workingDirFileField.setEnabled(false);
					entryPointField.setEnabled(false);
				}
			}

			@Override
			public void onSave(EditorHandle handle) 
			{
				File sourceFile = handle.getContentSourceFile();
				statusPanel.setSuccessMessage(language.getText("wadscript.status.message.saved", sourceFile.getName()));
				setWorkingDirectoryField(sourceFile.getParentFile());
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return "WadScript";
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(640, 480), createEmptyBorder(4, 4, 4, 4), new BorderLayout(0, 4), 
			node(BorderLayout.CENTER, editorPanel),
			node(BorderLayout.SOUTH, containerOf(new BorderLayout(0, 4),
				node(BorderLayout.CENTER, containerOf(new BorderLayout(0, 4),
					node(BorderLayout.NORTH, utils.createTitlePanel(language.getText("wadscript.workdir.title"), workingDirFileField)),
					node(BorderLayout.SOUTH, utils.createTitlePanel(language.getText("wadscript.entrypoint.title"), containerOf(new BorderLayout(4, 0),
						node(BorderLayout.CENTER, entryPointField),
						node(BorderLayout.LINE_END, utils.createButtonFromLanguageKey(
							icons.getImage("script-run.png"), 
							"wadscript.entrypoint.action", 
							(b, e) -> executeScript()
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
				utils.createItemFromLanguageKey("wadscript.menu.file.item.new", (c, e) -> newEditor()),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.open", (c, e) -> openEditor()),
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
			newEditor();
	}

	@Override
	public boolean shouldClose() 
	{
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors();
		return true;
	}
	
	// ====================================================================

	private void newEditor()
	{
		editorPanel.newEditor("New " + NEW_COUNTER.getAndIncrement(), EMPTY_SCRIPT);
	}
	
	private void openEditor()
	{
		Container parent = receiver.getApplicationContainer();
		
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
				SwingUtils.error(parent, language.getText("wadscript.open.error.notfound", file.getAbsolutePath()));
			} catch (IOException e) {
				LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
				SwingUtils.error(parent, language.getText("wadscript.open.error.ioerror", file.getAbsolutePath()));
			} catch (SecurityException e) {
				LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
				SwingUtils.error(parent, language.getText("wadscript.open.error.security", file.getAbsolutePath()));
			}
		}
	}
	
	private void setWorkingDirectoryField(File directory)
	{
		workingDirFileField.setValue(directory);
	}
	
	private void executeScript()
	{
		// TODO: Do this. 
	}

	private static class WadScriptEditorPanel extends MultiFileEditorPanel
	{
		private static final long serialVersionUID = -2590465129796097892L;

		public WadScriptEditorPanel(Listener listener)
		{
			super(listener);
		}

		@Override
		protected RSyntaxTextArea createTextArea() 
		{
			return super.createTextArea();
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
