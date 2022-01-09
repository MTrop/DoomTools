package net.mtrop.doom.tools.doommake.gui.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.mtrop.doom.tools.gui.swing.panels.TextOutputPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

public final class TextOutputPanelTest 
{
	public static void main(String[] args) 
	{
		SwingUtils.setSystemLAF();
		TextOutputPanel panel = new TextOutputPanel();
		PrintStream out = panel.getPrintStream();
		
		SwingUtils.apply(frame("Test", containerOf(
			scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, panel)
		)), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
		
		for (int i = 0; i < 40; i++)
			out.println("Hello, world!");
	}
}
