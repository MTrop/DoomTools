package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single thing entry.
 * @author Matthew Tropiano
 */
public class DEHThing implements DEHObject<DEHThing>
{
	public static final int EDITORNUMBER_NONE = -1;
	public static final int SOUND_NONE = 0;
	public static final int FRAME_NULL = 0;
	
	private int editorNumber;
	
	private int health;
	private int speed;
	private int radius; // stored as fixed point 16.16
	private int height; // stored as fixed point 16.16
	private int damage;
	private int reactionTime;
	private int painChance;
	private int mass;

	/** Spawn frame index. */
	private int spawnFrameIndex;
	/** Walk frame index. */
	private int walkFrameIndex;
	/** Pain frame index. */
	private int painFrameIndex;
	/** Melee frame index. */
	private int meleeFrameIndex;
	/** Missile frame index. */
	private int missileFrameIndex;
	/** Death frame index. */
	private int deathFrameIndex;
	/** Extreme death frame index. */
	private int extremeDeathFrameIndex;
	/** Raise frame index. */
	private int raiseFrameIndex;

	/** Alert sound index. */
	private int seeSoundIndex;
	/** Attack sound index. */
	private int attackSoundIndex;
	/** Pain sound index. */
	private int painSoundIndex;
	/** Death sound index. */
	private int deathSoundIndex;
	/** Active sound index. */
	private int activeSoundIndex;

	/**
	 * Creates a new blank thing.
	 */
	public DEHThing()
	{
		this.editorNumber = EDITORNUMBER_NONE;
		
		this.health = 0;
		this.speed = 0;
		this.radius = 0;
		this.height = 0;
		this.damage = 0;
		this.reactionTime = 0;
		this.painChance = 0;
		this.mass = 0;
		
		this.spawnFrameIndex = FRAME_NULL;
		this.walkFrameIndex = FRAME_NULL;
		this.painFrameIndex = FRAME_NULL;
		this.meleeFrameIndex = FRAME_NULL;
		this.missileFrameIndex = FRAME_NULL;
		this.deathFrameIndex = FRAME_NULL;
		this.extremeDeathFrameIndex = FRAME_NULL;
		this.raiseFrameIndex = FRAME_NULL;
		
		this.seeSoundIndex = SOUND_NONE;
		this.attackSoundIndex = SOUND_NONE;
		this.painSoundIndex = SOUND_NONE;
		this.deathSoundIndex = SOUND_NONE;
		this.activeSoundIndex = SOUND_NONE;
	}

	public int getEditorNumber() 
	{
		return editorNumber;
	}

	public void setEditorNumber(int editorNumber)
	{
		if (editorNumber == 0)
			throw new IllegalArgumentException("Editor number can not be 0.");
		RangeUtils.checkRange("Editor number", -1, 0x10000, editorNumber);
		this.editorNumber = editorNumber;
	}

	public int getHealth() 
	{
		return health;
	}

	public void setHealth(int health)
	{
		RangeUtils.checkRange("Health", 0, 999999, health);
		this.health = health;
	}

	public int getSpeed()
	{
		return speed;
	}

	public void setSpeed(int speed) 
	{
		RangeUtils.checkRange("Speed", 0, 999999, speed);
		this.speed = speed;
	}

	public int getRadius() 
	{
		return radius;
	}

	public void setRadius(int radius) 
	{
		RangeUtils.checkRange("Radius", 0, 65535, radius);
		this.radius = radius;
	}

	public int getHeight() 
	{
		return height;
	}

	public void setHeight(int height) 
	{
		RangeUtils.checkRange("Height", 0, 65535, height);
		this.height = height;
	}

	public int getDamage() 
	{
		return damage;
	}

	public void setDamage(int damage) 
	{
		RangeUtils.checkRange("Damage", 0, 999999, damage);
		this.damage = damage;
	}

	public int getReactionTime()
	{
		return reactionTime;
	}

	public void setReactionTime(int reactionTime)
	{
		RangeUtils.checkRange("Reaction time", 0, Integer.MAX_VALUE, reactionTime);
		this.reactionTime = reactionTime;
	}

