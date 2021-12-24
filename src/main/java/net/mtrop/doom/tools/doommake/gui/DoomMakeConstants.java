package net.mtrop.doom.tools.doommake.gui;

import net.mtrop.doom.tools.struct.util.OSUtils;

public interface DoomMakeConstants 
{
	/** Common paths. */
	interface Paths
	{
		/** DoomMake Config folder base. */
	    String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + "/DoomMake/";
	}
	
	/** Common channel names. */
	interface Channels
	{
		/** A DoomMake process has started. */
		String DOOMMAKE_STARTED = "doommake_started";
		/** A DoomMake process has finished successfully. */
		String DOOMMAKE_FINISHED_SUCCESS = "doommake_finished_success";
		/** A DoomMake process has finished unsuccessfully. */
		String DOOMMAKE_FINISHED_ERROR = "doommake_finished_error";
	}
    
}
