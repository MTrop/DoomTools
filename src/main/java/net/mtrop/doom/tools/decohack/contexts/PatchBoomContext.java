/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchBoom;
import net.mtrop.doom.tools.struct.util.StringUtils;

/**
 * Patch context for Boom.
 * @author Matthew Tropiano
 */
public class PatchBoomContext extends AbstractPatchContext<DEHPatchBoom> implements DEHPatchBoom
{
	private static final DEHPatchBoom BOOMPATCH = new PatchBoom();
	
	private Map<String, String> strings;
	private Map<EpisodeMap, Integer> pars;

	public PatchBoomContext() 
	{
		super();
		this.strings = new TreeMap<>();
		this.pars = new TreeMap<>();
	}
	
	@Override
	public int getVersion() 
	{
		return 21;
	}

	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return BOOMPATCH;
	}

	@Override
	public DEHActionPointer getActionPointerByMnemonic(String mnemonic) 
	{
		DEHActionPointer out = super.getActionPointerByMnemonic(mnemonic);
		if (out == null)
			out = DEHActionPointerDoom19.getActionPointerByMnemonic(mnemonic);
		return out;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.BOOM;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.BOOM;
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

	/**
	 * @return the set of string mnemonics in this patch.
	 */
	public Set<String> getStringKeys()
	{
		return strings.keySet();
	}

	@Override
	public String getString(String key)
	{
		return strings.getOrDefault(key, getSourcePatch().getString(key));
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
	
	@Override
	public void writePatch(Writer writer, String comment) throws IOException 
	{
		super.writePatch(writer, comment);
		
		// CODEPTR
		boolean codeptrHeader = false;
		for (Integer i : getUsedActionPointerIndices())
		{
			DEHActionPointer pointer = getActionPointer(i);
			DEHActionPointer original = getSourcePatch().getActionPointer(i);
			if (pointer == null)
				continue;
			if (!pointer.equals(original))
			{
				if (!codeptrHeader)
				{
					writer.append("[CODEPTR]").append(CRLF);
					codeptrHeader = true;
				}
				writer.append("FRAME ")
					.append(String.valueOf(i))
					.append(" = ")
					.append(pointer.getMnemonic())
					.append(CRLF);
			}
		}
		if (codeptrHeader)
			writer.append(CRLF).flush();
		
		// STRINGS
		boolean stringsHeader = false;
		for (String key : getStringKeys())
		{
			String value;
			if (!Objects.equals(value = getString(key), getSourcePatch().getString(key)))
			{
				if (!stringsHeader)
				{
					writer.append("[STRINGS]").append(CRLF);
					stringsHeader = true;
				}
				writer.append(key)
					.append(" = ")
					.append(StringUtils.withEscChars(value)).append(CRLF);
			}
		}
		if (stringsHeader)
			writer.append(CRLF).flush();
		
		// PARS
		boolean parsHeader = false;
		for (EpisodeMap em : getParEntries())
		{
			Integer seconds;
			if ((seconds = getParSeconds(em)) != getSourcePatch().getParSeconds(em))
			{
				if (!parsHeader)
				{
					writer.append("[PARS]").append(CRLF);
					parsHeader = true;
				}
				
				writer.append("par ");
				
				if (em.getEpisode() != 0)
					writer.append(String.valueOf(em.getEpisode())).append(' ');
				
				writer.append(String.valueOf(em.getMap()))
					.append(' ')
					.append(String.valueOf(seconds))
					.append(CRLF);
			}
		}
		if (parsHeader)
			writer.append(CRLF).flush();
	}
	
}
