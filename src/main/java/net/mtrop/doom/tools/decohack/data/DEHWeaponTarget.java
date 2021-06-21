/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
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
	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 * @return this object.
	 */
	public SELF setAmmoType(Ammo ammoType);
	
	/**
	 * Sets the ammo per shot.
	 * @param ammoPerShot the ammo per shot.
	 * @return this object.
	 */
	public SELF setAmmoPerShot(int ammoPerShot);
	
	/**
	 * Sets weapon flags.
	 * @param flags the weapon flags to set.
	 * @return this object.
	 */
	public SELF setMBF21Flags(int flags);
	
	/**
	 * Sets weapon flags, or'ing them into the current value.
	 * @param bits the weapon flags to set.
	 * @return this object.
	 */
	public SELF addMBF21Flag(int bits);
	
	/**
	 * Clears weapon flags, and-not'ing them into the current value.
	 * @param bits the weapon flags to set.
	 * @return this object.
	 */
	public SELF removeMBF21Flag(int bits);

	/**
	 * Sets the raise frame index.
	 * @param raiseFrameIndex the index.
	 * @return this object.
	 */
	public SELF setRaiseFrameIndex(int raiseFrameIndex);

	/**
	 * Sets the lower frame index.
	 * @param lowerFrameIndex the index.
	 * @return this object.
	 */
	public SELF setLowerFrameIndex(int lowerFrameIndex);
	
	/**
	 * Sets the ready frame index.
	 * @param readyFrameIndex the index.
	 * @return this object.
	 */
	public SELF setReadyFrameIndex(int readyFrameIndex);
	
	/**
	 * Sets the fire frame index.
	 * @param fireFrameIndex the index.
	 * @return this object.
	 */
	public SELF setFireFrameIndex(int fireFrameIndex);
	
	/**
	 * Sets the flash frame index.
	 * @param flashFrameIndex the index.
	 * @return this object.
	 */
	public SELF setFlashFrameIndex(int flashFrameIndex);
	
	public SELF setLabel(String label, int index);

}
