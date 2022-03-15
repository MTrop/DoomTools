package net.mtrop.doom.tools.gui;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

import javax.swing.JFrame;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.doommake.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.doommake.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.swing.DoomToolsApplicationFrame;
import net.mtrop.doom.tools.gui.swing.DoomToolsMainWindow;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * Manages the DoomTools GUI window. 
 * @author Matthew Tropiano
 */
public final class DoomToolsGUIMain 
{
	/**
	 * Valid application names. 
	 */
	public interface ApplicationNames
	{
		/** DoomMake - New Project. */
		String DOOMMAKE_NEW = "doommake-new";
		/** DoomMake - Open Project. */
		String DOOMMAKE_OPEN = "doommake-open";
	}
	
	/**
	 * Supported GUI Themes
	 */
	public enum Theme
	{
		LIGHT("com.formdev.flatlaf.FlatLightLaf"),
		DARK("com.formdev.flatlaf.FlatDarkLaf"),
		INTELLIJ("com.formdev.flatlaf.FlatIntelliJLaf"),
		DARCULA("com.formdev.flatlaf.FlatDarculaLaf");
		
		public static final Map<String, Theme> MAP = EnumUtils.createCaseInsensitiveNameMap(Theme.class);
		
		private final String className;
		
		private Theme(String className)
		{
			this.className = className;
		}
	}
	
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsGUIMain.class); 

    /** Instance socket. */
	private static final int INSTANCE_SOCKET_PORT = 54666;
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsGUIMain> INSTANCE = new SingletonProvider<>(() -> new DoomToolsGUIMain());
    /** Application starter linker. */
    private static final DoomToolsApplicationStarter STARTER = new DoomToolsApplicationStarter()
	{
		@Override
		public void startApplication(DoomToolsApplicationInstance applicationInstance) 
		{
			DoomToolsGUIMain.startApplication(applicationInstance);
		}
	};
    
    /** Instance socket. */
    @SuppressWarnings("unused")
	private static ServerSocket instanceSocket;
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsGUIMain get()
	{
		return INSTANCE.get();
	}

	/**
	 * @return true if already running, false if not.
	 */
	public static boolean isAlreadyRunning()
	{
		try {
			instanceSocket = new ServerSocket(INSTANCE_SOCKET_PORT, 50, InetAddress.getByName(null));
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	/**
	 * Starts an orphaned main GUI Application.
	 * Inherits the working directory and environment.
	 * @return the process created.
	 * @throws IOException if the application could not be created.
	 * @see Common#spawnJava(Class) 
	 */
	public static Process startGUIAppProcess() throws IOException
	{
		return Common.spawnJava(DoomToolsGUIMain.class).exec();
	}
	
	/**
	 * Starts an orphaned GUI Application by name.
	 * Inherits the working directory and environment.
	 * @param appName the application name (see {@link ApplicationNames}).
	 * @param args optional addition arguments (some apps require them).
	 * @return the process created.
	 * @throws IOException if the application could not be created.
	 * @see Common#spawnJava(Class) 
	 */
	public static Process startGUIAppProcess(String appName, String ... args) throws IOException
	{
		return Common.spawnJava(DoomToolsGUIMain.class).arg(appName).args(args).exec();
	}
	
	
    
	/* ==================================================================== */

	/**
	 * Adds a new application instance to the main desktop by class.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 */
	public static <A extends DoomToolsApplicationInstance> void startApplication(Class<A> applicationClass)
	{
		startApplication(Common.create(applicationClass));
	}
    
	/**
	 * Adds a new application instance to the main desktop.
	 * @param applicationInstance the application instance.
	 */
	public static void startApplication(final DoomToolsApplicationInstance applicationInstance)
	{
		final DoomToolsApplicationFrame frame = new DoomToolsApplicationFrame(applicationInstance, STARTER);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				if (applicationInstance.shouldClose())
				{
					frame.setVisible(false);
					applicationInstance.onClose();
					frame.dispose();
				}
			}
		});
		frame.setVisible(true);
	}
	
	/**
	 * Sets the preferred Look And Feel.
	 */
	public static void setLAF() 
	{
		Theme theme = Theme.MAP.get(DoomToolsSettingsManager.get().getThemeName());
		SwingUtils.setLAF(theme != null ? theme.className : Theme.LIGHT.className);
	}

    /* ==================================================================== */
	
    /**
	 * Main method - check for running local instance. If running, do nothing.
	 * @param args command line arguments.
	 */
	public static void main(String[] args) 
	{
		setLAF();
		
		// no args - run main application.
		if (args.length == 0)
		{
	    	if (isAlreadyRunning())
	    	{
	    		System.err.println("DoomTools is already running.");
	    		System.exit(1);
	    		return;
	    	}
			get().createAndDisplayMainWindow();
		}
		// run standalone application.
		else 
		{
			try 
			{
				switch (args[0])
				{
					default:
		        		SwingUtils.error("Expected valid application name.");
			    		System.err.println("ERROR: Expected valid application name.");
		        		System.exit(-1);
		        		return;
					case ApplicationNames.DOOMMAKE_NEW:
						startApplication(new DoomMakeNewProjectApp(Common.arrayElement(args, 1)));
						break;
					case ApplicationNames.DOOMMAKE_OPEN:
						startApplication(new DoomMakeOpenProjectApp(new File(args[1])));
						break;
				}
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
	    		SwingUtils.error("Missing argument for application: " + e.getLocalizedMessage());
	    		System.err.println("ERROR: Missing argument for application.");
        		System.exit(-1);
			}
		}
		
	}

	/** Settings singleton. */
	private DoomToolsSettingsManager settings;
	/** Language manager. */
    private DoomToolsLanguageManager language;
    /** The main window. */
    private DoomToolsMainWindow window;
    
    private DoomToolsGUIMain()
    {
    	this.settings = DoomToolsSettingsManager.get();
    	this.language = DoomToolsLanguageManager.get();
    	this.window = null;
    }

    /**
     * Creates and displays the main window.
     */
    public void createAndDisplayMainWindow()
    {
    	LOG.info("Creating main window...");
    	window = new DoomToolsMainWindow(this::attemptShutDown);
    	window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	window.addWindowListener(new WindowAdapter()
    	{
    		@Override
    		public void windowClosing(WindowEvent e) 
    		{
    			attemptShutDown();
    		}
		});
    	
    	Rectangle windowBounds;
    	if ((windowBounds = settings.getWindowBounds()) != null)
    		window.setBounds(windowBounds);
    	
    	window.setVisible(true);
		if (settings.getWindowMaximized())
			window.setExtendedState(window.getExtendedState() | DoomToolsMainWindow.MAXIMIZED_BOTH);
		
    	LOG.info("Window created.");
    }

    // Attempts a shutdown, prompting the user first.
    private boolean attemptShutDown()
    {
    	LOG.debug("Shutdown attempted.");
		if (SwingUtils.yesTo(window, language.getText("doomtools.quit")))
		{
			shutDown();
			return true;
		}
		return false;
    }

    // Saves and quits.
    private void shutDown()
    {
    	LOG.info("Shutting down DoomTools GUI...");
    	
    	LOG.info("Sending close to all open apps...");
    	window.shutDownApps();
    	
    	LOG.debug("Disposing main window...");
    	settings.setWindowBounds(window.getX(), window.getY(), window.getWidth(), window.getHeight(), 
    		(window.getExtendedState() & DoomToolsMainWindow.MAXIMIZED_BOTH) != 0
    	);
    	window.setVisible(false);
    	window.dispose();
    	LOG.debug("Main window disposed.");
    	
    	LOG.info("Exiting JVM...");
    	System.exit(0);
    }
    
}
