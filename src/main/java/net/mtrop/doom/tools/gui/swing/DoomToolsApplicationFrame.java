package net.mtrop.doom.tools.gui.swing;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;

/**
 * A single application frame for a DoomTools application.
 * @author Matthew Tropiano
 */
public class DoomToolsApplicationFrame extends DoomToolsFrame 
{
	private static final long serialVersionUID = -3110415734317195898L;
	
	/** The application instance. */
	private DoomToolsApplicationInstance instance;
	
	/**
	 * Creates an application frame from an application instance.
	 * @param instance the instance to use.
	 */
	public DoomToolsApplicationFrame(DoomToolsApplicationInstance instance)
	{
		this.instance = instance;
		setTitle(instance.getName());
		setJMenuBar(instance.getMenuBar());
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
