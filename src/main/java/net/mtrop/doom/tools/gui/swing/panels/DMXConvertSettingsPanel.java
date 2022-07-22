/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DMXConvertSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

/**
 * A DMX Convert panel for common settings.
 * @author Matthew Tropiano
 */
public class DMXConvertSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 4112598862871731156L;
	
	/** Language singleton. */
	private final DoomToolsGUIUtils utils;
	/** Language singleton. */
	private final DoomToolsLanguageManager language;
	/** Settings singleton. */
	private final DMXConvertSettingsManager settings;
	
	/**
	 * Creates the settings panel.
	 */
	public DMXConvertSettingsPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DMXConvertSettingsManager.get();
		
		containerOf(this, dimension(450, 200), borderLayout(0, 4),
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("dmxconv.settings.label.width")),
				utils.formField("dmxconv.settings.ffmpeg.label", fileField(
					settings.getFFmpegPath(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("dmxconv.settings.ffmpeg.title"), 
						currentFile, 
						language.getText("dmxconv.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					settings::setFFmpegPath
				))
			)),
			node(BorderLayout.CENTER, containerOf())
		);
	}
	
}
