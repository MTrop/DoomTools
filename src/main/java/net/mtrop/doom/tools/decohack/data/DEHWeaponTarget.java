/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * An interface that describes a Weapon whose information can be set.
 * @author Matthew Tropiano
 * @param <SELF> This object type.
 */
public interface DEHWeaponTarget<SELF extends DEHWeaponTarget<SELF>> extends DEHActor<DEHWeaponTarget<SELF>>
{
	static final String STATE_LABEL_READY = "ready";
	static final String STATE_LABEL_SELECT = "select";
	static final String STATE_LABEL_DESELECT = "deselect";
	static final String STATE_LABEL_FIRE = "fire";
	static final String STATE_LABEL_FLASH = "flash";
	static final String STATE_LABEL_LIGHTDONE = "lightdone";

	static final int DEFAULT_AMMO_PER_SHOT = -1;
	
	static final int DEFAULT_SLOT = -1;
	static final int DEFAULT_SLOT_PRIORITY = -1;
	static final int DEFAULT_SWITCH_PRIORITY = -1;
	static final boolean DEFAULT_INITIAL_OWNED = false;
	static final boolean DEFAULT_INITIAL_RAISED = false;
	static final String DEFAULT_CAROUSEL_ICON = "SMUNKN";
	static final int DEFAULT_ALLOW_SWITCH_WITH_OWNED_WEAPON = -1;
	static final int DEFAULT_NO_SWITCH_WITH_OWNED_WEAPON = -1;
	static final int DEFAULT_ALLOW_SWITCH_WITH_OWNED_ITEM = -1;
	static final int DEFAULT_NO_SWITCH_WITH_OWNED_ITEM = -1;

	/**
	 * Clears the properties.
	 * @return this object.
	 */
	SELF clearProperties();
	
	/**
	 * Clears the sounds.
	 * @return this object.
	 */
	SELF clearFlags();

	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 * @return this object.
	 */
	SELF setAmmoType(int ammoType);
	
	/**
	 * Sets the ammo per shot.
	 * @param ammoPerShot the ammo per shot.
	 * @return this object.
	 */
	SELF setAmmoPerShot(int ammoPerShot);
	
	/**
	 * Sets weapon flags.
	 * @param flags the weapon flags to set.
	 * @return this object.
	 */
	SELF setMBF21Flags(int flags);
	
	/**
	 * Sets weapon flags, or'ing them into the current value.
	 * @param bits the weapon flags to set.
	 * @return this object.
	 */
	SELF addMBF21Flag(int bits);
	
	/**
	 * Clears weapon flags, and-not'ing them into the current value.
	 * @param bits the weapon flags to set.
	 * @return this object.
	 */
	SELF removeMBF21Flag(int bits);

	/**
	 * Sets the slot binding for this weapon.
	 * @param slot the new slot.
	 * @return this object.
	 */
	SELF setSlot(int slot);
	
	/**
	 * Sets the slot priority for this weapon.
	 * @param priority the new slot priority.
	 * @return this object.
	 */
	SELF setSlotPriority(int priority);
	
	/**
	 * Sets the switch priority for this weapon.
	 * @param priority the new switch priority.
	 * @return this object.
	 */
	SELF setSwitchPriority(int priority);
	
	/**
	 * Sets if this weapon is initially owned.
	 * @param owned true if so, false if not.
	 * @return this object.
	 */
	SELF setInitialOwned(boolean owned);
	
	/**
	 * Sets if this weapon is initially raised on respawn.
	 * @param raised true if so, false if not.
	 * @return this object.
	 */
	SELF setInitialRaised(boolean raised);
	
	/**
	 * Sets this weapon's carousel icon.
	 * @param icon the graphic name of the icon.
	 * @return this object.
	 */
	SELF setCarouselIcon(String icon);
	
	/**
	 * Sets if this weapon, if picked up, is switched to with an owned weapon.
	 * @param weaponId the weapon id.
	 * @return this object.
	 */
	SELF setAllowSwitchWithOwnedWeapon(int weaponId);
	
	/**
	 * Sets if this weapon, if picked up, is never switched to with an owned weapon.
	 * @param weaponId the weapon id.
	 * @return this object.
	 */
	SELF setNoSwitchWithOwnedWeapon(int weaponId);
	
	/**
	 * Sets if this weapon, if picked up, is switched to with an owned item.
	 * @param itemId the item id.
	 * @return this object.
	 */
	SELF setAllowSwitchWithOwnedItem(int itemId);
	
	/**
	 * Sets if this weapon, if picked up, is never switched to with an owned item.
	 * @param itemId the item id.
	 * @return this object.
	 */
	SELF setNoSwitchWithOwnedItem(int itemId);
	
	/**
	 * Sets the raise frame index.
	 * @param raiseFrameIndex the index.
	 * @return this object.
	 */
	SELF setRaiseFrameIndex(int raiseFrameIndex);

	/**
	 * Sets the lower frame index.
	 * @param lowerFrameIndex the index.
	 * @return this object.
	 */
	SELF setLowerFrameIndex(int lowerFrameIndex);
	
	/**
	 * Sets the ready frame index.
	 * @param readyFrameIndex the index.
	 * @return this object.
	 */
	SELF setReadyFrameIndex(int readyFrameIndex);
	
	/**
	 * Sets the fire frame index.
	 * @param fireFrameIndex the index.
	 * @return this object.
	 */
	SELF setFireFrameIndex(int fireFrameIndex);
	
	/**
	 * Sets the flash frame index.
	 * @param flashFrameIndex the index.
	 * @return this object.
	 */
	SELF setFlashFrameIndex(int flashFrameIndex);

	void setCustomPropertyValue(DEHProperty property, String value);

	void clearCustomPropertyValues();
	
}
