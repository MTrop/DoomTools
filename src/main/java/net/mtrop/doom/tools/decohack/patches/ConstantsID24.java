package net.mtrop.doom.tools.decohack.patches;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;

import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19.*;
import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF.*;
import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21.*;


/**
 * Constants for ID24.
 * TODO: For ID24, make maps of things.
 */
interface ConstantsID24 
{
	static final Map<Integer, String> DEHSPRITEID24 = new TreeMap<Integer, String>() 
	{
		private static final long serialVersionUID = 7408827073601555756L;
		{
			put(-1879048192, "BSH1");
			put(-1879048191, "BSH2");
			put(-1879048190, "BSHE");
			put(-1879048189, "CBR2");
			put(-1879048188, "CHR1");
			put(-1879048187, "CSPI");
			put(-1879048186, "CYB2");
			put(-1879048185, "FLMF");
			put(-1879048184, "FLMG");
			put(-1879048183, "GBAL");
			put(-1879048182, "GHUL");
			put(-1879048181, "GOR6");
			put(-1879048180, "GOR7");
			put(-1879048179, "GOR8");
			put(-1879048178, "GORA");
			put(-1879048177, "HBB2");
			put(-1879048176, "HBBQ");
			put(-1879048175, "HDB7");
			put(-1879048174, "HDB8");
			put(-1879048173, "HETB");
			put(-1879048172, "HETC");
			put(-1879048171, "HETF");
			put(-1879048170, "HETG");
			put(-1879048169, "IFLM");
			put(-1879048168, "LAMP");
			put(-1879048167, "PHED");
			put(-1879048166, "POB6");
			put(-1879048165, "POL7");
			put(-1879048164, "POLA");
			put(-1879048163, "PPOS");
			put(-1879048162, "STC1");
			put(-1879048161, "STC2");
			put(-1879048160, "STC3");
			put(-1879048159, "STG1");
			put(-1879048158, "STG2");
			put(-1879048157, "STG3");
			put(-1879048156, "STMI");
			put(-1879048155, "TLP6");
			put(-1879048154, "VASS");
			put(-1879048153, "VFLM");
			put(-1879048152, "INCN");
			put(-1879048151, "CBLD");
			put(-1879048150, "FCPU");
			put(-1879048149, "FTNK");
		}
	};

