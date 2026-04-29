/*******************************************************************************
 * Copyright (c) 2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.Component;
import java.awt.Container;
import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexTextureEditorSettingsManager;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;

public class WadTexTextureEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(WadTexTextureEditorApp.class); 

	private final DoomToolsIconManager icons;
	private final WadTexTextureEditorSettingsManager settings;
	

	public WadTexTextureEditorApp(File projectDirectory, String iwadBasePath, String paletteWadPath)
	{
		this();
		
		// TODO: Finish this.
	}

	public WadTexTextureEditorApp()
	{
		this.icons = DoomToolsIconManager.get();
		this.settings = WadTexTextureEditorSettingsManager.get();
		
		// TODO: Finish this.
	}

	public static WadTexTextureEditorApp openAndCreate(Component parent)
	{
		// TODO: Finish this.
		return null;
	}

	@Override
	public String getTitle()
	{
		return language.getText("wadtex.texture.title");
	}

	@Override
	public Container createContentPane()
	{
		// TODO: Finish this.
		return containerOf();
	}

}
