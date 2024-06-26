/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import java.util.Set;

/**
 * Common DeHackEd Patch interface for Boom/BEX.
 * @author Matthew Tropiano
 */
public interface DEHPatchBoom extends DEHPatch
{
	/**
	 * Episode map.
	 */
	public static class EpisodeMap implements Comparable<EpisodeMap>
	{
		private int episode;
		private int map;
		
		private EpisodeMap(int episode, int map)
		{
			this.episode = episode;
			this.map = map;
		}
		
		public static EpisodeMap create(int episode, int map)
		{
			return new EpisodeMap(episode, map);
		}

		public int getEpisode() 
		{
			return episode;
		}
		
		public int getMap() 
		{
			return map;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof EpisodeMap)
				return equals((EpisodeMap)obj);
			return super.equals(obj);
		}

		public boolean equals(EpisodeMap obj) 
		{
			return episode == obj.episode
				&& map == obj.map
			;
		}
		
		@Override
		public int hashCode() 
		{
			return Integer.hashCode(episode) << 5 ^ Integer.hashCode(map);
		}

		@Override
		public int compareTo(EpisodeMap episodeMap)
		{
			return episode == episodeMap.episode ? map - episodeMap.map : episode - episodeMap.episode;
		}
	}
	
	/**
	 * Checks if a string key is correct.
	 * @param key the key.
	 * @return true if valid, false if not.
	 */
	boolean isValidStringKey(String key);
	
	/**
	 * Gets a string by its macro key.
	 * @param key the key.
	 * @return the corresponding string, or null if not a valid string.
	 */
	String getString(String key);
	
	/**
	 * @return all possible string lookup keys.
	 */
	Set<String> getStringKeys();

	/**
	 * @return all added par entries.
	 */
	Set<EpisodeMap> getParEntries();

	/**
	 * Gets par time seconds.
	 * @param map the map number.
	 * @return the seconds.
	 */
	default Integer getParSeconds(int map)
	{
		return getParSeconds(0, map);
	}

	/**
	 * Gets par time seconds.
	 * @param episode the episode number.
	 * @param map the map number.
	 * @return the seconds.
	 */
	default Integer getParSeconds(int episode, int map)
	{
		return getParSeconds(EpisodeMap.create(episode, map));
	}

	/**
	 * Gets par time seconds.
	 * @param episodeMap the episode map number.
	 * @return the seconds.
	 */
	Integer getParSeconds(EpisodeMap episodeMap);

}
