package net.mtrop.doom.tools.gui.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.io.PrintStream;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

public final class TextOutputPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		DoomToolsTextOutputPanel panel = new DoomToolsTextOutputPanel();
		PrintStream out = panel.getPrintStream();
		
		ObjectUtils.apply(frame("Test",
			scroll(ScrollPolicy.AS_NEEDED, panel)
		), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
		
		for (int i = 0; i < 40; i++)
			out.println("Hello, world!");
	}
}
