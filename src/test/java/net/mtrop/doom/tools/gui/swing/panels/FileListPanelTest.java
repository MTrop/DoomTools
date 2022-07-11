package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;


public final class FileListPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		final FileListPanel panel = new FileListPanel("Files and Such", ListSelectionMode.MULTIPLE_INTERVAL, true, true, null, null);
		panel.setBorder(createEmptyBorder(8,8,8,8));

		ObjectUtils.apply(frame("Test",
			containerOf(
				node(BorderLayout.CENTER, panel)
			)
		), 
		(frame) -> {
			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
