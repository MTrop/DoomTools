package net.mtrop.doom.tools.doommake.gui;

import javax.swing.JFrame;

import net.mtrop.doom.tools.doommake.gui.swing.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

public final class DoomMakeSettingsPanelTest 
{
	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		DoomMakeSettingsPanel panel = new DoomMakeSettingsPanel();
		SwingUtils.apply(frame("Test", containerOf(panel)), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
