/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.swing.panels;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeProjectTargetListPanel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.io.File;
import java.io.FileNotFoundException;

public final class DoomMakeProjectTargetListPanelTest 
{
	public static void main(String[] args) throws FileNotFoundException, ProcessCallException 
	{
		DoomToolsGUIMain.setLAF();
		ObjectUtils.apply(frame("Test", new DoomMakeProjectTargetListPanel(
			DoomMakeProjectHelper.get().getProjectTargets(new File(args[0])),
			(target) -> { System.out.println("SELECT: " + target); }, 
			(target) -> { System.out.println("DCLICK: " + target); }
		)), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
