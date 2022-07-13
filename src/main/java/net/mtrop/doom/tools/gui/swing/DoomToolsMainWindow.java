package net.mtrop.doom.tools.gui.swing;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.blackrook.json.JSONReader;
import com.blackrook.json.JSONWriter;
import com.blackrook.json.JSONWriter.Options;

import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.Environment;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doomtools.DoomToolsUpdater;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.DoomToolsWorkspace;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.gui.apps.DecoHackCompilerApp;
import net.mtrop.doom.tools.gui.apps.DecoHackEditorApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesCompilerApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesEditorApp;
import net.mtrop.doom.tools.gui.apps.WTExportApp;
import net.mtrop.doom.tools.gui.apps.WTexScanApp;
import net.mtrop.doom.tools.gui.apps.WTexScanTExportApp;
import net.mtrop.doom.tools.gui.apps.WadMergeEditorApp;
import net.mtrop.doom.tools.gui.apps.WadMergeExecutorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptEditorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptExecutorApp;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils.HelpSource;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsAboutPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsDesktopPane;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsProgressPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The main DoomTools application window.
 * @author Matthew Tropiano
 */
public class DoomToolsMainWindow extends JFrame 
{
    private static final long serialVersionUID = -8837485206120777188L;

	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsMainWindow.class); 

    private static final Options JSON_OPTIONS = ObjectUtils.apply(new Options(), (options) -> {
    	options.setIndentation("\t");
    });
    
	/** Utils. */
	private DoomToolsGUIUtils utils;
	/** Task manager. */
	private DoomToolsTaskManager tasks;
    /** Language manager. */
    private DoomToolsLanguageManager language;
    /** Settings manager. */
    private DoomToolsSettingsManager settings;

	/* ==================================================================== */

    /** Desktop pane. */
	private DoomToolsDesktopPane desktop;

	/** Shutdown hook. */
	private Runnable shutDownHook;
	
    /** Application starter linker. */
    private DoomToolsApplicationStarter applicationStarter;

	/* ==================================================================== */

    // State
    
    /** Current workspace. */
    private File currentWorkspace;
    /** Save Workspace action. */
    private Action actionSaveWorkspace;
    /** Save Workspace As action. */
    private Action actionSaveWorkspaceAs;
    /** Clear workspace action. */
    private Action actionClearWorkspace;
    
	/**
	 * Creates the DoomTools main window.
	 * @param shutDownHook the application shutdown hook.
	 */
	public DoomToolsMainWindow(Runnable shutDownHook)
	{
		super();
		this.utils = DoomToolsGUIUtils.get();
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomToolsSettingsManager.get();
		
		this.shutDownHook = shutDownHook;
		
		this.applicationStarter = new DoomToolsApplicationStarter()
		{
			@Override
			public void startApplication(DoomToolsApplicationInstance instance) 
			{
				addApplication(instance);
			}
		};
		
		this.currentWorkspace = null;
		this.actionSaveWorkspace = actionItem(language.getText("doomtools.menu.file.item.workspace.save"), (e) -> saveWorkspace());
		this.actionSaveWorkspaceAs = actionItem(language.getText("doomtools.menu.file.item.workspace.saveas"), (e) -> saveWorkspaceAs());
		this.actionClearWorkspace = actionItem(language.getText("doomtools.menu.file.item.workspace.close"), (e) -> clearWorkspace());

		setIconImages(utils.getWindowIcons());
		setTitle("DoomTools");
		setJMenuBar(createMenuBar());
		setContentPane(this.desktop = new DoomToolsDesktopPane());
		setLocationByPlatform(true);
		pack();
		updateWorkspaceActions();
	}

	/**
	 * Adds a new application instance to the desktop.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 * @throws RuntimeException if the class could not be instantiated.
	 */
	public <A extends DoomToolsApplicationInstance> void addApplication(Class<A> applicationClass)
	{
		addApplication(Common.create(applicationClass));
	}

	/**
	 * Adds a new application instance to the desktop.
	 * @param applicationInstance the application instance.
	 * @throws RuntimeException if the class could not be instantiated.
	 */
	public void addApplication(DoomToolsApplicationInstance applicationInstance)
	{
		desktop.addApplicationFrame(applicationInstance, applicationStarter).setVisible(true);
		updateWorkspaceActions();
	}

	/**
	 * Shuts down all the apps in the window.
	 * @return true if all applications were closed, false if not.
	 */
	public boolean shutDownApps()
	{
		return desktop.clearWorkspace();
	}

	// Saves a workspace to a target file.
	private boolean saveWorkspaceTo(File workspaceFile)
	{
		DoomToolsWorkspace out = desktop.getWorkspace();
		out.setWindowWidth(getWidth());
		out.setWindowHeight(getHeight());
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(workspaceFile), "UTF-8"))
		{
			JSONWriter.writeJSON(out, JSON_OPTIONS, writer);
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "I/O Error saving workspace: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.saveas.notwritten", workspaceFile.getAbsolutePath()));
			return false;
		}
		catch (SecurityException e)
		{
			LOG.errorf(e, "Security Error saving workspace: Access Denied: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.saveas.notwritten.security", workspaceFile.getAbsolutePath()));
			return false;
		}
		catch (Exception e)
		{
			LOG.errorf(e, "Unexpected Error saving workspace: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.saveas.notwritten.unexpected", workspaceFile.getAbsolutePath()));
			return false;
		}
		
		currentWorkspace = workspaceFile;
		LOG.infof("Saved workspace: %s", workspaceFile.getAbsolutePath());
		SwingUtils.info(this, language.getText("doomtools.workspace.saveas.success"));
		return true;
	}
	
	private boolean loadWorkspaceFrom(File workspaceFile)
	{
		DoomToolsWorkspace workspace;
		try (Reader reader = new InputStreamReader(new FileInputStream(workspaceFile), "UTF-8"))
		{
			workspace = JSONReader.readJSON(DoomToolsWorkspace.class, reader);
		} 
		catch (FileNotFoundException e) 
		{
			LOG.errorf(e, "Workspace not found: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.open.notfound", workspaceFile.getAbsolutePath()));
			return false;
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "I/O Error loading workspace: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.open.notopened", workspaceFile.getAbsolutePath()));
			return false;
		}
		catch (SecurityException e)
		{
			LOG.errorf(e, "Security Error saving workspace: Access Denied: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.saveas.notwritten.security", workspaceFile.getAbsolutePath()));
			return false;
		}
		catch (Exception e)
		{
			LOG.errorf(e, "Unexpected Error saving workspace: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.saveas.notwritten.unexpected", workspaceFile.getAbsolutePath()));
			return false;
		}
		
		LOG.infof("Opened workspace: %s", workspaceFile.getAbsolutePath());
		setBounds(getX(), getY(), workspace.getWindowWidth(), workspace.getWindowHeight());
		try {
			desktop.setWorkspace(workspace, applicationStarter);
			currentWorkspace = workspaceFile;
			updateWorkspaceActions();
			return true;
		} catch (Exception e) {
			LOG.errorf(e, "Unexpected Error loading workspace: %s", workspaceFile.getAbsolutePath());
			SwingUtils.error(this, language.getText("doomtools.workspace.open.unexpected", workspaceFile.getAbsolutePath()));
			return false;
		}
	}

	private JMenuBar createMenuBar()
	{
		return menuBar(
			// File
			utils.createMenuFromLanguageKey("doomtools.menu.file",
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.open", (c, e) -> openWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.save", actionSaveWorkspace),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.saveas", actionSaveWorkspaceAs),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.close", actionClearWorkspace),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.settings", (c, e) -> openSettingsModal()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.exit", (c, e) -> shutDownHook.run())
			),

			// Tools
			utils.createMenuFromLanguageKey("doomtools.menu.tools",
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack.item.editor", (c, e) -> addApplication(new DecoHackEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack.item.compile", (c, e) -> addApplication(new DecoHackCompilerApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake",
						utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.new", (c, e) -> addApplication(new DoomMakeNewProjectApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.open", (c, e) -> openDoomMakeProject())
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge.item.editor", (c, e) -> addApplication(new WadMergeEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge.item.execute", (c, e) -> addApplication(new WadMergeExecutorApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript.item.editor", (c, e) -> addApplication(new WadScriptEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript.item.execute", (c, e) -> addApplication(new WadScriptExecutorApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.wswantbl",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wswantbl.item.editor", (c, e) -> addApplication(new WSwAnTablesEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wswantbl.item.compile", (c, e) -> addApplication(new WSwAnTablesCompilerApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wtexscan", (c, e) -> addApplication(new WTexScanApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wtexport", (c, e) -> addApplication(new WTExportApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.scanexport", (c, e) -> addApplication(new WTexScanTExportApp()))
				)
			),

			// View
			utils.createMenuFromLanguageKey("doomtools.menu.view",
				utils.createItemFromLanguageKey("doomtools.menu.view.item.cascade", (c, e) -> desktop.cascadeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.minimize", (c, e) -> desktop.minimizeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.restore", (c, e) -> desktop.restoreWorkspace())
			),

			// Help
			utils.createMenuFromLanguageKey("doomtools.menu.help",
				utils.createItemFromLanguageKey("doomtools.menu.help.item.about", (c, e) -> openAboutModal()),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.licenses", (c, e) -> openLicensesModal()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.opendocs", (c, e) -> openDocs()),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.opensettings", (c, e) -> openSettingsFolder()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.openweb", (c, e) -> openWebsite()),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.openrepo", (c, e) -> openRepositorySite()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (c, e) -> openChangeLogModal()),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.update", (c, e) -> openUpdate())
			)
		);
	}
	
	private void openAboutModal()
	{
		modal(this, utils.getWindowIcons(), 
			language.getText("doomtools.about.title"), 
			new DoomToolsAboutPanel(), 
			choice("OK", KeyEvent.VK_O)
		).openThenDispose();
	}
	
	private void openLicensesModal()
	{
		String[] licenses = {
			"docs/licenses/LICENSE-BlackRookBase.txt",
			"docs/licenses/LICENSE-BlackRookJSON.txt",
			"docs/licenses/LICENSE-DoomStruct.txt",
			"docs/licenses/LICENSE-RookScript.txt",
			"docs/licenses/LICENSE-RookScript-Desktop.txt",
			"docs/licenses/LICENSE-FlatLaF.txt",
			"docs/licenses/LICENSE-RSyntaxTextArea.txt",
			"docs/licenses/LICENSE-AutoComplete.txt",
			"docs/licenses/LICENSE-Silk Icons.txt"
		};
		
		HelpSource[] sources = new HelpSource[licenses.length];
		for (int i = 0; i < licenses.length; i++) {
			String lic = licenses[i];
			sources[i] = utils.helpResource(lic);
		}
		
		utils.createHelpModal(ModalityType.APPLICATION_MODAL, sources).openThenDispose();
	}
	
	private void openChangeLogModal()
	{
		utils.createHelpModal(ModalityType.APPLICATION_MODAL, utils.helpResource("docs/CHANGELOG.md")).openThenDispose();
	}
	
	private void openSettingsModal()
	{
		DoomToolsSettingsPanel settingsPanel = new DoomToolsSettingsPanel();
		modal(this, utils.getWindowIcons(), 
			language.getText("doomtools.settings.title"), 
			settingsPanel 
		).openThenDispose();
		settingsPanel.commitSettings();
	}
	
	private void openDocs()
	{
		final String path; 
		try {
			path = Environment.getDoomToolsPath();
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.open"));
			return;
		}
		
		LOG.info("Opening the DoomTools documentation folder...");

		File docsDir = new File(path + File.separator + "docs");
		if (!docsDir.exists())
		{
			SwingUtils.error(language.getText("doomtools.error.opendocs.notfound"));
		}
		else
		{
			try {
				Desktop.getDesktop().open(docsDir);
			} catch (IOException e) {
				SwingUtils.error(language.getText("doomtools.error.opendocs.io"));
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("doomtools.error.opendocs.security"));
			}
		}
	}
	
	private void openSettingsFolder()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.open"));
			return;
		}
		
		LOG.info("Opening the DoomTools settings folder...");
		
		File settingsDir = new File(Paths.APPDATA_PATH);
		if (!settingsDir.exists())
		{
			SwingUtils.error(language.getText("doomtools.error.opensettings.notfound"));
			return;
		}
		
		try {
			Desktop.getDesktop().open(settingsDir);
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.opensettings.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.opensettings.security"));
		}
	}
	
	private void openWebsite()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.browse"));
			return;
		}
				
		LOG.info("Opening the DoomTools website...");

		try {
			Desktop.getDesktop().browse(new URI(DoomToolsMain.DOOMTOOLS_WEBSITE));
		} catch (URISyntaxException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.url"));
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.security"));
		}
	}
	
	private void openRepositorySite()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.browse"));
			return;
		}
				
		LOG.info("Opening the DoomTools code repository website...");

		try {
			Desktop.getDesktop().browse(new URI(DoomToolsMain.DOOMTOOLS_REPO_WEBSITE));
		} catch (URISyntaxException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.url"));
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.security"));
		}
	}
	
	private void openUpdate()
	{
		final String path; 
		try {
			path = Environment.getDoomToolsPath();
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		final DoomToolsProgressPanel progressPanel = new DoomToolsProgressPanel(48);
		progressPanel.setActivityMessage("Please wait...");
		progressPanel.setProgressLabel("");
		progressPanel.setIndeterminate();
		
		Modal<Object> progressModal = modal(
			utils.getWindowIcons(),
			language.getText("doomtools.update.title"),
			containerOf(createEmptyBorder(8, 8, 8, 8), node(BorderLayout.CENTER, progressPanel)),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);

		final AtomicBoolean successful = new AtomicBoolean(false);
		
		// Listener 
		DoomToolsUpdater.Listener listener = new DoomToolsUpdater.Listener() 
		{
			@Override
			public void onMessage(String message) 
			{
				progressPanel.setActivityMessage(message);
			}

			@Override
			public void onError(String message) 
			{
				SwingUtils.error(progressModal, message);
				progressPanel.setErrorMessage(language.getText("doomtools.update.failed"));
			}

			@Override
			public void onDownloadStart() 
			{
				progressPanel.setActivityMessage(language.getText("doomtools.update.downloading"));
				progressPanel.setProgressLabel("0%");
			}

			@Override
			public void onDownloadTransfer(long current, Long max) 
			{
				int kbs = (int)(current / 1024L);
				if (max != null)
				{
					int maxkbs = (int)(max / 1024L);
					int pct = kbs * 100 / maxkbs;
					progressPanel.setActivityMessage(language.getText("doomtools.update.downloading.amount2", kbs, maxkbs));
					progressPanel.setProgressLabel(pct + "%");
					progressPanel.setProgress(0, kbs, maxkbs);
				}
				else // length was not in response
				{
					progressPanel.setActivityMessage(language.getText("doomtools.update.downloading.amount1", kbs));
					progressPanel.setProgressLabel("N/A");
				}
			}

			@Override
			public void onDownloadFinish() 
			{
				progressPanel.setActivityMessage(language.getText("doomtools.update.downloading.finished"));
				progressPanel.setProgressLabel("100%");
				progressPanel.setProgress(0, 100, 100);
			}

			@Override
			public boolean shouldContinue(String versionString)
			{
				progressPanel.setActivityMessage(language.getText("doomtools.update.downloading.found"));
				return SwingUtils.yesTo(progressModal, language.getText("doomtools.update.continue", versionString));
			}

			@Override
			public void onUpToDate() 
			{
				progressPanel.setSuccessMessage(language.getText("doomtools.update.downloading.uptodate"));
				progressPanel.setProgressLabel("100%");
				progressPanel.setProgress(0, 100, 100);
			}

			@Override
			public void onUpdateSuccessful() 
			{
				progressPanel.setSuccessMessage(language.getText("doomtools.update.downloading.success"));
				successful.set(true);
			}

			@Override
			public void onUpdateAbort() 
			{
				progressPanel.setErrorMessage(language.getText("doomtools.update.downloading.aborted"));
				progressPanel.setProgressLabel("");
				progressPanel.setProgress(0, 0, 100);
			}
		};
		
		try {
			
			InstancedFuture<Integer> instance = tasks.spawn(new DoomToolsUpdater(new File(path), listener));
			progressModal.openThenDispose(); // will hold here until closed.
			if (!instance.isDone())
				instance.cancel();
			
			if (successful.get())
				SwingUtils.info(this, language.getText("doomtools.update.success"));
			
		} catch (Exception e) {
			LOG.error(e, "Uncaught error during update.");
			SwingUtils.error(this, "Uncaught error during update: " + e.getClass().getSimpleName());
		}
	}
	

	/* ==================================================================== */

	// Sets availability of workspace actions by state.
	private void updateWorkspaceActions()
	{
		boolean workspacePresent = desktop.hasWorkspace();
		actionSaveWorkspace.setEnabled(workspacePresent);
		actionSaveWorkspaceAs.setEnabled(workspacePresent);
		actionClearWorkspace.setEnabled(workspacePresent);
	}
	
	// Open workspace.
	private void openWorkspace()
	{
		if (desktop.hasWorkspace())
		{
			if (SwingUtils.noTo(language.getText("doomtools.workspace.warning")))
				return;
		}
		
		File workspaceFile = chooseFile(
			this,
			language.getText("doomtools.workspace.open.browse.title"),
			currentWorkspace,
			language.getText("doomtools.workspace.open.browse.accept"),
			FileFilters.WORKSPACES
		);
		
		if (workspaceFile == null)
			return;
		
		loadWorkspaceFrom(workspaceFile);
	}

	// Save workspace.
	private void saveWorkspace()
	{
		if (!desktop.hasWorkspace())
		{
			SwingUtils.info(this, language.getText("doomtools.workspace.save.noworkpace"));
			return;
		}
		
		if (currentWorkspace != null)
			saveWorkspaceTo(currentWorkspace);
		else
			saveWorkspaceAs();
	}
	
	// Save workspace as.
	private void saveWorkspaceAs()
	{
		if (!desktop.hasWorkspace())
		{
			SwingUtils.info(this, language.getText("doomtools.workspace.save.noworkpace"));
			return;
		}
		
		File workspaceFile = utils.chooseFile(
			this,
			language.getText("doomtools.workspace.saveas.browse.title"),
			language.getText("doomtools.workspace.saveas.browse.accept"),
			settings::getLastProjectDirectory,
			settings::setLastProjectDirectory,
			(filter, file) -> filter == FileFilters.WORKSPACES ? FileUtils.addMissingExtension(file, "dtw") : file,
			FileFilters.WORKSPACES
		);
		
		if (workspaceFile == null)
			return;

		saveWorkspaceTo(workspaceFile);
	}
	
	private void clearWorkspace()
	{
		if (SwingUtils.yesTo(this, language.getText("doomtools.closeall")))
		{
			desktop.clearWorkspace();
			currentWorkspace = null;
			updateWorkspaceActions();
		}
	}
	
	private void openDoomMakeProject()
	{
		DoomMakeOpenProjectApp app;
		if ((app = DoomMakeOpenProjectApp.openAndCreate(this)) != null)
			addApplication(app);
	}

}
