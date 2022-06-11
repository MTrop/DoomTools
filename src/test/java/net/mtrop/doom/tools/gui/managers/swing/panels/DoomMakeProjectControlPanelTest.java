package net.mtrop.doom.tools.gui.managers.swing.panels;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeProjectControlPanel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.io.File;

public final class DoomMakeProjectControlPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		DoomMakeProjectControlPanel panel = new DoomMakeProjectControlPanel(new File(args[0]));
		ObjectUtils.apply(frame("Test", containerOf(panel)), 
		(frame) -> {
			frame.setBounds(0, 0, 200, 100);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
