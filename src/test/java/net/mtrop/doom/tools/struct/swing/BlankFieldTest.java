package net.mtrop.doom.tools.struct.swing;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

public final class BlankFieldTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		SwingUtils.apply(frame("Test", containerOf(
			node(form(96)
				.addField("Stuff", integerField(123))
				.addField("", separatorField())
				.addField("asdfasdf", stringField("asdfasdf"))
				.addField("", buttonField(
					button("Click Me", (c, e) -> System.out.println("asdfasdf")),
					button("Click Me Too", (c, e) -> System.out.println("asdfasdf2"))
				))
			)
		)), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
		});
	}
}
