/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

public final class ProgressPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		DoomToolsProgressPanel panel = new DoomToolsProgressPanel(48);
		panel.setBorder(createEmptyBorder(8,8,8,8));

		ObjectUtils.apply(frame("Test", panel), 
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
