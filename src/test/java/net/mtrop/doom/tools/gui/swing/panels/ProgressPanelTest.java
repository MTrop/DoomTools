package net.mtrop.doom.tools.gui.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import net.mtrop.doom.tools.struct.swing.SwingUtils;

public final class ProgressPanelTest 
{
	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		ProgressPanel panel = new ProgressPanel(48);
		panel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

		SwingUtils.apply(frame("Test", panel), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
		});

		panel.setActivityMessage("asdfasdfasdf");
		panel.setProgress(0, 25, 100);
		panel.setProgressLabel("25%");
		panel.setIndeterminate();
	}
}
