package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single thing entry.
 * NOTE: All sound positions are 1-BASED. 0 = no sound, [index+1] is the sound.
 * @author Matthew Tropiano
 */
public class DEHThing implements DEHObject<DEHThing>, DEHActor
{
	public static final String STATE_LABEL_SPAWN = "spawn";
	public static final String STATE_LABEL_SEE = "see";
	public static final String STATE_LABEL_MELEE = "melee";
	public static final String STATE_LABEL_MISSILE = "missile";
	public static final String STATE_LABEL_PAIN = "pain";
	public static final String STATE_LABEL_DEATH = "death";
	public static final String STATE_LABEL_XDEATH = "xdeath";
	public static final String STATE_LABEL_RAISE = "raise";

	public static final int EDITORNUMBER_NONE = -1;
	public static final int SOUND_NONE = 0;
	public static final int FRAME_NULL = 0;
	
	private String name;
	
	private int editorNumber;
	
	private int health;
	private int speed; // written as fixed point 16.16 if PROJECTILE
	private int radius; // written as fixed point 16.16
	private int height; // written as fixed point 16.16
	private int damage;
	private int reactionTime;
	private int painChance;
	private int flags;
	private int mass;

	/** State indices (label name to index). */
	private Map<String, Integer> stateIndexMap;
	
	/** Alert sound position. */
	private int seeSoundPosition;
	/** Attack sound position. */
	private int attackSoundPosition;
	/** Pain sound position. */
	private int painSoundPosition;
	/** Death sound position. */
	private int deathSoundPosition;
	/** Active sound position. */
	private int activeSoundPosition;

	/**
	 * Creates a new blank thing.
	 */
	public DEHThing()
	{
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		setName("");
		setEditorNumber(EDITORNUMBER_NONE);
		
		setHealth(0);
		setSpeed(0);
		setRadius(0);
		setHeight(0);
		setDamage(0);
		setReactionTime(0);
		setPainChance(0);
		setFlags(0x00000000);
		setMass(0);
		
		setSpawnFrameIndex(FRAME_NULL);
		setWalkFrameIndex(FRAME_NULL);
		setPainFrameIndex(FRAME_NULL);
		setMeleeFrameIndex(FRAME_NULL);
		setMissileFrameIndex(FRAME_NULL);
		setDeathFrameIndex(FRAME_NULL);
		setExtremeDeathFrameIndex(FRAME_NULL);
		setRaiseFrameIndex(FRAME_NULL);
		
		setSeeSoundPosition(SOUND_NONE);
		setAttackSoundPosition(SOUND_NONE);
		setPainSoundPosition(SOUND_NONE);
		setDeathSoundPosition(SOUND_NONE);
		setActiveSoundPosition(SOUND_NONE);
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
		setFlags(source.flags);
		setMass(source.mass);
		
		setSpawnFrameIndex(source.getSpawnFrameIndex());
		setWalkFrameIndex(source.getWalkFrameIndex());
		setPainFrameIndex(source.getPainFrameIndex());
		setMeleeFrameIndex(source.getMeleeFrameIndex());
		setMissileFrameIndex(source.getMissileFrameIndex());
		setDeathFrameIndex(source.getDeathFrameIndex());
		setExtremeDeathFrameIndex(source.getExtremeDeathFrameIndex());
		setRaiseFrameIndex(source.getRaiseFrameIndex());
		
		setSeeSoundPosition(source.seeSoundPosition);
		setAttackSoundPosition(source.attackSoundPosition);
		setPainSoundPosition(source.painSoundPosition);
		setDeathSoundPosition(source.deathSoundPosition);
		setActiveSoundPosition(source.activeSoundPosition);
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
		RangeUtils.checkRange("Speed", 0, 65535, speed);
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
		RangeUtils.checkRange("Damage", -999999, 999999, damage);
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
		RangeUtils.checkRange("Pain chance", 0, Integer.MAX_VALUE, painChance);
		this.painChance = painChance;
		return this;
	}

	public int getFlags() 
	{
		return flags;
	}

	public DEHThing setFlags(int bits) 
	{
		this.flags = bits;
		return this;
	}

	public int getMass() 
	{
		return mass;
	}

	public DEHThing setMass(int mass) 
	{
		RangeUtils.checkRange("Mass", 0, Integer.MAX_VALUE, mass);
		this.mass = mass;
		return this;
	}

	public int getSpawnFrameIndex()
	{
		return getLabel(STATE_LABEL_SPAWN);
	}

	public DEHThing setSpawnFrameIndex(int spawnFrameIndex)
	{
		RangeUtils.checkRange("Spawn frame index", 0, Integer.MAX_VALUE, spawnFrameIndex);
		setLabel(STATE_LABEL_SPAWN, spawnFrameIndex);
		return this;
	}

