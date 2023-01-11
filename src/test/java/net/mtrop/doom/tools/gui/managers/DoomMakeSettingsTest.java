/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers;

import java.io.File;

import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;
import net.mtrop.doom.tools.struct.util.OSUtils;

public final class DoomMakeSettingsTest 
{
	public static void main(String[] args) 
	{
		DoomToolsSettingsManager.get().setLastProjectDirectory(new File(OSUtils.getWorkingDirectoryPath()));
	}
}
