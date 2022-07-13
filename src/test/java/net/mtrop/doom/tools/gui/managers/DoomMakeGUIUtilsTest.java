package net.mtrop.doom.tools.gui.managers;

import java.awt.Dialog.ModalityType;

import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;

public final class DoomMakeGUIUtilsTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		utils.createHelpModal(ModalityType.APPLICATION_MODAL,
			utils.helpText("Hello", "Hello, help!"),
			utils.helpResource("src/main/java/net/mtrop/doom/tools/DecoHackMain.java", false),
			utils.helpProcess(DecoHackMain.class, "--help-full")
		).open();
	}
}
