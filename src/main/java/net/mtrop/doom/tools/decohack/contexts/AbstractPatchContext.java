/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHObject;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatch;
import net.mtrop.doom.tools.struct.IntervalMap;

/**
 * Abstract patch context.
 * @author Matthew Tropiano
 * @param <P> DEH patch type.
 */
public abstract class AbstractPatchContext<P extends DEHPatch> implements DEHPatch
{
	protected static final String CRLF = "\r\n";

	private Map<Integer, DEHAmmo> ammo;
	private Map<Integer, DEHSound> sounds;
	private Map<Integer, DEHWeapon> weapons;
	private Map<Integer, DEHThing> things;
	private Map<Integer, DEHState> states;
	private Map<Integer, DEHActionPointer> pointers;
	private DEHMiscellany miscellany;

	private int freeStateCount;
	private int freePointerStateCount;
	private int freeThingCount;

	protected IntervalMap<Boolean> freeStatesMap;
	protected IntervalMap<Boolean> protectedStatesMap;
	protected IntervalMap<Boolean> freeThingsMap;
	
	protected Map<String, Integer> thingAliasMap;
	protected Map<String, Integer> weaponAliasMap;
	protected Map<String, DEHActionPointer> pointerMnemonicMap;
	
	/**
	 * Shadows a DEH object from the source patch to the editable object,
	 * or returning it if it has already been shadowed.
	 * @param <T> the object type.
	 * @param index the object index.
	 * @param targetMap the target map to put the object into.
	 * @param fetcher the fetcher function, called if not found.
	 * @return the object or null if not valid.
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends DEHObject<T>> T shadow(int index, Map<Integer, T> targetMap, Function<Integer, T> fetcher)
	{
		T obj;
		if ((obj = targetMap.get(index)) == null)
		{
			T srcObj;
			if ((srcObj = fetcher.apply(index)) != null)
				targetMap.put(index, obj = ((T)Common.create(srcObj.getClass())).copyFrom(srcObj));
		}
		return obj;
	}

	/**
	 * Copies an object from the source patch to the editable object,
	 * or returning it if it has already been copied.
	 * @param <T> the object type.
	 * @param key the object key.
	 * @param targetMap the target map to put the object into.
	 * @param fetcher the fetcher function, called if not found.
	 * @return the object or null if not valid.
	 */
	protected static <K, T> T copy(K key, Map<K, T> targetMap, Function<K, T> fetcher)
	{
		T obj;
		if ((obj = targetMap.get(key)) == null)
		{
			T srcObj;
			if ((srcObj = fetcher.apply(key)) != null)
				targetMap.put(key, obj = srcObj);
		}
		return obj;
	}

	/**
	 * Creates a new patch context.
	 */
	protected AbstractPatchContext()
	{
		DEHPatch source = getSourcePatch();
		
		this.ammo = new TreeMap<>();
		this.sounds = new TreeMap<>();
		this.weapons = new TreeMap<>();
		this.things = new TreeMap<>();
		this.states = new TreeMap<>();
		this.pointers = new TreeMap<>();
		
		this.miscellany = (new DEHMiscellany()).copyFrom(source.getMiscellany());
		
		this.freeStateCount = 0;
		this.freeStatesMap = new IntervalMap<>(0, getStateCount() - 1, false);
		this.protectedStatesMap = new IntervalMap<>(0, getStateCount() - 1, false);

		this.freeThingCount = 0;
		this.freeThingsMap = new IntervalMap<>(0, getThingCount() - 1, false);
		this.thingAliasMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.weaponAliasMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.pointerMnemonicMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
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
	
	/**
	 * @return the Doom Version.
	 */
	public abstract int getVersion();
	
	@Override
	public DEHMiscellany getMiscellany() 
	{
		return miscellany;
	}

	@Override
	public int getAmmoCount() 
	{
		return getSourcePatch().getAmmoCount();
	}

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		return shadow(index, ammo, (i) -> getSourcePatch().getAmmo(i));
	}
	
	/**
	 * @return the set of used/fetched ammo indices.
	 */
	public Set<Integer> getUsedAmmoIndices()
	{
		return ammo.keySet();
	}

	@Override
	public int getSoundCount() 
	{
		return getSourcePatch().getSoundCount();
	}

	@Override
	public DEHSound getSound(int index)
	{
		return shadow(index, sounds, (i) -> getSourcePatch().getSound(i));
	}

	/**
	 * @return the set of used/fetched sound indices.
	 */
	public Set<Integer> getUsedSoundIndices()
	{
		return sounds.keySet();
	}