	public int getPainChance()
{
		return painChance;
	}

	public void setPainChance(int painChance)
	{
		RangeUtils.checkRange("Pain chance", 0, 255, painChance);
		this.painChance = painChance;
	}

	public int getMass() 
	{
		return mass;
	}

	public void setMass(int mass) 
	{
		RangeUtils.checkRange("Mass", 0, 255, mass);
		this.mass = mass;
	}

	public int getSpawnFrameIndex()
	{
		return spawnFrameIndex;
	}

	public void setSpawnFrameIndex(int spawnFrameIndex)
	{
		RangeUtils.checkRange("Spawn frame index", 0, Integer.MAX_VALUE, spawnFrameIndex);
		this.spawnFrameIndex = spawnFrameIndex;
	}

	public int getWalkFrameIndex() 
	{
		return walkFrameIndex;
	}

	public void setWalkFrameIndex(int walkFrameIndex) 
	{
		RangeUtils.checkRange("Walk frame index", 0, Integer.MAX_VALUE, walkFrameIndex);
		this.walkFrameIndex = walkFrameIndex;
	}

	public int getPainFrameIndex()
	{
		return painFrameIndex;
	}

	public void setPainFrameIndex(int painFrameIndex) 
	{
		RangeUtils.checkRange("Pain frame index", 0, Integer.MAX_VALUE, painFrameIndex);
		this.painFrameIndex = painFrameIndex;
	}

	public int getMeleeFrameIndex() 
	{
		return meleeFrameIndex;
	}

	public void setMeleeFrameIndex(int meleeFrameIndex)
	{
		RangeUtils.checkRange("Melee frame index", 0, Integer.MAX_VALUE, meleeFrameIndex);
		this.meleeFrameIndex = meleeFrameIndex;
	}

	public int getMissileFrameIndex() 
	{
		return missileFrameIndex;
	}

	public void setMissileFrameIndex(int missileFrameIndex) 
	{
		RangeUtils.checkRange("Attack frame index", 0, Integer.MAX_VALUE, missileFrameIndex);
		this.missileFrameIndex = missileFrameIndex;
	}

	public int getDeathFrameIndex() 
	{
		return deathFrameIndex;
	}

	public void setDeathFrameIndex(int deathFrameIndex) 
	{
		RangeUtils.checkRange("Death frame index", 0, Integer.MAX_VALUE, deathFrameIndex);
		this.deathFrameIndex = deathFrameIndex;
	}

	public int getExtremeDeathFrameIndex() 
	{
		return extremeDeathFrameIndex;
	}

