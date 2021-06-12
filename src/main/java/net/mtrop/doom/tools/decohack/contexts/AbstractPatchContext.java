/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.patches.DEHPatch;

/**
 * Abstract patch context.
 * @author Matthew Tropiano
 * @param <P> DEH patch type.
 */
public abstract class AbstractPatchContext<P extends DEHPatch> implements DEHPatch
{
	private DEHAmmo[] ammo;
	private DEHSound[] sounds;
	private DEHWeapon[] weapons;
	private DEHThing[] things;
	private DEHState[] states;
	private DEHActionPointer[] pointers;
	private DEHMiscellany miscellany;

	private boolean[] freeStates;
	private int freeStateCount;
	private int freePointerStateCount;
	private boolean[] protectedStates;
	
	/**
	 * Creates a new patch context.
	 */
	public AbstractPatchContext()
	{
		DEHPatch source = getSourcePatch();
		
		this.ammo = new DEHAmmo[source.getAmmoCount()];
		for (int i = 0; i < this.ammo.length; i++)
			if (source.getAmmo(i) != null)
				this.ammo[i] = (new DEHAmmo()).copyFrom(source.getAmmo(i), getSupportedFeatureLevel());
		
		this.sounds = new DEHSound[source.getSoundCount()];
		for (int i = 0; i < this.sounds.length; i++)
			if (source.getSound(i) != null)
				this.sounds[i] = (new DEHSound()).copyFrom(source.getSound(i), getSupportedFeatureLevel());
		
		this.weapons = new DEHWeapon[source.getWeaponCount()];
		for (int i = 0; i < this.weapons.length; i++)
			if (source.getWeapon(i) != null)
				this.weapons[i] = (new DEHWeapon()).copyFrom(source.getWeapon(i), getSupportedFeatureLevel());
		
		this.things = new DEHThing[source.getThingCount()];
		for (int i = 1; i < this.things.length; i++)
			if (source.getThing(i) != null)
				this.things[i] = (new DEHThing()).copyFrom(source.getThing(i), getSupportedFeatureLevel());
		
		this.states = new DEHState[source.getStateCount()];
		for (int i = 0; i < this.states.length; i++)
			if (source.getState(i) != null)
				this.states[i] = (new DEHState()).copyFrom(source.getState(i), getSupportedFeatureLevel());
		
		this.pointers = new DEHActionPointer[source.getActionPointerCount()];
		for (int i = 0; i < this.pointers.length; i++)
			if (source.getActionPointer(i) != null)
				this.pointers[i] = source.getActionPointer(i);		
		
		this.miscellany = (new DEHMiscellany()).copyFrom(source.getMiscellany(), getSupportedFeatureLevel());
		
		this.freeStates = new boolean[states.length];
		this.freeStateCount = 0;
		this.protectedStates = new boolean[states.length];
		
		// Protect first two states from clear.
		setProtectedState(0, true); // NULL state. 
		setProtectedState(1, true); // Gunflash Light0.
	}
	
	/**
	 * Gets the original source patch state (for restoration or reference).
	 * <p><b>DO NOT ALTER THE CONTENTS OF THIS PATCH. THIS IS A REFERENCE STATE.</b> 
	 * @return the original source patch.
	 */
	public abstract P getSourcePatch();
	
	@Override
	public DEHMiscellany getMiscellany() 
	{
		return miscellany;
	}

