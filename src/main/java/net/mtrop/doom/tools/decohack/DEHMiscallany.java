package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * All other miscellaneous DeHackEd stuff.
 * @author Matthew Tropiano
 */
public class DEHMiscallany implements DEHObject<DEHMiscallany>
{
	private boolean monsterInfightingEnabled;
	private int initialBullets;
	private int initialHealth;
	private int greenArmorClass;
	private int blueArmorClass;
	private int soulsphereHealth;
	private int maxSoulsphereHealth;
	private int megasphereHealth;
	private int godModeHealth;
	private int idfaArmor;
	private int idfaArmorClass;
	private int idkfaArmor;
	private int idkfaArmorClass;
	private int bfgCellsPerShot;
	private int maxHealth;
	private int maxArmor;
	
	/**
	 * Creates a new DEHMiscellany with default values.
	 */
	public DEHMiscallany()
	{
		setMonsterInfightingEnabled(false);
		setInitialBullets(50);
		setInitialHealth(100);
		setGreenArmorClass(1);
		setBlueArmorClass(2);
		setSoulsphereHealth(100);
		setMaxSoulsphereHealth(200);
		setGodModeHealth(100);
		setBFGCellsPerShot(40);
		setIDFAArmor(200);
		setIDFAArmorClass(2);
		setIDKFAArmor(200);
		setIDKFAArmorClass(2);
		setMaxArmor(200);
		setMaxHealth(200);
	}
	
	/**
	 * Gets if monster infighting is enabled.
	 * @return true if so, false if not.
	 */
	public boolean isMonsterInfightingEnabled() 
	{
		return monsterInfightingEnabled;
	}
	
	/**
	 * Sets if monster infighting is enabled.
	 * @param monsterInfightingEnabled true if so, false if not.
	 */
	public void setMonsterInfightingEnabled(boolean monsterInfightingEnabled) 
	{
		this.monsterInfightingEnabled = monsterInfightingEnabled;
	}
	
	/**
	 * @return the initial amount of player bullets.
	 */
	public int getInitialBullets() 
	{
		return initialBullets;
	}

	/**
	 * Sets the initial amount of player bullets.
	 * @param initialBullets the value.
	 * @throws IllegalArgumentException if the value is in an invalid range.
	 */
	public void setInitialBullets(int initialBullets) 
	{
		RangeUtils.checkRange("Initial bullets", 0, Integer.MAX_VALUE, initialBullets);
		this.initialBullets = initialBullets;
	}
	
	/**
	 * @return the initial amount of player health.
	 */
	public int getInitialHealth() 
	{
		return initialHealth;
	}
	
	/**
	 * Sets the initial amount of player health.
	 * @param initialHealth the value.
	 */
	public void setInitialHealth(int initialHealth) 
	{
		RangeUtils.checkRange("Initial health", 0, Integer.MAX_VALUE, initialHealth);
		this.initialHealth = initialHealth;
	}
	
	/**
	 * @return the green armor class.
	 */
	public int getGreenArmorClass() 
	{
		return greenArmorClass;
	}
	
	public void setGreenArmorClass(int greenArmorClass)
	{
		RangeUtils.checkRange("Green armor class", 0, Integer.MAX_VALUE, greenArmorClass);
		this.greenArmorClass = greenArmorClass;
	}
	
	public int getBlueArmorClass() 
	{
		return blueArmorClass;
	}
	
	public void setBlueArmorClass(int blueArmorClass) 
	{
		RangeUtils.checkRange("Blue armor class", 0, Integer.MAX_VALUE, blueArmorClass);
		this.blueArmorClass = blueArmorClass;
	}
	
	public int getSoulsphereHealth()
	{
		return soulsphereHealth;
	}
	
	public void setSoulsphereHealth(int soulsphereHealth)
	{
		RangeUtils.checkRange("Soulsphere health", 0, 255, blueArmorClass);
		this.soulsphereHealth = soulsphereHealth;
	}
	
	public int getMaxSoulsphereHealth() 
	{
		return maxSoulsphereHealth;
	}
	
	public void setMaxSoulsphereHealth(int maxSoulsphereHealth) 
	{
		RangeUtils.checkRange("Max soulsphere health", 0, Integer.MAX_VALUE, maxSoulsphereHealth);
		this.maxSoulsphereHealth = maxSoulsphereHealth;
	}
	
	public int getMegasphereHealth() 
	{
		return megasphereHealth;
	}
	
	public void setMegasphereHealth(int megasphereHealth) 
	{
		RangeUtils.checkRange("Megasphere health", 0, Integer.MAX_VALUE, megasphereHealth);
		this.megasphereHealth = megasphereHealth;
	}
	
	public int getGodModeHealth() 
	{
		return godModeHealth;
	}
	
	public void setGodModeHealth(int godModeHealth) 
	{
		RangeUtils.checkRange("God mode health", 0, Integer.MAX_VALUE, godModeHealth);
		this.godModeHealth = godModeHealth;
	}
	
