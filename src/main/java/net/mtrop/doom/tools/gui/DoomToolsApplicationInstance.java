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
	 * Fetches this application instance's content pane.
	 * @return the application's content pane.
	 */
	Container getContentPane();

	/**
	 * Fetches this application instance's menu bar, if used as a desktop frame.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	default JMenuBar getMenuBar()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as an internal frame.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	default JMenuBar getInternalMenuBar()
	{
		return null;
	}

	/**
	 * Fetches a map of settings for this application instance
	 * so that it may be restored later in a workspace.
	 * @param settingsMap the target settings map.
	 */
	default void saveSettings(Map<String, String> settingsMap)
	{
		// Do nothing.
	}

	/**
	 * Applies a set of settings to this instance.
	 * Presumably, these are the same settings as fetched from {{@link #saveSettings(Map)}.
	 * @param settingsMap the settings map.
	 */
	default void applySettings(Map<String, String> settingsMap)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is about to close.
	 * @return true if the application should close, false if not.
	 */
	default boolean shouldClose()
	{
		return true;
	}
	
	/**
	 * Called when the application is opened.
	 */
	default void onOpen()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is minimized.
	 */
	default void onMinimize()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is restored from iconification.
	 */
	default void onRestore()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is focused.
	 */
	default void onFocus()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is unfocused.
	 */
	default void onBlur()
	{
		// Do nothing.
	}

	/**
	 * Called when the application is closing.
	 */
	default void onClose()
	{
		// Do nothing.
	}
	
}
