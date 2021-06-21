/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
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
	public SELF setEditorNumber(int editorNumber);

	public SELF setHealth(int health);

	public SELF setSpeed(int speed);

	public SELF setFastSpeed(int fastSpeed);

	public SELF setRadius(int radius);

	public SELF setHeight(int height);

	public SELF setDamage(int damage);

	public SELF setReactionTime(int reactionTime);

	public SELF setPainChance(int painChance);

	public SELF setFlags(int bits);

	public SELF addFlag(int bits);
	
	public SELF removeFlag(int bits);

	public SELF setMBF21Flags(int bits);

	public SELF addMBF21Flag(int bits);
	
	public SELF removeMBF21Flag(int bits);

	public SELF setMass(int mass);

	public SELF setMeleeRange(int meleeRange);

	public SELF setInfightingGroup(int infightingGroup);
	
	public SELF setProjectileGroup(int projectileGroup);
	
	public SELF setSplashGroup(int splashGroup);
	
	public SELF setDroppedItem(int droppedItem);
	
	public SELF setSpawnFrameIndex(int spawnFrameIndex);

	public SELF setWalkFrameIndex(int walkFrameIndex);

	public SELF setPainFrameIndex(int painFrameIndex);

	public SELF setMeleeFrameIndex(int meleeFrameIndex);

	public SELF setMissileFrameIndex(int missileFrameIndex);

	public SELF setDeathFrameIndex(int deathFrameIndex);

	public SELF setExtremeDeathFrameIndex(int extremeDeathFrameIndex);

	public SELF setRaiseFrameIndex(int raiseFrameIndex);

	public SELF setLabel(String label, int index);

	public SELF setSeeSoundPosition(int seeSoundPosition);

	public SELF setAttackSoundPosition(int attackSoundPosition);

	public SELF setPainSoundPosition(int painSoundPosition);

	public SELF setDeathSoundPosition(int deathSoundPosition);

	public SELF setActiveSoundPosition(int activeSoundPosition);

	public SELF setRipSoundPosition(int ripSoundPosition);

}