	public int getWalkFrameIndex() 
	{
		return getLabel(STATE_LABEL_SEE);
	}

	public DEHThing setWalkFrameIndex(int walkFrameIndex) 
	{
		RangeUtils.checkRange("Walk frame index", 0, Integer.MAX_VALUE, walkFrameIndex);
		setLabel(STATE_LABEL_SEE, walkFrameIndex);
		return this;
	}

	public int getPainFrameIndex()
	{
		return getLabel(STATE_LABEL_PAIN);
	}

	public DEHThing setPainFrameIndex(int painFrameIndex) 
	{
		RangeUtils.checkRange("Pain frame index", 0, Integer.MAX_VALUE, painFrameIndex);
		setLabel(STATE_LABEL_PAIN, painFrameIndex);
		return this;
	}

	public int getMeleeFrameIndex() 
	{
		return getLabel(STATE_LABEL_MELEE);
	}

	public DEHThing setMeleeFrameIndex(int meleeFrameIndex)
	{
		RangeUtils.checkRange("Melee frame index", 0, Integer.MAX_VALUE, meleeFrameIndex);
		setLabel(STATE_LABEL_MELEE, meleeFrameIndex);
		return this;
	}

	public int getMissileFrameIndex() 
	{
		return getLabel(STATE_LABEL_MISSILE);
	}

	public DEHThing setMissileFrameIndex(int missileFrameIndex) 
	{
		RangeUtils.checkRange("Attack frame index", 0, Integer.MAX_VALUE, missileFrameIndex);
		setLabel(STATE_LABEL_MISSILE, missileFrameIndex);
		return this;
	}

	public int getDeathFrameIndex() 
	{
		return getLabel(STATE_LABEL_DEATH);
	}

	public DEHThing setDeathFrameIndex(int deathFrameIndex) 
	{
		RangeUtils.checkRange("Death frame index", 0, Integer.MAX_VALUE, deathFrameIndex);
		setLabel(STATE_LABEL_DEATH, deathFrameIndex);
		return this;
	}

	public int getExtremeDeathFrameIndex() 
	{
		return getLabel(STATE_LABEL_XDEATH);
	}

