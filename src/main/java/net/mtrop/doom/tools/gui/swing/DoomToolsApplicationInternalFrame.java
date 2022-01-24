package net.mtrop.doom.tools.gui.swing;

import javax.swing.JInternalFrame;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;

/**
 * A single internal application frame for a DoomTools application.
 * @author Matthew Tropiano
 */
public class DoomToolsApplicationInternalFrame extends JInternalFrame 
{
	private static final long serialVersionUID = 7311434945898035762L;
	
	/** The application instance. */
	private DoomToolsApplicationInstance instance;
	
	/**
	 * Creates an application frame from an application instance.
	 * @param instance the instance to use.
	 */
	public DoomToolsApplicationInternalFrame(DoomToolsApplicationInstance instance)
	{
		this.instance = instance;
		setTitle(instance.getName());
		setJMenuBar(instance.getInternalMenuBar());
		setContentPane(instance.getContentPane());
	}
	
	/**
	 * @return the instance for this frame.
	 */
	public DoomToolsApplicationInstance getInstance() 
	{
		return instance;
	}
	
}
