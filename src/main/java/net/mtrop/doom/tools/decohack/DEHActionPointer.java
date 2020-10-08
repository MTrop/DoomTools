package net.mtrop.doom.tools.decohack;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public enum DEHActionPointer
{
	NULL         (0,   "NULL"),
	LIGHT0       (1,   "Light0"),
	WEAPONREADY  (2,   "WeaponReady"),
	LOWER        (3,   "Lower"),
	RAISE        (4,   "Raise"),
	PUNCH        (6,   "Punch"),
	REFIRE       (9,   "ReFire"),
	FIREPISTOL   (14,  "FirePistol"),
	LIGHT1       (17,  "Light1"),
	FIRESHOTGUN  (22,  "FireShotgun"),
	LIGHT2       (31,  "Light2"),
	FIRESHOTGUN2 (36,  "FireShotgun2"),
	CHECKRELOAD  (38,  "CheckReload"),
	OPENSHOTGUN2 (39,  "OpenShotgun2"),
	LOADSHOTGUN2 (41,  "LoadShotgun2"),
	CLOSESHOTGUN2(43,  "CloseShotgun2"),
	FIRECGUN     (52,  "FireCGun"),
	GUNFLASH     (60,  "GunFlash"),
	FIREMISSILE  (61,  "FireMissile"),
	SAW          (71,  "Saw"),
	FIREPLASMA   (77,  "FirePlasma"),
	BFGSOUND     (84,  "BFGsound"),
	FIREBFG      (86,  "FireBFG"),
	BFGSPRAY     (119, "BFGSpray"),
	EXPLODE      (127, "Explode"),
	PAIN         (157, "Pain"),
	PLAYERSCREAM (159, "PlayerScream"),
	FALL         (160, "Fall"),
	XSCREAM      (166, "XScream"),
	LOOK         (174, "Look"),
	CHASE        (176, "Chase"),
	FACETARGET   (184, "FaceTarget"),
	POSATTACK    (185, "PosAttack"),
	SCREAM       (190, "Scream"),
	VILECHASE    (243, "VileChase"),
	VILESTART    (255, "VileStart"),
	VILETARGET   (257, "VileTarget"),
	VILEATTACK   (264, "VileAttack"),
	STARTFIRE    (281, "StartFire"),
	FIRE         (282, "Fire"),
	FIRECRACKLE  (285, "FireCrackle"),
	TRACER       (316, "Tracer"),
	SKELWHOOSH   (336, "SkelWhoosh"),
	SKELFIST     (338, "SkelFist"),
	SKELMISSILE  (341, "SkelMissile"),
	FATRAISE     (376, "FatRaise"),
	FATATTACK1   (377, "FatAttack1"),
	FATATTACK2   (380, "FatAttack2"),
	FATATTACK3   (383, "FatAttack3"),
	BOSSDEATH    (397, "BossDeath"),
	CPOSATTACK   (417, "CPosAttack"),
	CPOSREFIRE   (419, "CPosRefire"),
	TROOPATTACK  (454, "TroopAttack"),
	SARGATTACK   (487, "SargAttack"),
	HEADATTACK   (506, "HeadAttack"),
	BRUISATTACK  (539, "BruisAttack"),
	SKULLATTACK  (590, "SkullAttack"),
	METAL        (603, "Metal"),
	SPOSATTACK   (616, "SPosAttack"),
	SPIDREFIRE   (618, "SpidRefire"),
	BABYMETAL    (635, "BabyMetal"),
	BSPIATTACK   (648, "BspiAttack"),
	HOOF         (676, "Hoof"),
	CYBERATTACK  (685, "CyberAttack"),
	PAINATTACK   (711, "PainAttack"),
	PAINDIE      (718, "PainDie"),
	KEENDIE      (774, "KeenDie"),
	BRAINPAIN    (779, "BrainPain"),
	BRAINSCREAM  (780, "BrainScream"),
	BRAINDIE     (783, "BrainDie"),
	BRAINAWAKE   (785, "BrainAwake"),
	BRAINSPIT    (786, "BrainSpit"),
	SPWANSOUND   (787, "SpawnSound"),
	SPAWNFLY     (788, "SpawnFly"),
	BRAINEXPLODE (801, "BrainExplode"),
	;
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;

	private static Map<String, DEHActionPointer> MNEMONIC_MAP = null;

	public static DEHActionPointer getByMnemonic(String mnemonic)
	{
		if (MNEMONIC_MAP == null)
		{
			MNEMONIC_MAP = new HashMap<>();
			for (DEHActionPointer ap : values())
				MNEMONIC_MAP.put(ap.getMnemonic(), ap);
		}
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private DEHActionPointer(int frame, String mnemonic)
	{
		this.frame = frame;
		this.mnemonic = mnemonic;
	}

	public int getFrame() 
	{
		return frame;
	}
	
	public String getMnemonic() 
	{
		return mnemonic;
	}
	
	
}
