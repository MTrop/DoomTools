package net.mtrop.doom.tools.gui;

import java.awt.Container;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JMenuBar;

/**
 * Interface for assembling the GUI part of a DoomTools application instance and 
 * listening to environment events.
 * Can be created as a JFrame or a JInternalFrame.
 * <p> Because of how workspace saving works, all applications must have a default constructor.
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
	 * This is only called once per application creation, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	public JMenuBar createDesktopMenuBar()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as an internal frame.
	 * This is only called once per application creation, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	public JMenuBar createInternalMenuBar()
	{
		return null;
	}

	/**
	 * Sets the current state of this application into a state object for persisting workspaces. 
	 * It is up to the application to figure out what to store.
	 * Applications that override this method should call this via <code>super</code> first!
	 * <p> All values should be JSON serializable!
	 * <p> This should NEVER be called by the application itself.
	 * @return this application's state properties (if any - can be null).
	 */
	public Map<String, String> getState()
	{
		return null;
	}

	/**
	 * Sets this applcation's state. 
	 * Called before it is started, if restoring from a workspace.
	 * Applications that override this method should call this via <code>super</code> first!
	 * <p> All values should be JSON serializable!
	 * <p> This should NEVER be called by the application itself.
	 * @param state the state object.
	 */
	public void setState(Map<String, String> state)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is about to close. 
	 * By default, this returns true. You may ask the user if they wish to close it, here.
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
	 * Called when the application is closed (after {@link #shouldClose()} is called and returns true).
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onClose()
	{
		// Do nothing.
	}
	
}