	public DEHThing setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		RangeUtils.checkRange("Extreme death frame index", 0, Integer.MAX_VALUE, extremeDeathFrameIndex);
		setLabel(STATE_LABEL_XDEATH, extremeDeathFrameIndex);
		return this;
	}

	public int getRaiseFrameIndex()
	{
		return getLabel(STATE_LABEL_RAISE);
	}

	public DEHThing setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		setLabel(STATE_LABEL_RAISE, raiseFrameIndex);
		return this;
	}

	@Override
	public Map<String, Integer> getStateIndexMap() 
	{
		return stateIndexMap;
	}
	
	private int getLabel(String label)
	{
		return stateIndexMap.getOrDefault(label, 0);
	}

	private void setLabel(String label, int index)
	{
		if (index == 0)
			stateIndexMap.remove(label);
		else
			stateIndexMap.put(label, index);
	}

	public int getSeeSoundPosition() 
	{
		return seeSoundPosition;
	}

	public DEHThing setSeeSoundPosition(int seeSoundPosition)
	{
		RangeUtils.checkRange("Alert sound position", 0, Integer.MAX_VALUE, seeSoundPosition);
		this.seeSoundPosition = seeSoundPosition;
		return this;
	}

	public int getAttackSoundPosition()
	{
		return attackSoundPosition;
	}

	public DEHThing setAttackSoundPosition(int attackSoundPosition)
	{
		RangeUtils.checkRange("Attack sound position", 0, Integer.MAX_VALUE, attackSoundPosition);
		this.attackSoundPosition = attackSoundPosition;
		return this;
	}

	public int getPainSoundPosition()
	{
		return painSoundPosition;
	}

	public DEHThing setPainSoundPosition(int painSoundPosition) 
	{
		RangeUtils.checkRange("Pain sound position", 0, Integer.MAX_VALUE, painSoundPosition);
		this.painSoundPosition = painSoundPosition;
		return this;
	}

	public int getDeathSoundPosition()
	{
		return deathSoundPosition;
	}

	public DEHThing setDeathSoundPosition(int deathSoundPosition) 
	{
		RangeUtils.checkRange("Death sound position", 0, Integer.MAX_VALUE, deathSoundPosition);
		this.deathSoundPosition = deathSoundPosition;
		return this;
	}

	public int getActiveSoundPosition()
	{
		return activeSoundPosition;
	}

	public DEHThing setActiveSoundPosition(int activeSoundPosition)
	{
		RangeUtils.checkRange("Active sound position", 0, Integer.MAX_VALUE, activeSoundPosition);
		this.activeSoundPosition = activeSoundPosition;
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
			&& flags == obj.flags
			&& mass == obj.mass
			&& getSpawnFrameIndex() == obj.getSpawnFrameIndex()
			&& getWalkFrameIndex() == obj.getWalkFrameIndex()
			&& getPainFrameIndex() == obj.getPainFrameIndex()
			&& getMeleeFrameIndex() == obj.getMeleeFrameIndex()
			&& getMissileFrameIndex() == obj.getMissileFrameIndex()
			&& getDeathFrameIndex() == obj.getDeathFrameIndex()
			&& getExtremeDeathFrameIndex() == obj.getExtremeDeathFrameIndex()
			&& getRaiseFrameIndex() == obj.getRaiseFrameIndex()
			&& seeSoundPosition == obj.seeSoundPosition
			&& activeSoundPosition == obj.activeSoundPosition
			&& attackSoundPosition == obj.attackSoundPosition
			&& painSoundPosition == obj.painSoundPosition
			&& deathSoundPosition == obj.deathSoundPosition
		;
	}	
	
	@Override
	public void writeObject(Writer writer, DEHThing thing) throws IOException
	{
		// If projectile, speed is fixed point.
		int speedVal = ((flags & (1 << 16)) != 0) ? speed << 16 : speed; 
		
		if (editorNumber != thing.editorNumber)
			writer.append("ID # = ").append(String.valueOf(editorNumber)).append("\r\n");
		if (health != thing.health)
			writer.append("Hit points = ").append(String.valueOf(health)).append("\r\n");
		if (speedVal != thing.speed)
			writer.append("Speed = ").append(String.valueOf(speedVal)).append("\r\n");
		if (radius != thing.radius)
			writer.append("Width = ").append(String.valueOf(radius << 16)).append("\r\n");
		if (height != thing.height)
			writer.append("Height = ").append(String.valueOf(height << 16)).append("\r\n");
		if (damage != thing.damage)
			writer.append("Missile damage = ").append(String.valueOf(damage)).append("\r\n");
		if (reactionTime != thing.reactionTime)
			writer.append("Reaction time = ").append(String.valueOf(reactionTime)).append("\r\n");
		if (painChance != thing.painChance)
			writer.append("Pain chance = ").append(String.valueOf(painChance)).append("\r\n");
		if (flags != thing.flags)
			writer.append("Bits = ").append(String.valueOf(flags)).append("\r\n");
		if (mass != thing.mass)
			writer.append("Mass = ").append(String.valueOf(mass)).append("\r\n");

		if (getSpawnFrameIndex() != thing.getSpawnFrameIndex())
			writer.append("Initial frame = ").append(String.valueOf(getSpawnFrameIndex())).append("\r\n");
		if (getWalkFrameIndex() != thing.getWalkFrameIndex())
			writer.append("First moving frame = ").append(String.valueOf(getWalkFrameIndex())).append("\r\n");
		if (getPainFrameIndex() != thing.getPainFrameIndex())
			writer.append("Injury frame = ").append(String.valueOf(getPainFrameIndex())).append("\r\n");
		if (getMeleeFrameIndex() != thing.getMeleeFrameIndex())
			writer.append("Close attack frame = ").append(String.valueOf(getMeleeFrameIndex() )).append("\r\n");
		if (getMissileFrameIndex() != thing.getMissileFrameIndex())
			writer.append("Far attack frame = ").append(String.valueOf(getMissileFrameIndex())).append("\r\n");
		if (getDeathFrameIndex() != thing.getDeathFrameIndex())
			writer.append("Death frame = ").append(String.valueOf(getDeathFrameIndex())).append("\r\n");
		if (getExtremeDeathFrameIndex() != thing.getExtremeDeathFrameIndex())
			writer.append("Exploding frame = ").append(String.valueOf(getExtremeDeathFrameIndex())).append("\r\n");
		if (getRaiseFrameIndex() != thing.getRaiseFrameIndex())
			writer.append("Respawn frame = ").append(String.valueOf(getRaiseFrameIndex())).append("\r\n");

		if (seeSoundPosition != thing.seeSoundPosition)
			writer.append("Alert sound = ").append(String.valueOf(seeSoundPosition)).append("\r\n");
		if (activeSoundPosition != thing.activeSoundPosition)
			writer.append("Action sound = ").append(String.valueOf(activeSoundPosition)).append("\r\n");
		if (attackSoundPosition != thing.attackSoundPosition)
			writer.append("Attack sound = ").append(String.valueOf(attackSoundPosition)).append("\r\n");
		if (painSoundPosition != thing.painSoundPosition)
			writer.append("Pain sound = ").append(String.valueOf(painSoundPosition)).append("\r\n");
		if (deathSoundPosition != thing.deathSoundPosition)
			writer.append("Death sound = ").append(String.valueOf(deathSoundPosition)).append("\r\n");
		
		writer.flush();
	}

}
