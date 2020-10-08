package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single weapon entry.
 * @author Matthew Tropiano
 */
public class DEHWeapon implements DEHObject<DEHWeapon>
{
	public static enum Ammo
	{
		BULLETS,
		SHELLS,
		CELLS,
		ROCKETS,
		UNUSED,
		INFINITE;
	}
	
	/** Weapon name. */
	private String name;
	
	/** Ammo type. */
	private Ammo ammoType;
	/** Raise frame index. */
	private int raiseFrameIndex;
	/** Lower frame index. */
	private int lowerFrameIndex;
	/** Ready frame index. */
	private int readyFrameIndex;
	/** Fire frame index. */
	private int fireFrameIndex;
	/** Muzzle flash index. */
	private int flashFrameIndex;
	
	public DEHWeapon()
	{
		setName("");
		setAmmoType(null);
		setRaiseFrameIndex(0);
		setLowerFrameIndex(0);
		setReadyFrameIndex(0);
		setFireFrameIndex(0);
		setFlashFrameIndex(0);
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
	 * @return a weapon entry.
	 */
	public static DEHWeapon create(String name, Ammo ammo, int raise, int lower, int ready, int fire, int flash)
	{
		DEHWeapon out = new DEHWeapon(); 
		out.setName(name);
		out.setAmmoType(ammo);
		out.setRaiseFrameIndex(raise);
		out.setLowerFrameIndex(lower);
		out.setReadyFrameIndex(ready);
		out.setFireFrameIndex(fire);
		out.setFlashFrameIndex(flash);
		return out;
	}
	
	@Override
	public DEHWeapon copyFrom(DEHWeapon source) 
	{
		setName(source.name);
		setAmmoType(source.ammoType);
		setRaiseFrameIndex(source.raiseFrameIndex);
		setLowerFrameIndex(source.lowerFrameIndex);
		setReadyFrameIndex(source.readyFrameIndex);
		setFireFrameIndex(source.fireFrameIndex);
		setFlashFrameIndex(source.flashFrameIndex);
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
	 * @return the raise frame index.
	 */
	public int getRaiseFrameIndex() 
	{
		return raiseFrameIndex;
	}
	
	/**
	 * Sets the raise frame index.
	 * @param raiseFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		this.raiseFrameIndex = raiseFrameIndex;
		return this;
	}
	
	/**
	 * @return the lower frame index.
	 */
	public int getLowerFrameIndex() 
	{
		return lowerFrameIndex;
	}
	
	/**
	 * Sets the lower frame index.
	 * @param lowerFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setLowerFrameIndex(int lowerFrameIndex) 
	{
		RangeUtils.checkRange("Lower frame index", 0, Integer.MAX_VALUE, lowerFrameIndex);
		this.lowerFrameIndex = lowerFrameIndex;
		return this;
	}
	
	/**
	 * @return the ready frame index.
	 */
	public int getReadyFrameIndex() 
	{
		return readyFrameIndex;
	}
	
	/**
	 * Sets the ready frame index.
	 * @param readyFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setReadyFrameIndex(int readyFrameIndex) 
	{
		RangeUtils.checkRange("Ready frame index", 0, Integer.MAX_VALUE, readyFrameIndex);
		this.readyFrameIndex = readyFrameIndex;
		return this;
	}
	
	/**
	 * @return the fire frame index.
	 */
	public int getFireFrameIndex()
	{
		return fireFrameIndex;
	}
	
	/**
	 * Sets the fire frame index.
	 * @param fireFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setFireFrameIndex(int fireFrameIndex) 
	{
		RangeUtils.checkRange("Fire frame index", 0, Integer.MAX_VALUE, fireFrameIndex);
		this.fireFrameIndex = fireFrameIndex;
		return this;
	}
	
	/**
	 * @return the flash frame index.
	 */
	public int getFlashFrameIndex()
	{
		return flashFrameIndex;
	}
	
	/**
	 * Sets the flash frame index.
	 * @param flashFrameIndex the index.
	 * @return this object.
	 */
	public DEHWeapon setFlashFrameIndex(int flashFrameIndex) 
	{
		RangeUtils.checkRange("Flash frame index", 0, Integer.MAX_VALUE, flashFrameIndex);
		this.flashFrameIndex = flashFrameIndex;
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
			&& raiseFrameIndex == obj.raiseFrameIndex
			&& lowerFrameIndex == obj.lowerFrameIndex
			&& readyFrameIndex == obj.readyFrameIndex
			&& fireFrameIndex == obj.fireFrameIndex
			&& flashFrameIndex == obj.flashFrameIndex
		;
	}	
	
	@Override
	public void writeObject(Writer writer, DEHWeapon weapon) throws IOException 
	{
		if (ammoType != weapon.ammoType)
			writer.append("Ammo type = ").append(String.valueOf(ammoType.ordinal())).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Select frame = ").append(String.valueOf(raiseFrameIndex)).append('\n');
		if (lowerFrameIndex != weapon.lowerFrameIndex)
			writer.append("Deselect frame = ").append(String.valueOf(lowerFrameIndex)).append('\n');
		if (readyFrameIndex != weapon.readyFrameIndex)
			writer.append("Bobbing frame = ").append(String.valueOf(readyFrameIndex)).append('\n');
		if (fireFrameIndex != weapon.fireFrameIndex)
			writer.append("Shooting frame = ").append(String.valueOf(fireFrameIndex)).append('\n');
		if (flashFrameIndex != weapon.flashFrameIndex)
			writer.append("Firing frame = ").append(String.valueOf(flashFrameIndex)).append('\n');
		writer.flush();
	}

}
