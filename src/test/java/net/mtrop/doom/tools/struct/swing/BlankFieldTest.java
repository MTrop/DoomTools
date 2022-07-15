package net.mtrop.doom.tools.struct.swing;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

public final class BlankFieldTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		ObjectUtils.apply(frame("Test", containerOf(
			node(form(96)
				.addField("Stuff", integerField(123))
				.addField("", separatorField())
				.addField("asdfasdf", stringField("asdfasdf"))
				.addField("", buttonField(
					button("Click Me", (b) -> System.out.println("asdfasdf")),
					button("Click Me Too", (b) -> System.out.println("asdfasdf2"))
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
