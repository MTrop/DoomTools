package net.mtrop.doom.tools.gui;

import net.mtrop.doom.tools.gui.doommake.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

public final class TestApplicationTest
{

	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		DoomToolsGUIMain.startApplication(new DoomMakeNewProjectApp(null));
	}

}
