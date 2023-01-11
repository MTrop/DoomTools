/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
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
	static final int NO_ITEM = 0;
	static final int DEFAULT_GROUP = 0;
	static final int DEFAULT_FASTSPEED = -1;
	static final int DEFAULT_MELEE_RANGE = 64;
	
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

	SELF setFastSpeed(int fastSpeed);

	SELF setRadius(int radius);

	SELF setHeight(int height);

	SELF setDamage(int damage);

	SELF setReactionTime(int reactionTime);

	SELF setPainChance(int painChance);

	SELF setFlags(int bits);

	SELF addFlag(int bits);
	
	SELF removeFlag(int bits);

	SELF setMBF21Flags(int bits);

	SELF addMBF21Flag(int bits);
	
	SELF removeMBF21Flag(int bits);

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

}
