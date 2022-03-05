package net.mtrop.doom.tools.gui;

/**
 * An interface for providing an application to start.
 * @author Matthew Tropiano
 */
public interface DoomToolsApplicationStarter
{
	/**
	 * Starts a new application instance by class.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 */
	<A extends DoomToolsApplicationInstance> void startApplication(Class<A> applicationClass);
    
	/**
	 * Starts a new application instance.
	 * @param <A> the instance type.
	 * @param applicationInstance the application instance.
	 */
	<A extends DoomToolsApplicationInstance> void startApplication(A applicationInstance);

}
