package net.mtrop.doom.tools.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsLanguageManager.Keys;
import net.mtrop.doom.tools.gui.swing.DoomToolsMainWindow;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * Manages the DoomTools GUI window. 
 * @author Matthew Tropiano
 */
public final class DoomToolsGUIMain 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsGUIMain.class); 

    /** Instance socket. */
	private static final int INSTANCE_SOCKET_PORT = 54666;
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsGUIMain> INSTANCE = new SingletonProvider<>(() -> new DoomToolsGUIMain());
    
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
	 * Main method - check for running local instance. If running, do nothing.
	 * @param args command line arguments.
	 */
    public static void main(String[] args) 
    {
    	if (isAlreadyRunning())
    	{
    		System.out.println("DoomTools is already running.");
    		return;
    	}
		
		SwingUtils.setSystemLAF();
		get().createAndDisplayMainWindow();
	}
    
	/* ==================================================================== */
	
    /** The main window. */
    private DoomToolsMainWindow window;
    
    private DoomToolsGUIMain()
    {
    	this.window = null;
    }

    /**
     * Creates and displays the main window.
     */
    public void createAndDisplayMainWindow()
    {
    	LOG.info("Creating main window...");
    	window = new DoomToolsMainWindow();
    	window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	window.addWindowListener(new WindowAdapter()
    	{
    		@Override
    		public void windowClosing(WindowEvent e) 
    		{
    			if (SwingUtils.yesTo(window, DoomToolsLanguageManager.get().getText(Keys.DOOMTOOLS_QUIT)))
    				shutDown();
    		}
		});
    	window.setVisible(true);
    	LOG.info("Window created.");
    }
    
    /**
     * Saves and quits.
     */
    private void shutDown()
    {
    	LOG.info("Shutting down DoomTools GUI...");
    	window.setVisible(false);
    	window.dispose();
    	LOG.info("Main window disposed.");
    	System.exit(0);
    }
    
}
