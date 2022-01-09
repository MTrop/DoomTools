package net.mtrop.doom.tools.gui.doommake.swing;

import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;

import javax.swing.JFrame;

public final class DoomMakeSettingsWindowTest 
{
	public static void main(String[] args) 
	{
		setSystemLAF();
		apply(new DoomMakeSettingsWindow(), (window) -> {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setVisible(true);
		});
	}
}
