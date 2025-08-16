/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
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
 * A single ammo entry.
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
	
	// ID24
	private int initialAmmo;
	private int maxUpgradedAmmo;
	private int boxAmmo;
	private int backpackAmmo;
	private int weaponAmmo;
	private int droppedAmmo;
	private int droppedBoxAmmo;
	private int droppedBackpackAmmo;
	private int droppedWeaponAmmo;
	private int deathmatchWeaponAmmo;

	private int skill1Multiplier; // stored as 16.16 fixed
	private int skill2Multiplier; // stored as 16.16 fixed
	private int skill3Multiplier; // stored as 16.16 fixed
	private int skill4Multiplier; // stored as 16.16 fixed
	private int skill5Multiplier; // stored as 16.16 fixed

	
	public DEHAmmo()
	{
		setName("");
		setMax(1);
		setPickup(1);
		// ID24
		setInitialAmmo(0);
		setMaxUpgradedAmmo(0);
		setBoxAmmo(0);
		setBackpackAmmo(0);
		setWeaponAmmo(0);
		setDroppedAmmo(0);
		setDroppedBoxAmmo(0);
		setDroppedBackpackAmmo(0);
		setDroppedWeaponAmmo(0);
		setDeathmatchWeaponAmmo(0);
		setSkill1Multiplier(2 << 16);
		setSkill2Multiplier(1 << 16);
		setSkill3Multiplier(1 << 16);
		setSkill4Multiplier(1 << 16);
		setSkill5Multiplier(2 << 16);
	}
	
	@Override
	public DEHAmmo copyFrom(DEHAmmo source) 
	{
		if (source == this)
			return this;

		setName(source.name);
		setMax(source.max);
		setPickup(source.pickup);
		
		clearCustomPropertyValues();
		for (Map.Entry<DEHProperty, String> entry : source.getCustomPropertySet())
			setCustomPropertyValue(entry.getKey(), entry.getValue());
		
		// ID24
		setInitialAmmo(source.initialAmmo);
		setMaxUpgradedAmmo(source.maxUpgradedAmmo);
		setBoxAmmo(source.boxAmmo);
		setBackpackAmmo(source.backpackAmmo);
		setWeaponAmmo(source.weaponAmmo);
		setDroppedAmmo(source.droppedAmmo);
		setDroppedBoxAmmo(source.droppedBoxAmmo);
		setDroppedBackpackAmmo(source.droppedBackpackAmmo);
		setDroppedWeaponAmmo(source.droppedWeaponAmmo);
		setDeathmatchWeaponAmmo(source.deathmatchWeaponAmmo);
		setSkill1Multiplier(source.skill1Multiplier);
		setSkill2Multiplier(source.skill2Multiplier);
		setSkill3Multiplier(source.skill3Multiplier);
		setSkill4Multiplier(source.skill4Multiplier);
		setSkill5Multiplier(source.skill5Multiplier);
		
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
		RangeUtils.checkRange("Ammo maximum", 0, Integer.MAX_VALUE, max);
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
		RangeUtils.checkRange("Ammo pickup", 0, Integer.MAX_VALUE, pickup);
		this.pickup = pickup;
		return this;
	}
	
	public int getInitialAmmo() 
	{
		return initialAmmo;
	}

	public int getMaxUpgradedAmmo() 
	{
		return maxUpgradedAmmo;
	}

	public int getBoxAmmo() 
	{
		return boxAmmo;
	}

	public int getBackpackAmmo()
	{
		return backpackAmmo;
	}

	public int getWeaponAmmo()
	{
		return weaponAmmo;
	}

	public int getDroppedAmmo() 
	{
		return droppedAmmo;
	}

	public int getDroppedBoxAmmo()
	{
		return droppedBoxAmmo;
	}

	public int getDroppedBackpackAmmo() 
	{
		return droppedBackpackAmmo;
	}

	public int getDroppedWeaponAmmo() 
	{
		return droppedWeaponAmmo;
	}

	public int getDeathmatchWeaponAmmo() 
	{
		return deathmatchWeaponAmmo;
	}

	public int getSkill1Multiplier() 
	{
		return skill1Multiplier;
	}

	public int getSkill2Multiplier()
	{
		return skill2Multiplier;
	}

	public int getSkill3Multiplier()
	{
		return skill3Multiplier;
	}

	public int getSkill4Multiplier()
	{
		return skill4Multiplier;
	}

	public int getSkill5Multiplier() 
	{
		return skill5Multiplier;
	}

	public DEHAmmo setInitialAmmo(int initialAmmo)
	{
		this.initialAmmo = initialAmmo;
		return this;
	}

	public DEHAmmo setMaxUpgradedAmmo(int maxUpgradedAmmo)
	{
		this.maxUpgradedAmmo = maxUpgradedAmmo;
		return this;
	}

	public DEHAmmo setBoxAmmo(int boxAmmo)
	{
		this.boxAmmo = boxAmmo;
		return this;
	}

	public DEHAmmo setBackpackAmmo(int backpackAmmo)
	{
		this.backpackAmmo = backpackAmmo;
		return this;
	}

	public DEHAmmo setWeaponAmmo(int weaponAmmo)
	{
		this.weaponAmmo = weaponAmmo;
		return this;
	}

	public DEHAmmo setDroppedAmmo(int droppedAmmo)
	{
		this.droppedAmmo = droppedAmmo;
		return this;
	}

	public DEHAmmo setDroppedBoxAmmo(int droppedBoxAmmo)
	{
		this.droppedBoxAmmo = droppedBoxAmmo;
		return this;
	}

	public DEHAmmo setDroppedBackpackAmmo(int droppedBackpackAmmo) 
	{
		this.droppedBackpackAmmo = droppedBackpackAmmo;
		return this;
	}

	public DEHAmmo setDroppedWeaponAmmo(int droppedWeaponAmmo)
	{
		this.droppedWeaponAmmo = droppedWeaponAmmo;
		return this;
	}

	public DEHAmmo setDeathmatchWeaponAmmo(int deachmatchWeaponAmmo)
	{
		this.deathmatchWeaponAmmo = deachmatchWeaponAmmo;
		return this;
	}

	public DEHAmmo setSkill1Multiplier(int skill1Multiplier)
	{
		this.skill1Multiplier = skill1Multiplier;
		return this;
	}

	public DEHAmmo setSkill2Multiplier(int skill2Multiplier)
	{
		this.skill2Multiplier = skill2Multiplier;
		return this;
	}

	public DEHAmmo setSkill3Multiplier(int skill3Multiplier)
	{
		this.skill3Multiplier = skill3Multiplier;
		return this;
	}

	public DEHAmmo setSkill4Multiplier(int skill4Multiplier)
	{
		this.skill4Multiplier = skill4Multiplier;
		return this;
	}

	public DEHAmmo setSkill5Multiplier(int skill5Multiplier)
	{
		this.skill5Multiplier = skill5Multiplier;
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
		return name == obj.name
			&& max == obj.max
			&& pickup == obj.pickup
			// ID24
			&& initialAmmo == obj.initialAmmo
			&& maxUpgradedAmmo == obj.maxUpgradedAmmo
			&& boxAmmo == obj.boxAmmo
			&& backpackAmmo == obj.backpackAmmo
			&& weaponAmmo == obj.weaponAmmo
			&& droppedAmmo == obj.droppedAmmo
			&& droppedBoxAmmo == obj.droppedBoxAmmo
			&& droppedBackpackAmmo == obj.droppedBackpackAmmo
			&& droppedWeaponAmmo == obj.droppedWeaponAmmo
			&& deathmatchWeaponAmmo == obj.deathmatchWeaponAmmo
			&& skill1Multiplier == obj.skill1Multiplier
			&& skill2Multiplier == obj.skill2Multiplier
			&& skill3Multiplier == obj.skill3Multiplier
			&& skill4Multiplier == obj.skill4Multiplier
			&& skill5Multiplier == obj.skill5Multiplier
		;
	}	
		
	@Override
	public void writeObject(Writer writer, DEHAmmo ammo, DEHFeatureLevel level) throws IOException
	{
		if (forceOutput || max != ammo.max)
			writer.append("Max ammo = ").append(String.valueOf(max)).append("\r\n");
		if (forceOutput || pickup != ammo.pickup)
			writer.append("Per ammo = ").append(String.valueOf(pickup)).append("\r\n");
		
		if (level.supports(DEHFeatureLevel.ID24))
		{
			if (forceOutput || initialAmmo!= ammo.initialAmmo)
				writer.append("Initial ammo = ").append(String.valueOf(initialAmmo)).append("\r\n");
			if (forceOutput || maxUpgradedAmmo != ammo.maxUpgradedAmmo)
				writer.append("Max upgraded ammo = ").append(String.valueOf(maxUpgradedAmmo)).append("\r\n");
			if (forceOutput || boxAmmo != ammo.boxAmmo)
				writer.append("Box ammo = ").append(String.valueOf(boxAmmo)).append("\r\n");
			if (forceOutput || backpackAmmo != ammo.backpackAmmo)
				writer.append("Backpack ammo = ").append(String.valueOf(backpackAmmo)).append("\r\n");
			if (forceOutput || weaponAmmo != ammo.weaponAmmo)
				writer.append("Weapon ammo = ").append(String.valueOf(weaponAmmo)).append("\r\n");
			if (forceOutput || droppedAmmo != ammo.droppedAmmo)
				writer.append("Dropped ammo = ").append(String.valueOf(droppedAmmo)).append("\r\n");
			if (forceOutput || droppedBoxAmmo != ammo.droppedBoxAmmo)
				writer.append("Dropped box ammo = ").append(String.valueOf(droppedBoxAmmo)).append("\r\n");
			if (forceOutput || droppedBackpackAmmo != ammo.droppedBackpackAmmo)
				writer.append("Dropped backpack ammo = ").append(String.valueOf(droppedBackpackAmmo)).append("\r\n");
			if (forceOutput || droppedWeaponAmmo != ammo.droppedWeaponAmmo)
				writer.append("Dropped weapon ammo = ").append(String.valueOf(droppedWeaponAmmo)).append("\r\n");
			if (forceOutput || deathmatchWeaponAmmo != ammo.deathmatchWeaponAmmo)
				writer.append("Deathmatch weapon ammo = ").append(String.valueOf(deathmatchWeaponAmmo)).append("\r\n");
			if (forceOutput || skill1Multiplier != ammo.skill1Multiplier)
				writer.append("Skill 1 multiplier = ").append(String.valueOf(skill1Multiplier)).append("\r\n");
			if (forceOutput || skill2Multiplier != ammo.skill2Multiplier)
				writer.append("Skill 2 multiplier = ").append(String.valueOf(skill2Multiplier)).append("\r\n");
			if (forceOutput || skill3Multiplier != ammo.skill3Multiplier)
				writer.append("Skill 3 multiplier = ").append(String.valueOf(skill3Multiplier)).append("\r\n");
			if (forceOutput || skill4Multiplier != ammo.skill4Multiplier)
				writer.append("Skill 4 multiplier = ").append(String.valueOf(skill4Multiplier)).append("\r\n");
			if (forceOutput || skill5Multiplier != ammo.skill5Multiplier)
				writer.append("Skill 5 multiplier = ").append(String.valueOf(skill5Multiplier)).append("\r\n");
		}
		
		writeCustomProperties(writer);
		writer.flush();
	}

}
