/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsExtended.*;

import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHThing;

/**
 * Patch implementation for DSDHacked, extending MBF21.
 * @author Matthew Tropiano
 */
public class PatchDSDHacked extends PatchMBF21
{
	/** The safe index for new states. */
	public static final int NEW_STATE_INDEX_START = 4000;
	/** The safe index for new things. */
	public static final int NEW_THING_INDEX_START = DEHTHING.length + DEHTHINGBOOM.length + DEHTHINGMBF.length + DEHTHINGEXTENDED.length;
	/** The safe index for new sounds. */
	public static final int NEW_SOUND_INDEX_START = SOUND_INDEX_EXTENDED_START + SOUNDSTRINGSEXTENDED.length;
	/** The safe index for new sprites. */
	public static final int NEW_SPRITE_INDEX_START = SPRITESTRINGS.length + SPRITESTRINGSMBF.length + SPRITESTRINGSEXTENDED.length;

	private static final DEHThing DEFAULT_THING = new DEHThing();
	private static final DEHSound DEFAULT_SOUND = DEHSound.create(127, false);
	
	@Override
	public Integer getSoundIndex(String name)
	{
		return super.getSoundIndex(name);
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return super.getSpriteIndex(name);
	}

	@Override
	public int getSoundCount()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public DEHSound getSound(int index)
	{
		if (index < 0)
			return null;
		
		return index >= NEW_SOUND_INDEX_START ? DEFAULT_SOUND : super.getSound(index);
	}

	@Override
	public int getThingCount() 
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public DEHThing getThing(int index)
	{
		if (index < 0)
			return null;

		return index >= 251 ? DEFAULT_THING : super.getThing(index);
	}

	@Override
	public int getStateCount()
	{
		return Integer.MAX_VALUE;
	}

}
