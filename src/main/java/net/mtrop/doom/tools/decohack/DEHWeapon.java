package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

/**
 * A single weapon entry.
 * @author Matthew Tropiano
 * @param <A> the ammo type enum.
 */
public class DEHWeapon<A extends Enum<A>> implements DEHObject
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
		this.flashFrameIndex = flashFrameIndex;
	}
	
	@Override
	public void writeObject(Writer writer) throws IOException 
	{
		writer.append("Ammo type = ").append(String.valueOf(ammoType.ordinal())).append('\n');
		writer.append("Select frame = ").append(String.valueOf(raiseFrameIndex)).append('\n');
		writer.append("Deselect frame = ").append(String.valueOf(lowerFrameIndex)).append('\n');
		writer.append("Bobbing frame = ").append(String.valueOf(readyFrameIndex)).append('\n');
		writer.append("Shooting frame = ").append(String.valueOf(fireFrameIndex)).append('\n');
		writer.append("Firing frame = ").append(String.valueOf(flashFrameIndex)).append('\n');
	}

}
