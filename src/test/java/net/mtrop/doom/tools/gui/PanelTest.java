/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.frame;

import java.awt.Container;

import javax.swing.JFrame;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

public final class PanelTest 
{
	public static void main(String[] args) 
	{
		if (args.length < 1 || ObjectUtils.isEmpty(args[0]))
		{
			System.err.println("ERROR: Need panel.");
			return;
		}

		DoomToolsGUIMain.setLAF();

		Object obj = null;
		try {
			obj = Common.create(Class.forName(args[0]));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}

		if (!(obj instanceof Container))
		{
			System.err.println("Not a Container: " + obj.getClass().getName());
			return;
		}
		
		Container container = (Container)obj;
		
		ObjectUtils.apply(frame("Test", container), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
		
	}

}
