/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.util.Map;

import net.mtrop.doom.tools.common.Common;

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
	 * @return a new application instance.
	 */
	static <A extends DoomToolsApplicationInstance> A createApplication(Class<A> applicationClass)
	{
		return createApplication(applicationClass, null);
	}
    
	/**
	 * Starts a new application instance by class.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 * @param state the state to use for state restoration. Can be null.
	 * @return a new application instance.
	 */
	static <A extends DoomToolsApplicationInstance> A createApplication(Class<A> applicationClass, Map<String, String> state) 
	{
		A instance = Common.create(applicationClass);
		if (state != null)
			instance.setApplicationState(state);
		return instance;
	}
    
	/**
	 * Starts a new application instance by class.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 */
	default <A extends DoomToolsApplicationInstance> void startApplication(Class<A> applicationClass)
	{
		startApplication(createApplication(applicationClass));
	}
    
	/**
	 * Starts a new application instance by class.
	 * @param <A> the instance type.
	 * @param applicationClass the application class.
	 * @param state the state to use for state restoration. Can be null.
	 */
	default <A extends DoomToolsApplicationInstance> void startApplication(Class<A> applicationClass, Map<String, String> state) 
	{
		startApplication(createApplication(applicationClass, null));
	}
    
	/**
	 * Starts a new application instance.
	 * @param instance the application instance.
	 */
	void startApplication(DoomToolsApplicationInstance instance);
    
}