	public int getBFGCellsPerShot() 
	{
		return bfgCellsPerShot;
	}
	
	public void setBFGCellsPerShot(int bfgCellsPerShot) 
	{
		RangeUtils.checkRange("BFG Cells Per Shot", 0, 255, bfgCellsPerShot);
		this.bfgCellsPerShot = bfgCellsPerShot;
	}
	
	public int getIDFAArmor()
	{
		return idfaArmor;
	}
	
	public void setIDFAArmor(int idfaArmor)
	{
		RangeUtils.checkRange("IDFA Armor", 0, Integer.MAX_VALUE, idfaArmor);
		this.idfaArmor = idfaArmor;
	}
	
	public int getIDFAArmorClass() 
	{
		return idfaArmorClass;
	}
	
	public void setIDFAArmorClass(int idfaArmorClass) 
	{
		RangeUtils.checkRange("IDFA Armor Class", 0, Integer.MAX_VALUE, idfaArmorClass);
		this.idfaArmorClass = idfaArmorClass;
	}
	
	public int getIDKFAArmor() 
	{
		return idkfaArmor;
	}
	
	public void setIDKFAArmor(int idkfaArmor) 
	{
		RangeUtils.checkRange("IDKFA Armor", 0, Integer.MAX_VALUE, idkfaArmor);
		this.idkfaArmor = idkfaArmor;
	}
	
	public int getIDKFAArmorClass()
	{
		return idkfaArmorClass;
	}
	
	public void setIDKFAArmorClass(int idkfaArmorClass)
	{
		RangeUtils.checkRange("IDKFA Armor Class", 0, Integer.MAX_VALUE, idkfaArmorClass);
		this.idkfaArmorClass = idkfaArmorClass;
	}
	
	public int getMaxArmor() 
	{
		return maxArmor;
	}
	
	public void setMaxArmor(int maxArmor) 
	{
		RangeUtils.checkRange("Max Armor", 0, Integer.MAX_VALUE, maxArmor);
		this.maxArmor = maxArmor;
	}
	
	public int getMaxHealth() 
	{
		return maxHealth;
	}
	
	public void setMaxHealth(int maxHealth) 
	{
		RangeUtils.checkRange("Max health", 0, Integer.MAX_VALUE, maxHealth);
		this.maxHealth = maxHealth;
	}
	
	@Override
	public void writeObject(Writer writer, DEHMiscallany misc) throws IOException
	{
		if (monsterInfightingEnabled != misc.monsterInfightingEnabled)
			writer.append("Monsters Infight = ").append(String.valueOf(221)).append('\n');
		if (initialBullets != misc.initialBullets)
			writer.append("Initial Bullets = ").append(String.valueOf(initialBullets)).append('\n');
		if (initialHealth != misc.initialHealth)
			writer.append("Initial Health = ").append(String.valueOf(initialHealth)).append('\n');
		if (greenArmorClass != misc.greenArmorClass)
			writer.append("Green Armor Class = ").append(String.valueOf(greenArmorClass)).append('\n');
		if (blueArmorClass != misc.blueArmorClass)
			writer.append("Blue Armor Class = ").append(String.valueOf(blueArmorClass)).append('\n');
		if (soulsphereHealth != misc.soulsphereHealth)
			writer.append("Soulsphere Health = ").append(String.valueOf(soulsphereHealth)).append('\n');
		if (maxSoulsphereHealth != misc.maxSoulsphereHealth)
			writer.append("Max Soulsphere = ").append(String.valueOf(maxSoulsphereHealth)).append('\n');
		if (megasphereHealth != misc.megasphereHealth)
			writer.append("Megasphere Health = ").append(String.valueOf(megasphereHealth)).append('\n');
		if (godModeHealth != misc.godModeHealth)
			writer.append("God Mode Health = ").append(String.valueOf(godModeHealth)).append('\n');
		if (idfaArmor != misc.idfaArmor)
			writer.append("IDFA Armor = ").append(String.valueOf(idfaArmor)).append('\n');
		if (idfaArmorClass != misc.idfaArmorClass)
			writer.append("IDFA Armor Class = ").append(String.valueOf(idfaArmorClass)).append('\n');
		if (idkfaArmor != misc.idkfaArmor)
			writer.append("IDKFA Armor = ").append(String.valueOf(idkfaArmor)).append('\n');
		if (idkfaArmorClass != misc.idkfaArmorClass)
			writer.append("IDKFA Armor Class = ").append(String.valueOf(idkfaArmorClass)).append('\n');
		if (bfgCellsPerShot != misc.bfgCellsPerShot)
			writer.append("BFG Cells/Shot = ").append(String.valueOf(bfgCellsPerShot)).append('\n');
		if (maxHealth != misc.maxHealth)
			writer.append("Max Health = ").append(String.valueOf(maxHealth)).append('\n');
		if (maxArmor != misc.maxArmor)
			writer.append("Max Armor = ").append(String.valueOf(maxArmor)).append('\n');
		writer.flush();
	}
	
}
