/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchDSDHacked;

/**
 * Patch context for DSDHacked.
 * @author Matthew Tropiano
 */
public class PatchDSDHackedContext extends AbstractPatchBoomContext
{
	private static final DEHPatchBoom DSDHACKEDPATCH = new PatchDSDHacked();
	
	private Map<String, Integer> soundIndexMap;
	private Map<String, Integer> spriteIndexMap;
	
	private int nextSoundIndex;
	private int nextSpriteIndex;
	
	public PatchDSDHackedContext()
	{
		super();
		this.soundIndexMap = new TreeMap<>();
		this.spriteIndexMap = new TreeMap<>();
		this.nextSoundIndex = PatchDSDHacked.NEW_SOUND_INDEX_START;
		this.nextSpriteIndex = PatchDSDHacked.NEW_SPRITE_INDEX_START;
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
		return DEHFeatureLevel.MBF21;
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

}
