package net.mtrop.doom.tools.gui.doommake;

import java.io.File;
import java.io.FileNotFoundException;

import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.struct.InstancedFuture;

public final class DoomMakeProjectHelperTest 
{
	public static void main(String[] args) throws FileNotFoundException, ProcessCallException, InterruptedException 
	{
		File projectDir = new File(args[0]);
		DoomMakeProjectHelper helper = DoomMakeProjectHelper.get();
		
		System.out.println(helper.getProjectTargets(projectDir));
		helper.callDoomMakeTarget(projectDir, System.out, System.err, "patch").result();
		InstancedFuture<Integer> instance = helper.callDoomMakeTarget(projectDir, System.out, null, "make");
		Thread.sleep(300);
		instance.cancel();
	}
}
