/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.awt.Container;

/**
 * Interface for a an object by which an application instance 
 * Can talk back to the application manager.
 * @author Matthew Tropiano
 */
public interface DoomToolsApplicationListener extends DoomToolsApplicationStarter
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
