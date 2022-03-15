package net.mtrop.doom.tools.gui.doommake.swing;

import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;

public final class DoomMakeSettingsWindowTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		apply(new DoomMakeSettingsWindow(), (window) -> {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setVisible(true);
		});
	}
}
