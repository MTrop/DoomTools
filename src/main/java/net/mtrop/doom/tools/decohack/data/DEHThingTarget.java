/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * An interface that describes a Thing whose information can be set.
 * NOTE: All sound positions are 1-BASED. 0 = no sound, [index+1] is the sound.
 * @author Matthew Tropiano
 * @param <SELF> This object type.
 */
public interface DEHThingTarget<SELF extends DEHThingTarget<SELF>> extends DEHActor<DEHThingTarget<SELF>>
{
	static final String STATE_LABEL_SPAWN = "spawn";
	static final String STATE_LABEL_SEE = "see";
	static final String STATE_LABEL_MELEE = "melee";
	static final String STATE_LABEL_MISSILE = "missile";
	static final String STATE_LABEL_PAIN = "pain";
	static final String STATE_LABEL_DEATH = "death";
	static final String STATE_LABEL_XDEATH = "xdeath";
	static final String STATE_LABEL_RAISE = "raise";

	static final int EDITORNUMBER_NONE = -1;
	static final int SOUND_NONE = 0;
	static final int FRAME_NULL = 0;
	static final int NO_ITEM = -1;
	
	static final int DEFAULT_GROUP = 0;
	static final int DEFAULT_FASTSPEED = -1;
	static final int DEFAULT_MELEE_RANGE = 64;
	
	static final int DEFAULT_ID24BITS = 0;
	static final int DEFAULT_MIN_RESPAWN_TICS = 420;
	static final int DEFAULT_RESPAWN_DICE = 4;
	static final int DEFAULT_PICKUP_AMMO_TYPE = -1;
	static final int DEFAULT_PICKUP_AMMO_CATEGORY = -1;
	static final int DEFAULT_PICKUP_WEAPON_TYPE = -1;
	static final int DEFAULT_PICKUP_ITEM_TYPE = -1;
	static final int DEFAULT_PICKUP_BONUS_COUNT = 6;
	static final int DEFAULT_PICKUP_SOUND = 0;
	static final String DEFAULT_PICKUP_MESSAGE = null;
	static final String DEFAULT_TRANSLATION = null;
	static final int DEFAULT_SELF_DAMAGE_FACTOR = (1 << 16); // fixed 1.0
	
	/**
	 * Clears the properties.
	 * @return this object.
	 */
	SELF clearProperties();
	
	/**
	 * Clears the sounds.
	 * @return this object.
	 */
	SELF clearSounds();

	/**
	 * Clears the sounds.
	 * @return this object.
	 */
	SELF clearFlags();
	
	SELF setEditorNumber(int editorNumber);

	/**
	 * Clears/resets editor keys defined on this actor.
	 * @return this object.
	 */
	SELF clearEditorKeys();

	/**
	 * Sets an editor key on this Thing.
	 * @param key the key.
	 * @param value the value.
	 * @return this thing.
	 */
	SELF setEditorKey(String key, String value);

	/**
	 * @return all set editor keys.
	 */
	String[] getEditorKeys();
	
	/**
	 * Gets an editor key value.
	 * @param key the key name.
	 * @return the key value, or null if not set.
	 */
	String getEditorKey(String key);
	
	void setCustomPropertyValue(DEHProperty property, String value);

	void clearCustomPropertyValues();
	
	SELF setHealth(int health);

	SELF setSpeed(int speed);

	SELF setFixedSpeed(Integer fixedSpeed);

	SELF setFastSpeed(int fastSpeed);

	SELF setRadius(int radius);

	SELF setHeight(int height);

	SELF setDamage(int damage);

	SELF setReactionTime(int reactionTime);

	SELF setPainChance(int painChance);

	SELF setFlags(int bits);

	SELF addFlag(int bits);
	
	SELF removeFlag(int bits);
	
	boolean hasFlag(int bit);

	SELF setMBF21Flags(int bits);

	SELF addMBF21Flag(int bits);
	
	SELF removeMBF21Flag(int bits);

	boolean hasMBF21Flag(int bit);

	SELF setMass(int mass);

	SELF setMeleeRange(int meleeRange);

	SELF setInfightingGroup(int infightingGroup);
	
	SELF setProjectileGroup(int projectileGroup);
	
	SELF setSplashGroup(int splashGroup);
	
	SELF setDroppedItem(int droppedItem);
	
	SELF setSpawnFrameIndex(int spawnFrameIndex);

	SELF setWalkFrameIndex(int walkFrameIndex);

	SELF setPainFrameIndex(int painFrameIndex);

	SELF setMeleeFrameIndex(int meleeFrameIndex);

	SELF setMissileFrameIndex(int missileFrameIndex);

	SELF setDeathFrameIndex(int deathFrameIndex);

	SELF setExtremeDeathFrameIndex(int extremeDeathFrameIndex);

	SELF setRaiseFrameIndex(int raiseFrameIndex);

	SELF setLabel(String label, int index);

	SELF setSeeSoundPosition(int seeSoundPosition);

	SELF setAttackSoundPosition(int attackSoundPosition);

	SELF setPainSoundPosition(int painSoundPosition);

	SELF setDeathSoundPosition(int deathSoundPosition);

	SELF setActiveSoundPosition(int activeSoundPosition);

	SELF setRipSoundPosition(int ripSoundPosition);

	SELF setID24Flags(int bits);

	SELF addID24Flag(int bits);

	SELF removeID24Flag(int bits);

	boolean hasID24Flag(int bit);
	
	SELF setMinRespawnTics(int tics);
	
	SELF setRespawnDice(int dice);
	
	SELF setPickupAmmoType(int typeId);
	
	SELF setPickupAmmoCategory(int categoryBits);
	
	SELF setPickupWeaponType(int weaponTypeId);
	
	SELF setPickupItemType(int itemTypeId);
	
	SELF setPickupBonusCount(int count);
	
	SELF setPickupSoundPosition(int soundPosition);
	
	SELF setPickupMessageMnemonic(String message);
	
	SELF setTranslation(String name);
	
	SELF setSelfDamageFactor(int selfDamageFactor);
	
}
