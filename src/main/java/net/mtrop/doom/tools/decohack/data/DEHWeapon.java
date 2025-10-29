/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * A single weapon entry.
 * @author Matthew Tropiano
 */
public class DEHWeapon extends DEHObject<DEHWeapon> implements DEHWeaponTarget<DEHWeapon>
{
	/** Weapon name. */
	private String name;
	/** Ammo type. */
	private int ammoType;

	// MBF21
	/** Ammo per shot. */
	private int ammoPerShot;
	/** Flags. */
	private int mbf21Flags;

	// ID24
	private int slot;
	private int slotPriority;
	private int switchPriority;
	private boolean initialOwned;
	private boolean initialRaised;
	private String carouselIcon;
	private int allowSwitchWithOwnedWeapon;
	private int noSwitchWithOwnedWeapon;
	private int allowSwitchWithOwnedItem;
	private int noSwitchWithOwnedItem;
	
	/** State indices (label name to index). */
	private Map<String, Integer> stateIndexMap;
	
	public DEHWeapon()
	{
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		setName("");
		clearProperties();
		clearFlags();
		clearLabels();
	}
	
	/**
	 * Creates a weapon entry.
	 * @param name the name.
	 * @param ammoType the ammo type.
	 * @param raise the raise frame index
	 * @param lower the lower frame index
	 * @param ready the ready frame index.
	 * @param fire the fire frame index.
	 * @param flash the muzzle flash index.
	 * @param ammoPerShot the ammot per shot, or {@link #DEFAULT_AMMO_PER_SHOT} for internal default.
	 * @param flags weapon flags.
	 * @return a weapon entry.
	 */
	public static DEHWeapon create(String name, int ammoType, int raise, int lower, int ready, int fire, int flash, int ammoPerShot, int flags)
	{
		DEHWeapon out = new DEHWeapon(); 
		out.setName(name);
		out.setAmmoType(ammoType);
		out.clearLabels();
		out.setRaiseFrameIndex(raise);
		out.setLowerFrameIndex(lower);
		out.setReadyFrameIndex(ready);
		out.setFireFrameIndex(fire);
		out.setFlashFrameIndex(flash);

		out.setAmmoPerShot(ammoPerShot);
		out.setMBF21Flags(flags);
		
		out.setSlot(DEFAULT_SLOT);
		out.setSlotPriority(DEFAULT_SLOT_PRIORITY);
		out.setSwitchPriority(DEFAULT_SWITCH_PRIORITY);
		out.setInitialOwned(DEFAULT_INITIAL_OWNED);
		out.setInitialRaised(DEFAULT_INITIAL_RAISED);
		out.setCarouselIcon(DEFAULT_CAROUSEL_ICON);
		out.setAllowSwitchWithOwnedWeapon(DEFAULT_ALLOW_SWITCH_WITH_OWNED_WEAPON);
		out.setNoSwitchWithOwnedWeapon(DEFAULT_NO_SWITCH_WITH_OWNED_WEAPON);
		out.setAllowSwitchWithOwnedItem(DEFAULT_ALLOW_SWITCH_WITH_OWNED_ITEM);
		out.setNoSwitchWithOwnedItem(DEFAULT_NO_SWITCH_WITH_OWNED_ITEM);
		
		return out;
	}
	
	@Override
	public DEHWeapon copyFrom(DEHWeapon source) 
	{
		if (source == this)
			return this;
		
		setName(source.name);
		setAmmoType(source.ammoType);
		clearLabels();
		for (String label : source.getLabels())
			setLabel(label, source.getLabel(label));

		clearCustomPropertyValues();
		for (Map.Entry<DEHProperty, String> entry : source.getCustomPropertySet())
			setCustomPropertyValue(entry.getKey(), entry.getValue());

		setAmmoPerShot(source.ammoPerShot);
		setMBF21Flags(source.mbf21Flags);
		
		setSlot(source.slot);
		setSlotPriority(source.slotPriority);
		setSwitchPriority(source.switchPriority);
		setInitialOwned(source.initialOwned);
		setInitialRaised(source.initialRaised);
		setCarouselIcon(source.carouselIcon);
		setAllowSwitchWithOwnedWeapon(source.allowSwitchWithOwnedWeapon);
		setNoSwitchWithOwnedWeapon(source.noSwitchWithOwnedWeapon);
		setAllowSwitchWithOwnedItem(source.allowSwitchWithOwnedItem);
		setNoSwitchWithOwnedItem(source.noSwitchWithOwnedItem);
		
		return this;
	}
	
