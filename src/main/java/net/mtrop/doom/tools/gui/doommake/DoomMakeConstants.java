package net.mtrop.doom.tools.gui.doommake;

import net.mtrop.doom.tools.common.Common;

public interface DoomMakeConstants 
{
	/** Common paths. */
	interface Paths
	{
		/** DoomTools Config folder base. */
	    String APPDATA_PATH = Common.SETTINGS_PATH;
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
