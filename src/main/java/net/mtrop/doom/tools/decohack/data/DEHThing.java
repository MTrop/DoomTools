/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingFlag;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single thing entry.
 * NOTE: All sound positions are 1-BASED. 0 = no sound, [index+1] is the sound.
 * @author Matthew Tropiano
 */
public class DEHThing extends DEHObject<DEHThing> implements DEHThingTarget<DEHThing>
{
	private String name;
	
	private int editorNumber;
	
	/** Editor keys. */
	private Map<String, String> editorKeyMap;
	
	private int health;
	private int speed; // written as fixed point 16.16 if MISSILE
	private Integer fixedSpeed; // used instead of speed if non-null.
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

	/** Dropped item. */
	private int droppedItem;

	/** MBF21 flags. */
	private int mbf21Flags;
	/** Infighting group. */
	private int infightingGroup;
	/** Projectile group. */
	private int projectileGroup;
	/** Splash group. */
	private int splashGroup;
	/** Nightmare/Fast speed of actor. */
	private int fastSpeed; // written as fixed point 16.16 if MISSILE
	/** Melee range. */
	private int meleeRange;  // written as fixed point 16.16
	
	/** Ripper sound position. */
	private int ripSoundPosition;

	/** ID24 flags. */
	private int id24Flags;
	private int minRespawnTics;
	private int respawnDice;
	private int pickupAmmoType;
	private int pickupAmmoCategory;
	private int pickupWeaponType;
	private int pickupItemType;
	private int pickupBonusCount;
	private String pickupMessage;
	private String translation;
	private int selfDamageFactor; // stored as fixed 16.16
	
	private int pickupSoundPosition;
	
	/**
	 * Creates a new blank thing.
	 */
	public DEHThing()
	{
		this.stateIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.editorKeyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		setName("");
		clearProperties();
		clearEditorKeys();
		clearFlags();
		clearSounds();
		clearLabels();
	}

	@Override
	public DEHThing copyFrom(DEHThing source) 
	{
		if (source == this)
			return this;
			
		setName(source.name);
		setEditorNumber(source.editorNumber);

		clearEditorKeys();
		for (String key : source.getEditorKeys())
			setEditorKey(key, source.getEditorKey(key));

		setHealth(source.health);
		setSpeed(source.speed);
		setFixedSpeed(source.fixedSpeed);
		setFastSpeed(source.fastSpeed);
		setRadius(source.radius);
		setHeight(source.height);
		setDamage(source.damage);
		setReactionTime(source.reactionTime);
		setPainChance(source.painChance);
		setFlags(source.flags);
		setMass(source.mass);
		
		setSeeSoundPosition(source.seeSoundPosition);
		setAttackSoundPosition(source.attackSoundPosition);
		setPainSoundPosition(source.painSoundPosition);
		setDeathSoundPosition(source.deathSoundPosition);
		setActiveSoundPosition(source.activeSoundPosition);
		
		clearLabels();
		for (String label : source.getLabels())
			setLabel(label, source.getLabel(label));

		// EXTENDED
		setDroppedItem(source.droppedItem);

		// MBF21
		setMBF21Flags(source.mbf21Flags);
		setInfightingGroup(source.infightingGroup);
		setProjectileGroup(source.projectileGroup);
		setSplashGroup(source.splashGroup);
		setFastSpeed(source.fastSpeed);
		setMeleeRange(source.meleeRange);
		setRipSoundPosition(source.ripSoundPosition);

		// ID24
		setID24Flags(source.id24Flags);
		setMinRespawnTics(source.minRespawnTics);
		setRespawnDice(source.respawnDice);
		setPickupAmmoType(source.pickupAmmoType);
		setPickupAmmoCategory(source.pickupAmmoCategory);
		setPickupWeaponType(source.pickupWeaponType);
		setPickupItemType(source.pickupItemType);
		setPickupBonusCount(source.pickupBonusCount);
		setPickupSoundPosition(source.pickupSoundPosition);
		setPickupMessageMnemonic(source.pickupMessage);
		setTranslation(source.translation);
		setSelfDamageFactor(source.selfDamageFactor);

		return this;
	}
	
