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