	@Override
	public int getThingCount() 
	{
		return getSourcePatch().getThingCount();
	}

	@Override
	public DEHThing getThing(int index)
	{
		return shadow(index, things, (i) -> getSourcePatch().getThing(i));
	}

	/**
	 * @return the set of used/fetched thing indices.
	 */
	public Set<Integer> getUsedThingIndices()
	{
		return things.keySet();
	}

	@Override
	public int getWeaponCount()
	{
		return getSourcePatch().getWeaponCount();
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return shadow(index, weapons, (i) -> getSourcePatch().getWeapon(i));
	}

	/**
	 * @return the set of used/fetched weapon indices.
	 */
	public Set<Integer> getUsedWeaponIndices()
	{
		return weapons.keySet();
	}

	@Override
	public int getStateCount()
	{
		return getSourcePatch().getStateCount();
	}

	@Override
	public DEHState getState(int index) 
	{
		return shadow(index, states, (i) -> getSourcePatch().getState(i));
	}

	/**
	 * @return the set of used/fetched state indices.
	 */
	public Set<Integer> getUsedStateIndices()
	{
		return states.keySet();
	}

	@Override
	public Integer getStateActionPointerIndex(int stateIndex) 
	{
		return getSourcePatch().getStateActionPointerIndex(stateIndex);
	}

	@Override
	public int getActionPointerCount() 
	{
		return getSourcePatch().getActionPointerCount();
	}

	@Override
	public DEHActionPointer getActionPointer(int index)
	{
		return copy(index, pointers, (i) -> getSourcePatch().getActionPointer(i));
	}

	/**
	 * Gets an action pointer by its mnemonic.
	 * @param mnemonic the action pointer mnemonic.
	 * @return the corresponding pointer, or null if no pointer.
	 */
	public DEHActionPointer getActionPointerByMnemonic(String mnemonic)
	{
		return pointerMnemonicMap.get(mnemonic);
	}
	
	/**
	 * @return the set of used/fetched pointer indices.
	 */
	public Set<Integer> getUsedActionPointerIndices()
	{
		return pointers.keySet();
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
		pointers.put(index, pointer);
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
		checkIndexRange(index, freeStatesMap);
		return freeStatesMap.getOrDefault(index, false);
	}

