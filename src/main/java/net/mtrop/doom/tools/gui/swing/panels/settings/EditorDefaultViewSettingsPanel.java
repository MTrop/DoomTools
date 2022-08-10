/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels.settings;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorViewSettings;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * A panel that manages the default editor view settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultViewSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = 6434971725684191370L;
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorViewSettings viewSettings;
	private EditorViewSettings defaultSettings;
	
	private JFormField<Charset> defaultEncodingField;
	private JFormField<Integer> tabSizeField;
	private JFormField<Boolean> tabEmulatedField;
	private JFormField<Boolean> lineWrapField;
	private JFormField<Boolean> wrapStyleField;
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultViewSettingsPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.viewSettings = settings.getDefaultEditorViewSettings();
		this.defaultSettings = new EditorViewSettings();

		Set<Charset> charsets = DoomToolsEditorProvider.get().getAvailableCommonCharsets();
		
		this.defaultEncodingField = comboField(comboBox(comboBoxModel(charsets), 
			(i) -> viewSettings.setDefaultEncoding(i)
		));
		this.defaultEncodingField.setValue(viewSettings.getDefaultEncoding());
		
		this.tabSizeField = spinnerField(spinner(spinnerModel(viewSettings.getTabSize(), 2, 8, 1), 
			(s) -> viewSettings.setTabSize((Integer)s.getValue())
		));
		this.tabEmulatedField = checkBoxField(checkBox(viewSettings.isTabsEmulated(),
			(v) -> viewSettings.setTabsEmulated(v)
		));
		this.lineWrapField = checkBoxField(checkBox(viewSettings.isLineWrap(),
			(v) -> viewSettings.setLineWrap(v)
		));
		this.wrapStyleField = checkBoxField(checkBox(viewSettings.isWrapStyleWord(),
			(v) -> viewSettings.setWrapStyleWord(v)
		));
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, utils.createForm(form(language.getInteger("texteditor.settings.label.width", 180)), 
				utils.formField("texteditor.settings.view.encoding", defaultEncodingField),
				utils.formField("texteditor.settings.view.tabsize", tabSizeField),
				utils.formField("texteditor.settings.view.spacetabs", tabEmulatedField),
				utils.formField("texteditor.settings.view.linewrap", lineWrapField),
				utils.formField("texteditor.settings.view.wordwrap", wrapStyleField)
				).addField(buttonField(button(language.getText("texteditor.settings.reset"), (b) -> resetSettings()))
			))
		);
	}
	
	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		defaultEncodingField.setValue(defaultSettings.getDefaultEncoding());
		tabSizeField.setValue(defaultSettings.getTabSize());
		tabEmulatedField.setValue(defaultSettings.isTabsEmulated());
		lineWrapField.setValue(defaultSettings.isLineWrap());
		wrapStyleField.setValue(defaultSettings.isWrapStyleWord());
	}

	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorViewSettings(viewSettings);
	}
	
}