	@Override
	public DEHWeapon clearProperties()
	{
		setAmmoType(-1);
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
	public DEHWeapon clearFlags() 
	{
		setMBF21Flags(0x00000000);
		return this;
	}

	@Override
	public DEHWeapon clearLabels()
	{
		stateIndexMap.clear();
		setLabel(STATE_LABEL_LIGHTDONE, 1);
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
	public DEHWeapon setName(String name) 
	{
		this.name = name;
		return this;
	}
	
	/**
	 * @return the ammo type.
	 */
	public int getAmmoType() 
	{
		return ammoType;
	}
	
	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 * @return this object.
	 */
	public DEHWeapon setAmmoType(int ammoType) 
	{
		this.ammoType = ammoType;
		return this;
	}
	
	/**
	 * @return the ammo per shot.
	 */
	public int getAmmoPerShot() 
	{
		return ammoPerShot;
	}
	
	/**
	 * Sets the ammo per shot.
	 * @param ammoPerShot the ammo per shot.
	 * @return this object.
	 */
	public DEHWeapon setAmmoPerShot(int ammoPerShot) 
	{
		this.ammoPerShot = ammoPerShot;
		return this;
	}
	
	/**
	 * @return this weapon's flags.
	 */
	public int getMBF21Flags() 
	{
		return mbf21Flags;
	}
	
	/**
	 * Sets weapon flags.
	 * @param flags the weapon flags to set.
	 * @return this object.
	 */
	public DEHWeapon setMBF21Flags(int flags)
	{
		this.mbf21Flags = flags;
		return this;
	}
	
	@Override
	public DEHWeapon addMBF21Flag(int bits)
	{
		this.mbf21Flags |= bits;
		return this;
	}

	@Override
	public DEHWeapon removeMBF21Flag(int bits)
	{
		this.mbf21Flags &= ~bits;
		return this;
	}

	/**
	 * @return the raise frame index.
	 */
	public int getRaiseFrameIndex() 
	{
		return getLabel(STATE_LABEL_SELECT);
	}
	
	/**
	 * Sets the raise frame index.
	 * @param raiseFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setRaiseFrameIndex(int raiseFrameIndex) 
	{
		setLabel(STATE_LABEL_SELECT, raiseFrameIndex);
		return this;
	}

	/**
	 * @return the lower frame index.
	 */
	public int getLowerFrameIndex() 
	{
		return getLabel(STATE_LABEL_DESELECT);
	}
	
	/**
	 * Sets the lower frame index.
	 * @param lowerFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setLowerFrameIndex(int lowerFrameIndex) 
	{
		setLabel(STATE_LABEL_DESELECT, lowerFrameIndex);
		return this;
	}
	
	/**
	 * @return the ready frame index.
	 */
	public int getReadyFrameIndex() 
	{
		return getLabel(STATE_LABEL_READY);
	}
	
	/**
	 * Sets the ready frame index.
	 * @param readyFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setReadyFrameIndex(int readyFrameIndex) 
	{
		setLabel(STATE_LABEL_READY, readyFrameIndex);
		return this;
	}
	
	/**
	 * @return the fire frame index.
	 */
	public int getFireFrameIndex()
	{
		return getLabel(STATE_LABEL_FIRE);
	}
	
	/**
	 * Sets the fire frame index.
	 * @param fireFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setFireFrameIndex(int fireFrameIndex) 
	{
		setLabel(STATE_LABEL_FIRE, fireFrameIndex);
		return this;
	}
	
	/**
	 * @return the flash frame index.
	 */
	public int getFlashFrameIndex()
	{
		return getLabel(STATE_LABEL_FLASH);
	}
	
	/**
	 * Sets the flash frame index.
	 * @param flashFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setFlashFrameIndex(int flashFrameIndex) 
	{
		setLabel(STATE_LABEL_FLASH, flashFrameIndex);
		return this;
	}
	
	@Override
	public String[] getLabels()
	{
		Set<String> labelSet = stateIndexMap.keySet();
		return labelSet.toArray(new String[labelSet.size()]);
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
	public DEHWeapon setLabel(String label, int index)
	{
		if (index == 0)
			stateIndexMap.remove(label);
		else
			stateIndexMap.put(label, index);
		return this;
	}

	@Override
	public DEHWeapon setSlot(int slot)
	{
		this.slot = slot; 
		return this;
	}

	@Override
	public DEHWeapon setSlotPriority(int priority) 
	{
		this.slotPriority = priority;
		return this;
	}

	@Override
	public DEHWeapon setSwitchPriority(int priority) 
	{
		this.switchPriority = priority;
		return this;
	}

	@Override
	public DEHWeapon setInitialOwned(boolean owned)
	{
		this.initialOwned = owned;
		return this;
	}

	@Override
	public DEHWeapon setInitialRaised(boolean raised) 
	{
		this.initialRaised = raised;
		return this;
	}

	@Override
	public DEHWeapon setCarouselIcon(String icon) 
	{
		this.carouselIcon = icon;
		return this;
	}

	@Override
	public DEHWeapon setAllowSwitchWithOwnedWeapon(int weaponId) 
	{
		this.allowSwitchWithOwnedWeapon = weaponId;
		return this;
	}

	@Override
	public DEHWeapon setNoSwitchWithOwnedWeapon(int weaponId) 
	{
		this.noSwitchWithOwnedWeapon = weaponId;
		return this;
	}

	@Override
	public DEHWeapon setAllowSwitchWithOwnedItem(int itemId) 
	{
		this.allowSwitchWithOwnedItem = itemId;
		return this;
	}

	@Override
	public DEHWeapon setNoSwitchWithOwnedItem(int itemId) 
	{
		this.noSwitchWithOwnedItem = itemId;
		return this;
	}

	public int getSlot() 
	{
		return slot;
	}

	public int getSlotPriority() 
	{
		return slotPriority;
	}

	public int getSwitchPriority()
	{
		return switchPriority;
	}

	public boolean isInitialOwned()
	{
		return initialOwned;
	}

	public boolean isInitialRaised() 
	{
		return initialRaised;
	}

	public String getCarouselIcon() 
	{
		return carouselIcon;
	}

	public int getAllowSwitchWithOwnedWeapon()
	{
		return allowSwitchWithOwnedWeapon;
	}

	public int getNoSwitchWithOwnedWeapon() 
	{
		return noSwitchWithOwnedWeapon;
	}

	public int getAllowSwitchWithOwnedItem() 
	{
		return allowSwitchWithOwnedItem;
	}

	public int getNoSwitchWithOwnedItem() 
	{
		return noSwitchWithOwnedItem;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHWeapon)
			return equals((DEHWeapon)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHWeapon obj) 
	{
		return name == obj.name
			&& ammoType == obj.ammoType
			&& getRaiseFrameIndex() == obj.getRaiseFrameIndex()
			&& getLowerFrameIndex() == obj.getLowerFrameIndex()
			&& getReadyFrameIndex() == obj.getReadyFrameIndex()
			&& getFireFrameIndex() == obj.getFireFrameIndex()
			&& getFlashFrameIndex() == obj.getFlashFrameIndex()
			// MBF21
			&& ammoPerShot == obj.ammoPerShot
			&& mbf21Flags == obj.mbf21Flags
			// ID24
			&& slot == obj.slot
			&& slotPriority == obj.slotPriority
			&& switchPriority == obj.switchPriority
			&& initialOwned == obj.initialOwned
			&& initialRaised == obj.initialRaised
			&& ObjectUtils.areEqual(carouselIcon, obj.carouselIcon)
			&& allowSwitchWithOwnedWeapon == obj.allowSwitchWithOwnedWeapon
			&& noSwitchWithOwnedWeapon == obj.noSwitchWithOwnedWeapon
			&& allowSwitchWithOwnedItem == obj.allowSwitchWithOwnedItem
			&& noSwitchWithOwnedItem == obj.noSwitchWithOwnedItem
		;
	}	
	
	@Override
	public void writeObject(Writer writer, DEHWeapon weapon, DEHFeatureLevel level) throws IOException 
	{
		if (forceOutput || ammoType != weapon.ammoType)
			writer.append("Ammo type = ").append(String.valueOf(ammoType)).append("\r\n");
		
		// These look backwards. They are not.
		if (forceOutput || getRaiseFrameIndex() != weapon.getRaiseFrameIndex())
			writer.append("Deselect frame = ").append(String.valueOf(getRaiseFrameIndex())).append("\r\n");
		if (forceOutput || getLowerFrameIndex() != weapon.getLowerFrameIndex())
			writer.append("Select frame = ").append(String.valueOf(getLowerFrameIndex())).append("\r\n");

		if (forceOutput || getReadyFrameIndex() != weapon.getReadyFrameIndex())
			writer.append("Bobbing frame = ").append(String.valueOf(getReadyFrameIndex())).append("\r\n");
		if (forceOutput || getFireFrameIndex() != weapon.getFireFrameIndex())
			writer.append("Shooting frame = ").append(String.valueOf(getFireFrameIndex())).append("\r\n");
		if (forceOutput || getFlashFrameIndex() != weapon.getFlashFrameIndex())
			writer.append("Firing frame = ").append(String.valueOf(getFlashFrameIndex())).append("\r\n");

		if (level.supports(DEHFeatureLevel.MBF21))
		{
			if (forceOutput || ammoPerShot != weapon.ammoPerShot)
				writer.append("Ammo per shot = ").append(String.valueOf(ammoPerShot)).append("\r\n");
			
			if (forceOutput || mbf21Flags != weapon.mbf21Flags)
				writer.append("MBF21 Bits = ").append(String.valueOf(mbf21Flags)).append("\r\n");
		}
		
		if (level.supports(DEHFeatureLevel.ID24))
		{
			if (forceOutput || slot != weapon.slot)
				writer.append("Slot = ").append(String.valueOf(slot)).append("\r\n");
			if (forceOutput || slotPriority != weapon.slotPriority)
				writer.append("Slot Priority = ").append(String.valueOf(slotPriority)).append("\r\n");
			if (forceOutput || switchPriority != weapon.switchPriority)
				writer.append("Switch Priority = ").append(String.valueOf(switchPriority)).append("\r\n");
			if (forceOutput || initialOwned != weapon.initialOwned)
				writer.append("Initial Owned = ").append(String.valueOf(initialOwned)).append("\r\n");
			if (forceOutput || initialRaised != weapon.initialRaised)
				writer.append("Initial Raised = ").append(String.valueOf(initialRaised)).append("\r\n");
			if (forceOutput || !carouselIcon.equalsIgnoreCase(weapon.carouselIcon))
				writer.append("Carousel icon = ").append(carouselIcon).append("\r\n");
			if (forceOutput || allowSwitchWithOwnedWeapon != weapon.allowSwitchWithOwnedWeapon)
				writer.append("Allow switch with owned weapon = ").append(String.valueOf(allowSwitchWithOwnedWeapon)).append("\r\n");
			if (forceOutput || noSwitchWithOwnedWeapon != weapon.noSwitchWithOwnedWeapon)
				writer.append("No switch with owned weapon = ").append(String.valueOf(noSwitchWithOwnedWeapon)).append("\r\n");
			if (forceOutput || allowSwitchWithOwnedItem != weapon.allowSwitchWithOwnedItem)
				writer.append("Allow switch with owned item = ").append(String.valueOf(allowSwitchWithOwnedItem)).append("\r\n");
			if (forceOutput || noSwitchWithOwnedItem != weapon.noSwitchWithOwnedItem)
				writer.append("No switch with owned item = ").append(String.valueOf(noSwitchWithOwnedItem)).append("\r\n");
		}
		
		writeCustomProperties(writer);
		writer.flush();
	}

}
