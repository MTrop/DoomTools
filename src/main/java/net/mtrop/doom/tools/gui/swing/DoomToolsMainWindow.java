package net.mtrop.doom.tools.gui.swing;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.Environment;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doomtools.DoomToolsUpdater;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.DoomToolsLogger;
import net.mtrop.doom.tools.gui.doommake.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.doommake.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsAboutPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsDesktopPane;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsSettingsPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The main DoomTools application window.
 * @author Matthew Tropiano
 */
public class DoomToolsMainWindow extends JFrame 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsMainWindow.class); 

    private static final long serialVersionUID = -8837485206120777188L;

	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
	/** Desktop pane. */
	private DoomToolsDesktopPane desktop;
	/** Shutdown hook. */
	private Runnable shutDownHook;
	
    /** Application starter linker. */
    private DoomToolsApplicationStarter applicationStarter;
	
	/**
	 * Creates the DoomTools main window.
	 * @param shutDownHook the application shutdown hook.
	 */
	public DoomToolsMainWindow(Runnable shutDownHook)
	{
		super();
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.shutDownHook = shutDownHook;
		
		this.applicationStarter = new DoomToolsApplicationStarter()
		{
			@Override
			public <A extends DoomToolsApplicationInstance> void startApplication(Class<A> applicationClass) 
			{
				addApplication(applicationClass);
			}

			@Override
			public <A extends DoomToolsApplicationInstance> void startApplication(A applicationInstance) 
			{
				addApplication(applicationInstance);
			}
		};
		
		setIconImages(utils.getWindowIcons());
		setTitle("DoomTools v" + Version.DOOMTOOLS);
		setJMenuBar(createMenuBar());
		setContentPane(this.desktop = new DoomToolsDesktopPane());
		setLocationByPlatform(true);
		pack();
	}

	private JMenuBar createMenuBar()
	{
		return menuBar(
			// File
			utils.createMenuFromLanguageKey("doomtools.menu.file",
				utils.createItemFromLanguageKey("doomtools.menu.file.item.settings",
					(c, e) -> openSettingsModal()
				),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.file.item.exit",
					(c, e) -> shutDownHook.run()
				)
			),

			// Tools
			utils.createMenuFromLanguageKey("doomtools.menu.tools",
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.new",
						(c, e) -> addApplication(new DoomMakeNewProjectApp(null))
					),
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.open",
						(c, e) -> {
							DoomMakeOpenProjectApp app;
							if ((app = DoomMakeOpenProjectApp.openAndCreate(this, null)) != null)
								addApplication(app);
						}
					)
				)
			),

			// View
			utils.createMenuFromLanguageKey("doomtools.menu.view",
				utils.createItemFromLanguageKey("doomtools.menu.view.item.cascade", (c, e) -> desktop.cascadeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.minimize", (c, e) -> desktop.minimizeWorkspace()),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.restore", (c, e) -> desktop.restoreWorkspace()),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.view.item.close", (c, e) -> {
					if (SwingUtils.yesTo(this, language.getText("doomtools.closeall")))
						desktop.clearWorkspace();
				})
			),

			// Help
			utils.createMenuFromLanguageKey("doomtools.menu.help",
				utils.createItemFromLanguageKey("doomtools.menu.help.item.about",
					(c, e) -> openAboutModal()
				),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.opendocs",
					(c, e) -> openDocs()
				),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.opensettings",
					(c, e) -> openSettingsFolder()
				),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.openweb",
					(c, e) -> openWebsite()
				),
				separator(),
				utils.createItemFromLanguageKey("doomtools.menu.help.item.update",
					(c, e) -> openUpdate()
				)
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
	
	private void openSettingsModal()
	{
		modal(this, utils.getWindowIcons(), 
			language.getText("doomtools.settings.title"), 
			new DoomToolsSettingsPanel() 
		).openThenDispose();
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
		
		if (Common.isEmpty(path))
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

		try {
			Desktop.getDesktop().open(new File(path + File.separator + "docs"));
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.opendocs.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.opendocs.security"));
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
		
		File settingsDir = new File(Common.SETTINGS_PATH);
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
	
	private void openUpdate()
	{
		final String path; 
		try {
			path = Environment.getDoomToolsPath();
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (Common.isEmpty(path))
		{
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		// TODO: Finish this. Create modal.
		
		// Listener 
		DoomToolsUpdater.Listener listener = new DoomToolsUpdater.Listener() 
		{
			@Override
			public void onMessage(String message) 
			{
				// TODO: Set active message.
			}

			@Override
			public void onError(String message) 
			{
				// TODO: Spit out error.
			}

			@Override
			public void onDownloadStart() 
			{
				// TODO: Init progress.
			}

			@Override
			public void onDownloadTransfer(long current, Long max) 
			{
				// TODO: Change progress.
			}

			@Override
			public void onDownloadFinish() 
			{
				// TODO: Finish progress.
			}

			@Override
			public boolean shouldContinue(String versionString)
			{
				// TODO: Ask to continue, return true for yes, false for no.
				return false;
			}

			@Override
			public void onUpToDate() 
			{
				// TODO: End successfully.
			}

			@Override
			public void onUpdateSuccessful() 
			{
				// TODO: End successfully.
			}

			@Override
			public void onUpdateAbort() 
			{
				// TODO: Abort message.
			}
		};
		
		try {
			// TODO: Spawn this.
			(new DoomToolsUpdater(new File(path), listener)).call();
		} catch (Exception e) {
			LOG.error(e, "Uncaught error during update.");
			SwingUtils.error(this, "Uncaught error during update: " + e.getClass().getSimpleName());
		}
	}
	
	/**
	 * Shuts down all the apps in the window.
	 */
	public void shutDownApps()
	{
		desktop.clearWorkspace();
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
	 * @param <A> the instance type.
	 * @param applicationInstance the application instance.
	 * @throws RuntimeException if the class could not be instantiated.
	 */
	public <A extends DoomToolsApplicationInstance> void addApplication(A applicationInstance)
	{
		desktop.addApplicationFrame(applicationInstance, applicationStarter).setVisible(true);
	}
	
}
