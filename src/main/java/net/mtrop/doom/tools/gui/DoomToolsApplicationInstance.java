package net.mtrop.doom.tools.gui;

import java.awt.Container;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JMenuBar;

/**
 * Interface for assembling the GUI part of a DoomTools application instance and 
 * listening to environment events.
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
	 * This is only called once, and is intended to be the function that constructs
	 * the main component for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's content pane.
	 */
	Container createContentPane();

	/**
	 * Sets the application control receiver.
	 * Called when the application gets attached to a window, but before it is shown.
	 * <p> This should NEVER be called by the application itself.
	 * @param receiver the receiver instance.
	 */
	default void setApplicationControlReceiver(DoomToolsApplicationControlReceiver receiver)
	{
		// Do nothing.
	}
	
	/**
	 * Fetches this application instance's specific icon.
	 * This is only called once, and is intended to be the function that gets
	 * the application's icon (by default, it's DoomTools's icon).
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's icon, or <code>null</code> for default.
	 */
	default Icon getIcon()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as a desktop frame.
	 * This is only called once, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	default JMenuBar createMenuBar()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as an internal frame.
	 * This is only called once, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	default JMenuBar createInternalMenuBar()
	{
		return null;
	}

	/**
	 * Fetches a map of settings for this application instance
	 * so that it may be restored later in a workspace.
	 * <p> This should NEVER be called by the application itself.
	 * @param settingsMap the target settings map.
	 */
	default void saveSettings(Map<String, String> settingsMap)
	{
		// Do nothing.
	}

	/**
	 * Applies a set of settings to this instance.
	 * Presumably, these are the same settings as fetched from {{@link #saveSettings(Map)}.
	 * <p> This should NEVER be called by the application itself.
	 * @param settingsMap the settings map.
	 */
	default void applySettings(Map<String, String> settingsMap)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is about to close.
	 * <p> This should NEVER be called by the application itself.
	 * @return true if the application should close, false if not.
	 */
	default boolean shouldClose()
	{
		return true;
	}
	
	/**
	 * Called when the application is opened.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onOpen()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is minimized.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onMinimize()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is restored from iconification.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onRestore()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is focused.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onFocus()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is unfocused.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onBlur()
	{
		// Do nothing.
	}

	/**
	 * Called when the application is closing.
	 * <p> This should NEVER be called by the application itself.
	 */
	default void onClose()
	{
		// Do nothing.
	}
	
}
