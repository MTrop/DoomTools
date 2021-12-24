package net.mtrop.doom.tools.doommake.gui;

import javax.swing.JFrame;

import net.mtrop.doom.tools.doommake.gui.swing.panels.DoomMakeAboutPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

public final class DoomMakeAboutPanelTest 
{
	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		SwingUtils.apply(frame("Test", containerOf(new DoomMakeAboutPanel())), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
