package net.mtrop.doom.tools.struct.swing;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

public final class KeyStrokeFieldTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		SwingUtils.apply(frame("Test", containerOf(
			node(form(96).addField("Keys", keyStrokeField(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)))
		))), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
		});
	}
}
