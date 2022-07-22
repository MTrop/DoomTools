/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchDSDHacked;

/**
 * Patch context for DSDHacked.
 * @author Matthew Tropiano
 */
public class PatchDSDHackedContext extends PatchMBF21Context
{
	private static final DEHPatchBoom DSDHACKEDPATCH = new PatchDSDHacked();
	
	private Map<String, Integer> soundIndexMap;
	private Map<String, Integer> spriteIndexMap;
	
	private int nextSoundIndex;
	private int nextSpriteIndex;
	
	public PatchDSDHackedContext()
	{
		super();
		this.soundIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.spriteIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.nextSoundIndex = PatchDSDHacked.NEW_SOUND_INDEX_START;
		this.nextSpriteIndex = PatchDSDHacked.NEW_SPRITE_INDEX_START;
	}
	
	@Override
	public int getVersion() 
	{
		return 2021;
	}
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return DSDHACKEDPATCH;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.MBF21;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.DSDHACKED;
	}

	@Override
	public Integer getSoundIndex(String name)
	{
		Integer out;
		if ((out = copy(name, soundIndexMap, (n) -> getSourcePatch().getSoundIndex(name))) == null)
			soundIndexMap.put(name.toUpperCase(), out = nextSoundIndex++);
		return out;
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		Integer out;
		if ((out = copy(name, spriteIndexMap, (n) -> getSourcePatch().getSpriteIndex(name))) == null)
			spriteIndexMap.put(name.toUpperCase(), out = nextSpriteIndex++);
		return out;
	}

	@Override
	public int getFreeStateCount() 
	{
		return (int)freeStatesMap.getIndexWidth(true);
	}

	@Override
	public int getFreePointerStateCount() 
	{
		return getFreeStateCount();
	}

	@Override
	public void setFreeState(int index, boolean state)
	{
		if (isProtectedState(index))
			throw new IllegalStateException("State " + index + " is a protected state.");
		freeStatesMap.set(index, state);
	}

	@Override
	public void setFreeState(int min, int max, boolean state)
	{
		checkIndexRange(min, freeStatesMap);
		checkIndexRange(max, freeStatesMap);
		if (min < 0 || max < 0)
			throw new IndexOutOfBoundsException("Index cannot be less than 0.");
		if (protectedStatesMap.getValueSet(min, max).contains(true))
			throw new IllegalStateException("One of the freed states is a protected state.");
		freeStatesMap.set(min, max, state);
	}

	@Override
	public void setProtectedState(int index, boolean state)
	{
		checkIndexRange(index, protectedStatesMap);
		protectedStatesMap.set(index, state);
	}

	@Override
	public void setProtectedState(int min, int max, boolean state)
	{
		checkIndexRange(min, protectedStatesMap);
		checkIndexRange(max, protectedStatesMap);
		protectedStatesMap.set(min, max, state);
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
	 * Gets how many free things there are.
	 * @return the amount of things flagged as "free."
	 */
	public int getFreeThingCount() 
	{
		return (int)freeThingsMap.getIndexWidth(true);
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
		freeThingsMap.set(index, state);
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
		freeThingsMap.set(min, max, state);
	}
	
	@Override
	public void writePatch(Writer writer, String comment) throws IOException
	{
		super.writePatch(writer, comment);
		
		// SPRITES
		boolean spritesHeader = false;
		for (Map.Entry<String, Integer> entry : spriteIndexMap.entrySet())
		{
			if (entry.getValue() == getSourcePatch().getSpriteIndex(entry.getKey()))
				continue;
			
			if (!spritesHeader)
			{
				writer.append("[SPRITES]").append(CRLF);
				spritesHeader = true;
			}

			writer.append(String.valueOf(entry.getValue()))
				.append(" = ")
				.append(String.valueOf(entry.getKey().toUpperCase()))
				.append(CRLF);
		}
		if (spritesHeader)
			writer.append(CRLF).flush();

		// SOUNDS
		boolean soundHeader = false;
		for (Map.Entry<String, Integer> entry : soundIndexMap.entrySet())
		{
			if (entry.getValue() == getSourcePatch().getSoundIndex(entry.getKey()))
				continue;

			if (!soundHeader)
			{
				writer.append("[SOUNDS]").append(CRLF);
				soundHeader = true;
			}

			writer.append(String.valueOf(entry.getValue()))
				.append(" = ")
				.append(String.valueOf(entry.getKey().toUpperCase()))
				.append(CRLF);
		}
		if (soundHeader)
			writer.append(CRLF).flush();
	}
	
}
