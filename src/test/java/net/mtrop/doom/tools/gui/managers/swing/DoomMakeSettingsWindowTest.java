package net.mtrop.doom.tools.gui.managers.swing;

import static net.mtrop.doom.tools.struct.util.ObjectUtils.*;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.swing.DoomMakeSettingsWindow;

public final class DoomMakeSettingsWindowTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		apply(new DoomMakeSettingsWindow(), (window) -> {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setVisible(true);
		});
	}
}
