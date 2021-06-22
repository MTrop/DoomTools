/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single thing entry where all of its fields are nulled out.
 * The purpose of this object is to prepare a thing where its values 
 * can be applied to many Thing entries. 
 * NOTE: All sound positions are 1-BASED. 0 = no sound, [index+1] is the sound.
 * @author Matthew Tropiano
 */
public class DEHThingTemplate implements DEHThingTarget<DEHThingTemplate>
{
	private Integer editorNumber;
	
	private Integer health;
	private Integer speed; // written as fixed point 16.16 if MISSILE
	private Integer radius; // written as fixed point 16.16
	private Integer height; // written as fixed point 16.16
	private Integer damage;
	private Integer reactionTime;
	private Integer painChance;
	private Integer flags;
	private Integer mass;
	
	private int addFlags;
	private int remFlags;
	
	/** State indices (label name to index). */
	private Map<String, Integer> stateIndexMap;
	
	/** Alert sound position. */
	private Integer seeSoundPosition;
	/** Attack sound position. */
	private Integer attackSoundPosition;
	/** Pain sound position. */
	private Integer painSoundPosition;
	/** Death sound position. */
	private Integer deathSoundPosition;
	/** Active sound position. */
	private Integer activeSoundPosition;

	/** Dropped item. */
	private Integer droppedItem;

	/** MBF21 flags. */
	private Integer mbf21Flags;
	
	private int addMBF21Flags;
	private int remMBF21Flags;

	/** Infighting group. */
	private Integer infightingGroup;
	/** Projectile group. */
	private Integer projectileGroup;
	/** Splash group. */
	private Integer splashGroup;
	/** Nightmare/Fast speed of actor. */
	private Integer fastSpeed; // written as fixed point 16.16 if MISSILE
	/** Melee range. */
	private Integer meleeRange;  // written as fixed point 16.16
	/** Ripper sound position. */
	private Integer ripSoundPosition;

	/**
	 * Creates a new blank thing.
	 */
	public DEHThingTemplate()
	{
		this.editorNumber = null;
		this.health = null;
		this.speed = null;
		this.radius = null;
		this.height = null;
		this.damage = null;
		this.reactionTime = null;
		this.painChance = null;
		this.flags = null;
		this.addFlags = 0;
		this.remFlags = 0;
		this.mass = null;
		
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		this.seeSoundPosition = null;
		this.attackSoundPosition = null;
		this.painSoundPosition = null;
		this.deathSoundPosition = null;
		this.activeSoundPosition = null;
		
		this.droppedItem = null;
		
		this.mbf21Flags = null;
		this.addMBF21Flags = 0;
		this.remMBF21Flags = 0;
		this.infightingGroup = null;
		this.projectileGroup = null;
		this.splashGroup = null;
		this.fastSpeed = null;
		this.meleeRange = null;
		this.ripSoundPosition = null;
	}

	/**
	 * Applies all set fields to a thing.
	 * @param destination the destination thing.
	 * @return this template.
	 */
	public DEHThingTemplate applyTo(DEHThing destination) 
	{
		if (editorNumber != null)
			destination.setEditorNumber(editorNumber);
		
		if (health != null)
			destination.setHealth(health);
		if (speed != null)
			destination.setSpeed(speed);
		if (fastSpeed != null)
			destination.setFastSpeed(fastSpeed);
		if (radius != null)
			destination.setRadius(radius);
		if (height != null)
			destination.setHeight(height);
		if (damage != null)
			destination.setDamage(damage);
		if (reactionTime != null)
			destination.setReactionTime(reactionTime);
		if (painChance != null)
			destination.setPainChance(painChance);
		if (mass != null)
			destination.setMass(mass);

		// if flags altered, replace.
		if (flags != null)
			destination.setFlags(flags);
		// else, just alter the adjustments.
		else
		{
			destination.addFlag(addFlags);
			destination.removeFlag(remFlags);
		}
			
		if (seeSoundPosition != null)
			destination.setSeeSoundPosition(seeSoundPosition);
		if (attackSoundPosition != null)
			destination.setAttackSoundPosition(attackSoundPosition);
		if (painSoundPosition != null)
			destination.setPainSoundPosition(painSoundPosition);
		if (deathSoundPosition != null)
			destination.setDeathSoundPosition(deathSoundPosition);
		if (activeSoundPosition != null)
			destination.setActiveSoundPosition(activeSoundPosition);
			
		if (droppedItem != null)
			destination.setDroppedItem(droppedItem);
	
		// if flags altered, replace.
		if (mbf21Flags != null)
			destination.setMBF21Flags(mbf21Flags);
		// else, just alter the adjustments.
		else
		{
			destination.addFlag(addMBF21Flags);
			destination.removeFlag(remMBF21Flags);
		}
		
		if (infightingGroup != null)
			destination.setInfightingGroup(infightingGroup);
		if (projectileGroup != null)
			destination.setProjectileGroup(projectileGroup);
		if (splashGroup != null)
			destination.setSplashGroup(splashGroup);
		if (fastSpeed != null)
			destination.setFastSpeed(fastSpeed);
		if (meleeRange != null)
			destination.setMeleeRange(meleeRange);
		if (ripSoundPosition != null)
			destination.setRipSoundPosition(ripSoundPosition);

		for (String label : stateIndexMap.keySet())
			destination.setLabel(label, stateIndexMap.getOrDefault(label, 0));

		return this;
	}
	
