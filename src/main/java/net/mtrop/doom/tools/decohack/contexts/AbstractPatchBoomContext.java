/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;

/**
 * Patch context for Boom.
 * @author Matthew Tropiano
 */
public abstract class AbstractPatchBoomContext extends AbstractPatchContext<DEHPatchBoom> implements DEHPatchBoom
{
	private Map<String, String> strings;
	private Map<EpisodeMap, Integer> pars;

	/**
	 * Creates a new Boom patch context.
	 */
	public AbstractPatchBoomContext()
	{
		super();
		this.strings = new TreeMap<>();
		this.pars = new TreeMap<>();
	}
	
	/**
	 * Gets the original source patch state (for restoration or reference).
	 * <p><b>DO NOT ALTER THE CONTENTS OF THIS PATCH. THIS IS A REFERENCE STATE.</b> 
	 * @return the original source patch.
	 */
	public abstract DEHPatchBoom getSourcePatch();
	
	@Override
	public int getVersion() 
	{
		return 21;
	}
	
	@Override
	public String getString(String key)
	{
		return strings.getOrDefault(key, getSourcePatch().getString(key));
	}

	/**
	 * Sets a new string.
	 * @param key the string key to replace.
	 * @param value the string value.
	 */
	public void setString(String key, String value)
	{
		strings.put(key, value);
	}
	
	@Override
	public Set<String> getStringKeys()
	{
		return getSourcePatch().getStringKeys();
	}

	@Override
	public boolean isValidStringKey(String key)
	{
		return getSourcePatch().isValidStringKey(key);
	}

	@Override
	public Integer getSoundIndex(String name)
	{
		return getSourcePatch().getSoundIndex(name.toUpperCase());
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return getSourcePatch().getSpriteIndex(name.toUpperCase());
	}

	@Override
	public Set<EpisodeMap> getParEntries()
	{
		return pars.keySet();
	}
	
	@Override
	public Integer getParSeconds(EpisodeMap episodeMap)
	{
		return pars.get(episodeMap);
	}

	/**
	 * Gets par time seconds.
	 * @param map the map number.
	 * @param seconds the amount of seconds.
	 */
	public void setParSeconds(int map, int seconds)
	{
		setParSeconds(0, map, seconds);
	}

	/**
	 * Gets par time seconds.
	 * @param episode the episode number.
	 * @param map the map number.
	 * @param seconds the amount of seconds.
	 */
	public void setParSeconds(int episode, int map, int seconds)
	{
		setParSeconds(EpisodeMap.create(episode, map), seconds);
	}

	/**
	 * Sets the seconds of a episode and map.
	 * @param episodeMap the episode and map.
	 * @param seconds the amount of seconds.
	 */
	public void setParSeconds(EpisodeMap episodeMap, int seconds)
	{
		pars.put(episodeMap, seconds);
	}
	
}
