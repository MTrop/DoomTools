/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.Color;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

public final class ColorFieldTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		ObjectUtils.apply(frame("Test", containerOf(
			node(form(48).addField("Color", colorField(Color.WHITE, "...", "Pick a Color", System.out::println))
		))), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
		});
	}
}
