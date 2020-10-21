package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single sound entry.
 * @author Matthew Tropiano
 */
public class DEHSound implements DEHObject<DEHSound>
{
	/** Sound priority (unsigned byte). */
	private int priority;
	/** Sound is singular. */
	private boolean singular;
	
	/**
	 * Creates a new DEHSound.
	 * @param priority the sound priority.
	 * @param singular if the sound is singularly played and at full volume.
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
		setPriority(source.priority);
		setSingular(source.singular);
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
		RangeUtils.checkByteUnsigned("Sound priority", priority);
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
	public void writeObject(Writer writer, DEHSound sound) throws IOException
	{
		if (priority != sound.priority)
			writer.append("Value = ").append(String.valueOf(priority)).append("\r\n");
		if (singular != sound.singular)
			writer.append("Zero/One = ").append(String.valueOf(singular ? 1 : 0)).append("\r\n");
		writer.flush();
	}
	
}
