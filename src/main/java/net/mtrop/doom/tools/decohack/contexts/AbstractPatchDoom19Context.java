package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.patches.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.PatchDoom19;

import java.util.HashMap;
import java.util.Map;

/**
 * Patch context for Doom 1.9.
 * @author Matthew Tropiano
 */
public abstract class AbstractPatchDoom19Context extends AbstractPatchContext<DEHPatchDoom19> implements DEHPatchDoom19
{
	private String[] strings;
	private Map<String, Integer> soundStringIndex;
	private Map<String, Integer> spriteStringIndex;

	/**
	 * Creates a new Doom v1.9 patch context.
	 */
	public AbstractPatchDoom19Context()
	{
		super();
		
		DEHPatchDoom19 source = getSourcePatch();
		
		this.strings = new String[source.getStringCount()];
		for (int i = 0; i < this.strings.length; i++)
			if (source.getString(i) != null)
				this.strings[i] = source.getString(i);
		
		int soundStringStart = getSoundStringIndex();
		this.soundStringIndex = new HashMap<>();
		for (int i = 0; i < getSoundCount(); i++)
			this.soundStringIndex.put(strings[i + soundStringStart].toUpperCase(), i);
		
		int spriteStringStart = getSpriteStringIndex();
		this.spriteStringIndex = new HashMap<>();
		for (int i = 0; i < PatchDoom19.STRING_INDEX_SPRITES_COUNT; i++)
			this.spriteStringIndex.put(strings[i + spriteStringStart].toUpperCase(), i);
	}
	
	/**
	 * Gets the original source patch state (for restoration or reference).
	 * <p><b>DO NOT ALTER THE CONTENTS OF THIS PATCH. THIS IS A REFERENCE STATE.</b> 
	 * @return the original source patch.
	 */
	public abstract DEHPatchDoom19 getSourcePatch();
	
	@Override
	public int getStringCount() 
	{
		return strings.length;
	}

	@Override
	public String getString(int index)
	{
		return Common.arrayElement(strings, index);
	}

	/**
	 * Sets a new string.
	 * @param index the string index to replace.
	 * @param value the string value.
	 * @throw IllegalArgumentException if the string to add is longer than the original string.
	 */
	public void setString(int index, String value)
	{
		String original = getSourcePatch().getString(index);
		if (value.length() > original.length())
			throw new IllegalArgumentException(String.format("Incoming string value for index %d is longer than the original string length: %d", index, original.length()));
		
		// if sprite.
		if (index >= getSoundStringIndex() && index < getSoundStringIndex() + getSoundCount())
		{
			soundStringIndex.remove(strings[index].toUpperCase());
			soundStringIndex.put(value.toUpperCase(), index - getSoundStringIndex());
		}
		// if sound name.
		else if (index >= getSpriteStringIndex() && index < getSpriteStringIndex() + PatchDoom19.STRING_INDEX_SPRITES_COUNT)
		{
			spriteStringIndex.remove(strings[index].toUpperCase());
			spriteStringIndex.put(value.toUpperCase(), index - getSpriteStringIndex());
		}
		
		strings[index] = value;
	}
	
	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	public abstract Integer getSoundStringIndex();

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	public abstract Integer getSpriteStringIndex();

	@Override
	public Integer getSoundIndex(String name)
	{
		return soundStringIndex.get(name.toUpperCase());
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return spriteStringIndex.get(name.toUpperCase());
	}

	@Override
	public Integer getActionPointerFrame(int index)
	{
		return getSourcePatch().getActionPointerFrame(index);
	}

}
