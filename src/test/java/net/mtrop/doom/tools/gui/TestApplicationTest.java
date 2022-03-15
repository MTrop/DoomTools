package net.mtrop.doom.tools.gui;

import net.mtrop.doom.tools.gui.doommake.DoomMakeNewProjectApp;

public final class TestApplicationTest
{

	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		DoomToolsGUIMain.startApplication(new DoomMakeNewProjectApp(null));
	}

}
