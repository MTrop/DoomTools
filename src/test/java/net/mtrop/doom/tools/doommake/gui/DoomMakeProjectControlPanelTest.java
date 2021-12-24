package net.mtrop.doom.tools.doommake.gui;

import javax.swing.JFrame;

import net.mtrop.doom.tools.doommake.gui.swing.panels.DoomMakeProjectControlPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.io.File;

public final class DoomMakeProjectControlPanelTest 
{
	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		DoomMakeProjectControlPanel panel = new DoomMakeProjectControlPanel(new File("H:\\DoomDev\\projects\\exor"));
		SwingUtils.apply(frame("Test", containerOf(panel)), 
		(frame) -> {
			frame.setBounds(0, 0, 200, 100);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
