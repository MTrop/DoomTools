package net.mtrop.doom.tools.gui;

import java.awt.Container;

/**
 * Interface for a an object by which an application instance 
 * Can talk back to the application manager.
 * @author Matthew Tropiano
 */
public interface DoomToolsApplicationControlReceiver
{
	/**
	 * Attempts to close this application instance. 
	 */
	void attemptClose();
	
	/**
	 * @return the application container.
	 */
	Container getApplicationContainer();
	
}
