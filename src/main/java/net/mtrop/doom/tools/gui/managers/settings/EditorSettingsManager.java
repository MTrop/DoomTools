/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.settings;

import java.awt.Font;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorAutoCompleteSettings;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorCodeSettings;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorViewSettings;
import net.mtrop.doom.tools.struct.SingletonProvider;


/**
 * Common editor settings singleton.
 * @author Matthew Tropiano
 */
public final class EditorSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
	private static final String SETTINGS_FILENAME = "editor.properties";

	/** The instance encapsulator. */
	private static final SingletonProvider<EditorSettingsManager> INSTANCE = new SingletonProvider<>(() -> new EditorSettingsManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static EditorSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */

	private static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);
	
	private EditorSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(EditorSettingsManager.class));
	}
	
	/**
	 * Gets the default editor view settings.
	 * @return the default settings.
	 */
	public EditorViewSettings getDefaultEditorViewSettings()
	{
		return getEditorViewSettings("default");
	}
	
	/**
	 * Sets the default editor view settings.
	 * @param settings the new default settings.
	 */
	public void setDefaultEditorViewSettings(EditorViewSettings settings)
	{
		setEditorViewSettings("default", settings);
		commit();
	}
	
	/**
	 * Gets the default editor code settings.
	 * @return the default settings.
	 */
	public EditorCodeSettings getDefaultEditorCodeSettings()
	{
		return getEditorCodeSettings("default");
	}
	
	/**
	 * Sets the default editor code settings.
	 * @param settings the new default settings.
	 */
	public void setDefaultEditorCodeSettings(EditorCodeSettings settings)
	{
		setEditorCodeSettings("default", settings);
		commit();
	}
	
	/**
	 * Gets the default editor auto-complete settings.
	 * @return the default settings.
	 */
	public EditorAutoCompleteSettings getDefaultEditorAutoCompleteSettings()
	{
		return getEditorAutoCompleteSettings("default");
	}
	
	/**
	 * Sets the default editor auto-complete settings.
	 * @param settings the new default settings.
	 */
	public void setDefaultEditorAutoCompleteSettings(EditorAutoCompleteSettings settings)
	{
		setEditorAutoCompleteSettings("default", settings);
		commit();
	}
	
	/**
	 * Gets the default editor theme.
	 * @return the theme name.
	 */
	public String getEditorThemeName()
	{
		return getString("theme.name", "default");
	}
	
	/**
	 * Sets the default editor theme.
	 * @param name the theme name.
	 */
	public void setEditorThemeName(String name)
	{
		setString("theme.name", name);
		commit();
	}
	
	/**
	 * Gets the default editor font.
	 * @return the font.
	 */
	public Font getEditorFont()
	{
		return getFont("theme.font", DEFAULT_FONT);
	}
	
	/**
	 * Sets the default editor font.
	 * @param font the font.
	 */
	public void setEditorFont(Font font)
	{
		setFont("theme.font", font);
		commit();
	}
	
}
