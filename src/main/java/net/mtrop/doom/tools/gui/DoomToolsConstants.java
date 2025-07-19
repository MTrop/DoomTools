/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.io.File;

import net.mtrop.doom.tools.struct.util.OSUtils;

public interface DoomToolsConstants 
{
	/** Common paths. */
	interface Paths
	{
		/** DoomTools Config folder base. */
		String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + File.separator + "DoomTools" + File.separator;
	}
	
}
