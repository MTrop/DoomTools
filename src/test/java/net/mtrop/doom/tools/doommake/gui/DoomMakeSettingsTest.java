package net.mtrop.doom.tools.doommake.gui;

import java.io.File;

import net.mtrop.doom.tools.struct.util.OSUtils;

public final class DoomMakeSettingsTest 
{
	public static void main(String[] args) 
	{
		DoomMakeSettings.get().setLastProjectDirectory(new File(OSUtils.getWorkingDirectoryPath()));
	}
}
