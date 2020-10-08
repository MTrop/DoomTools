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
	
	private String name;
	
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
		setName("");
		setEditorNumber(EDITORNUMBER_NONE);
		
		setHealth(0);
		setSpeed(0);
		setRadius(0);
		setHeight(0);
		setDamage(0);
		setReactionTime(0);
		setPainChance(0);
		setMass(0);
		
		setSpawnFrameIndex(FRAME_NULL);
		setWalkFrameIndex(FRAME_NULL);
		setPainFrameIndex(FRAME_NULL);
		setMeleeFrameIndex(FRAME_NULL);
		setMissileFrameIndex(FRAME_NULL);
		setDeathFrameIndex(FRAME_NULL);
		setExtremeDeathFrameIndex(FRAME_NULL);
		setRaiseFrameIndex(FRAME_NULL);
		
		setSeeSoundIndex(SOUND_NONE);
		setAttackSoundIndex(SOUND_NONE);
		setPainSoundIndex(SOUND_NONE);
		setDeathSoundIndex(SOUND_NONE);
		setActiveSoundIndex(SOUND_NONE);
	}

	@Override
	public DEHThing copyFrom(DEHThing source) 
	{
		setName(source.name);
		setEditorNumber(source.editorNumber);
		
		setHealth(source.health);
		setSpeed(source.speed);
		setRadius(source.radius);
		setHeight(source.height);
		setDamage(source.damage);
		setReactionTime(source.reactionTime);
		setPainChance(source.painChance);
		setMass(source.mass);
		
		setSpawnFrameIndex(source.spawnFrameIndex);
		setWalkFrameIndex(source.walkFrameIndex);
		setPainFrameIndex(source.painFrameIndex);
		setMeleeFrameIndex(source.meleeFrameIndex);
		setMissileFrameIndex(source.missileFrameIndex);
		setDeathFrameIndex(source.deathFrameIndex);
		setExtremeDeathFrameIndex(source.extremeDeathFrameIndex);
		setRaiseFrameIndex(source.raiseFrameIndex);
		
		setSeeSoundIndex(source.seeSoundIndex);
		setAttackSoundIndex(source.attackSoundIndex);
		setPainSoundIndex(source.painSoundIndex);
		setDeathSoundIndex(source.deathSoundIndex);
		setActiveSoundIndex(source.activeSoundIndex);
		return this;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public DEHThing setName(String name) 
	{
		this.name = name;
		return this;
	}
	
	public int getEditorNumber() 
	{
		return editorNumber;
	}

	public DEHThing setEditorNumber(int editorNumber)
	{
		if (editorNumber == 0)
			throw new IllegalArgumentException("Editor number can not be 0.");
		RangeUtils.checkRange("Editor number", -1, 0x10000, editorNumber);
		this.editorNumber = editorNumber;
		return this;
	}

	public int getHealth() 
	{
		return health;
	}

	public DEHThing setHealth(int health)
	{
		RangeUtils.checkRange("Health", 0, 999999, health);
		this.health = health;
		return this;
	}

	public int getSpeed()
	{
		return speed;
	}

	public DEHThing setSpeed(int speed) 
	{
		RangeUtils.checkRange("Speed", 0, 999999, speed);
		this.speed = speed;
		return this;
	}

	public int getRadius() 
	{
		return radius;
	}

	public DEHThing setRadius(int radius) 
	{
		RangeUtils.checkRange("Radius", 0, 65535, radius);
		this.radius = radius;
		return this;
	}

	public int getHeight() 
	{
		return height;
	}

	public DEHThing setHeight(int height) 
	{
		RangeUtils.checkRange("Height", 0, 65535, height);
		this.height = height;
		return this;
	}

	public int getDamage() 
	{
		return damage;
	}

	public DEHThing setDamage(int damage) 
	{
		RangeUtils.checkRange("Damage", 0, 999999, damage);
		this.damage = damage;
		return this;
	}

	public int getReactionTime()
	{
		return reactionTime;
	}

	public DEHThing setReactionTime(int reactionTime)
	{
		RangeUtils.checkRange("Reaction time", 0, Integer.MAX_VALUE, reactionTime);
		this.reactionTime = reactionTime;
		return this;
	}

	public int getPainChance()
{
		return painChance;
	}

	public DEHThing setPainChance(int painChance)
	{
		RangeUtils.checkRange("Pain chance", 0, 255, painChance);
		this.painChance = painChance;
		return this;
	}

	public int getMass() 
	{
		return mass;
	}

	public DEHThing setMass(int mass) 
	{
		RangeUtils.checkRange("Mass", 0, 255, mass);
		this.mass = mass;
		return this;
	}

	public int getSpawnFrameIndex()
	{
		return spawnFrameIndex;
	}

	public DEHThing setSpawnFrameIndex(int spawnFrameIndex)
	{
		RangeUtils.checkRange("Spawn frame index", 0, Integer.MAX_VALUE, spawnFrameIndex);
		this.spawnFrameIndex = spawnFrameIndex;
		return this;
	}

	public int getWalkFrameIndex() 
	{
		return walkFrameIndex;
	}

	public DEHThing setWalkFrameIndex(int walkFrameIndex) 
	{
		RangeUtils.checkRange("Walk frame index", 0, Integer.MAX_VALUE, walkFrameIndex);
		this.walkFrameIndex = walkFrameIndex;
		return this;
	}

	public int getPainFrameIndex()
	{
		return painFrameIndex;
	}

	public DEHThing setPainFrameIndex(int painFrameIndex) 
	{
		RangeUtils.checkRange("Pain frame index", 0, Integer.MAX_VALUE, painFrameIndex);
		this.painFrameIndex = painFrameIndex;
		return this;
	}

	public int getMeleeFrameIndex() 
	{
		return meleeFrameIndex;
	}

	public DEHThing setMeleeFrameIndex(int meleeFrameIndex)
	{
		RangeUtils.checkRange("Melee frame index", 0, Integer.MAX_VALUE, meleeFrameIndex);
		this.meleeFrameIndex = meleeFrameIndex;
		return this;
	}

	public int getMissileFrameIndex() 
	{
		return missileFrameIndex;
	}

	public DEHThing setMissileFrameIndex(int missileFrameIndex) 
	{
		RangeUtils.checkRange("Attack frame index", 0, Integer.MAX_VALUE, missileFrameIndex);
		this.missileFrameIndex = missileFrameIndex;
		return this;
	}

	public int getDeathFrameIndex() 
	{
		return deathFrameIndex;
	}

	public DEHThing setDeathFrameIndex(int deathFrameIndex) 
	{
		RangeUtils.checkRange("Death frame index", 0, Integer.MAX_VALUE, deathFrameIndex);
		this.deathFrameIndex = deathFrameIndex;
		return this;
	}

	public int getExtremeDeathFrameIndex() 
	{
		return extremeDeathFrameIndex;
	}

	public DEHThing setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		RangeUtils.checkRange("Extreme death frame index", 0, Integer.MAX_VALUE, extremeDeathFrameIndex);
		this.extremeDeathFrameIndex = extremeDeathFrameIndex;
		return this;
	}

	public int getRaiseFrameIndex()
	{
		return raiseFrameIndex;
	}

	public DEHThing setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		this.raiseFrameIndex = raiseFrameIndex;
		return this;
	}

	public int getSeeSoundIndex() 
	{
		return seeSoundIndex;
	}

	public DEHThing setSeeSoundIndex(int alertSoundIndex)
	{
		RangeUtils.checkRange("Alert sound index", 0, Integer.MAX_VALUE, alertSoundIndex);
		this.seeSoundIndex = alertSoundIndex;
		return this;
	}

	public int getAttackSoundIndex()
	{
		return attackSoundIndex;
	}

	public DEHThing setAttackSoundIndex(int attackSoundIndex)
	{
		RangeUtils.checkRange("Attack sound index", 0, Integer.MAX_VALUE, attackSoundIndex);
		this.attackSoundIndex = attackSoundIndex;
		return this;
	}

	public int getPainSoundIndex()
	{
		return painSoundIndex;
	}

	public DEHThing setPainSoundIndex(int painSoundIndex) 
	{
		RangeUtils.checkRange("Pain sound index", 0, Integer.MAX_VALUE, painSoundIndex);
		this.painSoundIndex = painSoundIndex;
		return this;
	}

	public int getDeathSoundIndex()
	{
		return deathSoundIndex;
	}

	public DEHThing setDeathSoundIndex(int deathSoundIndex) 
	{
		RangeUtils.checkRange("Death sound index", 0, Integer.MAX_VALUE, deathSoundIndex);
		this.deathSoundIndex = deathSoundIndex;
		return this;
	}

	public int getActiveSoundIndex()
	{
		return activeSoundIndex;
	}

	public DEHThing setActiveSoundIndex(int activeSoundIndex)
	{
		RangeUtils.checkRange("Active sound index", 0, Integer.MAX_VALUE, activeSoundIndex);
		this.activeSoundIndex = activeSoundIndex;
		return this;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHThing)
			return equals((DEHThing)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHThing obj) 
	{
		return editorNumber == obj.editorNumber
			&& health == obj.health
			&& speed == obj.speed
			&& radius == obj.radius
			&& height == obj.height
			&& damage == obj.damage
			&& reactionTime == obj.reactionTime
			&& painChance == obj.painChance
			&& mass == obj.mass
			&& spawnFrameIndex == obj.spawnFrameIndex
			&& walkFrameIndex == obj.walkFrameIndex
			&& painFrameIndex == obj.painFrameIndex
			&& meleeFrameIndex == obj.meleeFrameIndex
			&& missileFrameIndex == obj.missileFrameIndex
			&& deathFrameIndex == obj.deathFrameIndex
			&& extremeDeathFrameIndex == obj.extremeDeathFrameIndex
			&& raiseFrameIndex == obj.raiseFrameIndex
			&& seeSoundIndex == obj.seeSoundIndex
			&& activeSoundIndex == obj.activeSoundIndex
			&& attackSoundIndex == obj.attackSoundIndex
			&& painSoundIndex == obj.painSoundIndex
			&& deathSoundIndex == obj.deathSoundIndex
		;
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

		if (seeSoundIndex != thing.seeSoundIndex)
			writer.append("Alert sound = ").append(String.valueOf(seeSoundIndex)).append('\n');
		if (activeSoundIndex != thing.activeSoundIndex)
			writer.append("Action sound = ").append(String.valueOf(activeSoundIndex)).append('\n');
		if (attackSoundIndex != thing.attackSoundIndex)
			writer.append("Attack sound = ").append(String.valueOf(attackSoundIndex)).append('\n');
		if (painSoundIndex != thing.painSoundIndex)
			writer.append("Pain sound = ").append(String.valueOf(painSoundIndex)).append('\n');
		if (deathSoundIndex != thing.deathSoundIndex)
			writer.append("Death sound = ").append(String.valueOf(deathSoundIndex)).append('\n');
		
		writer.flush();
	}

}
