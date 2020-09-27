package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single thing entry.
 * @author Matthew Tropiano
 */
public class DEHThing implements DEHObject
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
	/** Attack frame index. */
	private int attackFrameIndex;
	/** Death frame index. */
	private int deathFrameIndex;
	/** Extreme death frame index. */
	private int extremeDeathFrameIndex;
	/** Raise frame index. */
	private int raiseFrameIndex;

	/** Alert sound index. */
	private int alertSoundIndex;
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
		this.attackFrameIndex = FRAME_NULL;
		this.deathFrameIndex = FRAME_NULL;
		this.extremeDeathFrameIndex = FRAME_NULL;
		this.raiseFrameIndex = FRAME_NULL;
		
		this.alertSoundIndex = SOUND_NONE;
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
		this.health = health;
	}

	public int getSpeed()
	{
		return speed;
	}

	public void setSpeed(int speed) 
	{
		this.speed = speed;
	}

	public int getRadius() 
	{
		return radius;
	}

	public void setRadius(int radius) 
	{
		this.radius = radius;
	}

	public int getHeight() 
	{
		return height;
	}

	public void setHeight(int height) 
	{
		this.height = height;
	}

	public int getDamage() 
	{
		return damage;
	}

	public void setDamage(int damage) 
	{
		this.damage = damage;
	}

	public int getReactionTime()
	{
		return reactionTime;
	}

	public void setReactionTime(int reactionTime)
	{
		this.reactionTime = reactionTime;
	}

	public int getPainChance()
{
		return painChance;
	}

	public void setPainChance(int painChance)
	{
		this.painChance = painChance;
	}

	public int getMass() 
	{
		return mass;
	}

	public void setMass(int mass) 
	{
		this.mass = mass;
	}

	public int getSpawnFrameIndex()
	{
		return spawnFrameIndex;
	}

	public void setSpawnFrameIndex(int spawnFrameIndex)
	{
		this.spawnFrameIndex = spawnFrameIndex;
	}

	public int getWalkFrameIndex() 
	{
		return walkFrameIndex;
	}

	public void setWalkFrameIndex(int walkFrameIndex) 
	{
		this.walkFrameIndex = walkFrameIndex;
	}

	public int getPainFrameIndex()
	{
		return painFrameIndex;
	}

	public void setPainFrameIndex(int painFrameIndex) 
	{
		this.painFrameIndex = painFrameIndex;
	}

	public int getMeleeFrameIndex() 
	{
		return meleeFrameIndex;
	}

	public void setMeleeFrameIndex(int meleeFrameIndex)
	{
		this.meleeFrameIndex = meleeFrameIndex;
	}

	public int getAttackFrameIndex() 
	{
		return attackFrameIndex;
	}

	public void setAttackFrameIndex(int attackFrameIndex) 
	{
		this.attackFrameIndex = attackFrameIndex;
	}

	public int getDeathFrameIndex() 
	{
		return deathFrameIndex;
	}

	public void setDeathFrameIndex(int deathFrameIndex) 
	{
		this.deathFrameIndex = deathFrameIndex;
	}

	public int getExtremeDeathFrameIndex() 
	{
		return extremeDeathFrameIndex;
	}

	public void setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		this.extremeDeathFrameIndex = extremeDeathFrameIndex;
	}

	public int getRaiseFrameIndex()
	{
		return raiseFrameIndex;
	}

	public void setRaiseFrameIndex(int raiseFrameIndex) {
		this.raiseFrameIndex = raiseFrameIndex;
	}

	public int getAlertSoundIndex() 
	{
		return alertSoundIndex;
	}

	public void setAlertSoundIndex(int alertSoundIndex)
	{
		this.alertSoundIndex = alertSoundIndex;
	}

	public int getAttackSoundIndex()
	{
		return attackSoundIndex;
	}

	public void setAttackSoundIndex(int attackSoundIndex)
	{
		this.attackSoundIndex = attackSoundIndex;
	}

	public int getPainSoundIndex()
	{
		return painSoundIndex;
	}

	public void setPainSoundIndex(int painSoundIndex) 
	{
		this.painSoundIndex = painSoundIndex;
	}

	public int getDeathSoundIndex()
	{
		return deathSoundIndex;
	}

	public void setDeathSoundIndex(int deathSoundIndex) 
	{
		this.deathSoundIndex = deathSoundIndex;
	}

	public int getActiveSoundIndex()
	{
		return activeSoundIndex;
	}

	public void setActiveSoundIndex(int activeSoundIndex)
	{
		this.activeSoundIndex = activeSoundIndex;
	}

	@Override
	public void writeObject(Writer writer) throws IOException
	{
		// TODO Auto-generated method stub
		/*
			ID # = 1
			Hit points = 1
			Speed = 1
			Width = 196608 // fixed point 16.16
			Height = 196608 // fixed point 16.16
			Missile damage = 1
			Reaction time = 1
			Pain chance = 1
			Mass = 1
			
			Death frame = 1
			Initial frame = 1
			Respawn frame = 1
			Exploding frame = 1
			Injury frame = 1
			Far attack frame = 1
			First moving frame = 1
			Close attack frame = 1
			
			Action sound = 1
			Pain sound = 1
			Death sound = 1
			Alert sound = 1
			Attack sound = 1
		 */
	}

}
