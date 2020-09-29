package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single weapon entry.
 * @author Matthew Tropiano
 * @param <A> the ammo type enum.
 */
public class DEHWeapon<A extends Enum<A>> implements DEHObject<DEHWeapon<A>>
{
	/** Weapon name. */
	private String name;
	
	/** Ammo type. */
	private A ammoType;
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
	
	public DEHWeapon(String name) 
	{
		this.name = name;
		
		this.ammoType = null;
		this.raiseFrameIndex = 0;
		this.lowerFrameIndex = 0;
		this.readyFrameIndex = 0;
		this.fireFrameIndex = 0;
		this.flashFrameIndex = 0;
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
	 */
	public void setName(String name) 
	{
		this.name = name;
	}
	
	/**
	 * @return the ammo type.
	 */
	public A getAmmoType() 
	{
		return ammoType;
	}
	
	/**
	 * Sets the ammo type.
	 * @param ammoType the type.
	 */
	public void setAmmoType(A ammoType) 
	{
		this.ammoType = ammoType;
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
	 */
	public void setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		this.raiseFrameIndex = raiseFrameIndex;
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
	 */
	public void setLowerFrameIndex(int lowerFrameIndex) 
	{
		RangeUtils.checkRange("Lower frame index", 0, Integer.MAX_VALUE, lowerFrameIndex);
		this.lowerFrameIndex = lowerFrameIndex;
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
	 */
	public void setReadyFrameIndex(int readyFrameIndex) 
	{
		RangeUtils.checkRange("Ready frame index", 0, Integer.MAX_VALUE, readyFrameIndex);
		this.readyFrameIndex = readyFrameIndex;
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
	 */
	public void setFireFrameIndex(int fireFrameIndex) 
	{
		RangeUtils.checkRange("Fire frame index", 0, Integer.MAX_VALUE, fireFrameIndex);
		this.fireFrameIndex = fireFrameIndex;
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
	 */
	public void setFlashFrameIndex(int flashFrameIndex) 
	{
		RangeUtils.checkRange("Flash frame index", 0, Integer.MAX_VALUE, flashFrameIndex);
		this.flashFrameIndex = flashFrameIndex;
	}
	
	@Override
	public void writeObject(Writer writer, DEHWeapon<A> weapon) throws IOException 
	{
		if (ammoType != weapon.ammoType)
			writer.append("Ammo type = ").append(String.valueOf(ammoType.ordinal())).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Select frame = ").append(String.valueOf(raiseFrameIndex)).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Deselect frame = ").append(String.valueOf(lowerFrameIndex)).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Bobbing frame = ").append(String.valueOf(readyFrameIndex)).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Shooting frame = ").append(String.valueOf(fireFrameIndex)).append('\n');
		if (raiseFrameIndex != weapon.raiseFrameIndex)
			writer.append("Firing frame = ").append(String.valueOf(flashFrameIndex)).append('\n');
		writer.flush();
	}

}
