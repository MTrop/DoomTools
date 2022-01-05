/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
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
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single weapon entry.
 * @author Matthew Tropiano
 */
public class DEHWeapon implements DEHObject<DEHWeapon>, DEHWeaponTarget<DEHWeapon>
{
	public static enum Ammo
	{
		BULLETS,
		SHELLS,
		CELLS,
		ROCKETS,
		UNUSED,
		INFINITE;
		
		public static final Map<Integer, Ammo> VALUES = EnumUtils.createOrdinalMap(Ammo.class);
	}
	
	/** Weapon name. */
	private String name;
	/** Ammo type. */
	private Ammo ammoType;
	/** Ammo per shot. */
	private int ammoPerShot;
	/** Flags. */
	private int mbf21Flags;

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
	 * @param ammo the ammo type.
	 * @param raise the raise frame index
	 * @param lower the lower frame index
	 * @param ready the ready frame index.
	 * @param fire the fire frame index.
	 * @param flash the muzzle flash index.
	 * @param ammoPerShot the ammot per shot, or {@link #DEFAULT_AMMO_PER_SHOT} for internal default.
	 * @param flags weapon flags.
	 * @return a weapon entry.
	 */
	public static DEHWeapon create(String name, Ammo ammo, int raise, int lower, int ready, int fire, int flash, int ammoPerShot, int flags)
	{
		DEHWeapon out = new DEHWeapon(); 
		out.setName(name);
		out.setAmmoType(ammo);
		out.clearLabels();
		out.setRaiseFrameIndex(raise);
		out.setLowerFrameIndex(lower);
		out.setReadyFrameIndex(ready);
		out.setFireFrameIndex(fire);
		out.setFlashFrameIndex(flash);
		out.setAmmoPerShot(ammoPerShot);
		out.setMBF21Flags(flags);
		return out;
	}
	
	@Override
	public DEHWeapon copyFrom(DEHWeapon source) 
	{
		if (source == this)
			return this;
		
		setName(source.name);
		setAmmoType(source.ammoType);
		setAmmoPerShot(source.ammoPerShot);
		
		setMBF21Flags(source.mbf21Flags);
		
		clearLabels();
		for (String label : source.getLabels())
			setLabel(label, source.getLabel(label));
		return this;
	}
	
	@Override
	public DEHWeapon clearProperties()
	{
		setAmmoType(Ammo.BULLETS);
		setAmmoPerShot(DEFAULT_AMMO_PER_SHOT);
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
	public Ammo getAmmoType() 
	{
		return ammoType;
	}
	
	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 * @return this object.
	 */
	public DEHWeapon setAmmoType(Ammo ammoType) 
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
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
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
		RangeUtils.checkRange("Lower frame index", 0, Integer.MAX_VALUE, lowerFrameIndex);
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
		RangeUtils.checkRange("Ready frame index", 0, Integer.MAX_VALUE, readyFrameIndex);
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
		RangeUtils.checkRange("Fire frame index", 0, Integer.MAX_VALUE, fireFrameIndex);
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
		RangeUtils.checkRange("Flash frame index", 0, Integer.MAX_VALUE, flashFrameIndex);
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
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHWeapon)
			return equals((DEHWeapon)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHWeapon obj) 
	{
		return ammoType == obj.ammoType
			&& ammoPerShot == obj.ammoPerShot
			&& mbf21Flags == obj.mbf21Flags
			&& getRaiseFrameIndex() == obj.getRaiseFrameIndex()
			&& getLowerFrameIndex() == obj.getLowerFrameIndex()
			&& getReadyFrameIndex() == obj.getReadyFrameIndex()
			&& getFireFrameIndex() == obj.getFireFrameIndex()
			&& getFlashFrameIndex() == obj.getFlashFrameIndex()
		;
	}	
	
	@Override
	public void writeObject(Writer writer, DEHWeapon weapon, DEHFeatureLevel level) throws IOException 
	{
		if (ammoType != weapon.ammoType)
			writer.append("Ammo type = ").append(String.valueOf(ammoType.ordinal())).append("\r\n");
		
		// These look backwards. They are not.
		if (getRaiseFrameIndex() != weapon.getRaiseFrameIndex())
			writer.append("Deselect frame = ").append(String.valueOf(getRaiseFrameIndex())).append("\r\n");
		if (getLowerFrameIndex() != weapon.getLowerFrameIndex())
			writer.append("Select frame = ").append(String.valueOf(getLowerFrameIndex())).append("\r\n");

		if (getReadyFrameIndex() != weapon.getReadyFrameIndex())
			writer.append("Bobbing frame = ").append(String.valueOf(getReadyFrameIndex())).append("\r\n");
		if (getFireFrameIndex() != weapon.getFireFrameIndex())
			writer.append("Shooting frame = ").append(String.valueOf(getFireFrameIndex())).append("\r\n");
		if (getFlashFrameIndex() != weapon.getFlashFrameIndex())
			writer.append("Firing frame = ").append(String.valueOf(getFlashFrameIndex())).append("\r\n");

		if (ammoPerShot != weapon.ammoPerShot)
			writer.append("Ammo per shot = ").append(String.valueOf(ammoPerShot)).append("\r\n");
		
		if (level.supports(DEHFeatureLevel.MBF21))
		{
			if (mbf21Flags != weapon.mbf21Flags)
				writer.append("MBF21 Bits = ").append(String.valueOf(mbf21Flags)).append("\r\n");
		}

		writer.flush();
	}

}