	public DEHThingTemplate setEditorNumber(int editorNumber)
	{
		if (editorNumber == 0)
			throw new IllegalArgumentException("Editor number can not be 0.");
		RangeUtils.checkRange("Editor number", -1, 0x10000, editorNumber);
		this.editorNumber = editorNumber;
		return this;
	}

	public DEHThingTemplate setHealth(int health)
	{
		RangeUtils.checkRange("Health", 0, 999999, health);
		this.health = health;
		return this;
	}

	public DEHThingTemplate setSpeed(int speed) 
	{
		RangeUtils.checkRange("Speed", 0, 65535, speed);
		this.speed = speed;
		return this;
	}

	public DEHThingTemplate setFastSpeed(int fastSpeed) 
	{
		RangeUtils.checkRange("Fast speed", -1, 65535, fastSpeed);
		this.fastSpeed = fastSpeed;
		return this;
	}

	public DEHThingTemplate setRadius(int radius) 
	{
		RangeUtils.checkRange("Radius", 0, 65535, radius);
		this.radius = radius;
		return this;
	}

	public DEHThingTemplate setHeight(int height) 
	{
		RangeUtils.checkRange("Height", 0, 65535, height);
		this.height = height;
		return this;
	}

	public DEHThingTemplate setDamage(int damage) 
	{
		RangeUtils.checkRange("Damage", -999999, 999999, damage);
		this.damage = damage;
		return this;
	}

	public DEHThingTemplate setReactionTime(int reactionTime)
	{
		RangeUtils.checkRange("Reaction time", 0, Integer.MAX_VALUE, reactionTime);
		this.reactionTime = reactionTime;
		return this;
	}

	public DEHThingTemplate setPainChance(int painChance)
	{
		RangeUtils.checkRange("Pain chance", 0, Integer.MAX_VALUE, painChance);
		this.painChance = painChance;
		return this;
	}

	public DEHThingTemplate setFlags(int bits) 
	{
		this.flags = bits;
		return this;
	}

	@Override
	public DEHThingTemplate addFlag(int bits)
	{
		if (this.flags != null)
			this.flags |= bits;
		else
			this.addFlags |= bits;
		return this;
	}

	@Override
	public DEHThingTemplate removeFlag(int bits)
	{
		if (this.flags != null)
			this.flags &= ~bits;
		else
			this.remFlags |= bits; // removed later
		return this;
	}

	public DEHThingTemplate setMBF21Flags(int bits) 
	{
		this.mbf21Flags = bits;
		return this;
	}

	@Override
	public DEHThingTemplate addMBF21Flag(int bits)
	{
		if (this.mbf21Flags != null)
			this.mbf21Flags |= bits;
		else
			this.addMBF21Flags |= bits;
		return this;
	}

	@Override
	public DEHThingTemplate removeMBF21Flag(int bits)
	{
		if (this.mbf21Flags != null)
			this.mbf21Flags &= ~bits;
		else
			this.remMBF21Flags |= bits; // removed later
		return this;
	}

	public DEHThingTemplate setMass(int mass) 
	{
		RangeUtils.checkRange("Mass", 0, Integer.MAX_VALUE, mass);
		this.mass = mass;
		return this;
	}

	public DEHThingTemplate setMeleeRange(int meleeRange) 
	{
		RangeUtils.checkRange("Melee range", 0, 65535, meleeRange);
		this.meleeRange = meleeRange;
		return this;
	}

	public DEHThingTemplate setInfightingGroup(int infightingGroup) 
	{
		RangeUtils.checkRange("Infighting group", 0, Integer.MAX_VALUE, infightingGroup);
		this.infightingGroup = infightingGroup;
		return this;
	}
	
	public DEHThingTemplate setProjectileGroup(int projectileGroup) 
	{
		RangeUtils.checkRange("Projectile group", -1, Integer.MAX_VALUE, projectileGroup);
		this.projectileGroup = projectileGroup;
		return this;
	}
	
	public DEHThingTemplate setSplashGroup(int splashGroup) 
	{
		RangeUtils.checkRange("Splash group", 0, Integer.MAX_VALUE, splashGroup);
		this.splashGroup = splashGroup;
		return this;
	}
	
