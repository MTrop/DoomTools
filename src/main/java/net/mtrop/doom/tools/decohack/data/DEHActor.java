/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * Describes all DeHackEd objects that are actors.
 * @author Matthew Tropiano
 * @param <SELF> this class type.
 */
public interface DEHActor<SELF>
{
	/**
	 * Gets all state indices for state labels defined on this actor.
	 * @return all of the defined labels for this actor.
	 */
	String[] getLabels();

	/**
	 * Checks if the actor has a state label by name.
	 * @param label the label.
	 * @return true if so, false if not.
	 */
	boolean hasLabel(String label);

	/**
	 * Gets a state index for a state label defined on this actor.
	 * @param label the label.
	 * @return the corresponding index, or 0 if it doesn't exist.
	 */
	int getLabel(String label);

	/**
	 * Sets a state index for a state label defined on this actor.
	 * @param label the label.
	 * @param index the new index.
	 * @return this object.
	 */
	SELF setLabel(String label, int index);

	/**
	 * Clears/resets state labels defined on this actor.
	 * @return this object.
	 */
	SELF clearLabels();

}
