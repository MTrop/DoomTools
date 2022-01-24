package net.mtrop.doom.tools.gui;

import java.awt.Container;
import java.util.Map;

import javax.swing.JMenuBar;

/**
 * Interface for assembling the GUI part of a DoomTools application instance.
 * Can be created as a JFrame or a JInternalFrame.
 * @author Matthew Tropiano
 */
public interface DoomToolsApplicationInstance
{
	/**
	 * @return this application's name.
	 */
	String getName();
	
	/**
	 * Fetches this application instance's menu bar, if used as a desktop frame.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	JMenuBar getMenuBar();

	/**
	 * Fetches this application instance's menu bar, if used as an internal frame.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	JMenuBar getInternalMenuBar();

	/**
	 * Fetches this application instance's content pane.
	 * @return the application's content pane.
	 */
	Container getContentPane();
	
	/**
	 * Fetches a map of settings for this application instance
	 * so that it may be restored later in a workspace.
	 * @return a map of setting name to setting value.
	 */
	Map<String, Object> fetchSettings();

	/**
	 * Applies a set of settings to this instance.
	 * Presumably, these are the same settings as fetched from {{@link #fetchSettings()}.
	 * @param settingsMap the settings map.
	 */
	void applySettings(Map<String, Object> settingsMap);

}
