/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single ammo entry.
 * TODO: Add ID24 entries.
 * @author Matthew Tropiano
 */
public class DEHAmmo extends DEHObject<DEHAmmo>
{
	/** Ammo name. */
	private String name;
	
	/** Ammo maximum. */
	private int max;
	/** Pickup amount. */
	private int pickup;
	
	public DEHAmmo()
	{
		setName("");
		setMax(1);
		setPickup(1);
	}
	
	@Override
	public DEHAmmo copyFrom(DEHAmmo source) 
	{
		if (source == this)
			return this;

		setName(source.name);
		setMax(source.max);
		setPickup(source.pickup);
		return this;
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
	 * @return this object.
	 */
	public DEHAmmo setName(String name) 
	{
		this.name = name;
		return this;
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
	 * @return this object.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public DEHAmmo setMax(int max) 
	{
		RangeUtils.checkRange("Ammo maximum", 0, 999999, max);
		this.max = max;
		return this;
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
	 * @return this object.
	 * @throws IllegalArgumentException if a provided value is out of range.
	 */
	public DEHAmmo setPickup(int pickup) 
	{
		RangeUtils.checkRange("Ammo pickup", 0, 999999, pickup);
		this.pickup = pickup;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHAmmo)
			return equals((DEHAmmo)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHAmmo obj) 
	{
		return max == obj.max
			&& pickup == obj.pickup
		;
	}	
		
	@Override
	public void writeObject(Writer writer, DEHAmmo ammo, DEHFeatureLevel level) throws IOException
	{
		if (max != ammo.max)
			writer.append("Max ammo = ").append(String.valueOf(max)).append("\r\n");
		if (pickup != ammo.pickup)
			writer.append("Per ammo = ").append(String.valueOf(pickup)).append("\r\n");
		writeCustomProperties(writer);
		writer.flush();
	}

}
