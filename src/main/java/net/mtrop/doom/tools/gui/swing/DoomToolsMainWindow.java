/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing;

import java.awt.BorderLayout;
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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import com.blackrook.json.JSONReader;
import com.blackrook.json.JSONWriter;
import com.blackrook.json.JSONWriter.Options;

import net.mtrop.doom.tools.Environment;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doomtools.DoomToolsUpdater;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsWorkspace;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.gui.apps.DImageConvertApp;
import net.mtrop.doom.tools.gui.apps.DImageConvertOffsetterApp;
import net.mtrop.doom.tools.gui.apps.DMXConvertApp;
import net.mtrop.doom.tools.gui.apps.DecoHackCompilerApp;
import net.mtrop.doom.tools.gui.apps.DecoHackEditorApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeExploderApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeStudioApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesCompilerApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesEditorApp;
import net.mtrop.doom.tools.gui.apps.WTExportApp;
import net.mtrop.doom.tools.gui.apps.WTexListApp;
import net.mtrop.doom.tools.gui.apps.WTexListTExportApp;
import net.mtrop.doom.tools.gui.apps.WTexScanApp;
import net.mtrop.doom.tools.gui.apps.WTexScanTExportApp;
import net.mtrop.doom.tools.gui.apps.WadMergeEditorApp;
import net.mtrop.doom.tools.gui.apps.WadMergeExecutorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptEditorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptExecutorApp;
import net.mtrop.doom.tools.gui.apps.WadTexCompilerApp;
import net.mtrop.doom.tools.gui.apps.WadTexEditorApp;
import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils.HelpSource;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsAboutJavaPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsAboutPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsDesktopPane;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsProgressPanel;
import net.mtrop.doom.tools.gui.swing.panels.settings.DoomToolsSettingsPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
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
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.open", (i) -> openWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.save", actionSaveWorkspace),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.saveas", actionSaveWorkspaceAs),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.workspace.close", actionClearWorkspace),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.settings", (i) -> openSettingsModal()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.exit", (i) -> shutDownHook.run())
			),

			// Tools
			utils.createMenuFromLanguageKey("doomtools.menu.tools",
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.converters",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.converters.item.dmxconv", (i) -> addApplication(new DMXConvertApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.converters.item.dimgconv", (i) -> addApplication(new DImageConvertApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.converters.item.dimgconv.offsetter", (i) -> openDImageConvertOffsetter())
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack.item.editor", (i) -> addApplication(new DecoHackEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.decohack.item.compile", (i) -> addApplication(new DecoHackCompilerApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.new", (i) -> addApplication(new DoomMakeNewProjectApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.open", (i) -> openDoomMakeProject()),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.studio", (i) -> openDoomMakeStudio()),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.item.exploder", (i) -> addApplication(new DoomMakeExploderApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge.item.editor", (i) -> addApplication(new WadMergeEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadmerge.item.execute", (i) -> addApplication(new WadMergeExecutorApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript.item.editor", (i) -> addApplication(new WadScriptEditorApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.wadscript.item.execute", (i) -> addApplication(new WadScriptExecutorApp()))
				),
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wswantbl",
						utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wswantbl.item.editor", (i) -> addApplication(new WSwAnTablesEditorApp())),
						utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wswantbl.item.compile", (i) -> addApplication(new WSwAnTablesCompilerApp()))
					),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wadtex",
						utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wadtex.item.editor", (i) -> addApplication(new WadTexEditorApp())),
						utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wadtex.item.compile", (i) -> addApplication(new WadTexCompilerApp()))
					),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wtexport", (i) -> addApplication(new WTExportApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wtexlist", (i) -> addApplication(new WTexListApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.listexport", (i) -> addApplication(new WTexListTExportApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.wtexscan", (i) -> addApplication(new WTexScanApp())),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.textures.item.scanexport", (i) -> addApplication(new WTexScanTExportApp()))
				)
			),

			// View
			utils.createMenuFromLanguageKey("doomtools.menu.view",
				utils.createItemFromLanguageKey("doomtools.menu.view.item.cascade", (i) -> desktop.cascadeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.minimize", (i) -> desktop.minimizeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.restore", (i) -> desktop.restoreWorkspace())
			),

			// Help
			utils.createMenuFromLanguageKey("doomtools.menu.help", ArrayUtils.joinArrays(
				ArrayUtils.arrayOf(
					utils.createItemFromLanguageKey("doomtools.menu.help.item.about", (i) -> openAboutModal()),
					utils.createItemFromLanguageKey("doomtools.menu.help.item.about.java", (i) -> openAboutJavaModal()),
					utils.createItemFromLanguageKey("doomtools.menu.help.item.licenses", (i) -> openLicensesModal()),
					separator()
				),
				AppCommon.get().getCommonHelpMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> openChangeLogModal()),
					utils.createItemFromLanguageKey("doomtools.menu.help.item.update", (i) -> openUpdate())
				)
			))
		);
	}
	
	private void openAboutModal()
	{
		modal(this, utils.getWindowIcons(), 
			language.getText("doomtools.about.title"), 
			new DoomToolsAboutPanel(), 
			choice("OK", KeyEvent.VK_O, (Object)null)
		).openThenDispose();
	}
	
	private void openAboutJavaModal()
	{
		modal(this, utils.getWindowIcons(), 
			language.getText("doomtools.about.java.title"), 
			new DoomToolsAboutJavaPanel(), 
			choice("OK", KeyEvent.VK_O, (Object)null)
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
			"docs/licenses/LICENSE-CommonMark.txt",
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
		
		File workspaceFile = utils.chooseFile(
			this,
			language.getText("doomtools.workspace.open.browse.title"),
			language.getText("doomtools.workspace.open.browse.accept"),
			() -> currentWorkspace != null ? currentWorkspace : settings.getLastProjectDirectory(),
			settings::setLastProjectDirectory,
			utils.createWorkspaceFilter()
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
		
		final FileFilter workspaceFilter = utils.createWorkspaceFilter();
		
		File workspaceFile = utils.chooseFile(
			this,
			language.getText("doomtools.workspace.saveas.browse.title"),
			language.getText("doomtools.workspace.saveas.browse.accept"),
			settings::getLastProjectDirectory,
			settings::setLastProjectDirectory,
			(filter, file) -> filter == workspaceFilter ? FileUtils.addMissingExtension(file, "dtw") : file,
			workspaceFilter
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
	
	private void openDImageConvertOffsetter()
	{
		DImageConvertOffsetterApp app;
		if ((app = DImageConvertOffsetterApp.openAndCreate(this)) != null)
			addApplication(app);
	}

	private void openDoomMakeProject()
	{
		DoomMakeOpenProjectApp app;
		if ((app = DoomMakeOpenProjectApp.openAndCreate(this)) != null)
			addApplication(app);
	}

	private void openDoomMakeStudio()
	{
		File dir;
		if ((dir = DoomMakeStudioApp.openAndGetDirectory(this)) == null)
			return;
		
		try {
			DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_STUDIO, dir.getAbsolutePath());
		} catch (IOException e) {
			LOG.error(e, "Couldn't start Open Project: " + dir.getAbsolutePath());
			SwingUtils.error(this, language.getText("doommake.error.app.openproject", dir.getAbsolutePath()));
		}
	}

}
