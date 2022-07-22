/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;

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
	SELF setAmmoType(Ammo ammoType);
	
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
	
}