	static final Map<Integer, PatchBoom.State> DEHSTATEID24 = new TreeMap<Integer, PatchBoom.State>() 
	{
		private static final long serialVersionUID = 8516432365864911888L;
		{
			put(-1879048192, PatchBoom.State.create(DEHState.create(-1879048165, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048191, PatchBoom.State.create(DEHState.create(-1879048176, 0, true, -1879048190, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048190, PatchBoom.State.create(DEHState.create(-1879048176, 1, true, -1879048189, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048189, PatchBoom.State.create(DEHState.create(-1879048176, 2, true, -1879048191, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048188, PatchBoom.State.create(DEHState.create(-1879048177, 0, true, -1879048187, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048187, PatchBoom.State.create(DEHState.create(-1879048177, 1, true, -1879048186, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048186, PatchBoom.State.create(DEHState.create(-1879048177, 2, true, -1879048188, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048185, PatchBoom.State.create(DEHState.create(-1879048181, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048184, PatchBoom.State.create(DEHState.create(-1879048180, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048183, PatchBoom.State.create(DEHState.create(-1879048179, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048182, PatchBoom.State.create(DEHState.create(-1879048178, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048181, PatchBoom.State.create(DEHState.create(-1879048175, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048180, PatchBoom.State.create(DEHState.create(-1879048174, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048179, PatchBoom.State.create(DEHState.create(-1879048164, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048178, PatchBoom.State.create(DEHState.create(-1879048166, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048177, PatchBoom.State.create(DEHState.create(95, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048176, PatchBoom.State.create(DEHState.create(-1879048192, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048175, PatchBoom.State.create(DEHState.create(-1879048192, 1, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048174, PatchBoom.State.create(DEHState.create(-1879048192, 2, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048173, PatchBoom.State.create(DEHState.create(-1879048191, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048172, PatchBoom.State.create(DEHState.create(-1879048191, 1, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048171, PatchBoom.State.create(DEHState.create(-1879048191, 2, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048170, PatchBoom.State.create(DEHState.create(-1879048156, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048169, PatchBoom.State.create(DEHState.create(-1879048159, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048168, PatchBoom.State.create(DEHState.create(-1879048158, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048167, PatchBoom.State.create(DEHState.create(-1879048157, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048166, PatchBoom.State.create(DEHState.create(-1879048162, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048165, PatchBoom.State.create(DEHState.create(-1879048161, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048164, PatchBoom.State.create(DEHState.create(-1879048160, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048163, PatchBoom.State.create(DEHState.create(-1879048188, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048162, PatchBoom.State.create(DEHState.create(-1879048168, 0, true, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048161, PatchBoom.State.create(DEHState.create(-1879048168, 1, true, -1879048160, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879048160, PatchBoom.State.create(DEHState.create(-1879048168, 1, true, -1879048159, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048159, PatchBoom.State.create(DEHState.create(-1879048168, 2, true, -1879048158, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048158, PatchBoom.State.create(DEHState.create(-1879048168, 3, true, -1879048159, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048157, PatchBoom.State.create(DEHState.create(-1879048155, 0, true, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048156, PatchBoom.State.create(DEHState.create(-1879048189, 0, true, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048155, PatchBoom.State.create(DEHState.create(138, 0, false, -1879048155, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048154, PatchBoom.State.create(DEHState.create(138, 0, false, -1879048154, 35, -1879048168, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048153, PatchBoom.State.create(DEHState.create(138, 0, false, 0, 250, -1879048182, 1, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048152, PatchBoom.State.create(DEHState.create(138, 0, false, -1879048152, 139, -1879048183, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048151, PatchBoom.State.create(DEHState.create(138, 0, false, 0, 105, -1879048184, 1, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048150, PatchBoom.State.create(DEHState.create(-1879048182, 0, false, -1879048149, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048149, PatchBoom.State.create(DEHState.create(-1879048182, 1, false, -1879048150, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048148, PatchBoom.State.create(DEHState.create(-1879048182, 0, false, -1879048147, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048147, PatchBoom.State.create(DEHState.create(-1879048182, 0, false, -1879048146, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048146, PatchBoom.State.create(DEHState.create(-1879048182, 1, false, -1879048145, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048145, PatchBoom.State.create(DEHState.create(-1879048182, 1, false, -1879048144, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048144, PatchBoom.State.create(DEHState.create(-1879048182, 2, false, -1879048143, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048143, PatchBoom.State.create(DEHState.create(-1879048182, 2, false, -1879048142, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048142, PatchBoom.State.create(DEHState.create(-1879048182, 1, false, -1879048141, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048141, PatchBoom.State.create(DEHState.create(-1879048182, 1, false, -1879048148, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048140, PatchBoom.State.create(DEHState.create(-1879048182, 3, true, -1879048139, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048139, PatchBoom.State.create(DEHState.create(-1879048182, 4, true, -1879048138, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048138, PatchBoom.State.create(DEHState.create(-1879048182, 5, true, -1879048137, 4, 0, 0, new int[]{-1879048180, 0, 0, 0, -524288, 0, 0, 0}, 0, null), MONSTERPROJECTILE));
			put(-1879048137, PatchBoom.State.create(DEHState.create(-1879048182, 6, true, -1879048148, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048136, PatchBoom.State.create(DEHState.create(-1879048182, 8, true, -1879048135, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048135, PatchBoom.State.create(DEHState.create(-1879048182, 10, true, -1879048148, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PAIN));
			put(-1879048134, PatchBoom.State.create(DEHState.create(-1879048182, 11, true, -1879048133, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048133, PatchBoom.State.create(DEHState.create(-1879048182, 12, true, -1879048132, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879048132, PatchBoom.State.create(DEHState.create(-1879048182, 13, true, -1879048131, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048131, PatchBoom.State.create(DEHState.create(-1879048182, 14, true, -1879048130, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048130, PatchBoom.State.create(DEHState.create(-1879048182, 15, true, -1879048129, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048129, PatchBoom.State.create(DEHState.create(-1879048182, 16, true, -1879048128, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048128, PatchBoom.State.create(DEHState.create(-1879048182, 17, true, -1879048127, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048127, PatchBoom.State.create(DEHState.create(-1879048182, 18, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048126, PatchBoom.State.create(DEHState.create(-1879048183, 0, true, -1879048125, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048125, PatchBoom.State.create(DEHState.create(-1879048183, 1, true, -1879048126, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048124, PatchBoom.State.create(DEHState.create(-1879048183, 2, true, -1879048123, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048123, PatchBoom.State.create(DEHState.create(48, 1, true, -1879048122, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048122, PatchBoom.State.create(DEHState.create(48, 2, true, -1879048121, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048121, PatchBoom.State.create(DEHState.create(48, 3, true, -1879048120, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048120, PatchBoom.State.create(DEHState.create(48, 4, true, 0, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048119, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048118, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048118, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048119, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048117, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048116, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048116, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048115, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048115, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048114, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048114, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048113, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048113, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048112, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048112, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048111, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048111, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048110, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048110, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048109, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048109, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048108, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048108, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048107, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048107, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048106, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048106, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048105, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048105, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048104, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048104, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048103, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048103, PatchBoom.State.create(DEHState.create(-1879048190, 1, true, -1879048102, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048102, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048101, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048101, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048100, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048100, PatchBoom.State.create(DEHState.create(-1879048190, 2, true, -1879048099, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048099, PatchBoom.State.create(DEHState.create(-1879048190, 0, true, -1879048117, 0, -1879048192, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048098, PatchBoom.State.create(DEHState.create(-1879048190, 3, true, -1879048098, 1, 0, 0, new int[]{100, 8, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879048097, PatchBoom.State.create(DEHState.create(-1879048190, 3, true, -1879048096, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048096, PatchBoom.State.create(DEHState.create(-1879048190, 3, true, -1879048117, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PAIN));
			put(-1879048095, PatchBoom.State.create(DEHState.create(-1879048190, 3, true, -1879048094, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879048094, PatchBoom.State.create(DEHState.create(-1879048190, 4, true, -1879048093, 6, 0, 0, new int[]{128, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879048093, PatchBoom.State.create(DEHState.create(-1879048190, 5, true, -1879048092, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048092, PatchBoom.State.create(DEHState.create(-1879048190, 6, true, -1879048091, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048091, PatchBoom.State.create(DEHState.create(-1879048190, 7, true, -1879048090, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048090, PatchBoom.State.create(DEHState.create(138, 0, false, 0, 20, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048089, PatchBoom.State.create(DEHState.create(-1879048187, 0, false, -1879048088, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048088, PatchBoom.State.create(DEHState.create(-1879048187, 1, false, -1879048089, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048087, PatchBoom.State.create(DEHState.create(-1879048187, 0, false, -1879048086, 20, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048086, PatchBoom.State.create(DEHState.create(-1879048187, 0, false, -1879048085, 0, -1879048185, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048085, PatchBoom.State.create(DEHState.create(-1879048187, 0, false, -1879048084, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048084, PatchBoom.State.create(DEHState.create(-1879048187, 0, false, -1879048083, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048083, PatchBoom.State.create(DEHState.create(-1879048187, 1, false, -1879048082, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048082, PatchBoom.State.create(DEHState.create(-1879048187, 1, false, -1879048081, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048081, PatchBoom.State.create(DEHState.create(-1879048187, 2, false, -1879048080, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048080, PatchBoom.State.create(DEHState.create(-1879048187, 2, false, -1879048079, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048079, PatchBoom.State.create(DEHState.create(-1879048187, 3, false, -1879048078, 0, -1879048185, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879048078, PatchBoom.State.create(DEHState.create(-1879048187, 3, false, -1879048077, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048077, PatchBoom.State.create(DEHState.create(-1879048187, 3, false, -1879048076, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048076, PatchBoom.State.create(DEHState.create(-1879048187, 4, false, -1879048075, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048075, PatchBoom.State.create(DEHState.create(-1879048187, 4, false, -1879048074, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048074, PatchBoom.State.create(DEHState.create(-1879048187, 5, false, -1879048073, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048073, PatchBoom.State.create(DEHState.create(-1879048187, 5, false, -1879048086, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879048072, PatchBoom.State.create(DEHState.create(-1879048187, 0, true, -1879048071, 20, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048071, PatchBoom.State.create(DEHState.create(-1879048187, 6, true, -1879048070, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SPOSATTACK));
			put(-1879048070, PatchBoom.State.create(DEHState.create(-1879048187, 7, true, -1879048069, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SPOSATTACK));
			put(-1879048069, PatchBoom.State.create(DEHState.create(-1879048187, 7, true, -1879048071, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SPIDREFIRE));
			put(-1879048068, PatchBoom.State.create(DEHState.create(-1879048187, 8, false, -1879048067, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048067, PatchBoom.State.create(DEHState.create(-1879048187, 8, false, -1879048086, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PAIN));
			put(-1879048066, PatchBoom.State.create(DEHState.create(-1879048187, 9, false, -1879048065, 20, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879048065, PatchBoom.State.create(DEHState.create(-1879048187, 10, false, -1879048064, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048064, PatchBoom.State.create(DEHState.create(-1879048187, 11, false, -1879048063, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048063, PatchBoom.State.create(DEHState.create(-1879048187, 12, false, -1879048062, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048062, PatchBoom.State.create(DEHState.create(-1879048187, 13, false, -1879048061, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048061, PatchBoom.State.create(DEHState.create(-1879048187, 14, false, -1879048060, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048060, PatchBoom.State.create(DEHState.create(-1879048187, 15, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), BOSSDEATH));
			put(-1879048059, PatchBoom.State.create(DEHState.create(-1879048187, 15, false, -1879048058, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048058, PatchBoom.State.create(DEHState.create(-1879048187, 14, false, -1879048057, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048057, PatchBoom.State.create(DEHState.create(-1879048187, 13, false, -1879048056, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048056, PatchBoom.State.create(DEHState.create(-1879048187, 12, false, -1879048055, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048055, PatchBoom.State.create(DEHState.create(-1879048187, 11, false, -1879048054, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048054, PatchBoom.State.create(DEHState.create(-1879048187, 10, false, -1879048053, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048053, PatchBoom.State.create(DEHState.create(-1879048187, 9, false, -1879048086, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048052, PatchBoom.State.create(DEHState.create(-1879048163, 0, false, -1879048051, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048051, PatchBoom.State.create(DEHState.create(-1879048163, 1, false, -1879048052, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879048050, PatchBoom.State.create(DEHState.create(-1879048163, 0, false, -1879048049, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048049, PatchBoom.State.create(DEHState.create(-1879048163, 0, false, -1879048048, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048048, PatchBoom.State.create(DEHState.create(-1879048163, 1, false, -1879048047, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048047, PatchBoom.State.create(DEHState.create(-1879048163, 1, false, -1879048046, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048046, PatchBoom.State.create(DEHState.create(-1879048163, 2, false, -1879048045, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048045, PatchBoom.State.create(DEHState.create(-1879048163, 2, false, -1879048044, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048044, PatchBoom.State.create(DEHState.create(-1879048163, 3, false, -1879048043, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048043, PatchBoom.State.create(DEHState.create(-1879048163, 3, false, -1879048050, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), CHASE));
			put(-1879048042, PatchBoom.State.create(DEHState.create(-1879048163, 4, false, -1879048041, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048041, PatchBoom.State.create(DEHState.create(-1879048163, 5, false, -1879048040, 2, 0, 0, new int[]{35, 0, 0, 0, 0, 0, 0, 0}, 1, null), MONSTERPROJECTILE));
			put(-1879048040, PatchBoom.State.create(DEHState.create(-1879048163, 4, false, -1879048039, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), null));
			put(-1879048039, PatchBoom.State.create(DEHState.create(-1879048163, 5, false, -1879048038, 2, 0, 0, new int[]{35, 0, 0, 0, 0, 0, 0, 0}, 1, null), MONSTERPROJECTILE));
			put(-1879048038, PatchBoom.State.create(DEHState.create(-1879048163, 4, false, -1879048037, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), null));
			put(-1879048037, PatchBoom.State.create(DEHState.create(-1879048163, 5, false, -1879048036, 2, 0, 0, new int[]{35, 0, 0, 0, 0, 0, 0, 0}, 1, null), MONSTERPROJECTILE));
			put(-1879048036, PatchBoom.State.create(DEHState.create(-1879048163, 4, false, -1879048050, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), null));
			put(-1879048035, PatchBoom.State.create(DEHState.create(-1879048163, 6, false, -1879048034, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), null));
			put(-1879048034, PatchBoom.State.create(DEHState.create(-1879048163, 6, false, -1879048050, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 1, null), PAIN));
			put(-1879048033, PatchBoom.State.create(DEHState.create(-1879048163, 7, false, -1879048032, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048032, PatchBoom.State.create(DEHState.create(-1879048163, 7, false, -1879048031, 5, 0, 0, new int[]{-1879048179, 11468800, 0, 0, 2621440, 131072, 0, 98304}, 0, null), SPAWNOBJECT));
			put(-1879048031, PatchBoom.State.create(DEHState.create(-1879048163, 8, false, -1879048030, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879048030, PatchBoom.State.create(DEHState.create(-1879048163, 9, false, -1879048029, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048029, PatchBoom.State.create(DEHState.create(-1879048163, 10, false, -1879048028, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048028, PatchBoom.State.create(DEHState.create(-1879048163, 11, false, -1879048027, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048027, PatchBoom.State.create(DEHState.create(-1879048163, 12, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048026, PatchBoom.State.create(DEHState.create(-1879048163, 13, false, -1879048025, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048025, PatchBoom.State.create(DEHState.create(-1879048163, 14, false, -1879048024, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), XSCREAM));
			put(-1879048024, PatchBoom.State.create(DEHState.create(-1879048163, 15, false, -1879048023, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879048023, PatchBoom.State.create(DEHState.create(-1879048163, 16, false, -1879048022, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879048022, PatchBoom.State.create(DEHState.create(-1879048163, 16, false, -1879048021, 5, 0, 0, new int[]{-1879048178, 11141120, 0, -524288, 2097152, 262144, 0, 131072}, 0, null), SPAWNOBJECT));
			put(-1879048021, PatchBoom.State.create(DEHState.create(-1879048163, 17, false, -1879048020, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048020, PatchBoom.State.create(DEHState.create(-1879048163, 18, false, -1879048019, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048019, PatchBoom.State.create(DEHState.create(-1879048163, 19, false, -1879048018, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048018, PatchBoom.State.create(DEHState.create(-1879048163, 20, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048017, PatchBoom.State.create(DEHState.create(-1879048163, 12, false, -1879048016, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048016, PatchBoom.State.create(DEHState.create(-1879048163, 11, false, -1879048015, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048015, PatchBoom.State.create(DEHState.create(-1879048163, 10, false, -1879048014, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048014, PatchBoom.State.create(DEHState.create(-1879048163, 9, false, -1879048013, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048013, PatchBoom.State.create(DEHState.create(-1879048163, 8, false, -1879048012, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048012, PatchBoom.State.create(DEHState.create(-1879048163, 7, false, -1879048050, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048011, PatchBoom.State.create(DEHState.create(-1879048167, 0, false, -1879048010, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048010, PatchBoom.State.create(DEHState.create(-1879048167, 1, false, -1879048009, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048009, PatchBoom.State.create(DEHState.create(-1879048167, 2, false, -1879048008, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048008, PatchBoom.State.create(DEHState.create(-1879048167, 3, false, -1879048007, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048007, PatchBoom.State.create(DEHState.create(-1879048167, 4, false, -1879048006, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048006, PatchBoom.State.create(DEHState.create(-1879048167, 5, false, -1879048005, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048005, PatchBoom.State.create(DEHState.create(-1879048167, 6, false, -1879048004, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048004, PatchBoom.State.create(DEHState.create(-1879048167, 7, false, -1879048003, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048003, PatchBoom.State.create(DEHState.create(-1879048167, 8, false, -1879048011, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048002, PatchBoom.State.create(DEHState.create(-1879048167, 9, false, -1879048002, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048001, PatchBoom.State.create(DEHState.create(-1879048163, 21, false, -1879048001, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879048000, PatchBoom.State.create(DEHState.create(-1879048163, 22, false, -1879047999, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047999, PatchBoom.State.create(DEHState.create(-1879048163, 23, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047998, PatchBoom.State.create(DEHState.create(-1879048154, 0, false, -1879047997, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879047997, PatchBoom.State.create(DEHState.create(-1879048154, 1, false, -1879047998, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879047996, PatchBoom.State.create(DEHState.create(-1879048154, 0, false, -1879047995, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047995, PatchBoom.State.create(DEHState.create(-1879048154, 0, false, -1879047994, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047994, PatchBoom.State.create(DEHState.create(-1879048154, 1, false, -1879047993, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047993, PatchBoom.State.create(DEHState.create(-1879048154, 1, false, -1879047992, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047992, PatchBoom.State.create(DEHState.create(-1879048154, 2, false, -1879047991, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047991, PatchBoom.State.create(DEHState.create(-1879048154, 2, false, -1879047990, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047990, PatchBoom.State.create(DEHState.create(-1879048154, 3, false, -1879047989, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047989, PatchBoom.State.create(DEHState.create(-1879048154, 3, false, -1879047996, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047988, PatchBoom.State.create(DEHState.create(-1879048154, 4, true, -1879047987, 0, -1879048159, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047987, PatchBoom.State.create(DEHState.create(-1879048154, 4, true, -1879047986, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047986, PatchBoom.State.create(DEHState.create(-1879048154, 5, true, -1879047985, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047985, PatchBoom.State.create(DEHState.create(-1879048154, 6, true, -1879047984, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047984, PatchBoom.State.create(DEHState.create(-1879048154, 7, true, -1879047996, 8, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), MONSTERPROJECTILE));
			put(-1879047983, PatchBoom.State.create(DEHState.create(-1879048154, 8, false, -1879047982, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047982, PatchBoom.State.create(DEHState.create(-1879048154, 8, false, -1879047996, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PAIN));
			put(-1879047981, PatchBoom.State.create(DEHState.create(-1879048154, 9, true, -1879047980, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047980, PatchBoom.State.create(DEHState.create(-1879048154, 10, true, -1879047979, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879047979, PatchBoom.State.create(DEHState.create(-1879048154, 11, true, -1879047978, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047978, PatchBoom.State.create(DEHState.create(-1879048154, 12, true, -1879047977, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879047977, PatchBoom.State.create(DEHState.create(-1879048154, 13, true, -1879047976, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047976, PatchBoom.State.create(DEHState.create(-1879048154, 14, true, -1879047975, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047975, PatchBoom.State.create(DEHState.create(-1879048154, 15, true, -1879047974, 7, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047974, PatchBoom.State.create(DEHState.create(-1879048154, 16, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), BOSSDEATH));
			put(-1879047973, PatchBoom.State.create(DEHState.create(-1879048154, 15, false, -1879047972, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047972, PatchBoom.State.create(DEHState.create(-1879048154, 14, false, -1879047971, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047971, PatchBoom.State.create(DEHState.create(-1879048154, 13, false, -1879047970, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047970, PatchBoom.State.create(DEHState.create(-1879048154, 12, false, -1879047969, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047969, PatchBoom.State.create(DEHState.create(-1879048154, 11, false, -1879047968, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047968, PatchBoom.State.create(DEHState.create(-1879048154, 10, false, -1879047967, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047967, PatchBoom.State.create(DEHState.create(-1879048154, 9, false, -1879047996, 8, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047966, PatchBoom.State.create(DEHState.create(-1879048153, 0, true, -1879047965, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047965, PatchBoom.State.create(DEHState.create(-1879048153, 1, true, -1879047966, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047964, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047963, 0, 0, 0, new int[]{512, 0, 0, 0, 0, 0, 0, 0}, 0, null), REMOVEFLAGS));
			put(-1879047963, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047962, 0, 0, 0, new int[]{16, 0, 0, 0, 0, 0, 0, 0}, 0, null), REMOVEFLAGS));
			put(-1879047962, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047961, 0, -1879047960, 128, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), RANDOMJUMP));
			put(-1879047961, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047959, 0, -1879048171, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047960, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047959, 0, -1879048170, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047959, PatchBoom.State.create(DEHState.create(-1879048153, 2, true, -1879047958, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047958, PatchBoom.State.create(DEHState.create(-1879048153, 3, true, -1879047957, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047957, PatchBoom.State.create(DEHState.create(-1879048153, 4, true, -1879047956, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047956, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047955, 0, -1879048169, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047955, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047954, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047954, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047953, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047953, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047952, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047952, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047951, 0, -1879048170, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047951, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047950, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047950, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047949, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047949, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047948, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047948, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047947, 0, -1879048169, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047947, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047946, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047946, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047945, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047945, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047944, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047944, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047943, 0, -1879048171, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047943, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047942, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047942, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047941, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047941, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047940, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047940, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047939, 0, -1879048170, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047939, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047938, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047938, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047937, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047937, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047936, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047936, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047935, 0, -1879048171, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047935, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047934, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047934, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047933, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047933, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047932, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047932, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047931, 0, -1879048170, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047931, PatchBoom.State.create(DEHState.create(-1879048153, 5, true, -1879047930, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047930, PatchBoom.State.create(DEHState.create(-1879048153, 6, true, -1879047929, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047929, PatchBoom.State.create(DEHState.create(-1879048153, 7, true, -1879047928, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047928, PatchBoom.State.create(DEHState.create(-1879048153, 8, true, -1879047927, 0, -1879048169, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047927, PatchBoom.State.create(DEHState.create(-1879048153, 8, true, -1879047926, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047926, PatchBoom.State.create(DEHState.create(-1879048153, 9, true, -1879047925, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047925, PatchBoom.State.create(DEHState.create(-1879048153, 10, true, -1879047924, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047924, PatchBoom.State.create(DEHState.create(-1879048153, 11, true, -1879047923, 4, 0, 0, new int[]{10, 128, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047923, PatchBoom.State.create(DEHState.create(-1879048153, 12, true, -1879047922, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047922, PatchBoom.State.create(DEHState.create(-1879048153, 13, true, -1879047921, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047921, PatchBoom.State.create(DEHState.create(-1879048153, 14, true, -1879047920, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047920, PatchBoom.State.create(DEHState.create(-1879048153, 15, true, -1879047919, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047919, PatchBoom.State.create(DEHState.create(-1879048153, 16, true, 0, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047918, PatchBoom.State.create(DEHState.create(-1879048186, 0, false, -1879047917, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879047917, PatchBoom.State.create(DEHState.create(-1879048186, 1, false, -1879047918, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOOK));
			put(-1879047916, PatchBoom.State.create(DEHState.create(-1879048186, 0, false, -1879047915, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), HOOF));
			put(-1879047915, PatchBoom.State.create(DEHState.create(-1879048186, 0, false, -1879047914, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047914, PatchBoom.State.create(DEHState.create(-1879048186, 1, false, -1879047913, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047913, PatchBoom.State.create(DEHState.create(-1879048186, 1, false, -1879047912, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047912, PatchBoom.State.create(DEHState.create(-1879048186, 2, false, -1879047911, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047911, PatchBoom.State.create(DEHState.create(-1879048186, 2, false, -1879047910, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047910, PatchBoom.State.create(DEHState.create(-1879048186, 3, false, -1879047909, 0, -1879048161, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047909, PatchBoom.State.create(DEHState.create(-1879048186, 3, false, -1879047908, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047908, PatchBoom.State.create(DEHState.create(-1879048186, 3, false, -1879047916, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHASE));
			put(-1879047907, PatchBoom.State.create(DEHState.create(-1879048186, 4, false, -1879047906, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047906, PatchBoom.State.create(DEHState.create(-1879048186, 5, true, -1879047905, 12, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CYBERATTACK));
			put(-1879047905, PatchBoom.State.create(DEHState.create(-1879048186, 4, false, -1879047904, 12, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047904, PatchBoom.State.create(DEHState.create(-1879048186, 5, true, -1879047903, 12, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CYBERATTACK));
			put(-1879047903, PatchBoom.State.create(DEHState.create(-1879048186, 4, false, -1879047902, 12, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FACETARGET));
			put(-1879047902, PatchBoom.State.create(DEHState.create(-1879048186, 5, true, -1879047916, 12, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CYBERATTACK));
			put(-1879047901, PatchBoom.State.create(DEHState.create(-1879048186, 6, false, -1879047916, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PAIN));
			put(-1879047900, PatchBoom.State.create(DEHState.create(-1879048186, 7, false, -1879047899, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047899, PatchBoom.State.create(DEHState.create(-1879048186, 8, false, -1879047898, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), SCREAM));
			put(-1879047898, PatchBoom.State.create(DEHState.create(-1879048186, 9, false, -1879047897, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047897, PatchBoom.State.create(DEHState.create(-1879048186, 10, false, -1879047896, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047896, PatchBoom.State.create(DEHState.create(-1879048186, 11, false, -1879047895, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047895, PatchBoom.State.create(DEHState.create(-1879048186, 12, false, -1879047894, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), FALL));
			put(-1879047894, PatchBoom.State.create(DEHState.create(-1879048186, 13, false, -1879047893, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047893, PatchBoom.State.create(DEHState.create(-1879048186, 14, false, -1879047892, 10, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047892, PatchBoom.State.create(DEHState.create(-1879048186, 15, false, -1879047891, 30, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047891, PatchBoom.State.create(DEHState.create(-1879048186, 15, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), BOSSDEATH));
			put(-1879047890, PatchBoom.State.create(DEHState.create(-1879048184, 0, false, -1879047890, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONREADY));
			put(-1879047889, PatchBoom.State.create(DEHState.create(-1879048184, 0, false, -1879047889, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOWER));
			put(-1879047888, PatchBoom.State.create(DEHState.create(-1879048184, 0, false, -1879047888, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), RAISE));
			put(-1879047887, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047886, 0, 0, 0, new int[]{-1879047885, 128, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONJUMP));
			put(-1879047886, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047884, 0, 0, 0, new int[]{-1879048173, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047885, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047884, 0, 0, 0, new int[]{-1879048172, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047884, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047883, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASH));
			put(-1879047883, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047882, 0, 0, 0, new int[]{1, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047882, PatchBoom.State.create(DEHState.create(-1879048185, 0, true, -1879047881, 1, 0, 0, new int[]{-1879048183, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047881, PatchBoom.State.create(DEHState.create(-1879048185, 1, true, -1879047880, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047880, PatchBoom.State.create(DEHState.create(-1879048184, 0, false, -1879047879, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047879, PatchBoom.State.create(DEHState.create(-1879048184, 0, false, -1879047890, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRE));
			put(-1879047878, PatchBoom.State.create(DEHState.create(138, 0, false, -1879047877, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LIGHT2));
			put(-1879047877, PatchBoom.State.create(DEHState.create(138, 0, false, 1, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LIGHT1));
			put(-1879047876, PatchBoom.State.create(DEHState.create(138, 0, true, -1879047875, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047875, PatchBoom.State.create(DEHState.create(-1879048169, 0, true, -1879047874, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047874, PatchBoom.State.create(DEHState.create(-1879048169, 1, true, -1879047873, 2, -1879048174, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047873, PatchBoom.State.create(DEHState.create(-1879048169, 2, true, -1879047872, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047872, PatchBoom.State.create(DEHState.create(-1879048169, 3, true, -1879047871, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047871, PatchBoom.State.create(DEHState.create(-1879048169, 4, true, -1879047870, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047870, PatchBoom.State.create(DEHState.create(-1879048169, 5, true, -1879047869, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047869, PatchBoom.State.create(DEHState.create(-1879048169, 6, true, -1879047868, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047868, PatchBoom.State.create(DEHState.create(-1879048169, 7, true, 0, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047867, PatchBoom.State.create(DEHState.create(-1879048169, 0, true, -1879047866, 0, -1879047865, 128, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), RANDOMJUMP));
			put(-1879047866, PatchBoom.State.create(DEHState.create(-1879048169, 0, true, -1879047864, 0, -1879048171, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047865, PatchBoom.State.create(DEHState.create(-1879048169, 0, true, -1879047864, 0, -1879048170, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047864, PatchBoom.State.create(DEHState.create(-1879048169, 8, true, -1879047863, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047863, PatchBoom.State.create(DEHState.create(-1879048169, 9, true, -1879047862, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047862, PatchBoom.State.create(DEHState.create(-1879048169, 8, true, -1879047861, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047861, PatchBoom.State.create(DEHState.create(-1879048169, 9, true, -1879047860, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047860, PatchBoom.State.create(DEHState.create(-1879048169, 10, true, -1879047859, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047859, PatchBoom.State.create(DEHState.create(-1879048169, 9, true, -1879047858, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047858, PatchBoom.State.create(DEHState.create(-1879048169, 10, true, -1879047857, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047857, PatchBoom.State.create(DEHState.create(-1879048169, 11, true, -1879047856, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047856, PatchBoom.State.create(DEHState.create(-1879048169, 10, true, -1879047855, 2, -1879048169, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), PLAYSOUND));
			put(-1879047855, PatchBoom.State.create(DEHState.create(-1879048169, 11, true, -1879047854, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047854, PatchBoom.State.create(DEHState.create(-1879048169, 12, true, -1879047853, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047853, PatchBoom.State.create(DEHState.create(-1879048169, 11, true, -1879047852, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047852, PatchBoom.State.create(DEHState.create(-1879048169, 12, true, -1879047851, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047851, PatchBoom.State.create(DEHState.create(-1879048169, 13, true, -1879047850, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047850, PatchBoom.State.create(DEHState.create(-1879048169, 12, true, -1879047849, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047849, PatchBoom.State.create(DEHState.create(-1879048169, 13, true, -1879047848, 2, 0, 0, new int[]{5, 64, 0, 0, 0, 0, 0, 0}, 0, null), RADIUSDAMAGE));
			put(-1879047848, PatchBoom.State.create(DEHState.create(-1879048169, 14, true, -1879047847, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047847, PatchBoom.State.create(DEHState.create(-1879048169, 13, true, -1879047846, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047846, PatchBoom.State.create(DEHState.create(-1879048169, 14, true, -1879047845, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047845, PatchBoom.State.create(DEHState.create(-1879048169, 15, true, -1879047844, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047844, PatchBoom.State.create(DEHState.create(-1879048169, 14, true, -1879047843, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047843, PatchBoom.State.create(DEHState.create(-1879048169, 15, true, 0, 2, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047842, PatchBoom.State.create(DEHState.create(-1879048152, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047841, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047841, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONREADY));
			put(-1879047840, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047840, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LOWER));
			put(-1879047839, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047839, 1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), RAISE));
			put(-1879047838, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047837, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047837, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047836, 0, 0, 0, new int[]{-1879047763, 1, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASHTO));
			put(-1879047836, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047835, 20, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047835, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047834, 0, 0, 0, new int[]{-1879047773, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHECKAMMO));
			put(-1879047834, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047773, 0, 0, 0, new int[]{-1879047833, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRETO));
			put(-1879047833, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047832, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047832, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047831, 0, 0, 0, new int[]{-1879047759, 1, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASHTO));
			put(-1879047831, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047830, 20, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047830, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047829, 0, 0, 0, new int[]{-1879047779, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHECKAMMO));
			put(-1879047829, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047779, 0, 0, 0, new int[]{-1879047828, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRETO));
			put(-1879047828, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047827, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047827, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047826, 0, 0, 0, new int[]{-1879047755, 1, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASHTO));
			put(-1879047826, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047825, 20, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047825, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047824, 0, 0, 0, new int[]{-1879047788, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHECKAMMO));
			put(-1879047824, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047788, 0, 0, 0, new int[]{-1879047823, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRETO));
			put(-1879047823, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047822, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047822, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047821, 0, 0, 0, new int[]{-1879047751, 1, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASHTO));
			put(-1879047821, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047820, 20, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047820, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047819, 0, 0, 0, new int[]{-1879047800, 0, 0, 0, 0, 0, 0, 0}, 0, null), CHECKAMMO));
			put(-1879047819, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047800, 0, 0, 0, new int[]{-1879047818, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRETO));
			put(-1879047818, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047817, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), CONSUMEAMMO));
			put(-1879047817, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047816, 0, 0, 0, new int[]{-1879047747, 1, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASHTO));
			put(-1879047816, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047815, 20, 0, 0, new int[]{-1879048177, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047815, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047814, 0, 0, 0, new int[]{-1879048182, -2293760, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047814, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047813, 0, 0, 0, new int[]{-1879048182, -1966080, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047813, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047812, 0, 0, 0, new int[]{-1879048182, -1638400, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047812, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047811, 0, 0, 0, new int[]{-1879048182, -1310720, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047811, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047810, 0, 0, 0, new int[]{-1879048182, -983040, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047810, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047809, 0, 0, 0, new int[]{-1879048182, -655360, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047809, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047808, 0, 0, 0, new int[]{-1879048182, -327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047808, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047807, 0, 0, 0, new int[]{-1879048182, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047807, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047806, 0, 0, 0, new int[]{-1879048182, 327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047806, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047805, 0, 0, 0, new int[]{-1879048182, 655360, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047805, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047804, 0, 0, 0, new int[]{-1879048182, 983040, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047804, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047803, 0, 0, 0, new int[]{-1879048182, 1310720, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047803, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047802, 0, 0, 0, new int[]{-1879048182, 1638400, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047802, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047801, 0, 0, 0, new int[]{-1879048182, 1966080, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047801, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047770, 0, 0, 0, new int[]{-1879048182, 2293760, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047800, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047799, 0, 0, 0, new int[]{-1879048182, -1802240, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047799, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047798, 0, 0, 0, new int[]{-1879048182, -1474560, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047798, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047797, 0, 0, 0, new int[]{-1879048182, -1146880, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047797, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047796, 0, 0, 0, new int[]{-1879048182, -819200, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047796, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047795, 0, 0, 0, new int[]{-1879048182, -491520, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047795, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047794, 0, 0, 0, new int[]{-1879048182, -163840, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047794, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047793, 0, 0, 0, new int[]{-1879048182, 163840, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047793, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047792, 0, 0, 0, new int[]{-1879048182, 491520, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047792, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047791, 0, 0, 0, new int[]{-1879048182, 819200, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047791, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047790, 0, 0, 0, new int[]{-1879048182, 1146880, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047790, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047789, 0, 0, 0, new int[]{-1879048182, 1474560, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047789, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047770, 0, 0, 0, new int[]{-1879048182, 1802240, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047788, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047787, 0, 0, 0, new int[]{-1879048182, -1310720, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047787, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047786, 0, 0, 0, new int[]{-1879048182, -983040, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047786, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047785, 0, 0, 0, new int[]{-1879048182, -655360, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047785, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047784, 0, 0, 0, new int[]{-1879048182, -327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047784, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047783, 0, 0, 0, new int[]{-1879048182, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047783, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047782, 0, 0, 0, new int[]{-1879048182, 327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047782, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047781, 0, 0, 0, new int[]{-1879048182, 655360, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047781, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047780, 0, 0, 0, new int[]{-1879048182, 983040, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047780, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047770, 0, 0, 0, new int[]{-1879048182, 1310720, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047779, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047778, 0, 0, 0, new int[]{-1879048182, -819200, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047778, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047777, 0, 0, 0, new int[]{-1879048182, -491520, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047777, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047776, 0, 0, 0, new int[]{-1879048182, -163840, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047776, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047775, 0, 0, 0, new int[]{-1879048182, 163840, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047775, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047774, 0, 0, 0, new int[]{-1879048182, 491520, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047774, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047770, 0, 0, 0, new int[]{-1879048182, 819200, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047773, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047772, 0, 0, 0, new int[]{-1879048182, -327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047772, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047771, 0, 0, 0, new int[]{-1879048182, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047771, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047770, 0, 0, 0, new int[]{-1879048182, 327680, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONPROJECTILE));
			put(-1879047770, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047769, 0, 0, 0, new int[]{-1879048176, 0, 0, 0, 0, 0, 0, 0}, 0, null), WEAPONSOUND));
			put(-1879047769, PatchBoom.State.create(DEHState.create(-1879048171, 0, true, -1879047768, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), GUNFLASH));
			put(-1879047768, PatchBoom.State.create(DEHState.create(-1879048171, 1, true, -1879047767, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047767, PatchBoom.State.create(DEHState.create(-1879048170, 3, false, -1879047766, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047766, PatchBoom.State.create(DEHState.create(-1879048170, 2, false, -1879047765, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047765, PatchBoom.State.create(DEHState.create(-1879048170, 1, false, -1879047764, 4, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047764, PatchBoom.State.create(DEHState.create(-1879048170, 0, false, -1879047841, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), REFIRE));
			put(-1879047763, PatchBoom.State.create(DEHState.create(-1879048172, 0, true, -1879047762, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047762, PatchBoom.State.create(DEHState.create(-1879048172, 1, true, -1879047761, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047761, PatchBoom.State.create(DEHState.create(-1879048172, 2, true, -1879047760, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047760, PatchBoom.State.create(DEHState.create(-1879048172, 3, true, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047759, PatchBoom.State.create(DEHState.create(-1879048172, 4, true, -1879047758, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047758, PatchBoom.State.create(DEHState.create(-1879048172, 5, true, -1879047757, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047757, PatchBoom.State.create(DEHState.create(-1879048172, 6, true, -1879047756, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047756, PatchBoom.State.create(DEHState.create(-1879048172, 7, true, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047755, PatchBoom.State.create(DEHState.create(-1879048172, 8, true, -1879047754, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047754, PatchBoom.State.create(DEHState.create(-1879048172, 9, true, -1879047753, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047753, PatchBoom.State.create(DEHState.create(-1879048172, 10, true, -1879047752, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047752, PatchBoom.State.create(DEHState.create(-1879048172, 11, true, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047751, PatchBoom.State.create(DEHState.create(-1879048172, 12, true, -1879047750, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047750, PatchBoom.State.create(DEHState.create(-1879048172, 13, true, -1879047749, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047749, PatchBoom.State.create(DEHState.create(-1879048172, 14, true, -1879047748, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047748, PatchBoom.State.create(DEHState.create(-1879048172, 15, true, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047747, PatchBoom.State.create(DEHState.create(-1879048172, 16, true, -1879047746, 6, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047746, PatchBoom.State.create(DEHState.create(-1879048172, 17, true, -1879047745, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047745, PatchBoom.State.create(DEHState.create(-1879048172, 18, true, -1879047744, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047744, PatchBoom.State.create(DEHState.create(-1879048172, 19, true, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047743, PatchBoom.State.create(DEHState.create(138, 0, false, -1879047742, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LIGHT1));
			put(-1879047742, PatchBoom.State.create(DEHState.create(138, 0, false, 1, 5, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), LIGHT2));
			put(-1879047741, PatchBoom.State.create(DEHState.create(138, 0, false, -1879047740, 0, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047740, PatchBoom.State.create(DEHState.create(138, 0, false, 0, 1, 0, 0, new int[]{-1879048181, 0, 0, 0, 0, 1310720, 0, 0}, 0, null), SPAWNOBJECT));
			put(-1879047739, PatchBoom.State.create(DEHState.create(-1879048173, 0, true, -1879047738, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047738, PatchBoom.State.create(DEHState.create(-1879048173, 1, true, -1879047737, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047737, PatchBoom.State.create(DEHState.create(-1879048173, 2, true, -1879047739, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047736, PatchBoom.State.create(DEHState.create(-1879048173, 3, true, -1879047735, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047735, PatchBoom.State.create(DEHState.create(-1879048173, 4, true, -1879047734, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047734, PatchBoom.State.create(DEHState.create(-1879048173, 5, true, -1879047733, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047733, PatchBoom.State.create(DEHState.create(-1879048173, 6, true, -1879047732, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047732, PatchBoom.State.create(DEHState.create(-1879048173, 7, true, -1879047731, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047731, PatchBoom.State.create(DEHState.create(-1879048173, 8, true, 0, 3, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047730, PatchBoom.State.create(DEHState.create(-1879048151, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047729, PatchBoom.State.create(DEHState.create(-1879048150, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
			put(-1879047728, PatchBoom.State.create(DEHState.create(-1879048149, 0, false, 0, -1, 0, 0, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, null), null));
		}
	};

	static final Map<Integer, DEHThing> DEHTHINGID24 = new TreeMap<Integer, DEHThing>() 
	{
		private static final long serialVersionUID = 4318271333802278697L;
		{
			put(-1879048192, (new DEHThing()).setName("Ghoul")
				.setEditorNumber(-28672)
				.setHealth(50)
				.setSpeed(12)
				.setRadius(16)
				.setHeight(40)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(128)
				.setFlags(4211206)
				.setMass(50)
				.setSpawnFrameIndex(-1879048150)
				.setWalkFrameIndex(-1879048148)
				.setPainFrameIndex(-1879048136)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879048140)
				.setDeathFrameIndex(-1879048134)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048178)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(-1879048179)
				.setDeathSoundPosition(-1879048180)
				.setActiveSoundPosition(-1879048181)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048191, (new DEHThing()).setName("Banshee")
				.setEditorNumber(-28671)
				.setHealth(100)
				.setSpeed(8)
				.setRadius(20)
				.setHeight(56)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(64)
				.setFlags(4211206)
				.setMass(500)
				.setSpawnFrameIndex(-1879048119)
				.setWalkFrameIndex(-1879048117)
				.setPainFrameIndex(-1879048097)
				.setMeleeFrameIndex(-1879048098)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879048095)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048192)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(-1879048190)
				.setDeathSoundPosition(-1879048191)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048190, (new DEHThing()).setName("Mindweaver")
				.setEditorNumber(-28670)
				.setHealth(500)
				.setSpeed(12)
				.setRadius(64)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(40)
				.setFlags(4194310)
				.setMass(600)
				.setSpawnFrameIndex(-1879048089)
				.setWalkFrameIndex(-1879048087)
				.setPainFrameIndex(-1879048068)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879048072)
				.setDeathFrameIndex(-1879048066)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(-1879048059)
				.setSeeSoundPosition(-1879048186)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(26)
				.setDeathSoundPosition(-1879048187)
				.setActiveSoundPosition(-1879048188)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048189, (new DEHThing()).setName("Shocktrooper")
				.setEditorNumber(-28669)
				.setHealth(100)
				.setSpeed(10)
				.setRadius(20)
				.setHeight(56)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(30)
				.setFlags(4194310)
				.setMass(100)
				.setSpawnFrameIndex(-1879048052)
				.setWalkFrameIndex(-1879048050)
				.setPainFrameIndex(-1879048035)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879048042)
				.setDeathFrameIndex(-1879048033)
				.setExtremeDeathFrameIndex(-1879048026)
				.setRaiseFrameIndex(-1879048017)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(-1879048164)
				.setDeathSoundPosition(-1879048166)
				.setActiveSoundPosition(-1879048167)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048188, (new DEHThing()).setName("Vassago")
				.setEditorNumber(-28668)
				.setHealth(1000)
				.setSpeed(8)
				.setRadius(24)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(100)
				.setFlags(4194310)
				.setMass(1000)
				.setSpawnFrameIndex(-1879047998)
				.setWalkFrameIndex(-1879047996)
				.setPainFrameIndex(-1879047983)
				.setMeleeFrameIndex(-1879047988)
				.setMissileFrameIndex(-1879047988)
				.setDeathFrameIndex(-1879047981)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(-1879047973)
				.setSeeSoundPosition(-1879048156)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(-1879048157)
				.setDeathSoundPosition(-1879048158)
				.setActiveSoundPosition(-1879048160)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(2)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048187, (new DEHThing()).setName("Tyrant")
				.setEditorNumber(-28667)
				.setHealth(1000)
				.setSpeed(16)
				.setRadius(40)
				.setHeight(110)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(20)
				.setFlags(4194310)
				.setMass(1000)
				.setSpawnFrameIndex(-1879047918)
				.setWalkFrameIndex(-1879047916)
				.setPainFrameIndex(-1879047901)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879047907)
				.setDeathFrameIndex(-1879047900)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048162)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(26)
				.setDeathSoundPosition(-1879048163)
				.setActiveSoundPosition(77)
				.setDroppedItem(-1)
				.setMBF21Flags(303720)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(3)
				.setSplashGroup(3)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048186, (new DEHThing()).setName("Tyrant (Boss 1)")
				.setEditorNumber(-28666)
				.setHealth(1000)
				.setSpeed(16)
				.setRadius(40)
				.setHeight(110)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(20)
				.setFlags(4194310)
				.setMass(1000)
				.setSpawnFrameIndex(-1879047918)
				.setWalkFrameIndex(-1879047916)
				.setPainFrameIndex(-1879047901)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879047907)
				.setDeathFrameIndex(-1879047900)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048162)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(26)
				.setDeathSoundPosition(-1879048163)
				.setActiveSoundPosition(77)
				.setDroppedItem(-1)
				.setMBF21Flags(303720)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(3)
				.setSplashGroup(3)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(2100)
				.setRespawnDice(64)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048185, (new DEHThing()).setName("Tyrant (Boss 2)")
				.setEditorNumber(-28665)
				.setHealth(1000)
				.setSpeed(16)
				.setRadius(40)
				.setHeight(110)
				.setDamage(0)
				.setReactionTime(8)
				.setPainChance(20)
				.setFlags(4194310)
				.setMass(1000)
				.setSpawnFrameIndex(-1879047918)
				.setWalkFrameIndex(-1879047916)
				.setPainFrameIndex(-1879047901)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(-1879047907)
				.setDeathFrameIndex(-1879047900)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048162)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(26)
				.setDeathSoundPosition(-1879048163)
				.setActiveSoundPosition(77)
				.setDroppedItem(-1)
				.setMBF21Flags(303720)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(3)
				.setSplashGroup(3)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(2100)
				.setRespawnDice(64)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048184, (new DEHThing()).setName("Incinerator Flame")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(40)
				.setRadius(13)
				.setHeight(8)
				.setDamage(5)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(-2147416560)
				.setMass(0)
				.setSpawnFrameIndex(-1879047876)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879047867)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(16)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048183, (new DEHThing()).setName("Heatwave Spawner")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(8)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(67088)
				.setMass(0)
				.setSpawnFrameIndex(-1879047741)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879047740)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(-1879048175)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(131072)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048182, (new DEHThing()).setName("Heatwave Ripper")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(20)
				.setRadius(16)
				.setHeight(8)
				.setDamage(10)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(-2147416560)
				.setMass(0)
				.setSpawnFrameIndex(-1879047739)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879047736)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(-1879048175)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(131072)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048181, (new DEHThing()).setName("Ghoul Ball")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(15)
				.setRadius(6)
				.setHeight(8)
				.setDamage(3)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(-2147416560)
				.setMass(0)
				.setSpawnFrameIndex(-1879048126)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879048124)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(16)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(17)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(1310720)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048180, (new DEHThing()).setName("Shocktrooper Head")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(8)
				.setRadius(6)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(66560)
				.setMass(0)
				.setSpawnFrameIndex(-1879048011)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879048002)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(-1879048165)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(1)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048179, (new DEHThing()).setName("Shocktrooper Torso")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(8)
				.setRadius(6)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(66576)
				.setMass(0)
				.setSpawnFrameIndex(-1879048001)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879048000)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048178, (new DEHThing()).setName("Vassago Flame")
				.setEditorNumber(0)
				.setHealth(0)
				.setSpeed(15)
				.setRadius(6)
				.setHeight(16)
				.setDamage(5)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(-2147416560)
				.setMass(0)
				.setSpawnFrameIndex(-1879047966)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879047964)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(-1879048174)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(91)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(1310720)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(2)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048177, (new DEHThing()).setName("Stalagmite (gray)")
				.setEditorNumber(-28664)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048177)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048176, (new DEHThing()).setName("Large corpse pile")
				.setEditorNumber(-28663)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(40)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048192)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048175, (new DEHThing()).setName("Human BBQ 1")
				.setEditorNumber(-28662)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048191)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048174, (new DEHThing()).setName("Human BBQ 2")
				.setEditorNumber(-28661)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048188)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048173, (new DEHThing()).setName("Hanging victim, both legs")
				.setEditorNumber(-28660)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(80)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048185)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048172, (new DEHThing()).setName("Hanging victim, both legs (blocking)")
				.setEditorNumber(-28659)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(80)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048185)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048171, (new DEHThing()).setName("Hanging victim, crucified")
				.setEditorNumber(-28658)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048184)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048170, (new DEHThing()).setName("Hanging victim, crucified (blocking)")
				.setEditorNumber(-28657)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048184)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048169, (new DEHThing()).setName("Hanging victim, arms bound")
				.setEditorNumber(-28656)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(72)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048183)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048168, (new DEHThing()).setName("Hanging victim, arms bound (blocking)")
				.setEditorNumber(-28655)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(72)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048183)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048167, (new DEHThing()).setName("Hanging baron of hell")
				.setEditorNumber(-28654)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(80)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048182)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048166, (new DEHThing()).setName("Hanging baron of hell (blocking)")
				.setEditorNumber(-28653)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(80)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048182)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048165, (new DEHThing()).setName("Hanging victim, chained")
				.setEditorNumber(-28652)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(84)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048181)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048164, (new DEHThing()).setName("Hanging victim, chained (blocking)")
				.setEditorNumber(-28651)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(84)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048181)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048163, (new DEHThing()).setName("Hanging torso, chained")
				.setEditorNumber(-28650)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(52)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048180)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048162, (new DEHThing()).setName("Hanging torso, chained (blocking)")
				.setEditorNumber(-28649)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(52)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048180)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048161, (new DEHThing()).setName("Skull pole trio")
				.setEditorNumber(-28648)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048179)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048160, (new DEHThing()).setName("Skull gibs")
				.setEditorNumber(-28647)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048178)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048159, (new DEHThing()).setName("Bush, short")
				.setEditorNumber(-28646)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048176)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048158, (new DEHThing()).setName("Bush, short burned 1")
				.setEditorNumber(-28645)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048175)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048157, (new DEHThing()).setName("Bush, short burned 2")
				.setEditorNumber(-28644)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048174)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048156, (new DEHThing()).setName("Bush, tall")
				.setEditorNumber(-28643)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048173)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048155, (new DEHThing()).setName("Bush, tall burned 1")
				.setEditorNumber(-28642)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048172)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048154, (new DEHThing()).setName("Bush, tall burned 2")
				.setEditorNumber(-28641)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048171)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048153, (new DEHThing()).setName("Cave rock column")
				.setEditorNumber(-28640)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(24)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048170)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048152, (new DEHThing()).setName("Cave stalagmite, large")
				.setEditorNumber(-28639)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048169)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048151, (new DEHThing()).setName("Cave stalagmite, medium")
				.setEditorNumber(-28638)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048168)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048150, (new DEHThing()).setName("Cave stalagmite, small")
				.setEditorNumber(-28637)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048167)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048149, (new DEHThing()).setName("Cave stalactite, large")
				.setEditorNumber(-28636)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(112)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048166)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048148, (new DEHThing()).setName("Cave stalactite, large (blocking)")
				.setEditorNumber(-28635)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(112)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048166)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048147, (new DEHThing()).setName("Cave stalactite, medium")
				.setEditorNumber(-28634)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048165)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048146, (new DEHThing()).setName("Cave stalactite, medium (blocking)")
				.setEditorNumber(-28633)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(64)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048165)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048145, (new DEHThing()).setName("Cave stalactite, small")
				.setEditorNumber(-28632)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(32)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048164)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048144, (new DEHThing()).setName("Cave stalactite, small (blocking)")
				.setEditorNumber(-28631)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(32)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(770)
				.setMass(0)
				.setSpawnFrameIndex(-1879048164)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048143, (new DEHThing()).setName("Office chair")
				.setEditorNumber(-28630)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048163)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048142, (new DEHThing()).setName("Office lamp (breakable)")
				.setEditorNumber(-28629)
				.setHealth(20)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(52)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(524294)
				.setMass(65536)
				.setSpawnFrameIndex(-1879048162)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(-1879048161)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(-1879048189)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048141, (new DEHThing()).setName("Ceiling lamp")
				.setEditorNumber(-28628)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(12)
				.setHeight(32)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(768)
				.setMass(0)
				.setSpawnFrameIndex(-1879048157)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048140, (new DEHThing()).setName("Candelabra (short)")
				.setEditorNumber(-28627)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(16)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(2)
				.setMass(0)
				.setSpawnFrameIndex(-1879048156)
				.setWalkFrameIndex(0)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048139, (new DEHThing()).setName("Ambient Klaxon")
				.setEditorNumber(-28626)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048155)
				.setWalkFrameIndex(-1879048154)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048138, (new DEHThing()).setName("Ambient Portal Open")
				.setEditorNumber(-28625)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048155)
				.setWalkFrameIndex(-1879048153)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048137, (new DEHThing()).setName("Ambient Portal Loop")
				.setEditorNumber(-28624)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048155)
				.setWalkFrameIndex(-1879048152)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);

			put(-1879048136, (new DEHThing()).setName("Ambient Portal Close")
				.setEditorNumber(-28623)
				.setHealth(0)
				.setSpeed(0)
				.setRadius(8)
				.setHeight(16)
				.setDamage(0)
				.setReactionTime(0)
				.setPainChance(0)
				.setFlags(0)
				.setMass(0)
				.setSpawnFrameIndex(-1879048155)
				.setWalkFrameIndex(-1879048151)
				.setPainFrameIndex(0)
				.setMeleeFrameIndex(0)
				.setMissileFrameIndex(0)
				.setDeathFrameIndex(0)
				.setExtremeDeathFrameIndex(0)
				.setRaiseFrameIndex(0)
				.setSeeSoundPosition(0)
				.setAttackSoundPosition(0)
				.setPainSoundPosition(0)
				.setDeathSoundPosition(0)
				.setActiveSoundPosition(0)
				.setDroppedItem(-1)
				.setMBF21Flags(0)
				.setFastSpeed(-1)
				.setMeleeRange(64)
				.setInfightingGroup(0)
				.setProjectileGroup(0)
				.setSplashGroup(0)
				.setRipSoundPosition(0)
				.setID24Flags(0)
				.setMinRespawnTics(420)
				.setRespawnDice(4)
				.setPickupAmmoType(-1)
				.setPickupAmmoCategory(-1)
				.setPickupWeaponType(-1)
				.setPickupItemType(-1)
				.setPickupBonusCount(6)
				.setPickupSoundPosition(0)
				.setPickupMessageMnemonic(null)
				.setTranslation(null)
			);
			
			// TODO: Finish Weapons/Ammo
			
		}
	};

	static final Map<Integer, DEHSound> DEHSOUNDID24 = new TreeMap<Integer, DEHSound>() 
	{
		private static final long serialVersionUID = -2091378296841110122L;
		{
			// TODO: Finish.
		}
	};

	static final Map<Integer, DEHWeapon> DEHWEAPONID24 = new TreeMap<Integer, DEHWeapon>() 
	{
		private static final long serialVersionUID = -3304640552185670385L;
		{
			// TODO: Finish.
		}
	};

	// TODO: Finish me.
	
}
