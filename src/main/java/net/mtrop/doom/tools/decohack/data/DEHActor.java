/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;

/**
 * Describes all DeHackEd objects that are actors.
 * @author Matthew Tropiano
 */
public interface DEHActor
{
	/**
	 * @return the state label map.
	 */
	Map<String, Integer> getStateIndexMap();
		
}
