/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels.settings;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain.GUIThemeType;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * The settings panel for all of DoomTools (and individual apps).
 * @author Matthew Tropiano
 */
public class DoomToolsSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 678866619269686755L;

	/** Language singleton. */
	private final DoomToolsLanguageManager language;
	/** Settings singleton. */
	private final DoomToolsSettingsManager settings;
	
	/** DoomMake setting panel. */
	private DoomMakeSettingsPanel doomMakeSettingsPanel;
	/** DMXConv setting panel. */
	private DMXConvertSettingsPanel dmxConvertSettingsPanel;
	
	
	/**
	 * Creates the settings panel.
	 */
	public DoomToolsSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomToolsSettingsManager.get();
		
		this.doomMakeSettingsPanel = new DoomMakeSettingsPanel();
		this.dmxConvertSettingsPanel = new DMXConvertSettingsPanel();
		
		containerOf(this,
			node(BorderLayout.CENTER, tabs(TabPlacement.LEFT,
				tab("DoomTools", containerOf(createEmptyBorder(8, 8, 8, 8),
					node(createMainPanel())
				)),
				tab("DMXConvert", containerOf(createEmptyBorder(8, 8, 8, 8),
					node(dmxConvertSettingsPanel)
				)),
				tab("DoomMake", containerOf(createEmptyBorder(8, 8, 8, 8),
					node(doomMakeSettingsPanel)
				))
			))
		);
	}
	
	// Create main panel.
	private Container createMainPanel()
	{
		List<String> themes = Arrays.asList(
			GUIThemeType.LIGHT.name(),
			GUIThemeType.DARK.name(),
			GUIThemeType.INTELLIJ.name(),
			GUIThemeType.DARCULA.name()
		);
		
		JFormField<String> themeField = comboField(comboBox(comboBoxModel(themes), (i) -> settings.setThemeName((String)i)));
		themeField.setValue(settings.getThemeName());
		
		final DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
		JFormField<File> chooserDirectoryField = fileField(
			settings.getFileChooserDefault(), 
			(current) -> {
				return utils.chooseDirectory(
					this,
					language.getText("wadtex.export.source.browse.title"), 
					language.getText("wadtex.export.source.browse.accept"),
					() -> current != null ? current : settings.getLastFileSave(),
					settings::setLastFileSave
				);
			},
			settings::setFileChooserDefault
		);
		
		return containerOf(
			node(BorderLayout.NORTH, form(language.getInteger("doomtools.settings.label.width"))
				.addField(language.getText("doomtools.settings.theme"), themeField)
				.addField(language.getText("doomtools.settings.chooser.default"), chooserDirectoryField)
			),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, wrappedLabel(language.getText("doomtools.settings.theme.notice")))
			))
		);
	}
	
	/**
	 * Commits unsaved settings.
	 */
	public void commitSettings()
	{
		// Do nothing. Fill this with stuff later, maybe.
	}
	
}
