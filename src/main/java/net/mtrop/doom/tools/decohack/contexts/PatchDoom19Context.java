/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.PatchDoom19;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Patch context for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchDoom19Context extends AbstractPatchContext<DEHPatchDoom19> implements DEHPatchDoom19
{
	private static final DEHPatchDoom19 DOOM19PATCH = new PatchDoom19();
	
	private String[] strings;
	private Map<String, Integer> soundStringIndex;
	private Map<String, Integer> spriteStringIndex;

	public PatchDoom19Context()
	{
		super();
		
		DEHPatchDoom19 source = getSourcePatch();
		
		this.strings = new String[source.getStringCount()];
		for (int i = 0; i < this.strings.length; i++)
			if (source.getString(i) != null)
				this.strings[i] = source.getString(i);
		
		int soundStringStart = getSoundStringIndex();
		this.soundStringIndex = new HashMap<>();
		for (int i = 1; i < getSoundCount(); i++)
			this.soundStringIndex.put(strings[i - 1 + soundStringStart].toUpperCase(), i);
		
		int spriteStringStart = getSpriteStringIndex();
		this.spriteStringIndex = new HashMap<>();
		for (int i = 0; i < PatchDoom19.STRING_INDEX_SPRITES_COUNT; i++)
			this.spriteStringIndex.put(strings[i + spriteStringStart].toUpperCase(), i);
	}
	
	@Override
	public int getVersion() 
	{
		return 19;
	}

	@Override
	public DEHPatchDoom19 getSourcePatch()
	{
		return DOOM19PATCH;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.DOOM19;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.DOOM19;
	}

	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	public Integer getSoundStringIndex()
	{
		return PatchDoom19.STRING_INDEX_SOUNDS;
	}

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	public Integer getSpriteStringIndex()
	{
		return PatchDoom19.STRING_INDEX_SPRITES;
	}

	@Override
	public int getStringCount() 
	{
		return strings.length;
	}

	@Override
	public String getString(int index)
	{
		return ArrayUtils.arrayElement(strings, index);
	}

	@Override
	public boolean enforceStringLength()
	{
		return true;
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
		int maxLength = calculateMaxStringLength(original);
		if (enforceStringLength() && value.length() > maxLength)
		{
			throw new IllegalArgumentException(
				String.format("Incoming string value for index %d is %d characters. Original string length is %d. Delete %d characters to fit!", 
				index, value.length(), maxLength, value.length() - maxLength)
			);
		}
		
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
	
	@Override
	public DEHActionPointer getActionPointerByMnemonic(String mnemonic) 
	{
		DEHActionPointer out = super.getActionPointerByMnemonic(mnemonic);
		if (out == null)
			out = DEHActionPointerDoom19.getActionPointerByMnemonic(mnemonic);
		return out;
	}

	@Override
	public void writePatch(Writer writer, String comment) throws IOException 
	{
		super.writePatch(writer, comment);
		
		for (Integer i : getUsedActionPointerIndices())
		{
			DEHActionPointer action = getActionPointer(i);
			DEHActionPointer original = getSourcePatch().getActionPointer(i);
			if (action == null)
				continue;
			if (!action.equals(original))
			{
				writer.append("Pointer ")
					.append(String.valueOf(i))
					.append(" (Frame ")
					.append(String.valueOf(getSourcePatch().getActionPointerFrame(i)))
					.append(")")
					.append(CRLF);
				writer.append("Codep Frame = ").append(String.valueOf(action.getFrame())).append(CRLF);
				writer.append(CRLF);
			}
		}
		writer.flush();

		for (int i = 0; i < getStringCount(); i++)
		{
			String str = getString(i);
			String original = getSourcePatch().getString(i);
			if (str == null)
				continue;
			if (!str.equals(original))
			{
				writer.append("Text ")
					.append(String.valueOf(original.length()))
					.append(" ")
					.append(String.valueOf(str.length()))
					.append(CRLF);
				writer.append(original).append(str);
				if (i < getStringCount() - 1)
					writer.append(CRLF);
				writer.flush();
			}
		}
	}
	
	// Calculates the max length for a new string.
	// Assumes the characters are already ASCII-encodable.
	private static int calculateMaxStringLength(String str)
	{
		int len = str.length() + 1; // length plus null
		len += (4 - (len % 4)) % 4; // pad to next DWORD
		return len - 1;             // minus the null 
	}
	
}
