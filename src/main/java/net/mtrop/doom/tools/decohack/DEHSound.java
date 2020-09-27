package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single sound entry.
 * @author Matthew Tropiano
 */
public class DEHSound implements DEHObject
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
	public DEHSound(int priority, boolean singular)
	{
		setPriority(priority);
		this.singular = singular;
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
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public void setPriority(int priority) 
	{
		RangeUtils.checkByteUnsigned("Sound priority", priority);
		this.priority = priority;
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
	 */
	public void setSingular(boolean singular) 
	{
		this.singular = singular;
	}
	
	@Override
	public void writeObject(Writer writer) throws IOException
	{
		writer.append("Value = ").append(String.valueOf(priority)).append('\n');
		writer.append("Zero/One = ").append(String.valueOf(singular ? 1 : 0)).append('\n');
	}
	
}