	/**
	 * Marks a state as "free" or not.
	 * @param index the state index to mark as free.
	 * @param state true to set as "free", false to unset.
	 * @throws IllegalStateException if the target state is protected.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #isProtectedState(int)
	 */
	public void setFreeState(int index, boolean state)
	{
		if (isProtectedState(index))
			throw new IllegalStateException("State " + index + " is a protected state.");
		
		checkIndexRange(index, freeStatesMap);

		boolean prev = freeStatesMap.get(index); 
		freeStatesMap.set(index, state);
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
	 * Marks a series of states as "free" or not.
	 * @param min the starting state index. 
	 * @param max the ending state index (inclusive).
	 * @param state true to set as "free", false to unset.
	 * @throws IllegalStateException if the target state is protected.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #isProtectedState(int)
	 */
	public void setFreeState(int min, int max, boolean state)
	{
		checkIndexRange(min, freeStatesMap);
		checkIndexRange(max, freeStatesMap);
		int a = Math.min(min, max);
		int b = Math.max(min, max);
		while (a <= b)
			setFreeState(a++, state);
	}
	
	/**
	 * Gets if a state is flagged as "protected".
	 * @param index the index.
	 * @return true if so, false if not.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public boolean isProtectedState(int index)
	{
		checkIndexRange(index, protectedStatesMap);
		return protectedStatesMap.getOrDefault(index, false);
	}

	/**
	 * Marks a state as "protected" - attempting to free this state or alter it
	 * directly will throw an exception.
	 * @param index the index to mark as protected.
	 * @param state true to set as "protected", false to unset.
	 */
	public void setProtectedState(int index, boolean state)
	{
		checkIndexRange(index, protectedStatesMap);
		protectedStatesMap.set(index, state);
	}

	/**
	 * Marks a state as "protected" - attempting to free this state or alter it
	 * directly will throw an exception.
	 * @param min the starting state index. 
	 * @param max the ending state index (inclusive).
	 * @param state true to set as "protected", false to unset.
	 */
	public void setProtectedState(int min, int max, boolean state)
	{
		checkIndexRange(min, protectedStatesMap);
		checkIndexRange(max, protectedStatesMap);
		int a = Math.min(min, max);
		int b = Math.max(min, max);
		while (a <= b)
			setProtectedState(a++, state);
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
		return searchNextFree(startingIndex, getStateCount(), (i) -> 
			isFillableState(i)
		);
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
		return searchNextFree(startingIndex, getStateCount(), (i) -> 
			isFillableState(i) && getStateActionPointerIndex(i) != null
		);
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
		return searchNextFree(startingIndex, getStateCount(), (i) -> 
			isFillableState(i) && getStateActionPointerIndex(i) == null
		);
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

	/**
	 * Gets how many free things there are.
	 * @return the amount of things flagged as "free."
	 */
	public int getFreeThingCount() 
	{
		return freeThingCount;
	}
	
	/**
	 * Gets if a thing is flagged as "free".
	 * @param thingIndex the index.
	 * @return true if so, false if not.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public boolean isFreeThing(int thingIndex)
	{
		checkIndexRange(thingIndex, freeThingsMap);
		return freeThingsMap.get(thingIndex);
	}

	/**
	 * Sets a thing as "free" or not. 
	 * @param index the thing index.
	 * @param state true to set as "free", false to unset.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public void setFreeThing(int index, boolean state)
	{
		checkIndexRange(index, freeThingsMap);
		
		boolean prev = freeThingsMap.get(index); 
		freeThingsMap.set(index, state);
		if (prev && !state)
			freeThingCount--;
		else if (!prev && state)
			freeThingCount++;
	}

	/**
	 * Sets a contiguous set of things as "free" or not. 
	 * @param min the minimum thing index.
	 * @param max the maximum thing index (inclusive).
	 * @param state true to set as "free", false to unset.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public void setFreeThing(int min, int max, boolean state)
	{
		checkIndexRange(min, freeThingsMap);
		checkIndexRange(max, freeThingsMap);
		
		int a = Math.min(min, max);
		int b = Math.max(min, max);
		while (a <= b)
			setFreeThing(a++, state);
	}

	/**
	 * Searches linearly for the next free thing in this context from a starting index.
	 * If the start index is free, it is returned. If a full search completes without finding
	 * a free index, <code>null</code> is returned.
	 * @param startingIndex the starting index.
	 * @return the next free thing index, or <code>null</code> if none found.
	 */
	public Integer findNextFreeThing(int startingIndex)
	{
		return searchNextFree(startingIndex, getThingCount(), (i) -> 
			isFreeThing(i)
		);
	}
	
	/**
	 * Sets an auto-allocated thing index via an identifier (case-insensitive).
	 * @param identifier the identifier.
	 * @param index the corresponding index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public void setThingAlias(String identifier, int index)
	{
		checkIndexRange(index, freeThingsMap);
		thingAliasMap.put(identifier, index);
	}
	
	/**
	 * Gets an auto-allocated thing index via an identifier (case-insensitive).
	 * @param identifier the identifier.
	 * @return the corresponding index, or null if no corresponding index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public Integer getThingAlias(String identifier)
	{
		return thingAliasMap.get(identifier);
	}

	/**
	 * Gets the set of available thing aliases.
	 * The returned set is a copy and can be manipulated without affecting the main set.
	 * @return the set of thing alias names.
	 */
	public SortedSet<String> getThingAliases()
	{
		SortedSet<String> out = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		out.addAll(thingAliasMap.keySet());
		return out;
	}
	
	/**
	 * Sets an auto-allocated weapon index via an identifier (case-insensitive).
	 * @param identifier the identifier.
	 * @param index the corresponding index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public void setWeaponAlias(String identifier, int index)
	{
		checkIndexRange(index, getWeaponCount());
		weaponAliasMap.put(identifier, index);
	}
	
	/**
	 * Gets an auto-allocated weapon index via an identifier (case-insensitive).
	 * @param identifier the identifier.
	 * @return the corresponding index, or null if no corresponding index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 */
	public Integer getWeaponAlias(String identifier)
	{
		return weaponAliasMap.get(identifier);
	}

	/**
	 * Gets the set of available weapon aliases.
	 * The returned set is a copy and can be manipulated without affecting the main set.
	 * @return the set of thing alias names.
	 */
	public SortedSet<String> getWeaponAliases()
	{
		SortedSet<String> out = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		out.addAll(weaponAliasMap.keySet());
		return out;
	}
	
	/**
	 * Writes the patch data to a writer.
	 * @param writer the output writer.
	 * @param comment a comment line (containing the version line).
	 * @throws IOException if a write error occurs.
	 */
	public void writePatch(Writer writer, String comment) throws IOException
	{
		writePatchHeader(writer, comment);
		writeCommonPatchBody(writer);
	}
	
	// Throws IndexOutOfBoundsException if out of range.
	protected void checkIndexRange(int index, IntervalMap<?> map)
	{
		if (index < 0 || index > map.getMaxIndex())
			throw new IndexOutOfBoundsException("Index cannot be less than 0 or greater than " + map.getMaxIndex());
	}

	// Throws IndexOutOfBoundsException if out of range.
	protected void checkIndexRange(int index, int count)
	{
		if (index < 0 || index >= count)
			throw new IndexOutOfBoundsException("Index cannot be less than 0 or greater than " + (count - 1));
	}

	/**
	 * Writes the patch header.
	 * @param writer the output writer.
	 * @param comment a comment line (containing the version line).
	 * @throws IOException if a write error occurs.
	 */
	protected void writePatchHeader(Writer writer, String comment) throws IOException
	{
		// Header
		writer.append("Patch File for DeHackEd v3.0").append(CRLF);
		
		// Comment Blurb
		writer.append("# ").append(comment).append(CRLF);
		writer.append("# Note: Use the pound sign ('#') to start comment lines.").append(CRLF);
		writer.append(CRLF);
	
		// Version
		writer.append("Doom version = ").append(String.valueOf(getVersion())).append(CRLF);
		writer.append("Patch format = 6").append(CRLF);
		writer.append(CRLF);
		writer.append(CRLF);
		writer.flush();
	}

	/**
	 * Writes the common patch body.
	 * @param writer the output writer.
	 * @throws IOException if a write error occurs.
	 */
	protected void writeCommonPatchBody(Writer writer) throws IOException
	{
		for (Integer i : getUsedThingIndices())
		{
			DEHThing thing = getThing(i);
			DEHThing original = getSourcePatch().getThing(i);
			if (thing == null)
				continue;
			if (!thing.equals(original))
			{
				writer.append("Thing ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(thing.getName()))
					.append(")")
					.append(CRLF);
				thing.writeObject(writer, original, getSupportedFeatureLevel());
				writer.append(CRLF);
			}
		}
		writer.flush();
	
		for (Integer i : getUsedStateIndices())
		{
			DEHState state = getState(i);
			DEHState original = getSourcePatch().getState(i);
			if (state == null)
				continue;
			if (!state.equals(original))
			{
				writer.append("Frame ").append(String.valueOf(i)).append(CRLF);
				state.writeObject(writer, original, getSupportedFeatureLevel());
				writer.append(CRLF);
			}
		}
		writer.flush();
	
		for (Integer i : getUsedSoundIndices())
		{
			DEHSound sound = getSound(i);
			DEHSound original = getSourcePatch().getSound(i);
			if (sound == null)
				continue;
			if (!sound.equals(original))
			{
				// Sound ids in DeHackEd are off by 1
				writer.append("Sound ").append(String.valueOf(i - 1)).append(CRLF);
				sound.writeObject(writer, original, getSupportedFeatureLevel());
				writer.append(CRLF);
			}
		}
		writer.flush();
	
		for (Integer i : getUsedWeaponIndices())
		{
			DEHWeapon weapon = getWeapon(i);
			DEHWeapon original = getSourcePatch().getWeapon(i);
			if (weapon == null)
				continue;
			if (!weapon.equals(original))
			{
				writer.append("Weapon ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(weapon.getName()))
					.append(")")
					.append(CRLF);
				weapon.writeObject(writer, original, getSupportedFeatureLevel());
				writer.append(CRLF);
			}
		}
		writer.flush();
	
		for (Integer i : getUsedAmmoIndices())
		{
			DEHAmmo ammo = getAmmo(i);
			DEHAmmo original = getSourcePatch().getAmmo(i);
			if (ammo == null)
				continue;
			if (!ammo.equals(original))
			{
				writer.append("Ammo ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(ammo.getName()))
					.append(")")
					.append(CRLF);
				ammo.writeObject(writer, original, getSupportedFeatureLevel());
				writer.append(CRLF);
			}
		}
		writer.flush();
	
		DEHMiscellany misc = getMiscellany();
		DEHMiscellany miscOriginal = getSourcePatch().getMiscellany();
		if (!misc.equals(miscOriginal))
		{
			writer.append("Misc ").append(String.valueOf(0)).append(CRLF);
			misc.writeObject(writer, miscOriginal, getSupportedFeatureLevel());
			writer.append(CRLF);
		}
		writer.flush();
	}

	// Search function for free states.
	private Integer searchNextFree(int startingIndex, int maxIndex, Function<Integer, Boolean> isFreeFunc)
	{
		int i = startingIndex;
		while (!isFreeFunc.apply(i))
		{
			i++;
			if (i >= maxIndex)
				i = 0;
			if (i == startingIndex)
				return null;
		}
		return i;
	}
	
}
