package net.mtrop.doom.tools.gui.swing;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsDesktopPane;

/**
 * The main DoomTools application window.
 * @author Matthew Tropiano
 */
public class DoomToolsMainWindow extends DoomToolsFrame 
{
	private static final long serialVersionUID = -8837485206120777188L;

	/** Desktop pane. */
	private DoomToolsDesktopPane desktop;
	
	/**
	 * Creates the DoomTools main window.
	 */
	public DoomToolsMainWindow()
	{
		super();
		setContentPane(desktop = new DoomToolsDesktopPane());
		pack();
	}
	
	/**
	 * Adds a new application instance to the desktop.
	 * @param <I> the instance type.
	 * @param applicationClass the application class.
	 * @throws RuntimeException if the class could not be instantiated.
	 */
	public <I extends DoomToolsApplicationInstance> void addApplication(Class<I> applicationClass)
	{
		desktop.addApplicationFrame(Common.create(applicationClass)).setVisible(true);
	}
	
}
