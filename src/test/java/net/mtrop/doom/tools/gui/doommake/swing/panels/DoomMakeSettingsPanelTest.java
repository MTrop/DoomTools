package net.mtrop.doom.tools.gui.doommake.swing.panels;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.apps.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

public final class DoomMakeSettingsPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		SwingUtils.apply(frame("Test", new DoomMakeSettingsPanel()), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
