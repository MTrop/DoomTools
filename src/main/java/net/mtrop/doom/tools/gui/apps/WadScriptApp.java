package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorHandle;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.OSUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;


/**
 * The WadScript application.
 * @author Matthew Tropiano
 */
public class WadScriptApp extends DoomToolsApplicationInstance
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(WadScriptApp.class); 

    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsIconManager icons;
	private DoomToolsLanguageManager language;
	private DoomToolsSettingsManager settings;
	
	// Components
	
	private MultiFileEditorPanel editorPanel;
	private JFormField<File> workingDirFileField;
	private DoomToolsStatusPanel statusPanel;
	
	// Fields
    
	private File currentWorkingDirectory;
	private String entryPoint;

	// State
	
	// ...
	
	/**
	 * Create a new WadScript application.
	 */
	public WadScriptApp() 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.icons = DoomToolsIconManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomToolsSettingsManager.get();
		
		try {
			this.currentWorkingDirectory = new File(OSUtils.getWorkingDirectoryPath()).getCanonicalFile();
		} catch (IOException e) {
			this.currentWorkingDirectory = new File(OSUtils.getWorkingDirectoryPath());
		}
		
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
		this.editorPanel = new MultiFileEditorPanel();
		this.entryPoint = "main";
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
					node(BorderLayout.NORTH, utils.createTitlePanel(language.getText("wadscript.workdir.title"), 
						workingDirFileField
					)),
					node(BorderLayout.SOUTH, utils.createTitlePanel(language.getText("wadscript.entrypoint.title"), containerOf(new BorderLayout(4, 0),
						node(BorderLayout.CENTER, stringTextField("main", (value) -> entryPoint = value)),
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
				utils.createItemFromLanguageKey("wadscript.menu.file.item.new", (c, e) -> editorPanel.newEditor("New File", "")),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.open", (c, e) -> {
					// TODO: Finish this.
				}),
				separator(),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.close", (c, e) -> editorPanel.closeCurrentEditor()),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.closeall", (c, e) -> editorPanel.closeAllEditors()),
				separator(),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.save", (c, e) -> editorPanel.saveCurrentEditor()),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.saveas", (c, e) -> editorPanel.saveAllEditors()),
				separator(),
				utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (c, e) -> receiver.attemptClose())
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
		
	}
	
	// ====================================================================
	
	// Adds an editor tab, returning the tab component.
	private EditorHandle addNewEditorTab(String title)
	{
		return null;
	}
	
	// ====================================================================

	private void executeScript()
	{
		
	}
	
}
