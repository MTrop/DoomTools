/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single sound entry.
 * @author Matthew Tropiano
 */
public class DEHSound extends DEHObject<DEHSound>
{
	/** Sound priority (unsigned byte). */
	private int priority;
	/** Sound is singular. */
	private boolean singular;
	
	/**
	 * Creates a new DEHSound.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public DEHSound()
	{
		setPriority(0);
		setSingular(false);
	}

	/**
	 * Creates a new DEHSound.
	 * @param priority the sound priority.
	 * @param singular if the sound is singularly played and at full volume.
	 * @return a new sound.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public static DEHSound create(int priority, boolean singular)
	{
		DEHSound out = new DEHSound();
		out.setPriority(priority);
		out.setSingular(singular);
		return out;
	}
	
	@Override
	public DEHSound copyFrom(DEHSound source) 
	{
		if (source == this)
			return this;
		
		setPriority(source.priority);
		setSingular(source.singular);

		clearCustomPropertyValues();
		for (Map.Entry<DEHProperty, String> entry : source.getCustomPropertySet())
			setCustomPropertyValue(entry.getKey(), entry.getValue());
		
		return this;
	}
	
	/**
	 * @return the sound priority.
	 */
	public int getPriority()
	{
		return priority;
	}
	
	/**
	 * Sets the sound priority.
	 * @param priority the sound priority.
	 * @return this object.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public DEHSound setPriority(int priority) 
	{
		RangeUtils.checkRange("Sound priority", 0, 127, priority);
		this.priority = priority;
		return this;
	}
	
	/**
	 * @return sound singularity.
	 */
	public boolean isSingular() 
	{
		return singular;
	}
	
	/**
	 * Sets sound singularity.
	 * @param singular true if so, false if not.
	 * @return this object.
	 */
	public DEHSound setSingular(boolean singular) 
	{
		this.singular = singular;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHSound)
			return equals((DEHSound)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHSound obj) 
	{
		return priority == obj.priority
			&& singular == obj.singular
		;
	}	
		
	@Override
	public void writeObject(Writer writer, DEHSound sound, DEHFeatureLevel level) throws IOException
	{
		if (forceOutput || priority != sound.priority)
			writer.append("Value = ").append(String.valueOf(priority)).append("\r\n");
		if (forceOutput || singular != sound.singular)
			writer.append("Zero/One = ").append(String.valueOf(singular ? 1 : 0)).append("\r\n");
		writeCustomProperties(writer);
		writer.flush();
	}
	
}
