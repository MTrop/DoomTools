package net.mtrop.doom.tools.gui;

import java.awt.Container;

import javax.swing.Icon;
import javax.swing.JMenuBar;

/**
 * Interface for assembling the GUI part of a DoomTools application instance and 
 * listening to environment events.
 * Can be created as a JFrame or a JInternalFrame.
 * @author Matthew Tropiano
 */
public abstract class DoomToolsApplicationInstance
{
	/** The control receiver set on all apps. */
	protected DoomToolsApplicationControlReceiver receiver;
	
	protected DoomToolsApplicationInstance()
	{
		this.receiver = null; // set later
	}
	
	/**
	 * @return this application's name.
	 */
	public abstract String getName();
	
	/**
	 * Fetches this application instance's content pane.
	 * This is only called once, and is intended to be the function that constructs
	 * the main component for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's content pane.
	 */
	public abstract Container createContentPane();

	/**
	 * Sets the application control receiver.
	 * Called when the application gets attached to a window, but before it is shown.
	 * <p> This should NEVER be called by the application itself.
	 * @param receiver the receiver instance.
	 */
	public final void setApplicationControlReceiver(DoomToolsApplicationControlReceiver receiver)
	{
		this.receiver = receiver;
	}
	
	/**
	 * Fetches this application instance's specific icon.
	 * This is only called once, and is intended to be the function that gets
	 * the application's icon (by default, it's DoomTools's icon).
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's icon, or <code>null</code> for default.
	 */
	public Icon getIcon()
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
	public JMenuBar createMenuBar()
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
	public JMenuBar createInternalMenuBar()
	{
		return null;
	}

	/**
	 * Creates a new settings object for saving into later, with NO defaults set.
	 * @return a new settings object.
	 */
	public DoomToolsApplicationSettings createSettings()
	{
		return new DoomToolsApplicationSettings();
	}

	/**
	 * Fetches a map of settings for this application instance
	 * so that it may be restored later in a workspace. 
	 * Applications that override this method should call this via <code>super</code> first!
	 * <p> All values should be JSON serializable!
	 * <p> This should NEVER be called by the application itself.
	 * @param settings the settings object.
	 */
	public void saveSettingsTo(DoomToolsApplicationSettings settings)
	{
		// Do nothing.
	}

	/**
	 * Applies a set of settings to this instance.
	 * Presumably, these are the same settings as set from {{@link #saveSettingsTo(DoomToolsApplicationSettings)}.
	 * Applications that override this method should call this via <code>super</code> first!
	 * <p> This should NEVER be called by the application itself.
	 * @param settings the settings object.
	 */
	public void applySettingsFrom(DoomToolsApplicationSettings settings)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is about to close.
	 * <p> This should NEVER be called by the application itself.
	 * @return true if the application should close, false if not.
	 */
	public boolean shouldClose()
	{
		return true;
	}
	
	/**
	 * Called when the application is opened.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onOpen()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is minimized.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onMinimize()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is restored from iconification.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onRestore()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is focused.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onFocus()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is unfocused.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onBlur()
	{
		// Do nothing.
	}

	/**
	 * Called when the application is closing.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onClose()
	{
		// Do nothing.
	}
	
}
