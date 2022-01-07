package net.mtrop.doom.tools.doommake.gui;

import java.io.File;
import java.io.FileNotFoundException;

import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.ProcessCallException;

public final class DoomMakeProjectHelperTest 
{
	public static void main(String[] args) throws FileNotFoundException, ProcessCallException, InterruptedException 
	{
		File projectDir = new File(args[0]);
		DoomMakeProjectHelper helper = DoomMakeProjectHelper.get();
		
		System.out.println(helper.getProjectTargets(projectDir));
		helper.callDoomMakeTarget(projectDir, System.out, null, null, "patch").waitFor();
		helper.callDoomMakeTarget(projectDir, System.out, null, null, "make").getKillSwitch().run();
	}
}
