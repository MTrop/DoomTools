package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single ammo entry.
 * @author Matthew Tropiano
 */
public class DEHAmmo implements DEHObject<DEHAmmo>
{
	/** Ammo name. */
	private String name;
	
	/** Ammo maximum. */
	private int max;
	/** Pickup amount. */
	private int pickup;
	
	/**
	 * Creates a new Ammo.
	 * @param name the name.
	 * @param max the max ammo.
	 * @param pickup the pickup amount.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public DEHAmmo(String name, int max, int pickup)
	{
		setName(name);
		setMax(max);
		setPickup(pickup);
	}
	
	/**
	 * @return the weapon name (not used ingame).
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * Sets the weapon name (not used ingame).
	 * @param name the name.
	 */
	public void setName(String name) 
	{
		this.name = name;
	}
	
	/**
	 * @return the max ammo.
	 */
	public int getMax()
	{
		return max;
	}
	
	/**
	 * Sets the max ammo.
	 * @param max the max ammo.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public void setMax(int max) 
	{
		RangeUtils.checkRange("Ammo maximum", 0, 999999, this.max = max);
	}
	
	/**
	 * @return the pickup ammo.
	 */
	public int getPickup()
	{
		return pickup;
	}
	
	/**
	 * Sets the pickup ammo.
	 * @param pickup the pickup ammo.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public void setPickup(int pickup) 
	{
		RangeUtils.checkRange("Ammo pickup", 0, 999999, this.pickup = pickup);
	}
	
	@Override
	public void writeObject(Writer writer, DEHAmmo ammo) throws IOException
	{
		if (max != ammo.max)
			writer.append("Max ammo = ").append(String.valueOf(max)).append('\n');
		if (pickup != ammo.pickup)
			writer.append("Per ammo = ").append(String.valueOf(pickup)).append('\n');
		writer.flush();
	}
	
}
