/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.frame;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.swing.panels.FindReplacePanel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

public final class FindReplacePanelTest
{
	public static void main(String[] args)
	{
		DoomToolsGUIMain.setLAF();
		ObjectUtils.apply(frame("Test", new FindReplacePanel()), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}
