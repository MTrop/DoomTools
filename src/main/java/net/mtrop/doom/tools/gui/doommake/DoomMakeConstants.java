package net.mtrop.doom.tools.gui.doommake;

import java.io.File;

import net.mtrop.doom.tools.struct.util.OSUtils;

public interface DoomMakeConstants 
{
	/** Common paths. */
	interface Paths
	{
		/** DoomTools Config folder base. */
	    String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + File.separator + "DoomTools" + File.separator;
	}
	
}
