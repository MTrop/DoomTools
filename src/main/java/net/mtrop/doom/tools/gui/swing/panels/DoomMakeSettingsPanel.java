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
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

/**
 * A DoomMake panel for common settings.
 * @author Matthew Tropiano
 */
public class DoomMakeSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 3657842361863713721L;
	
	/** Language singleton. */
	private final DoomToolsGUIUtils utils;
	/** Language singleton. */
	private final DoomToolsLanguageManager language;
	/** Settings singleton. */
	private final DoomMakeSettingsManager settings;
	
	/**
	 * Creates the settings panel.
	 */
	public DoomMakeSettingsPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomMakeSettingsManager.get();
		
		containerOf(this, dimension(450, 200), borderLayout(0, 4),
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("doommake.settings.label.width")),
				utils.formField("doommake.settings.vscode.label", fileField(
					settings.getPathToIDE(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("doommake.settings.vscode.title"), 
						currentFile, 
						language.getText("doommake.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					(value) -> settings.setPathToIDE(value)
				)),
				utils.formField("doommake.settings.slade.label", fileField(
					settings.getPathToSlade(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("doommake.settings.slade.title"), 
						currentFile, 
						language.getText("doommake.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					(value) -> settings.setPathToSlade(value)
				)),
				utils.formField("doommake.settings.maps.label", fileField(
					settings.getPathToMapEditor(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("doommake.settings.maps.title"),
						currentFile, 
						language.getText("doommake.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					(value) -> settings.setPathToMapEditor(value)
				))
			)),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, wrappedLabel(language.getText("doommake.settings.notice")))
		);
	}
	
}
