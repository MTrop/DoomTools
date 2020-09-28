package net.mtrop.doom.tools.decohack;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public enum ActionPointer
{
	NULL         (0,  "NULL"),
	LIGHT0       (1,  "Light0"),
	WEAPONREADY  (2,  "WeaponReady"),
	LOWER        (3,  "Lower"),
	RAISE        (4,  "Raise"),
	PUNCH        (6,  "Punch"),
	REFIRE       (9,  "ReFire"),
	FIREPISTOL   (14, "FirePistol"),
	LIGHT1       (17, "Light1"),
	FIRESHOTGUN  (22, "FireShotgun"),
	LIGHT2       (31, "Light2"),
	FIRESHOTGUN2 (36, "FireShotgun2"),
	CHECKRELOAD  (38, "CheckReload"),
	OPENSHOTGUN2 (39, "OpenShotgun2"),
	LOADSHOTGUN2 (41, "LoadShotgun2"),
	CLOSESHOTGUN2(43, "CloseShotgun2"),
	FIRECGUN     (52, "FireCGun"),
	GUNFLASH     (60, "GunFlash"),
	FIREMISSILE  (61, "FireMissile"),
	SAW          (71, "Saw"),
	FIREPLASMA   (77, "FirePlasma"),
	BFGSOUND     (84, "BFGsound"),
	FIREBFG      (86, "FireBFG"),
	
	// TODO: Finish this.
	;
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	
	private ActionPointer(int frame, String mnemonic)
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
