/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
	
	/** Editor keys. */
	private Map<String, String> editorKeyMap;

	private Integer health;
	private Integer speed; // written as fixed point 16.16 if MISSILE
	private Integer fixedSpeed; // used instead of speed if non-null.
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

	/** ID24 flags. */
	private Integer id24Flags;
	
	private int addID24Flags;
	private int remID24Flags;

	private Integer minRespawnTics;
	private Integer respawnDice;
	private Integer pickupAmmoType;
	private Integer pickupAmmoCategory;
	private Integer pickupWeaponType;
	private Integer pickupItemType;
	private Integer pickupBonusCount;
	private Integer pickupSound;
	private String pickupMessage;
	private String translation;
	private Integer selfDamageFactor; // stored as fixed point 16.16 

	/** Custom properties. */
	private Map<DEHProperty, String> customProperties;
	
	/**
	 * Creates a new blank thing.
	 */
	public DEHThingTemplate()
	{
		this.editorNumber = null;
		
		this.editorKeyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		
		this.health = null;
		this.speed = null;
		this.fixedSpeed = null;
		this.radius = null;
		this.height = null;
		this.damage = null;
		this.reactionTime = null;
		this.painChance = null;
		this.mass = null;

		this.flags = null;
		this.addFlags = 0;
		this.remFlags = 0;
		
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		this.seeSoundPosition = null;
		this.attackSoundPosition = null;
		this.painSoundPosition = null;
		this.deathSoundPosition = null;
		this.activeSoundPosition = null;
		
		// EXTENDED
		this.droppedItem = null;

		// MBF21
		this.mbf21Flags = null;
		this.addMBF21Flags = 0;
		this.remMBF21Flags = 0;
		
		this.infightingGroup = null;
		this.projectileGroup = null;
		this.splashGroup = null;
		this.fastSpeed = null;
		this.meleeRange = null;
		this.ripSoundPosition = null;

		// ID24
		this.id24Flags = null;
		this.addID24Flags = 0;
		this.remID24Flags = 0;
		
		this.minRespawnTics = null;
		this.respawnDice = null;
		this.pickupAmmoType = null;
		this.pickupAmmoCategory = null;
		this.pickupWeaponType = null;
		this.pickupItemType = null;
		this.pickupBonusCount = null;
		this.pickupSound = null;
		this.pickupMessage = null;
		this.translation = null;
		this.selfDamageFactor = null;

		this.customProperties = new HashMap<>();
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
		
		for (Map.Entry<String, String> entry : editorKeyMap.entrySet())
			destination.setEditorKey(entry.getKey(), entry.getValue());
		
		if (health != null)
			destination.setHealth(health);
		if (fixedSpeed != null)
			destination.setFixedSpeed(fixedSpeed);
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
		
		for (String label : stateIndexMap.keySet())
			destination.setLabel(label, stateIndexMap.getOrDefault(label, 0));

		// EXTENDED
		
		if (droppedItem != null)
			destination.setDroppedItem(droppedItem);
	
		// MBF21

		// if flags altered, replace.
		if (mbf21Flags != null)
			destination.setMBF21Flags(mbf21Flags);
		// else, just alter the adjustments.
		else
		{
			destination.addMBF21Flag(addMBF21Flags);
			destination.removeMBF21Flag(remMBF21Flags);
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

		// ID24

		// if flags altered, replace.
		if (id24Flags != null)
			destination.setID24Flags(id24Flags);
		// else, just alter the adjustments.
		else
		{
			destination.addID24Flag(addID24Flags);
			destination.removeID24Flag(remID24Flags);
		}

		if (minRespawnTics != null)
			destination.setMinRespawnTics(minRespawnTics);
		if (respawnDice != null)
			destination.setRespawnDice(respawnDice);
		if (pickupAmmoType != null)
			destination.setPickupAmmoType(pickupAmmoType);
		if (pickupAmmoCategory != null)
			destination.setPickupAmmoCategory(pickupAmmoCategory);
		if (pickupWeaponType != null)
			destination.setPickupWeaponType(pickupWeaponType);
		if (pickupItemType != null)
			destination.setPickupItemType(pickupItemType);
		if (pickupBonusCount != null)
			destination.setPickupBonusCount(pickupBonusCount);
		if (pickupSound != null)
			destination.setPickupSoundPosition(pickupSound);
		if (pickupMessage != null)
			destination.setPickupMessageMnemonic(pickupMessage);
		if (translation != null)
			destination.setTranslation(translation);
		if (selfDamageFactor != null)
			destination.setSelfDamageFactor(selfDamageFactor);
		
		for (Map.Entry<DEHProperty, String> property : customProperties.entrySet())
			destination.setCustomPropertyValue(property.getKey(), property.getValue());

		return this;
	}
	
	/**
	 * Sets a custom property value.
	 * @param property the property.
	 * @param value the value.
	 */
	public void setCustomPropertyValue(DEHProperty property, int value)
	{
		setCustomPropertyValue(property, String.valueOf(value));
	}

	@Override
	public void setCustomPropertyValue(DEHProperty property, String value)
	{
		customProperties.put(property, value);
	}

	@Override
	public void clearCustomPropertyValues()
	{
		customProperties.clear();
	}

	@Override
	public DEHThingTemplate clearProperties() 
	{
		setEditorNumber(EDITORNUMBER_NONE);
		setHealth(0);
		setSpeed(0);
		setFixedSpeed(null);
		setRadius(0);
		setHeight(0);
		setDamage(0);
		setReactionTime(0);
		setPainChance(0);
		setMass(0);

		// EXTENDED
		setDroppedItem(NO_ITEM);
		
		// MBF21
		setInfightingGroup(DEFAULT_GROUP);
		setProjectileGroup(DEFAULT_GROUP);
		setSplashGroup(DEFAULT_GROUP);
		setFastSpeed(DEFAULT_FASTSPEED);
		setMeleeRange(DEFAULT_MELEE_RANGE);
		
		// ID24
		setMinRespawnTics(DEFAULT_MIN_RESPAWN_TICS);
		setRespawnDice(DEFAULT_RESPAWN_DICE);
		setPickupAmmoType(DEFAULT_PICKUP_AMMO_TYPE);
		setPickupAmmoCategory(DEFAULT_PICKUP_AMMO_CATEGORY);
		setPickupWeaponType(DEFAULT_PICKUP_WEAPON_TYPE);
		setPickupItemType(DEFAULT_PICKUP_ITEM_TYPE);
		setPickupBonusCount(DEFAULT_PICKUP_BONUS_COUNT);
		setPickupMessageMnemonic(DEFAULT_PICKUP_MESSAGE);
		setTranslation(DEFAULT_TRANSLATION);
		setSelfDamageFactor(DEFAULT_SELF_DAMAGE_FACTOR);
		
		clearCustomPropertyValues();
		return this;
	}

	@Override
	public DEHThingTemplate clearSounds() 
	{
		setSeeSoundPosition(SOUND_NONE);
		setAttackSoundPosition(SOUND_NONE);
		setPainSoundPosition(SOUND_NONE);
		setDeathSoundPosition(SOUND_NONE);
		setActiveSoundPosition(SOUND_NONE);
		// MBF21
		setRipSoundPosition(SOUND_NONE);
		// ID24
		setPickupSoundPosition(SOUND_NONE);
		return this;
	}

	@Override
	public DEHThingTemplate clearFlags() 
	{
		setFlags(0x00000000);
		setMBF21Flags(0x00000000);
		setID24Flags(0x00000000);
		return this;
	}

	@Override
	public DEHThingTemplate clearLabels()
	{
		stateIndexMap.clear();
		// If a template clears labels, explicitly set state 0.
		setSpawnFrameIndex(0);
		setWalkFrameIndex(0);
		setPainFrameIndex(0);
		setMeleeFrameIndex(0);
		setMissileFrameIndex(0);
		setDeathFrameIndex(0);
		setExtremeDeathFrameIndex(0);
		setRaiseFrameIndex(0);
		return this;
	}

	@Override
	public DEHThingTemplate setEditorNumber(int editorNumber)
	{
		if (editorNumber == 0)
			throw new IllegalArgumentException("Editor number can not be 0.");
		RangeUtils.checkShort("Editor number", editorNumber);
		this.editorNumber = editorNumber;
		return this;
	}

	@Override
	public DEHThingTemplate clearEditorKeys() 
	{
		editorKeyMap.clear();
		return this;
	}
	
	@Override
	public DEHThingTemplate setEditorKey(String key, String value)
	{
		editorKeyMap.put(key, value);
		return this;
	}

	@Override
	public String[] getEditorKeys() 
	{
		Set<String> keySet = editorKeyMap.keySet();
		return keySet.toArray(new String[keySet.size()]);
	}

	@Override
	public String getEditorKey(String key) 
	{
		return editorKeyMap.get(key);
	}
	
	@Override
	public DEHThingTemplate setHealth(int health)
	{
		RangeUtils.checkRange("Health", 0, 999999, health);
		this.health = health;
		return this;
	}

	@Override
	public DEHThingTemplate setSpeed(int speed) 
	{
		RangeUtils.checkRange("Speed", -32768, 32767, speed);
		this.speed = speed;
		this.fixedSpeed = null; // set fixed speed to null if this is set.
		return this;
	}

	@Override
	public DEHThingTemplate setFixedSpeed(Integer fixedSpeed)
	{
		this.fixedSpeed = fixedSpeed;
		return this;
	}

	@Override
	public DEHThingTemplate setFastSpeed(int fastSpeed) 
	{
		RangeUtils.checkRange("Fast speed", -1, 65535, fastSpeed);
		this.fastSpeed = fastSpeed;
		return this;
	}

	@Override
	public DEHThingTemplate setRadius(int radius) 
	{
		RangeUtils.checkRange("Radius", 0, 65535, radius);
		this.radius = radius;
		return this;
	}

	@Override
	public DEHThingTemplate setHeight(int height) 
	{
		RangeUtils.checkRange("Height", 0, 65535, height);
		this.height = height;
		return this;
	}
	
	@Override
	public DEHThingTemplate setDamage(int damage) 
	{
		RangeUtils.checkRange("Damage", -999999, 999999, damage);
		this.damage = damage;
		return this;
	}

	@Override
	public DEHThingTemplate setReactionTime(int reactionTime)
	{
		RangeUtils.checkRange("Reaction time", 0, Integer.MAX_VALUE, reactionTime);
		this.reactionTime = reactionTime;
		return this;
	}

	@Override
	public DEHThingTemplate setPainChance(int painChance)
	{
		RangeUtils.checkRange("Pain chance", 0, Integer.MAX_VALUE, painChance);
		this.painChance = painChance;
		return this;
	}

	@Override
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

	@Override
	public boolean hasFlag(int bit)
	{
		if (this.flags == null)
		{
			return (this.addFlags & bit) != 0
				&& (this.remFlags & bit) == 0
			;
		}
		else
		{
			return (this.flags & bit) != 0;
		}
	}

	@Override
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
			this.addMBF21Flags |= bits; // added later
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

	@Override
	public boolean hasMBF21Flag(int bit)
	{
		if (this.mbf21Flags == null)
		{
			return (this.addMBF21Flags & bit) != 0
				&& (this.remMBF21Flags & bit) == 0
			;
		}
		else
		{
			return (this.mbf21Flags & bit) != 0;
		}
	}

	@Override
	public DEHThingTemplate setMass(int mass) 
	{
		this.mass = mass;
		return this;
	}

	@Override
	public DEHThingTemplate setMeleeRange(int meleeRange) 
	{
		RangeUtils.checkRange("Melee range", 0, 65535, meleeRange);
		this.meleeRange = meleeRange;
		return this;
	}

	@Override
	public DEHThingTemplate setInfightingGroup(int infightingGroup) 
	{
		RangeUtils.checkRange("Infighting group", 0, Integer.MAX_VALUE, infightingGroup);
		this.infightingGroup = infightingGroup;
		return this;
	}
	
	@Override
	public DEHThingTemplate setProjectileGroup(int projectileGroup) 
	{
		RangeUtils.checkRange("Projectile group", -1, Integer.MAX_VALUE, projectileGroup);
		this.projectileGroup = projectileGroup;
		return this;
	}
	
	@Override
	public DEHThingTemplate setSplashGroup(int splashGroup) 
	{
		RangeUtils.checkRange("Splash group", 0, Integer.MAX_VALUE, splashGroup);
		this.splashGroup = splashGroup;
		return this;
	}
	
	@Override
	public DEHThingTemplate setDroppedItem(int droppedItem) 
	{
		RangeUtils.checkRange("Dropped item", 0, Integer.MAX_VALUE, droppedItem);
		this.droppedItem = droppedItem;
		return this;
	}
	
	@Override
	public DEHThingTemplate setSpawnFrameIndex(int spawnFrameIndex)
	{
		RangeUtils.checkRange("Spawn frame index", 0, Integer.MAX_VALUE, spawnFrameIndex);
		setLabel(DEHThing.STATE_LABEL_SPAWN, spawnFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setWalkFrameIndex(int walkFrameIndex) 
	{
		RangeUtils.checkRange("Walk frame index", 0, Integer.MAX_VALUE, walkFrameIndex);
		setLabel(DEHThing.STATE_LABEL_SEE, walkFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setPainFrameIndex(int painFrameIndex) 
	{
		RangeUtils.checkRange("Pain frame index", 0, Integer.MAX_VALUE, painFrameIndex);
		setLabel(DEHThing.STATE_LABEL_PAIN, painFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setMeleeFrameIndex(int meleeFrameIndex)
	{
		RangeUtils.checkRange("Melee frame index", 0, Integer.MAX_VALUE, meleeFrameIndex);
		setLabel(DEHThing.STATE_LABEL_MELEE, meleeFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setMissileFrameIndex(int missileFrameIndex) 
	{
		RangeUtils.checkRange("Attack frame index", 0, Integer.MAX_VALUE, missileFrameIndex);
		setLabel(DEHThing.STATE_LABEL_MISSILE, missileFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setDeathFrameIndex(int deathFrameIndex) 
	{
		RangeUtils.checkRange("Death frame index", 0, Integer.MAX_VALUE, deathFrameIndex);
		setLabel(DEHThing.STATE_LABEL_DEATH, deathFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		RangeUtils.checkRange("Extreme death frame index", 0, Integer.MAX_VALUE, extremeDeathFrameIndex);
		setLabel(DEHThing.STATE_LABEL_XDEATH, extremeDeathFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setRaiseFrameIndex(int raiseFrameIndex) 
	{
		RangeUtils.checkRange("Raise frame index", 0, Integer.MAX_VALUE, raiseFrameIndex);
		setLabel(DEHThing.STATE_LABEL_RAISE, raiseFrameIndex);
		return this;
	}

	@Override
	public DEHThingTemplate setLabel(String label, int index)
	{
		if (index < 0)
			throw new IllegalArgumentException("index cannot be < 0");
		
		// 0 is a valid index for applying to things - preserve in map.
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
	public DEHThingTemplate setSeeSoundPosition(int seeSoundPosition)
	{
		RangeUtils.checkRange("Alert sound position", 0, Integer.MAX_VALUE, seeSoundPosition);
		this.seeSoundPosition = seeSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setAttackSoundPosition(int attackSoundPosition)
	{
		RangeUtils.checkRange("Attack sound position", 0, Integer.MAX_VALUE, attackSoundPosition);
		this.attackSoundPosition = attackSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setPainSoundPosition(int painSoundPosition) 
	{
		RangeUtils.checkRange("Pain sound position", 0, Integer.MAX_VALUE, painSoundPosition);
		this.painSoundPosition = painSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setDeathSoundPosition(int deathSoundPosition) 
	{
		RangeUtils.checkRange("Death sound position", 0, Integer.MAX_VALUE, deathSoundPosition);
		this.deathSoundPosition = deathSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setActiveSoundPosition(int activeSoundPosition)
	{
		RangeUtils.checkRange("Active sound position", 0, Integer.MAX_VALUE, activeSoundPosition);
		this.activeSoundPosition = activeSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setRipSoundPosition(int ripSoundPosition)
	{
		RangeUtils.checkRange("Rip sound position", 0, Integer.MAX_VALUE, ripSoundPosition);
		this.ripSoundPosition = ripSoundPosition;
		return this;
	}

	@Override
	public DEHThingTemplate setID24Flags(int bits) 
	{
		this.id24Flags = bits;
		return this;
	}

	@Override
	public DEHThingTemplate addID24Flag(int bits)
	{
		if (this.id24Flags != null)
			this.id24Flags |= bits;
		else
			this.addID24Flags |= bits; // added later
		return this;
	}

	@Override
	public DEHThingTemplate removeID24Flag(int bits)
	{
		if (this.id24Flags != null)
			this.id24Flags &= ~bits;
		else
			this.remID24Flags |= bits; // removed later
		return this;
	}

	@Override
	public boolean hasID24Flag(int bit)
	{
		if (this.id24Flags == null)
		{
			return (this.addID24Flags & bit) != 0
				&& (this.remID24Flags & bit) == 0
			;
		}
		else
		{
			return (this.id24Flags & bit) != 0;
		}
	}

	@Override
	public DEHThingTemplate setMinRespawnTics(int tics)
	{
		this.minRespawnTics = tics;
		return this;
	}
	
	@Override
	public DEHThingTemplate setRespawnDice(int dice)
	{
		this.respawnDice = dice;
		return this;
	}

	@Override
	public DEHThingTemplate setPickupAmmoType(int typeId)
	{
		this.pickupAmmoType = typeId;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupAmmoCategory(int categoryBits)
	{
		this.pickupAmmoCategory = categoryBits;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupWeaponType(int weaponTypeId)
	{
		this.pickupWeaponType = weaponTypeId;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupItemType(int itemTypeId)
	{
		this.pickupItemType = itemTypeId;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupBonusCount(int count)
	{
		this.pickupBonusCount = count;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupSoundPosition(int soundPosition)
	{
		this.pickupSound = soundPosition;
		return this;
	}
	
	@Override
	public DEHThingTemplate setPickupMessageMnemonic(String message)
	{
		this.pickupMessage = message;
		return this;
	}
	
	@Override
	public DEHThingTemplate setTranslation(String name)
	{
		this.translation = name;
		return this;
	}

	@Override
	public DEHThingTemplate setSelfDamageFactor(int selfDamageFactor)
	{
		this.selfDamageFactor = selfDamageFactor;
		return this;
	}

}
