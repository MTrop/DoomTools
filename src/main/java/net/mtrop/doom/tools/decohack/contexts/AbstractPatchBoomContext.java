package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHPatchBoom;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Patch context for Boom.
 * @author Matthew Tropiano
 */
public abstract class AbstractPatchBoomContext extends AbstractPatchContext<DEHPatchBoom> implements DEHPatchBoom
{
	private Map<String, String> strings;

	/**
	 * Creates a new Boom patch context.
	 */
	public AbstractPatchBoomContext()
	{
		super();
		this.strings = new HashMap<>();
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
	public void writePatch(Writer writer, String comment) throws IOException
	{
		writePatchHeader(writer, comment, 21, 6);
		writePatchBody(writer);
		
		// TODO: Change for Boom: [CODEPTR], [STRINGS], [PARS]
	}
	
}
