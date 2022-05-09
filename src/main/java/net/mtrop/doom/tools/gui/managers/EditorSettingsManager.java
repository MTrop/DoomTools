package net.mtrop.doom.tools.gui.managers;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorViewSettings;
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
	}
	
	
}