	public void setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		RangeUtils.checkRange("Extreme death frame index", 0, Integer.MAX_VALUE, extremeDeathFrameIndex);
		this.extremeDeathFrameIndex = extremeDeathFrameIndex;
	}

	public int getRaiseFrameIndex()
	{
		return raiseFrameIndex;
	}

	public void setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		this.raiseFrameIndex = raiseFrameIndex;
	}

	public int getSeeSoundIndex() 
	{
		return seeSoundIndex;
	}

	public void setSeeSoundIndex(int alertSoundIndex)
	{
		RangeUtils.checkRange("Alert sound index", 0, Integer.MAX_VALUE, alertSoundIndex);
		this.seeSoundIndex = alertSoundIndex;
	}

	public int getAttackSoundIndex()
	{
		return attackSoundIndex;
	}

	public void setAttackSoundIndex(int attackSoundIndex)
	{
		RangeUtils.checkRange("Attack sound index", 0, Integer.MAX_VALUE, attackSoundIndex);
		this.attackSoundIndex = attackSoundIndex;
	}

	public int getPainSoundIndex()
	{
		return painSoundIndex;
	}

	public void setPainSoundIndex(int painSoundIndex) 
	{
		RangeUtils.checkRange("Pain sound index", 0, Integer.MAX_VALUE, painSoundIndex);
		this.painSoundIndex = painSoundIndex;
	}

	public int getDeathSoundIndex()
	{
		return deathSoundIndex;
	}

	public void setDeathSoundIndex(int deathSoundIndex) 
	{
		RangeUtils.checkRange("Death sound index", 0, Integer.MAX_VALUE, deathSoundIndex);
		this.deathSoundIndex = deathSoundIndex;
	}

	public int getActiveSoundIndex()
	{
		return activeSoundIndex;
	}

	public void setActiveSoundIndex(int activeSoundIndex)
	{
		RangeUtils.checkRange("Active sound index", 0, Integer.MAX_VALUE, activeSoundIndex);
		this.activeSoundIndex = activeSoundIndex;
	}

	@Override
	public void writeObject(Writer writer, DEHThing thing) throws IOException
	{
		if (editorNumber != thing.editorNumber)
			writer.append("ID # = ").append(String.valueOf(editorNumber)).append('\n');
		if (health != thing.health)
			writer.append("Hit points = ").append(String.valueOf(health)).append('\n');
		if (speed != thing.speed)
			writer.append("Speed = ").append(String.valueOf(speed)).append('\n');
		if (radius != thing.radius)
			writer.append("Width = ").append(String.valueOf(radius << 16)).append('\n');
		if (height != thing.height)
			writer.append("Height = ").append(String.valueOf(height << 16)).append('\n');
		if (damage != thing.damage)
			writer.append("Missile damage = ").append(String.valueOf(damage)).append('\n');
		if (reactionTime != thing.reactionTime)
			writer.append("Reaction time = ").append(String.valueOf(reactionTime)).append('\n');
		if (painChance != thing.painChance)
			writer.append("Pain chance = ").append(String.valueOf(painChance)).append('\n');
		if (mass != thing.mass)
			writer.append("Mass = ").append(String.valueOf(mass)).append('\n');

		if (spawnFrameIndex != thing.spawnFrameIndex)
			writer.append("Initial frame = ").append(String.valueOf(spawnFrameIndex)).append('\n');
		if (walkFrameIndex != thing.walkFrameIndex)
			writer.append("First moving frame = ").append(String.valueOf(walkFrameIndex)).append('\n');
		if (painFrameIndex != thing.painFrameIndex)
			writer.append("Injury frame = ").append(String.valueOf(painFrameIndex)).append('\n');
		if (meleeFrameIndex != thing.meleeFrameIndex)
			writer.append("Close attack frame = ").append(String.valueOf(meleeFrameIndex)).append('\n');
		if (missileFrameIndex != thing.missileFrameIndex)
			writer.append("Far attack frame = ").append(String.valueOf(missileFrameIndex)).append('\n');
		if (deathFrameIndex != thing.deathFrameIndex)
			writer.append("Death frame = ").append(String.valueOf(deathFrameIndex)).append('\n');
		if (extremeDeathFrameIndex != thing.extremeDeathFrameIndex)
			writer.append("Exploding frame = ").append(String.valueOf(extremeDeathFrameIndex)).append('\n');
		if (raiseFrameIndex != thing.raiseFrameIndex)
			writer.append("Respawn frame = ").append(String.valueOf(raiseFrameIndex)).append('\n');

		if (painSoundIndex != thing.painSoundIndex)
			writer.append("Pain sound = ").append(String.valueOf(painSoundIndex)).append('\n');
		if (deathSoundIndex != thing.deathSoundIndex)
			writer.append("Death sound = ").append(String.valueOf(deathSoundIndex)).append('\n');
		if (seeSoundIndex != thing.seeSoundIndex)
			writer.append("Alert sound = ").append(String.valueOf(seeSoundIndex)).append('\n');
		if (activeSoundIndex != thing.activeSoundIndex)
			writer.append("Action sound = ").append(String.valueOf(activeSoundIndex)).append('\n');
		if (attackSoundIndex != thing.attackSoundIndex)
			writer.append("Attack sound = ").append(String.valueOf(attackSoundIndex)).append('\n');
		
		writer.flush();
	}

}