	@Override
	public DEHThing clearProperties() 
	{
		setEditorNumber(EDITORNUMBER_NONE);
		editorKeyMap.clear();
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
	public DEHThing clearSounds() 
	{
		setSeeSoundPosition(SOUND_NONE);
		setAttackSoundPosition(SOUND_NONE);
		setPainSoundPosition(SOUND_NONE);
		setDeathSoundPosition(SOUND_NONE);
		setActiveSoundPosition(SOUND_NONE);
		setRipSoundPosition(SOUND_NONE);
		setPickupSoundPosition(SOUND_NONE);
		return this;
	}

	@Override
	public DEHThing clearFlags() 
	{
		setFlags(0x00000000);
		setMBF21Flags(0x00000000);
		setID24Flags(0x00000000);
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

	@Override
	public DEHThing setEditorNumber(int editorNumber)
	{
		if (editorNumber == 0)
			throw new IllegalArgumentException("Editor number can not be 0.");
		RangeUtils.checkShort("Editor number", editorNumber);
		this.editorNumber = editorNumber;
		return this;
	}

	@Override
	public DEHThing clearEditorKeys() 
	{
		editorKeyMap.clear();
		return this;
	}
	
	@Override
	public DEHThing setEditorKey(String key, String value)
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
	
	public boolean hasEditorKeys()
	{
		return !editorKeyMap.isEmpty();
	}
	
	public int getHealth() 
	{
		return health;
	}

	@Override
	public DEHThing setHealth(int health)
	{
		RangeUtils.checkRange("Health", 0, Integer.MAX_VALUE, health);
		this.health = health;
		return this;
	}

	public int getSpeed()
	{
		return speed;
	}

	@Override
	public DEHThing setSpeed(int speed) 
	{
		RangeUtils.checkRange("Speed", -32768, 32767, speed);
		this.speed = speed;
		this.fixedSpeed = null; // reset fixed speed if set.
		return this;
	}
	
	public Integer getFixedSpeed() 
	{
		return fixedSpeed;
	}
	
	@Override
	public DEHThing setFixedSpeed(Integer fixedSpeed)
	{
		this.fixedSpeed = fixedSpeed;
		return this;
	}

	public int getFastSpeed() 
	{
		return fastSpeed;
	}

	@Override
	public DEHThing setFastSpeed(int fastSpeed) 
	{
		RangeUtils.checkRange("Fast speed", -1, 65535, fastSpeed);
		this.fastSpeed = fastSpeed;
		return this;
	}

	public int getRadius() 
	{
		return radius;
	}

	@Override
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

	@Override
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

	@Override
	public DEHThing setDamage(int damage) 
	{
		this.damage = damage;
		return this;
	}

	public int getReactionTime()
	{
		return reactionTime;
	}

	@Override
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

	@Override
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

	@Override
	public DEHThing setFlags(int bits) 
	{
		this.flags = bits;
		return this;
	}

	@Override
	public DEHThing addFlag(int bits)
	{
		this.flags |= bits;
		return this;
	}

	@Override
	public DEHThing removeFlag(int bits)
	{
		this.flags &= ~bits;
		return this;
	}

	@Override
	public boolean hasFlag(int bit)
	{
		return (this.flags & bit) != 0;
	}

	public int getMBF21Flags() 
	{
		return mbf21Flags;
	}

	public DEHThing setMBF21Flags(int bits) 
	{
		this.mbf21Flags = bits;
		return this;
	}

	@Override
	public DEHThing addMBF21Flag(int bits)
	{
		this.mbf21Flags |= bits;
		return this;
	}

	@Override
	public DEHThing removeMBF21Flag(int bits)
	{
		this.mbf21Flags &= ~bits;
		return this;
	}

	@Override
	public boolean hasMBF21Flag(int bit) 
	{
		return (this.mbf21Flags & bit) != 0;
	}

	public int getMass() 
	{
		return mass;
	}

	@Override
	public DEHThing setMass(int mass) 
	{
		this.mass = mass;
		return this;
	}

	public int getMeleeRange() 
	{
		return meleeRange;
	}

	@Override
	public DEHThing setMeleeRange(int meleeRange) 
	{
		RangeUtils.checkRange("Melee range", 0, 65535, meleeRange);
		this.meleeRange = meleeRange;
		return this;
	}

	public int getInfightingGroup() 
	{
		return infightingGroup;
	}
	
	@Override
	public DEHThing setInfightingGroup(int infightingGroup) 
	{
		RangeUtils.checkRange("Infighting group", 0, Integer.MAX_VALUE, infightingGroup);
		this.infightingGroup = infightingGroup;
		return this;
	}
	
	public int getProjectileGroup() 
	{
		return projectileGroup;
	}
	
	@Override
	public DEHThing setProjectileGroup(int projectileGroup) 
	{
		RangeUtils.checkRange("Projectile group", -1, Integer.MAX_VALUE, projectileGroup);
		this.projectileGroup = projectileGroup;
		return this;
	}
	
	public int getSplashGroup()
	{
		return splashGroup;
	}
	
	@Override
	public DEHThing setSplashGroup(int splashGroup) 
	{
		RangeUtils.checkRange("Splash group", 0, Integer.MAX_VALUE, splashGroup);
		this.splashGroup = splashGroup;
		return this;
	}
	
	public int getDroppedItem() 
	{
		return droppedItem;
	}
	
	@Override
	public DEHThing setDroppedItem(int droppedItem) 
	{
		this.droppedItem = droppedItem;
		return this;
	}
	
	public int getSpawnFrameIndex()
	{
		return getLabel(STATE_LABEL_SPAWN);
	}

	@Override
	public DEHThing setSpawnFrameIndex(int spawnFrameIndex)
	{
		setLabel(STATE_LABEL_SPAWN, spawnFrameIndex);
		return this;
	}

	public int getWalkFrameIndex() 
	{
		return getLabel(STATE_LABEL_SEE);
	}

	@Override
	public DEHThing setWalkFrameIndex(int walkFrameIndex) 
	{
		setLabel(STATE_LABEL_SEE, walkFrameIndex);
		return this;
	}

	public int getPainFrameIndex()
	{
		return getLabel(STATE_LABEL_PAIN);
	}

	@Override
	public DEHThing setPainFrameIndex(int painFrameIndex) 
	{
		setLabel(STATE_LABEL_PAIN, painFrameIndex);
		return this;
	}

	public int getMeleeFrameIndex() 
	{
		return getLabel(STATE_LABEL_MELEE);
	}

	@Override
	public DEHThing setMeleeFrameIndex(int meleeFrameIndex)
	{
		setLabel(STATE_LABEL_MELEE, meleeFrameIndex);
		return this;
	}

	public int getMissileFrameIndex() 
	{
		return getLabel(STATE_LABEL_MISSILE);
	}

	@Override
	public DEHThing setMissileFrameIndex(int missileFrameIndex) 
	{
		setLabel(STATE_LABEL_MISSILE, missileFrameIndex);
		return this;
	}

	public int getDeathFrameIndex() 
	{
		return getLabel(STATE_LABEL_DEATH);
	}

	@Override
	public DEHThing setDeathFrameIndex(int deathFrameIndex) 
	{
		setLabel(STATE_LABEL_DEATH, deathFrameIndex);
		return this;
	}

	public int getExtremeDeathFrameIndex() 
	{
		return getLabel(STATE_LABEL_XDEATH);
	}

	@Override
	public DEHThing setExtremeDeathFrameIndex(int extremeDeathFrameIndex)
	{
		setLabel(STATE_LABEL_XDEATH, extremeDeathFrameIndex);
		return this;
	}

	public int getRaiseFrameIndex()
	{
		return getLabel(STATE_LABEL_RAISE);
	}

	@Override
	public DEHThing setRaiseFrameIndex(int raiseFrameIndex) 
	{
		setLabel(STATE_LABEL_RAISE, raiseFrameIndex);
		return this;
	}

	@Override
	public String[] getLabels()
	{
		Set<String> labelSet = stateIndexMap.keySet();
		return labelSet.toArray(new String[labelSet.size()]);
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
	public DEHThing setLabel(String label, int index)
	{
		if (index == 0)
			stateIndexMap.remove(label);
		else
			stateIndexMap.put(label, index);
		return this;
	}

	@Override
	public DEHThing clearLabels()
	{
		stateIndexMap.clear();
		return this;
	}

	public int getSeeSoundPosition() 
	{
		return seeSoundPosition;
	}

	@Override
	public DEHThing setSeeSoundPosition(int seeSoundPosition)
	{
		this.seeSoundPosition = seeSoundPosition;
		return this;
	}

	public int getAttackSoundPosition()
	{
		return attackSoundPosition;
	}

	@Override
	public DEHThing setAttackSoundPosition(int attackSoundPosition)
	{
		this.attackSoundPosition = attackSoundPosition;
		return this;
	}

	public int getPainSoundPosition()
	{
		return painSoundPosition;
	}

	@Override
	public DEHThing setPainSoundPosition(int painSoundPosition) 
	{
		this.painSoundPosition = painSoundPosition;
		return this;
	}

	public int getDeathSoundPosition()
	{
		return deathSoundPosition;
	}

	@Override
	public DEHThing setDeathSoundPosition(int deathSoundPosition) 
	{
		this.deathSoundPosition = deathSoundPosition;
		return this;
	}

	public int getActiveSoundPosition()
	{
		return activeSoundPosition;
	}

	@Override
	public DEHThing setActiveSoundPosition(int activeSoundPosition)
	{
		this.activeSoundPosition = activeSoundPosition;
		return this;
	}

	public int getRipSoundPosition()
	{
		return ripSoundPosition;
	}

	@Override
	public DEHThing setRipSoundPosition(int ripSoundPosition)
	{
		this.ripSoundPosition = ripSoundPosition;
		return this;
	}

	@Override
	public DEHThing setID24Flags(int bits) 
	{
		id24Flags = bits;
		return this;
	}

	@Override
	public DEHThing addID24Flag(int bits) 
	{
		id24Flags |= bits;
		return this;
	}

	@Override
	public DEHThing removeID24Flag(int bits) 
	{
		id24Flags &= ~bits;
		return this;
	}

	@Override
	public boolean hasID24Flag(int bit) 
	{
		return (this.id24Flags & bit) != 0;
	}

	@Override
	public DEHThing setMinRespawnTics(int tics) 
	{
		this.minRespawnTics = tics;
		return this;
	}

	@Override
	public DEHThing setRespawnDice(int dice) 
	{
		this.respawnDice = dice;
		return this;
	}

	@Override
	public DEHThing setPickupAmmoType(int typeId) 
	{
		this.pickupAmmoType = typeId;
		return this;
	}

	@Override
	public DEHThing setPickupAmmoCategory(int categoryBits) 
	{
		this.pickupAmmoCategory = categoryBits;
		return this;
	}

	@Override
	public DEHThing setPickupWeaponType(int weaponTypeId) 
	{
		this.pickupWeaponType = weaponTypeId;
		return this;
	}

	@Override
	public DEHThing setPickupItemType(int itemTypeId) 
	{
		this.pickupItemType = itemTypeId;
		return this;
	}

	@Override
	public DEHThing setPickupBonusCount(int count) 
	{
		this.pickupBonusCount = count;
		return this;
	}

	@Override
	public DEHThing setPickupSoundPosition(int soundPosition) 
	{
		this.pickupSoundPosition = soundPosition;
		return this;
	}

	@Override
	public DEHThing setPickupMessageMnemonic(String message) 
	{
		this.pickupMessage = message;
		return this;
	}

	@Override
	public DEHThing setTranslation(String name) 
	{
		this.translation = name;
		return this;
	}
	
	@Override
	public DEHThing setSelfDamageFactor(int selfDamageFactor)
	{
		this.selfDamageFactor = selfDamageFactor;
		return this;
	}

	public int getMinRespawnTics() 
	{
		return minRespawnTics;
	}

	public int getRespawnDice() 
	{
		return respawnDice;
	}

	public int getPickupAmmoType() 
	{
		return pickupAmmoType;
	}

	public int getPickupAmmoCategory() 
	{
		return pickupAmmoCategory;
	}

	public int getPickupWeaponType() 
	{
		return pickupWeaponType;
	}

	public int getPickupItemType() 
	{
		return pickupItemType;
	}

	public int getPickupBonusCount() 
	{
		return pickupBonusCount;
	}

	public String getPickupMessage() 
	{
		return pickupMessage;
	}

	public String getTranslation()
	{
		return translation;
	}

	public int getPickupSoundPosition() 
	{
		return pickupSoundPosition;
	}
	
	public int getSelfDamageFactor()
	{
		return selfDamageFactor;
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
		return name == obj.name
			&& editorNumber == obj.editorNumber
			&& health == obj.health
			&& speed == obj.speed
			&& fixedSpeed == obj.fixedSpeed
			&& fastSpeed == obj.fastSpeed
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
			// EXTENDED
			&& droppedItem == obj.droppedItem
			// MBF21
			&& mbf21Flags == obj.mbf21Flags
			&& meleeRange == obj.meleeRange
			&& infightingGroup == obj.infightingGroup
			&& projectileGroup == obj.projectileGroup
			&& splashGroup == obj.splashGroup
			&& ripSoundPosition == obj.ripSoundPosition
			// ID24
			&& id24Flags == obj.id24Flags
			&& minRespawnTics == obj.minRespawnTics
			&& respawnDice == obj.respawnDice
			&& pickupAmmoType == obj.pickupAmmoType
			&& pickupAmmoCategory == obj.pickupAmmoCategory
			&& pickupWeaponType == obj.pickupWeaponType
			&& pickupItemType == obj.pickupItemType
			&& pickupBonusCount == obj.pickupBonusCount
			&& ObjectUtils.areEqual(pickupMessage, obj.pickupMessage)
			&& ObjectUtils.areEqual(translation, obj.translation)
			&& pickupSoundPosition == obj.pickupSoundPosition
			&& selfDamageFactor == obj.selfDamageFactor
		;
	}	
	
	@Override
	public void writeObject(Writer writer, DEHThing thing, DEHFeatureLevel level) throws IOException
	{
		boolean isProjectile = (flags & DEHThingFlag.MISSILE.getValue()) != 0;
		boolean thingIsProjectile = (thing.flags & DEHThingFlag.MISSILE.getValue()) != 0;

		int speedVal;
		if (fixedSpeed != null)
			speedVal = fixedSpeed;
		else
			speedVal = isProjectile ? speed << 16 : speed;
		
		
		int thingSpeedVal = thingIsProjectile ? thing.speed << 16 : thing.speed;
		int fastSpeedVal = isProjectile && fastSpeed != DEFAULT_FASTSPEED ? fastSpeed << 16 : fastSpeed;
		int thingFastSpeedVal = thingIsProjectile && thing.fastSpeed != DEFAULT_FASTSPEED ? thing.fastSpeed << 16 : thing.fastSpeed;

		if (editorNumber != thing.editorNumber)
			writer.append("ID # = ").append(String.valueOf(editorNumber)).append("\r\n");
		
		// Editor keys
		if (getEditorKey("angled") != null || getEditorKey("notangled") != null)
		{
			writer.append("#$Editor angled = ").append(getEditorKey("angled") != null ? "true" : "false").append("\r\n");
		}
		if (getEditorKey("category") != null || getEditorKey("group") != null)
		{
			writer.append("#$Editor category = ");
			String value;
			if ((value = getEditorKey("category")) != null)
				writer.append(value).append("\r\n");
			else
				writer.append(getEditorKey("group")).append("\r\n");
		}
		if (getEditorKey("color") != null)
		{
			writer.append("#$Editor color id = ").append(getEditorKey("color")).append("\r\n");
		}
		if (getEditorKey("colour") != null)
		{
			writer.append("#$Editor color rgb = ").append(getEditorKey("colour")).append("\r\n");
		}
		if (getEditorKey("sprite") != null || getEditorKey("editorsprite") != null)
		{
			writer.append("#$Editor sprite = ");
			String value;
			if ((value = getEditorKey("sprite")) != null)
				writer.append(value).append("\r\n");
			else
				writer.append(getEditorKey("editorsprite")).append("\r\n");
		}
		
		if (health != thing.health)
			writer.append("Hit points = ").append(String.valueOf(health)).append("\r\n");
		if (speedVal != thingSpeedVal)
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
			writer.append("Close attack frame = ").append(String.valueOf(getMeleeFrameIndex())).append("\r\n");
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

		// Extended features
		if (level.supports(DEHFeatureLevel.EXTENDED))
		{
			if (droppedItem != thing.droppedItem)
				writer.append("Dropped item = ").append(String.valueOf(droppedItem)).append("\r\n");
		}

		// MBF21 features
		if (level.supports(DEHFeatureLevel.MBF21))
		{
			if (mbf21Flags != thing.mbf21Flags)
				writer.append("MBF21 Bits = ").append(String.valueOf(mbf21Flags)).append("\r\n");
			if (infightingGroup != thing.infightingGroup)
				writer.append("Infighting group = ").append(String.valueOf(infightingGroup)).append("\r\n");
			if (projectileGroup != thing.projectileGroup)
				writer.append("Projectile group = ").append(String.valueOf(projectileGroup)).append("\r\n");
			if (splashGroup != thing.splashGroup)
				writer.append("Splash group = ").append(String.valueOf(splashGroup)).append("\r\n");
			if (fastSpeedVal != thingFastSpeedVal)
				writer.append("Fast speed = ").append(String.valueOf(fastSpeedVal)).append("\r\n");
			if (meleeRange != thing.meleeRange)
				writer.append("Melee range = ").append(String.valueOf(meleeRange << 16)).append("\r\n");
			if (ripSoundPosition != thing.ripSoundPosition)
				writer.append("Rip sound = ").append(String.valueOf(ripSoundPosition)).append("\r\n");
		}
		
		// ID24 features
		if (level.supports(DEHFeatureLevel.ID24))
		{
			if (id24Flags != thing.id24Flags)
				writer.append("ID24 Bits = ").append(String.valueOf(id24Flags)).append("\r\n");
			if (minRespawnTics != thing.minRespawnTics)
				writer.append("Min respawn tics = ").append(String.valueOf(minRespawnTics)).append("\r\n");
			if (respawnDice != thing.respawnDice)
				writer.append("Respawn dice = ").append(String.valueOf(respawnDice)).append("\r\n");
			if (pickupAmmoType != thing.pickupAmmoType)
				writer.append("Pickup ammo type = ").append(String.valueOf(pickupAmmoType)).append("\r\n");
			if (pickupAmmoCategory != thing.pickupAmmoCategory)
				writer.append("Pickup ammo category = ").append(String.valueOf(pickupAmmoCategory)).append("\r\n");
			if (pickupWeaponType != thing.pickupWeaponType)
				writer.append("Pickup weapon type = ").append(String.valueOf(pickupWeaponType)).append("\r\n");
			if (pickupItemType != thing.pickupItemType)
				writer.append("Pickup item type = ").append(String.valueOf(pickupItemType)).append("\r\n");
			if (pickupBonusCount != thing.pickupBonusCount)
				writer.append("Pickup bonus count = ").append(String.valueOf(pickupBonusCount)).append("\r\n");
			if (!ObjectUtils.areEqual(pickupMessage, thing.pickupMessage))
				writer.append("Pickup message = ").append(pickupMessage).append("\r\n");
			if (!ObjectUtils.areEqual(translation, thing.translation))
				writer.append("Translation = ").append(translation).append("\r\n");
			if (pickupSoundPosition != thing.pickupSoundPosition)
				writer.append("Pickup sound = ").append(String.valueOf(pickupSoundPosition)).append("\r\n");
			if (selfDamageFactor != thing.selfDamageFactor)
				writer.append("Self damage factor = ").append(String.valueOf(selfDamageFactor)).append("\r\n");
		}
		
		writeCustomProperties(writer);
		writer.flush();
	}

}