	@Override
	public int getAmmoCount() 
	{
		return ammo.length;
	}

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		return Common.arrayElement(ammo, index);
	}

	@Override
	public int getSoundCount() 
	{
		return sounds.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		return Common.arrayElement(sounds, index);
	}

	@Override
	public int getThingCount() 
	{
		return things.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		return Common.arrayElement(things, index);
	}

	@Override
	public int getWeaponCount()
	{
		return weapons.length;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return Common.arrayElement(weapons, index);
	}

	@Override
	public int getStateCount()
	{
		return states.length;
	}

	@Override
	public DEHState getState(int index) 
	{
		return Common.arrayElement(states, index);
	}

	@Override
	public Integer getStateActionPointerIndex(int stateIndex) 
	{
		return getSourcePatch().getStateActionPointerIndex(stateIndex);
	}

	@Override
	public int getActionPointerCount() 
	{
		return pointers.length;
	}

	@Override
	public DEHActionPointer getActionPointer(int index)
	{
		return Common.arrayElement(pointers, index);
	}

	/**
	 * @return this patch context's supported action pointer type level.
	 */
	public abstract DEHActionPointerType getSupportedActionPointerType();

	/**
	 * @return this patch context's feature level.
	 */
	public abstract DEHFeatureLevel getSupportedFeatureLevel();

	/**
	 * Checks if the provided pointer type is supported by this one.
	 * @param type the provided type.
	 * @return true if so, false if not.
	 */
	public boolean supports(DEHActionPointerType type)
	{
		return getSupportedActionPointerType().supports(type);
	}

	/**
	 * Checks if the provided feature level is supported by this patch.
	 * @param level the provided level.
	 * @return true if so, false if not.
	 */
	public boolean supports(DEHFeatureLevel level)
	{
		return getSupportedFeatureLevel().supports(level);
	}

	/**
	 * Sets the pointer at an action pointer index.
	 * @param index the pointer index.
	 * @param pointer the pointer.
	 * @throws IndexOutOfBoundsException if index is out of bounds.
	 */
	public void setActionPointer(int index, DEHActionPointer pointer)
	{
		if (pointer == null)
			throw new IllegalArgumentException("Pointer cannot be null.");
		pointers[index] = pointer;
	}

	/**
	 * Gets how many free states there are.
	 * @return the amount of states flagged as "free."
	 */
	public int getFreeStateCount() 
	{
		return freeStateCount;
	}
	
	/**
	 * Gets how many free pointer-having states there are.
	 * @return the amount of pointer-attached states flagged as "free."
	 */
	public int getFreePointerStateCount() 
	{
		return freePointerStateCount;
	}
	
	/**
	 * Gets if a state is flagged as "free".
	 * @param index the index.
	 * @return true if so, false if not.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public boolean isFreeState(int index)
	{
		return freeStates[index];
	}
	
	/**
	 * Marks a state as "free" - painting thing/weapon states will be written to these.
	 * @param index the index to mark as free.
	 * @param state true to set as "free", false to unset.
	 * @throws IllegalStateException if the target state is protected.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #protectState(int)
	 */
	public void setFreeState(int index, boolean state)
	{
		if (isProtectedState(index))
			throw new IllegalStateException("State " + index + " is a protected state.");
		boolean prev = freeStates[index]; 
		freeStates[index] = state;
		if (prev && !state)
		{
			freeStateCount--;
			if (getStateActionPointerIndex(index) != null)
				freePointerStateCount--;
		}
		else if (!prev && state)
		{
			freeStateCount++;
			if (getStateActionPointerIndex(index) != null)
				freePointerStateCount++;
		}
	}
	
	/**
	 * Gets if a state is flagged as "protected".
	 * @param index the index.
	 * @return true if so, false if not.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public boolean isProtectedState(int index)
	{
		return protectedStates[index];
	}

	/**
	 * Marks a state as "protected" - attempting to free this state or alter it
	 * directly will throw an exception.
	 * @param index the index to mark as protected.
	 * @param state true to set as "protected", false to unset.
	 */
	public void setProtectedState(int index, boolean state)
	{
		protectedStates[index] = state;
	}

	/**
	 * Searches through the states and flags them as "free" until it hits a "protected" or "free" state.
	 * The state traversal is through the "next state" indices on each state.
	 * @param startingStateIndex the state index to start from.
	 * @return the amount of states freed.
	 */
	public int freeConnectedStates(int startingStateIndex)
	{
		int out = 0;
		int index = startingStateIndex;
		while (!isProtectedState(index) && !isFreeState(index))
		{
			setFreeState(index, true);
			index = getState(index).getNextStateIndex();
			out++;
		}
		return out;
	}
	
	/**
	 * Flags each associated state in a thing as "free".
	 * Each starting state index is taken from the state that the corresponding thing uses.
	 * Each connected state (connected via next state indices) is freed until an already free state
	 * is reached or a protected state is reached. 
	 * @param thingIndex the thing slot index.
	 * @return the amount of states freed.
	 * @see #getThing(int)
	 * @see #setFreeState(int, boolean)
	 * @see #freeConnectedStates(int)
	 */
	public int freeThingStates(int thingIndex)
	{
		int out = 0;
		DEHThing thing = getThing(thingIndex);
		for (String label : thing.getLabels())
			out += freeConnectedStates(thing.getLabel(label));
		return out;
	}
	
	/**
	 * Flags each associated state in a weapon as "free".
	 * Each starting state index is taken from the state that the corresponding weapon uses.
	 * Each connected state (connected via next state indices) is freed until an already free state
	 * is reached or a protected state is reached. 
	 * @param weaponIndex the thing slot index.
	 * @return the amount of states freed.
	 * @see #getWeapon(int)
	 * @see #setFreeState(int, boolean)
	 * @see #freeConnectedStates(int)
	 */
	public int freeWeaponStates(int weaponIndex)
	{
		int out = 0;
		DEHWeapon weapon = getWeapon(weaponIndex);
		for (String label : weapon.getLabels())
			out += freeConnectedStates(weapon.getLabel(label));
		return out;
	}
	
	/**
	 * Searches linearly for the next free state in this context from a starting index.
	 * If the start index is free, it is returned. If a full search completes without finding
	 * a free index, <code>null</code> is returned.
	 * @param startingIndex the starting index.
	 * @return the next free state, or <code>null</code> if none found.
	 */
	public Integer findNextFreeState(int startingIndex)
	{
		int i = startingIndex;
		while (!isFillableState(i))
		{
			i++;
			if (i == startingIndex)
				return null;
			if (i >= getStateCount())
				i = 0;
		}
		return i;
	}
	
	/**
	 * Searches linearly for the next free state WITH an action pointer in this context from a starting index.
	 * If the start index is free, it is returned. If a full search completes without finding
	 * a free index, <code>null</code> is returned.
	 * @param startingIndex the starting index.
	 * @return the next free state, or <code>null</code> if none found.
	 */
	public Integer findNextFreeActionPointerState(int startingIndex)
	{
		int i = startingIndex;
		while (!(isFillableState(i) && getStateActionPointerIndex(i) != null))
		{
			i++;
			if (i == startingIndex)
				return null;
			if (i >= getStateCount())
				i = 0;
		}
		return i;
	}
	
	/**
	 * Searches linearly for the next free state WITHOUT an action pointer in this context from a starting index.
	 * If the start index is free, it is returned. If a full search completes without finding
	 * a free index, <code>null</code> is returned.
	 * @param startingIndex the starting index.
	 * @return the next free state, or <code>null</code> if none found.
	 */
	public Integer findNextFreeNonActionPointerState(int startingIndex)
	{
		int i = startingIndex;
		while (!(isFillableState(i) && getStateActionPointerIndex(i) == null))
		{
			i++;
			if (i == startingIndex)
				return null;
			if (i >= getStateCount())
				i = 0;
		}
		return i;
	}

	/**
	 * Checks if a state is considered "fillable," which means it is both
	 * free and not protected.
	 * Equivalent to: <code>isFreeState(index) && !isProtectedState(index)</code>
	 * @param index the state index to check.
	 * @return true if so, false if not.
	 * @see #isFreeState(int)
	 * @see #isProtectedState(int)
	 */
	public boolean isFillableState(int index)
	{
		return isFreeState(index) && !isProtectedState(index);
	}
	
}
