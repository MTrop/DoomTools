package net.mtrop.doom.tools.doommake.gui;

import net.mtrop.doom.tools.struct.OSUtils;

public final class DoomMakeSettingsTest 
{
	public static void main(String[] args) 
	{
		DoomMakeSettings.get().setLastProjectDirectory(OSUtils.getWorkingDirectoryPath());
	}
}
