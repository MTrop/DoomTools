/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single weapon entry where all of its fields are nulled out.
 * The purpose of this object is to prepare a weapon where its values 
 * can be applied to many Weapon entries.
 * @author Matthew Tropiano
 */
public class DEHWeaponTemplate implements DEHWeaponTarget<DEHWeaponTemplate>
{
	/** Ammo type. */
	private Ammo ammoType;	
	/** Ammo per shot. */
	private Integer ammoPerShot;
	/** State indices (label name to index). */
	private Map<String, Integer> stateIndexMap;
	/** Flags. */
	private Integer mbf21Flags;
	private int addMBF21Flags;
	private int remMBF21Flags;

	public DEHWeaponTemplate()
	{
		this.ammoType = null;
		this.ammoPerShot = null;
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		this.mbf21Flags = null;
		this.addMBF21Flags = 0;
		this.remMBF21Flags = 0;
	}
	
	/**
	 * Applies all set fields to a weapon.
	 * @param destination the destination weapon.
	 * @return this template.
	 */
	public DEHWeaponTemplate applyTo(DEHWeapon destination) 
	{
		if (ammoType != null)
			destination.setAmmoType(ammoType);
		if (ammoPerShot != null)
			destination.setAmmoPerShot(ammoPerShot);
		// if flags altered, replace.
		if (mbf21Flags != null)
			destination.setMBF21Flags(mbf21Flags);
		// else, just alter the adjustments.
		else
		{
			destination.addMBF21Flag(addMBF21Flags);
			destination.removeMBF21Flag(remMBF21Flags);
		}
		
		for (String label : getLabels())
			destination.setLabel(label, getLabel(label));
		
		return this;
	}
	
	@Override
	public DEHWeaponTemplate clearProperties()
	{
		setAmmoType(Ammo.BULLETS);
		setAmmoPerShot(DEFAULT_AMMO_PER_SHOT);
		return this;
	}

	@Override
	public DEHWeaponTemplate clearFlags() 
	{
		setMBF21Flags(0x00000000);
		return this;
	}

	@Override
	public DEHWeaponTemplate clearLabels()
	{
		stateIndexMap.clear();
		// If a template clears labels, explicitly set state 0.
		setRaiseFrameIndex(0);
		setLowerFrameIndex(0);
		setReadyFrameIndex(0);
		setFireFrameIndex(0);
		setFlashFrameIndex(0);
		return this;
	}

	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 * @return this object.
	 */
	public DEHWeaponTemplate setAmmoType(Ammo ammoType) 
	{
		this.ammoType = ammoType;
		return this;
	}
	
	/**
	 * Sets the ammo per shot.
	 * @param ammoPerShot the ammo per shot.
	 * @return this object.
	 */
	public DEHWeaponTemplate setAmmoPerShot(int ammoPerShot) 
	{
		this.ammoPerShot = ammoPerShot;
		return this;
	}
	
	/**
	 * Sets weapon flags.
	 * @param bits the weapon flags to set.
	 * @return this object.
	 */
	public DEHWeaponTemplate setMBF21Flags(int bits) 
	{
		this.mbf21Flags = bits;
		return this;
	}

	@Override
	public DEHWeaponTemplate addMBF21Flag(int bits)
	{
		if (this.mbf21Flags != null)
			this.mbf21Flags |= bits;
		else
			this.addMBF21Flags |= bits; // added later
		return this;
	}

	@Override
	public DEHWeaponTemplate removeMBF21Flag(int bits)
	{
		if (this.mbf21Flags != null)
			this.mbf21Flags &= ~bits;
		else
			this.remMBF21Flags |= bits; // removed later
		return this;
	}

	/**
	 * Sets the raise frame index.
	 * @param raiseFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeaponTemplate setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		setLabel(DEHWeapon.STATE_LABEL_SELECT, raiseFrameIndex);
		return this;
	}

	/**
	 * Sets the lower frame index.
	 * @param lowerFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeaponTemplate setLowerFrameIndex(int lowerFrameIndex) 
	{
		RangeUtils.checkRange("Lower frame index", 0, Integer.MAX_VALUE, lowerFrameIndex);
		setLabel(DEHWeapon.STATE_LABEL_DESELECT, lowerFrameIndex);
		return this;
	}
	
	/**
	 * Sets the ready frame index.
	 * @param readyFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeaponTemplate setReadyFrameIndex(int readyFrameIndex) 
	{
		RangeUtils.checkRange("Ready frame index", 0, Integer.MAX_VALUE, readyFrameIndex);
		setLabel(DEHWeapon.STATE_LABEL_READY, readyFrameIndex);
		return this;
	}
	
	/**
	 * Sets the fire frame index.
	 * @param fireFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeaponTemplate setFireFrameIndex(int fireFrameIndex) 
	{
		RangeUtils.checkRange("Fire frame index", 0, Integer.MAX_VALUE, fireFrameIndex);
		setLabel(DEHWeapon.STATE_LABEL_FIRE, fireFrameIndex);
		return this;
	}
	
	/**
	 * Sets the flash frame index.
	 * @param flashFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeaponTemplate setFlashFrameIndex(int flashFrameIndex) 
	{
		RangeUtils.checkRange("Flash frame index", 0, Integer.MAX_VALUE, flashFrameIndex);
		setLabel(DEHWeapon.STATE_LABEL_FLASH, flashFrameIndex);
		return this;
	}
	
	public DEHWeaponTemplate setLabel(String label, int index)
	{
		if (index == 0)
			stateIndexMap.remove(label);
		else
			stateIndexMap.put(label, index);
		return this;
	}

	@Override
	public String[] getLabels()
	{
		return stateIndexMap.keySet().toArray(new String[stateIndexMap.size()]);
	}

	@Override
	public boolean hasLabel(String label)
	{
		return stateIndexMap.containsKey(label);
	}

	@Override
	public int getLabel(String label)
	{
		return stateIndexMap.getOrDefault(label, 0);
	}

}
