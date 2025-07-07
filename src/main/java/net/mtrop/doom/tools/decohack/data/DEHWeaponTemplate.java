/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
	private Integer ammoType;	
	/** State indices (label name to index). */
	private Map<String, Integer> stateIndexMap;

	// MBF21
	
	/** Ammo per shot. */
	private Integer ammoPerShot;
	/** Flags. */
	private Integer mbf21Flags;
	private int addMBF21Flags;
	private int remMBF21Flags;

	// ID24
	
	private Integer slot;
	private Integer slotPriority;
	private Integer switchPriority;
	private Boolean initialOwned;
	private Boolean initialRaised;
	private String carouselIcon;
	private Integer allowSwitchWithOwnedWeapon;
	private Integer noSwitchWithOwnedWeapon;
	private Integer allowSwitchWithOwnedItem;
	private Integer noSwitchWithOwnedItem;

	
	/** Custom properties. */
	private Map<DEHProperty, String> customProperties;
	

	public DEHWeaponTemplate()
	{
		this.ammoType = 0;
		this.ammoPerShot = null;
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		this.mbf21Flags = null;
		this.addMBF21Flags = 0;
		this.remMBF21Flags = 0;

		this.customProperties = new HashMap<>();
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
		
		if (slot != null)
			destination.setSlot(slot);
		if (slotPriority != null)
			destination.setSlot(slot);
		if (switchPriority != null)
			destination.setSwitchPriority(switchPriority);
		if (initialOwned != null)
			destination.setInitialOwned(initialOwned);
		if (initialRaised != null)
			destination.setInitialRaised(initialRaised);
		if (carouselIcon != null)
			destination.setCarouselIcon(carouselIcon);
		if (allowSwitchWithOwnedWeapon != null)
			destination.setAllowSwitchWithOwnedWeapon(allowSwitchWithOwnedWeapon);
		if (noSwitchWithOwnedWeapon != null)
			destination.setNoSwitchWithOwnedWeapon(noSwitchWithOwnedWeapon);
		if (allowSwitchWithOwnedItem != null)
			destination.setAllowSwitchWithOwnedItem(allowSwitchWithOwnedItem);
		if (noSwitchWithOwnedItem != null)
			destination.setNoSwitchWithOwnedItem(noSwitchWithOwnedItem);
		
		for (String label : getLabels())
			destination.setLabel(label, getLabel(label));
		
		for (Map.Entry<DEHProperty, String> property : customProperties.entrySet())
			destination.setCustomPropertyValue(property.getKey(), property.getValue());

		return this;
	}
	
	/**
	 * Sets a custom property value.
	 * @param property the property.
	 * @param value the value.
	 */
	public void setCustomPropertyValue(DEHProperty property, int value)
	{
		setCustomPropertyValue(property, String.valueOf(value));
	}

	@Override
	public void setCustomPropertyValue(DEHProperty property, String value)
	{
		customProperties.put(property, value);
	}

	@Override
	public void clearCustomPropertyValues()
	{
		customProperties.clear();
	}

	@Override
	public DEHWeaponTemplate clearProperties()
	{
		setAmmoType(0);

		setAmmoPerShot(DEFAULT_AMMO_PER_SHOT);
		
		setSlot(DEFAULT_SLOT);
		setSlotPriority(DEFAULT_SLOT_PRIORITY);
		setSwitchPriority(DEFAULT_SWITCH_PRIORITY);
		setInitialOwned(DEFAULT_INITIAL_OWNED);
		setInitialRaised(DEFAULT_INITIAL_RAISED);
		setCarouselIcon(DEFAULT_CAROUSEL_ICON);
		setAllowSwitchWithOwnedWeapon(DEFAULT_ALLOW_SWITCH_WITH_OWNED_WEAPON);
		setNoSwitchWithOwnedWeapon(DEFAULT_NO_SWITCH_WITH_OWNED_WEAPON);
		setAllowSwitchWithOwnedItem(DEFAULT_ALLOW_SWITCH_WITH_OWNED_ITEM);
		setNoSwitchWithOwnedItem(DEFAULT_NO_SWITCH_WITH_OWNED_ITEM);
		
		clearCustomPropertyValues();
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
	public DEHWeaponTemplate setAmmoType(int ammoType) 
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
		// 0 is a valid index for applying to weapons - preserve in map.
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

	@Override
	public DEHWeaponTemplate setSlot(int slot)
	{
		this.slot = slot; 
		return this;
	}

	@Override
	public DEHWeaponTemplate setSlotPriority(int priority) 
	{
		this.slotPriority = priority;
		return this;
	}

	@Override
	public DEHWeaponTemplate setSwitchPriority(int priority) 
	{
		this.switchPriority = priority;
		return this;
	}

	@Override
	public DEHWeaponTemplate setInitialOwned(boolean owned)
	{
		this.initialOwned = owned;
		return this;
	}

	@Override
	public DEHWeaponTemplate setInitialRaised(boolean raised) 
	{
		this.initialRaised = raised;
		return this;
	}

	@Override
	public DEHWeaponTemplate setCarouselIcon(String icon) 
	{
		this.carouselIcon = icon;
		return this;
	}

	@Override
	public DEHWeaponTemplate setAllowSwitchWithOwnedWeapon(int weaponId) 
	{
		this.allowSwitchWithOwnedWeapon = weaponId;
		return this;
	}

	@Override
	public DEHWeaponTemplate setNoSwitchWithOwnedWeapon(int weaponId) 
	{
		this.noSwitchWithOwnedWeapon = weaponId;
		return this;
	}

	@Override
	public DEHWeaponTemplate setAllowSwitchWithOwnedItem(int itemId) 
	{
		this.allowSwitchWithOwnedItem = itemId;
		return this;
	}

	@Override
	public DEHWeaponTemplate setNoSwitchWithOwnedItem(int itemId) 
	{
		this.noSwitchWithOwnedItem = itemId;
		return this;
	}

}
