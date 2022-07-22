/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorThemeType;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A panel that manages the default editor theme settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultThemeSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = -2628032968866995717L;
	
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private Map<String, EditorThemeType> friendlyThemeNameMap;
	private Map<String, Font> friendlyFontNameMap;
	
	private JFormField<String> themeField;
	private JFormField<String> fontField;
	private JFormField<Integer> fontSizeField;
	
	private String themeName;
	private Font fontType;
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultThemeSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.friendlyThemeNameMap = EnumUtils.createMap(EditorThemeType.class, (i, e) -> e.getFriendlyName());
		this.friendlyFontNameMap = ObjectUtils.apply(new TreeMap<>(), (map) -> {
			Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
				.forEach((font) -> {map.put(font.getName(), font);});
		});
		
		this.themeName = settings.getEditorThemeName();
		this.fontType = settings.getEditorFont();

		List<String> themes = new ArrayList<>(friendlyThemeNameMap.keySet());
		List<String> fonts = new ArrayList<>(friendlyFontNameMap.keySet());
		
		themeField = comboField(comboBox(comboBoxModel(themes), (i) -> {
			themeName = friendlyThemeNameMap.get(i).name();
		}));
		themeField.setValue(EditorThemeType.THEME_MAP.getOrDefault(settings.getEditorThemeName(), EditorThemeType.DEFAULT).getFriendlyName());
		
		fontField = comboField(comboBox(comboBoxModel(fonts), (i) -> updateFont()));
		fontSizeField = spinnerField(spinner(spinnerModel(fontType.getSize(), 1, 72 * 3, 1), (c) -> updateFont()));
		fontField.setValue(fontType.getName());
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(language.getInteger("texteditor.settings.label.width", 180))
				.addField(language.getText("texteditor.settings.theme"), themeField)
				.addField(language.getText("texteditor.settings.font"), fontField)
				.addField(language.getText("texteditor.settings.font.size"), fontSizeField)
				.addField(buttonField(button(language.getText("texteditor.settings.reset"), (b) -> resetSettings())))
			)
		);
	}

	private void updateFont()
	{
		float size = (float)(int)fontSizeField.getValue();
		fontType = friendlyFontNameMap.get(fontField.getValue()).deriveFont(size);
	}
	
	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		themeField.setValue(EditorThemeType.DEFAULT.getFriendlyName());
		fontField.setValue("Monospaced.plain");
		fontSizeField.setValue(12);
	}
	
	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setEditorThemeName(themeName);
		settings.setEditorFont(fontType);
	}
	
}