	public DEHThingTemplate setDroppedItem(int droppedItem) 
	{
		RangeUtils.checkRange("Dropped item", 0, Integer.MAX_VALUE, droppedItem);
		this.droppedItem = droppedItem;
		return this;
	}
	
	public DEHThingTemplate setSpawnFrameIndex(int spawnFrameIndex)
	{
		RangeUtils.checkRange("Spawn frame index", 0, Integer.MAX_VALUE, spawnFrameIndex);
		setLabel(DEHThing.STATE_LABEL_SPAWN, spawnFrameIndex);
		return this;
	}

	public DEHThingTemplate setWalkFrameIndex(int walkFrameIndex) 
	{
		RangeUtils.checkRange("Walk frame index", 0, Integer.MAX_VALUE, walkFrameIndex);
		setLabel(DEHThing.STATE_LABEL_SEE, walkFrameIndex);
		return this;
	}

	public DEHThingTemplate setPainFrameIndex(int painFrameIndex) 
	{
		RangeUtils.checkRange("Pain frame index", 0, Integer.MAX_VALUE, painFrameIndex);
		setLabel(DEHThing.STATE_LABEL_PAIN, painFrameIndex);
		return this;
	}

	public DEHThingTemplate setMeleeFrameIndex(int meleeFrameIndex)
	{
		RangeUtils.checkRange("Melee frame index", 0, Integer.MAX_VALUE, meleeFrameIndex);
		setLabel(DEHThing.STATE_LABEL_MELEE, meleeFrameIndex);
		return this;
	}

	public DEHThingTemplate setMissileFrameIndex(int missileFrameIndex) 
	{
		RangeUtils.checkRange("Attack frame index", 0, Integer.MAX_VALUE, missileFrameIndex);
		setLabel(DEHThing.STATE_LABEL_MISSILE, missileFrameIndex);
		return this;
	}

	public DEHThingTemplate setDeathFrameIndex(int deathFrameIndex) 
	{
		RangeUtils.checkRange("Death frame index", 0, Integer.MAX_VALUE, deathFrameIndex);
		setLabel(DEHThing.STATE_LABEL_DEATH, deathFrameIndex);
		return this;
	}

	public DEHThingTemplate setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		RangeUtils.checkRange("Extreme death frame index", 0, Integer.MAX_VALUE, extremeDeathFrameIndex);
		setLabel(DEHThing.STATE_LABEL_XDEATH, extremeDeathFrameIndex);
		return this;
	}

	public DEHThingTemplate setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		setLabel(DEHThing.STATE_LABEL_RAISE, raiseFrameIndex);
		return this;
	}

	public DEHThingTemplate setLabel(String label, int index)
	{
		if (index < 0)
			throw new IllegalArgumentException("index cannot be < 0");
		
		if (index == 0)
			stateIndexMap.remove(label);
		else
			stateIndexMap.put(label, index);
		return this;
	}

	@Override
	public String[] getLabels()
	{
		return stateIndexMap.keySet().toArray(new String[stateIndexMap.size()]);
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
	public DEHThingTemplate clearLabels()
	{
		stateIndexMap.clear();
		return this;
	}

	public DEHThingTemplate setSeeSoundPosition(int seeSoundPosition)
	{
		RangeUtils.checkRange("Alert sound position", 0, Integer.MAX_VALUE, seeSoundPosition);
		this.seeSoundPosition = seeSoundPosition;
		return this;
	}

	public DEHThingTemplate setAttackSoundPosition(int attackSoundPosition)
	{
		RangeUtils.checkRange("Attack sound position", 0, Integer.MAX_VALUE, attackSoundPosition);
		this.attackSoundPosition = attackSoundPosition;
		return this;
	}

	public DEHThingTemplate setPainSoundPosition(int painSoundPosition) 
	{
		RangeUtils.checkRange("Pain sound position", 0, Integer.MAX_VALUE, painSoundPosition);
		this.painSoundPosition = painSoundPosition;
		return this;
	}

	public DEHThingTemplate setDeathSoundPosition(int deathSoundPosition) 
	{
		RangeUtils.checkRange("Death sound position", 0, Integer.MAX_VALUE, deathSoundPosition);
		this.deathSoundPosition = deathSoundPosition;
		return this;
	}

	public DEHThingTemplate setActiveSoundPosition(int activeSoundPosition)
	{
		RangeUtils.checkRange("Active sound position", 0, Integer.MAX_VALUE, activeSoundPosition);
		this.activeSoundPosition = activeSoundPosition;
		return this;
	}

	public DEHThingTemplate setRipSoundPosition(int ripSoundPosition)
	{
		RangeUtils.checkRange("Rip sound position", 0, Integer.MAX_VALUE, ripSoundPosition);
		this.ripSoundPosition = ripSoundPosition;
		return this;
	}

}
