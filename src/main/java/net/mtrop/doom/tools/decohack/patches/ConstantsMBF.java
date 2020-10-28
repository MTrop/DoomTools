package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.*;

import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;

/**
 * Constants for MBF. 
 * @author Matthew Tropiano
 */
interface ConstantsMBF 
{
	static final DEHSound[] DEHSOUNDMBF = 
	{
		DEHSound.create(98, false),
		DEHSound.create(70, false),
		DEHSound.create(120, false),
		DEHSound.create(70, false),
		DEHSound.create(96, false),
	};
	
	static final PatchBoom.State[] DEHSTATEMBF = 
	{
		PatchBoom.State.create(DEHState.create(22, 1, true, 965, 1000), DIE),
		PatchBoom.State.create(DEHState.create(22, 1, true, 970, 4), SCREAM),
		PatchBoom.State.create(DEHState.create(22, 2, true, 971, 6), DETONATE),
		PatchBoom.State.create(DEHState.create(22, 3, true, 0, 10), NULL),
		PatchBoom.State.create(DEHState.create(139, 0, false, 973, 10), LOOK),
		PatchBoom.State.create(DEHState.create(139, 1, false, 972, 10), LOOK),
		PatchBoom.State.create(DEHState.create(139, 0, false, 975, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 0, false, 976, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 1, false, 977, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 1, false, 978, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 2, false, 979, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 2, false, 980, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 3, false, 981, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 3, false, 974, 2), CHASE),
		PatchBoom.State.create(DEHState.create(139, 4, false, 983, 8), FACETARGET),
		PatchBoom.State.create(DEHState.create(139, 5, false, 984, 8), FACETARGET),
		PatchBoom.State.create(DEHState.create(139, 6, false, 974, 8), SARGATTACK),
		PatchBoom.State.create(DEHState.create(139, 7, false, 986, 2), NULL),
		PatchBoom.State.create(DEHState.create(139, 7, false, 974, 2), PAIN),
		PatchBoom.State.create(DEHState.create(139, 8, false, 988, 8), NULL),
		PatchBoom.State.create(DEHState.create(139, 9, false, 989, 8), SCREAM),
		PatchBoom.State.create(DEHState.create(139, 10, false, 990, 4), NULL),
		PatchBoom.State.create(DEHState.create(139, 11, false, 991, 4), FALL),
		PatchBoom.State.create(DEHState.create(139, 12, false, 992, 4), NULL),
		PatchBoom.State.create(DEHState.create(139, 13, false, 0, -1), NULL),
		PatchBoom.State.create(DEHState.create(139, 13, false, 994, 5), NULL),
		PatchBoom.State.create(DEHState.create(139, 12, false, 995, 5), NULL),
		PatchBoom.State.create(DEHState.create(139, 11, false, 996, 5), NULL),
		PatchBoom.State.create(DEHState.create(139, 10, false, 997, 5), NULL),
		PatchBoom.State.create(DEHState.create(139, 9, false, 998, 5), NULL),
		PatchBoom.State.create(DEHState.create(139, 8, false, 974, 5), NULL),
		PatchBoom.State.create(DEHState.create(14, 0, false, 1000, 10), BFGSOUND),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1001, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1002, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1003, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1004, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1005, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1006, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1007, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1008, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1009, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1010, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1011, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1012, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1013, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1014, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1015, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1016, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1017, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1018, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1019, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1020, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1021, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1022, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1023, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1024, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1025, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1026, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1027, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1028, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1029, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1030, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1031, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1032, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1033, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1034, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1035, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1036, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1037, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1038, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1039, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1040, 1), FIREOLDBFG),
		PatchBoom.State.create(DEHState.create(14, 1, false, 1041, 0), LIGHT0),
		PatchBoom.State.create(DEHState.create(14, 1, false, 81, 20), REFIRE),
		PatchBoom.State.create(DEHState.create(140, 0, true, 1043, 6), NULL),
		PatchBoom.State.create(DEHState.create(140, 1, true, 1042, 6), NULL),
		PatchBoom.State.create(DEHState.create(140, 2, true, 1045, 4), NULL),
		PatchBoom.State.create(DEHState.create(140, 3, true, 1046, 4), NULL),
		PatchBoom.State.create(DEHState.create(140, 4, true, 1047, 4), NULL),
		PatchBoom.State.create(DEHState.create(140, 5, true, 1048, 4), NULL),
		PatchBoom.State.create(DEHState.create(140, 6, true, 0, 4), NULL),
		PatchBoom.State.create(DEHState.create(141, 0, true, 1050, 4), NULL),
		PatchBoom.State.create(DEHState.create(141, 1, true, 1049, 4), NULL),
		PatchBoom.State.create(DEHState.create(141, 2, true, 1052, 6), NULL),
		PatchBoom.State.create(DEHState.create(141, 3, true, 1053, 6), NULL),
		PatchBoom.State.create(DEHState.create(141, 4, true, 0, 6), NULL),
		PatchBoom.State.create(DEHState.create(142, 0, false, 1054, 6), NULL),
		PatchBoom.State.create(DEHState.create(143, 0, false, 1055, 6), NULL),
		PatchBoom.State.create(DEHState.create(44, 0, false, 1056, 10), LOOK),
		PatchBoom.State.create(DEHState.create(44, 1, false, 1058, 5), CHASE),
		PatchBoom.State.create(DEHState.create(44, 2, false, 1059, 5), CHASE),
		PatchBoom.State.create(DEHState.create(44, 3, false, 1060, 5), CHASE),
		PatchBoom.State.create(DEHState.create(44, 0, false, 1057, 5), CHASE),
		PatchBoom.State.create(DEHState.create(44, 4, false, 1062, 4), FACETARGET),
		PatchBoom.State.create(DEHState.create(44, 5, false, 1063, 5), BETASKULLATTACK),
		PatchBoom.State.create(DEHState.create(44, 5, false, 1057, 4), NULL),
		PatchBoom.State.create(DEHState.create(44, 6, false, 1065, 4), NULL),
		PatchBoom.State.create(DEHState.create(44, 7, false, 1057, 2), PAIN),
		PatchBoom.State.create(DEHState.create(44, 8, false, 1057, 4), NULL),
		PatchBoom.State.create(DEHState.create(44, 9, false, 1068, 5), NULL),
		PatchBoom.State.create(DEHState.create(44, 10, false, 1069, 5), NULL),
		PatchBoom.State.create(DEHState.create(44, 11, false, 1070, 5), NULL),
		PatchBoom.State.create(DEHState.create(44, 12, false, 1071, 5), NULL),
		PatchBoom.State.create(DEHState.create(44, 13, false, 1072, 5), SCREAM),
		PatchBoom.State.create(DEHState.create(44, 14, false, 1073, 5), NULL),
		PatchBoom.State.create(DEHState.create(44, 15, false, 1074, 5), FALL),
		PatchBoom.State.create(DEHState.create(44, 16, false, 1074, 5), STOP),
		PatchBoom.State.create(DEHState.create(22, 1, true, 128, 8), MUSHROOM)
	};
	
	public static final DEHThing[] DEHTHINGMBF = 
	{
		(new DEHThing()).setName("Dog")
			.setEditorNumber(888)
			.setHealth(500)
			.setSpeed(10)
			.setRadius(11)
			.setHeight(28)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(180)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(972)
			.setWalkFrameIndex(974)
			.setPainFrameIndex(985)
			.setMeleeFrameIndex(982)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(987)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(993)
			.setSeeSoundPosition(109)
			.setAttackSoundPosition(110)
			.setPainSoundPosition(113)
			.setDeathSoundPosition(112)
			.setActiveSoundPosition(111),
		(new DEHThing()).setName("Beta Plasma 1")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(25)
			.setRadius(13)
			.setHeight(8)
			.setDamage(4)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(536938000)
			.setMass(100)
			.setSpawnFrameIndex(1042)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(1044)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(8)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Beta Plasma 2")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(25)
			.setRadius(6)
			.setHeight(8)
			.setDamage(4)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(536938000)
			.setMass(100)
			.setSpawnFrameIndex(1049)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(1051)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(8)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Beta Sceptre")
			.setEditorNumber(2016)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(10)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(1054)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(DEHThing.FRAME_NULL)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(DEHThing.SOUND_NONE)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Beta Bible")
			.setEditorNumber(2017)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(10)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(1055)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(DEHThing.FRAME_NULL)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(DEHThing.SOUND_NONE)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
	};
	
}
