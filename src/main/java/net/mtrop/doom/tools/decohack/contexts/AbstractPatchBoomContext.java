package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHPatchBoom;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

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

	@Override
	public void writePatch(Writer writer, String comment) throws IOException
	{
		writePatchHeader(writer, comment, 21, 6);
		writePatchBody(writer);
		
		// CODEPTR
		boolean codeptrHeader = false;
		for (int i = 0; i < getStateCount(); i++)
		{
			DEHActionPointer pointer = getActionPointer(i);
			DEHActionPointer original = getSourcePatch().getActionPointer(i);
			if (!pointer.equals(original))
			{
				if (!codeptrHeader)
				{
					writer.append("[CODEPTR]").append("\r\n");
					codeptrHeader = true;
				}
				writer.append("Frame ")
					.append(String.valueOf(i))
					.append(" = ")
					.append(pointer.getMnemonic())
					.append("\r\n");
			}
		}
		if (codeptrHeader)
			writer.append("\r\n").flush();
		
		// STRINGS
		boolean stringsHeader = false;
		for (String keys : getStringKeys())
		{
			String value;
			if (Objects.equals(value = getString(keys), getSourcePatch().getString(keys)))
			{
				if (!stringsHeader)
				{
					writer.append("[STRINGS]").append("\r\n");
					stringsHeader = true;
				}
				writer.append(keys)
					.append(" = ")
					.append(Common.withEscChars(value)).append("\r\n");
			}
		}
		if (stringsHeader)
			writer.append("\r\n").flush();
		
		// PARS
		boolean parsHeader = false;
		for (EpisodeMap em : getParEntries())
		{
			Integer seconds;
			if ((seconds = getParSeconds(em)) != getSourcePatch().getParSeconds(em))
			{
				if (!parsHeader)
				{
					writer.append("[PARS]").append("\r\n");
					parsHeader = true;
				}
				
				writer.append("par ");
				
				if (em.getEpisode() != 0)
					writer.append(String.valueOf(em.getEpisode())).append(' ');
				
				writer.append(String.valueOf(em.getMap()))
					.append(' ')
					.append(String.valueOf(seconds))
					.append("\r\n");
			}
		}
		if (parsHeader)
			writer.append("\r\n").flush();
	}
	
}
