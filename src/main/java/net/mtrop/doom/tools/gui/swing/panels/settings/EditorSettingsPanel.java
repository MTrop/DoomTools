/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.swing.panels.EditorDefaultAutoCompleteSettingsPanel;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.util.ObjectUtils.apply;


/**
 * A panel that manages all of the editor settings. 
 * @author Matthew Tropiano
 */
public class EditorSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = 4672195985365552035L;

	private DoomToolsLanguageManager language;
	
	private EditorDefaultThemeSettingsPanel themeSettingsPanel;
	private EditorDefaultViewSettingsPanel viewSettingsPanel;
	private EditorDefaultCodeSettingsPanel codeSettingsPanel;
	private EditorDefaultAutoCompleteSettingsPanel autoCompleteSettingsPanel;
	
	
	/**
	 * Creates a new panel.
	 */
	public EditorSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		
		this.themeSettingsPanel = new EditorDefaultThemeSettingsPanel();
		this.viewSettingsPanel = new EditorDefaultViewSettingsPanel();
		this.codeSettingsPanel = new EditorDefaultCodeSettingsPanel();
		this.autoCompleteSettingsPanel = new EditorDefaultAutoCompleteSettingsPanel();
		
		JPanel scrolledPanel = apply(new JPanel(), (panel) -> {
			containerOf(panel, boxLayout(panel, BoxAxis.Y_AXIS),
				node(titlePanel(language.getText("texteditor.settings.view"), viewSettingsPanel)),
				node(titlePanel(language.getText("texteditor.settings.code"), codeSettingsPanel)),
				node(titlePanel(language.getText("texteditor.settings.autocomp"), autoCompleteSettingsPanel))
			);
		});
		
		containerOf(this, borderLayout(0, 4),
			node(BorderLayout.NORTH, titlePanel(language.getText("texteditor.settings.theme"), themeSettingsPanel)),
			node(BorderLayout.CENTER, scroll(scrolledPanel)),
			node(BorderLayout.SOUTH, containerOf(createEmptyBorder(4, 4, 4, 4), flowLayout(Flow.TRAILING, 4, 0),
				node(button(language.getText("texteditor.settings.reset"), (b) -> resetSettings())),
				node(button(language.getText("texteditor.settings.apply"), (b) -> commitSettings()))
			))
		);
	}
	
	// The title panel.
	private static Container titlePanel(String title, Container container)
	{
		Border border = createTitledBorder(
			createLineBorder(Color.GRAY, 1), title, TitledBorder.LEADING, TitledBorder.TOP
		);
		return containerOf(border, 
			node(containerOf(createEmptyBorder(0, 4, 0, 4),
				node(BorderLayout.CENTER, container)
			))
		);
	}

	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		themeSettingsPanel.resetSettings();
		viewSettingsPanel.resetSettings();
		codeSettingsPanel.resetSettings();
		autoCompleteSettingsPanel.resetSettings();
	}

	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		themeSettingsPanel.commitSettings();
		viewSettingsPanel.commitSettings();
		codeSettingsPanel.commitSettings();
		autoCompleteSettingsPanel.commitSettings();
	}
	
}
