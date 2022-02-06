package net.mtrop.doom.tools.gui.swing;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.doommake.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.doommake.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsAboutPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsDesktopPane;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.awt.event.KeyEvent;

/**
 * The main DoomTools application window.
 * @author Matthew Tropiano
 */
public class DoomToolsMainWindow extends JFrame 
{
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
			utils.createMenuFromLanguageKey("doomtools.menu.file",
				utils.createItemFromLanguageKey("doomtools.menu.file.item.exit",
					(c, e) -> shutDownHook.run()
				)
			),
			utils.createMenuFromLanguageKey("doomtools.menu.tools",
				utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake",
					utils.createItemFromLanguageKey("doomtools.menu.tools.item.doommake.new",
						(c, e) -> addApplication(DoomMakeNewProjectApp.class)
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
			utils.createMenuFromLanguageKey("doomtools.menu.help",
				utils.createItemFromLanguageKey("doomtools.menu.help.item.about",
					(c, e) -> openAboutModal()
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
