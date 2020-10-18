package net.mtrop.doom.tools.decohack.patches;

import net.mtrop.doom.tools.decohack.DEHAmmo;
import net.mtrop.doom.tools.decohack.DEHMiscellany;
import net.mtrop.doom.tools.decohack.DEHSound;
import net.mtrop.doom.tools.decohack.DEHThing;
import net.mtrop.doom.tools.decohack.DEHWeapon;
import net.mtrop.doom.tools.decohack.DEHWeapon.Ammo;

/**
 * Patch constants. 
 * @author Matthew Tropiano
 */
interface Constants 
{
	static final int S_NULL          = 0;
	static final int S_LIGHTDONE     = 1;
	static final int S_PUNCH         = 2;
	static final int S_PUNCHDOWN     = 3;
	static final int S_PUNCHUP       = 4;
	static final int S_PUNCH1        = 5;
	static final int S_PUNCH2        = 6;
	static final int S_PUNCH3        = 7;
	static final int S_PUNCH4        = 8;
	static final int S_PUNCH5        = 9;
	static final int S_PISTOL        = 10;
	static final int S_PISTOLDOWN    = 11;
	static final int S_PISTOLUP      = 12;
	static final int S_PISTOL1       = 13;
	static final int S_PISTOL2       = 14;
	static final int S_PISTOL3       = 15;
	static final int S_PISTOL4       = 16;
	static final int S_PISTOLFLASH   = 17;
	static final int S_SGUN          = 18;
	static final int S_SGUNDOWN      = 19;
	static final int S_SGUNUP        = 20;
	static final int S_SGUN1         = 21;
	static final int S_SGUN2         = 22;
	static final int S_SGUN3         = 23;
	static final int S_SGUN4         = 24;
	static final int S_SGUN5         = 25;
	static final int S_SGUN6         = 26;
	static final int S_SGUN7         = 27;
	static final int S_SGUN8         = 28;
	static final int S_SGUN9         = 29;
	static final int S_SGUNFLASH1    = 30;
	static final int S_SGUNFLASH2    = 31;
	static final int S_DSGUN         = 32;
	static final int S_DSGUNDOWN     = 33;
	static final int S_DSGUNUP       = 34;
	static final int S_DSGUN1        = 35;
	static final int S_DSGUN2        = 36;
	static final int S_DSGUN3        = 37;
	static final int S_DSGUN4        = 38;
	static final int S_DSGUN5        = 39;
	static final int S_DSGUN6        = 40;
	static final int S_DSGUN7        = 41;
	static final int S_DSGUN8        = 42;
	static final int S_DSGUN9        = 43;
	static final int S_DSGUN10       = 44;
	static final int S_DSNR1         = 45;
	static final int S_DSNR2         = 46;
	static final int S_DSGUNFLASH1   = 47;
	static final int S_DSGUNFLASH2   = 48;
	static final int S_CHAIN         = 49;
	static final int S_CHAINDOWN     = 50;
	static final int S_CHAINUP       = 51;
	static final int S_CHAIN1        = 52;
	static final int S_CHAIN2        = 53;
	static final int S_CHAIN3        = 54;
	static final int S_CHAINFLASH1   = 55;
	static final int S_CHAINFLASH2   = 56;
	static final int S_MISSILE       = 57;
	static final int S_MISSILEDOWN   = 58;
	static final int S_MISSILEUP     = 59;
	static final int S_MISSILE1      = 60;
	static final int S_MISSILE2      = 61;
	static final int S_MISSILE3      = 62;
	static final int S_MISSILEFLASH1 = 63;
	static final int S_MISSILEFLASH2 = 64;
	static final int S_MISSILEFLASH3 = 65;
	static final int S_MISSILEFLASH4 = 66;
	static final int S_SAW           = 67;
	static final int S_SAWB          = 68;
	static final int S_SAWDOWN       = 69;
	static final int S_SAWUP         = 70;
	static final int S_SAW1          = 71;
	static final int S_SAW2          = 72;
	static final int S_SAW3          = 73;
	static final int S_PLASMA        = 74;
	static final int S_PLASMADOWN    = 75;
	static final int S_PLASMAUP      = 76;
	static final int S_PLASMA1       = 77;
	static final int S_PLASMA2       = 78;
	static final int S_PLASMAFLASH1  = 79;
	static final int S_PLASMAFLASH2  = 80;
	static final int S_BFG           = 81;
	static final int S_BFGDOWN       = 82;
	static final int S_BFGUP         = 83;
	static final int S_BFG1          = 84;
	static final int S_BFG2          = 85;
	static final int S_BFG3          = 86;
	static final int S_BFG4          = 87;
	static final int S_BFGFLASH1     = 88;
	static final int S_BFGFLASH2     = 89;
	static final int S_BLOOD1        = 90;
	static final int S_BLOOD2        = 91;
	static final int S_BLOOD3        = 92;
	static final int S_PUFF1         = 93;
	static final int S_PUFF2         = 94;
	static final int S_PUFF3         = 95;
	static final int S_PUFF4         = 96;
	static final int S_TBALL1        = 97;
	static final int S_TBALL2        = 98;
	static final int S_TBALLX1       = 99;
	static final int S_TBALLX2       = 100;
	static final int S_TBALLX3       = 101;
	static final int S_RBALL1        = 102;
	static final int S_RBALL2        = 103;
	static final int S_RBALLX1       = 104;
	static final int S_RBALLX2       = 105;
	static final int S_RBALLX3       = 106;
	static final int S_PLASBALL      = 107;
	static final int S_PLASBALL2     = 108;
	static final int S_PLASEXP       = 109;
	static final int S_PLASEXP2      = 110;
	static final int S_PLASEXP3      = 111;
	static final int S_PLASEXP4      = 112;
	static final int S_PLASEXP5      = 113;
	static final int S_ROCKET        = 114;
	static final int S_BFGSHOT       = 115;
	static final int S_BFGSHOT2      = 116;
	static final int S_BFGLAND       = 117;
	static final int S_BFGLAND2      = 118;
	static final int S_BFGLAND3      = 119;
	static final int S_BFGLAND4      = 120;
	static final int S_BFGLAND5      = 121;
	static final int S_BFGLAND6      = 122;
	static final int S_BFGEXP        = 123;
	static final int S_BFGEXP2       = 124;
	static final int S_BFGEXP3       = 125;
	static final int S_BFGEXP4       = 126;
	static final int S_EXPLODE1      = 127;
	static final int S_EXPLODE2      = 128;
	static final int S_EXPLODE3      = 129;
	static final int S_TFOG          = 130;
	static final int S_TFOG01        = 131;
	static final int S_TFOG02        = 132;
	static final int S_TFOG2         = 133;
	static final int S_TFOG3         = 134;
	static final int S_TFOG4         = 135;
	static final int S_TFOG5         = 136;
	static final int S_TFOG6         = 137;
	static final int S_TFOG7         = 138;
	static final int S_TFOG8         = 139;
	static final int S_TFOG9         = 140;
	static final int S_TFOG10        = 141;
	static final int S_IFOG          = 142;
	static final int S_IFOG01        = 143;
	static final int S_IFOG02        = 144;
	static final int S_IFOG2         = 145;
	static final int S_IFOG3         = 146;
	static final int S_IFOG4         = 147;
	static final int S_IFOG5         = 148;
	static final int S_PLAY          = 149;
	static final int S_PLAY_RUN1     = 150;
	static final int S_PLAY_RUN2     = 151;
	static final int S_PLAY_RUN3     = 152;
	static final int S_PLAY_RUN4     = 153;
	static final int S_PLAY_ATK1     = 154;
	static final int S_PLAY_ATK2     = 155;
	static final int S_PLAY_PAIN     = 156;
	static final int S_PLAY_PAIN2    = 157;
	static final int S_PLAY_DIE1     = 158;
	static final int S_PLAY_DIE2     = 159;
	static final int S_PLAY_DIE3     = 160;
	static final int S_PLAY_DIE4     = 161;
	static final int S_PLAY_DIE5     = 162;
	static final int S_PLAY_DIE6     = 163;
	static final int S_PLAY_DIE7     = 164;
	static final int S_PLAY_XDIE1    = 165;
	static final int S_PLAY_XDIE2    = 166;
	static final int S_PLAY_XDIE3    = 167;
	static final int S_PLAY_XDIE4    = 168;
	static final int S_PLAY_XDIE5    = 169;
	static final int S_PLAY_XDIE6    = 170;
	static final int S_PLAY_XDIE7    = 171;
	static final int S_PLAY_XDIE8    = 172;
	static final int S_PLAY_XDIE9    = 173;
	static final int S_POSS_STND     = 174;
	static final int S_POSS_STND2    = 175;
	static final int S_POSS_RUN1     = 176;
	static final int S_POSS_RUN2     = 177;
	static final int S_POSS_RUN3     = 178;
	static final int S_POSS_RUN4     = 179;
	static final int S_POSS_RUN5     = 180;
	static final int S_POSS_RUN6     = 181;
	static final int S_POSS_RUN7     = 182;
	static final int S_POSS_RUN8     = 183;
	static final int S_POSS_ATK1     = 184;
	static final int S_POSS_ATK2     = 185;
	static final int S_POSS_ATK3     = 186;
	static final int S_POSS_PAIN     = 187;
	static final int S_POSS_PAIN2    = 188;
	static final int S_POSS_DIE1     = 189;
	static final int S_POSS_DIE2     = 190;
	static final int S_POSS_DIE3     = 191;
	static final int S_POSS_DIE4     = 192;
	static final int S_POSS_DIE5     = 193;
	static final int S_POSS_XDIE1    = 194;
	static final int S_POSS_XDIE2    = 195;
	static final int S_POSS_XDIE3    = 196;
	static final int S_POSS_XDIE4    = 197;
	static final int S_POSS_XDIE5    = 198;
	static final int S_POSS_XDIE6    = 199;
	static final int S_POSS_XDIE7    = 200;
	static final int S_POSS_XDIE8    = 201;
	static final int S_POSS_XDIE9    = 202;
	static final int S_POSS_RAISE1   = 203;
	static final int S_POSS_RAISE2   = 204;
	static final int S_POSS_RAISE3   = 205;
	static final int S_POSS_RAISE4   = 206;
	static final int S_SPOS_STND     = 207;
	static final int S_SPOS_STND2    = 208;
	static final int S_SPOS_RUN1     = 209;
	static final int S_SPOS_RUN2     = 210;
	static final int S_SPOS_RUN3     = 211;
	static final int S_SPOS_RUN4     = 212;
	static final int S_SPOS_RUN5     = 213;
	static final int S_SPOS_RUN6     = 214;
	static final int S_SPOS_RUN7     = 215;
	static final int S_SPOS_RUN8     = 216;
	static final int S_SPOS_ATK1     = 217;
	static final int S_SPOS_ATK2     = 218;
	static final int S_SPOS_ATK3     = 219;
	static final int S_SPOS_PAIN     = 220;
	static final int S_SPOS_PAIN2    = 221;
	static final int S_SPOS_DIE1     = 222;
	static final int S_SPOS_DIE2     = 223;
	static final int S_SPOS_DIE3     = 224;
	static final int S_SPOS_DIE4     = 225;
	static final int S_SPOS_DIE5     = 226;
	static final int S_SPOS_XDIE1    = 227;
	static final int S_SPOS_XDIE2    = 228;
	static final int S_SPOS_XDIE3    = 229;
	static final int S_SPOS_XDIE4    = 230;
	static final int S_SPOS_XDIE5    = 231;
	static final int S_SPOS_XDIE6    = 232;
	static final int S_SPOS_XDIE7    = 233;
	static final int S_SPOS_XDIE8    = 234;
	static final int S_SPOS_XDIE9    = 235;
	static final int S_SPOS_RAISE1   = 236;
	static final int S_SPOS_RAISE2   = 237;
	static final int S_SPOS_RAISE3   = 238;
	static final int S_SPOS_RAISE4   = 239;
	static final int S_SPOS_RAISE5   = 240;
	static final int S_VILE_STND     = 241;
	static final int S_VILE_STND2    = 242;
	static final int S_VILE_RUN1     = 243;
	static final int S_VILE_RUN2     = 244;
	static final int S_VILE_RUN3     = 245;
	static final int S_VILE_RUN4     = 246;
	static final int S_VILE_RUN5     = 247;
	static final int S_VILE_RUN6     = 248;
	static final int S_VILE_RUN7     = 249;
	static final int S_VILE_RUN8     = 250;
	static final int S_VILE_RUN9     = 251;
	static final int S_VILE_RUN10    = 252;
	static final int S_VILE_RUN11    = 253;
	static final int S_VILE_RUN12    = 254;
	static final int S_VILE_ATK1     = 255;
	static final int S_VILE_ATK2     = 256;
	static final int S_VILE_ATK3     = 257;
	static final int S_VILE_ATK4     = 258;
	static final int S_VILE_ATK5     = 259;
	static final int S_VILE_ATK6     = 260;
	static final int S_VILE_ATK7     = 261;
	static final int S_VILE_ATK8     = 262;
	static final int S_VILE_ATK9     = 263;
	static final int S_VILE_ATK10    = 264;
	static final int S_VILE_ATK11    = 265;
	static final int S_VILE_HEAL1    = 266;
	static final int S_VILE_HEAL2    = 267;
	static final int S_VILE_HEAL3    = 268;
	static final int S_VILE_PAIN     = 269;
	static final int S_VILE_PAIN2    = 270;
	static final int S_VILE_DIE1     = 271;
	static final int S_VILE_DIE2     = 272;
	static final int S_VILE_DIE3     = 273;
	static final int S_VILE_DIE4     = 274;
	static final int S_VILE_DIE5     = 275;
	static final int S_VILE_DIE6     = 276;
	static final int S_VILE_DIE7     = 277;
	static final int S_VILE_DIE8     = 278;
	static final int S_VILE_DIE9     = 279;
	static final int S_VILE_DIE10    = 280;
	static final int S_FIRE1         = 281;
	static final int S_FIRE2         = 282;
	static final int S_FIRE3         = 283;
	static final int S_FIRE4         = 284;
	static final int S_FIRE5         = 285;
	static final int S_FIRE6         = 286;
	static final int S_FIRE7         = 287;
	static final int S_FIRE8         = 288;
	static final int S_FIRE9         = 289;
	static final int S_FIRE10        = 290;
	static final int S_FIRE11        = 291;
	static final int S_FIRE12        = 292;
	static final int S_FIRE13        = 293;
	static final int S_FIRE14        = 294;
	static final int S_FIRE15        = 295;
	static final int S_FIRE16        = 296;
	static final int S_FIRE17        = 297;
	static final int S_FIRE18        = 298;
	static final int S_FIRE19        = 299;
	static final int S_FIRE20        = 300;
	static final int S_FIRE21        = 301;
	static final int S_FIRE22        = 302;
	static final int S_FIRE23        = 303;
	static final int S_FIRE24        = 304;
	static final int S_FIRE25        = 305;
	static final int S_FIRE26        = 306;
	static final int S_FIRE27        = 307;
	static final int S_FIRE28        = 308;
	static final int S_FIRE29        = 309;
	static final int S_FIRE30        = 310;
	static final int S_SMOKE1        = 311;
	static final int S_SMOKE2        = 312;
	static final int S_SMOKE3        = 313;
	static final int S_SMOKE4        = 314;
	static final int S_SMOKE5        = 315;
	static final int S_TRACER        = 316;
	static final int S_TRACER2       = 317;
	static final int S_TRACEEXP1     = 318;
	static final int S_TRACEEXP2     = 319;
	static final int S_TRACEEXP3     = 320;
	static final int S_SKEL_STND     = 321;
	static final int S_SKEL_STND2    = 322;
	static final int S_SKEL_RUN1     = 323;
	static final int S_SKEL_RUN2     = 324;
	static final int S_SKEL_RUN3     = 325;
	static final int S_SKEL_RUN4     = 326;
	static final int S_SKEL_RUN5     = 327;
	static final int S_SKEL_RUN6     = 328;
	static final int S_SKEL_RUN7     = 329;
	static final int S_SKEL_RUN8     = 330;
	static final int S_SKEL_RUN9     = 331;
	static final int S_SKEL_RUN10    = 332;
	static final int S_SKEL_RUN11    = 333;
	static final int S_SKEL_RUN12    = 334;
	static final int S_SKEL_FIST1    = 335;
	static final int S_SKEL_FIST2    = 336;
	static final int S_SKEL_FIST3    = 337;
	static final int S_SKEL_FIST4    = 338;
	static final int S_SKEL_MISS1    = 339;
	static final int S_SKEL_MISS2    = 340;
	static final int S_SKEL_MISS3    = 341;
	static final int S_SKEL_MISS4    = 342;
	static final int S_SKEL_PAIN     = 343;
	static final int S_SKEL_PAIN2    = 344;
	static final int S_SKEL_DIE1     = 345;
	static final int S_SKEL_DIE2     = 346;
	static final int S_SKEL_DIE3     = 347;
	static final int S_SKEL_DIE4     = 348;
	static final int S_SKEL_DIE5     = 349;
	static final int S_SKEL_DIE6     = 350;
	static final int S_SKEL_RAISE1   = 351;
	static final int S_SKEL_RAISE2   = 352;
	static final int S_SKEL_RAISE3   = 353;
	static final int S_SKEL_RAISE4   = 354;
	static final int S_SKEL_RAISE5   = 355;
	static final int S_SKEL_RAISE6   = 356;
	static final int S_FATSHOT1      = 357;
	static final int S_FATSHOT2      = 358;
	static final int S_FATSHOTX1     = 359;
	static final int S_FATSHOTX2     = 360;
	static final int S_FATSHOTX3     = 361;
	static final int S_FATT_STND     = 362;
	static final int S_FATT_STND2    = 363;
	static final int S_FATT_RUN1     = 364;
	static final int S_FATT_RUN2     = 365;
	static final int S_FATT_RUN3     = 366;
	static final int S_FATT_RUN4     = 367;
	static final int S_FATT_RUN5     = 368;
	static final int S_FATT_RUN6     = 369;
	static final int S_FATT_RUN7     = 370;
	static final int S_FATT_RUN8     = 371;
	static final int S_FATT_RUN9     = 372;
	static final int S_FATT_RUN10    = 373;
	static final int S_FATT_RUN11    = 374;
	static final int S_FATT_RUN12    = 375;
	static final int S_FATT_ATK1     = 376;
	static final int S_FATT_ATK2     = 377;
	static final int S_FATT_ATK3     = 378;
	static final int S_FATT_ATK4     = 379;
	static final int S_FATT_ATK5     = 380;
	static final int S_FATT_ATK6     = 381;
	static final int S_FATT_ATK7     = 382;
	static final int S_FATT_ATK8     = 383;
	static final int S_FATT_ATK9     = 384;
	static final int S_FATT_ATK10    = 385;
	static final int S_FATT_PAIN     = 386;
	static final int S_FATT_PAIN2    = 387;
	static final int S_FATT_DIE1     = 388;
	static final int S_FATT_DIE2     = 389;
	static final int S_FATT_DIE3     = 390;
	static final int S_FATT_DIE4     = 391;
	static final int S_FATT_DIE5     = 392;
	static final int S_FATT_DIE6     = 393;
	static final int S_FATT_DIE7     = 394;
	static final int S_FATT_DIE8     = 395;
	static final int S_FATT_DIE9     = 396;
	static final int S_FATT_DIE10    = 397;
	static final int S_FATT_RAISE1   = 398;
	static final int S_FATT_RAISE2   = 399;
	static final int S_FATT_RAISE3   = 400;
	static final int S_FATT_RAISE4   = 401;
	static final int S_FATT_RAISE5   = 402;
	static final int S_FATT_RAISE6   = 403;
	static final int S_FATT_RAISE7   = 404;
	static final int S_FATT_RAISE8   = 405;
	static final int S_CPOS_STND     = 406;
	static final int S_CPOS_STND2    = 407;
	static final int S_CPOS_RUN1     = 408;
	static final int S_CPOS_RUN2     = 409;
	static final int S_CPOS_RUN3     = 410;
	static final int S_CPOS_RUN4     = 411;
	static final int S_CPOS_RUN5     = 412;
	static final int S_CPOS_RUN6     = 413;
	static final int S_CPOS_RUN7     = 414;
	static final int S_CPOS_RUN8     = 415;
	static final int S_CPOS_ATK1     = 416;
	static final int S_CPOS_ATK2     = 417;
	static final int S_CPOS_ATK3     = 418;
	static final int S_CPOS_ATK4     = 419;
	static final int S_CPOS_PAIN     = 420;
	static final int S_CPOS_PAIN2    = 421;
	static final int S_CPOS_DIE1     = 422;
	static final int S_CPOS_DIE2     = 423;
	static final int S_CPOS_DIE3     = 424;
	static final int S_CPOS_DIE4     = 425;
	static final int S_CPOS_DIE5     = 426;
	static final int S_CPOS_DIE6     = 427;
	static final int S_CPOS_DIE7     = 428;
	static final int S_CPOS_XDIE1    = 429;
	static final int S_CPOS_XDIE2    = 430;
	static final int S_CPOS_XDIE3    = 431;
	static final int S_CPOS_XDIE4    = 432;
	static final int S_CPOS_XDIE5    = 433;
	static final int S_CPOS_XDIE6    = 434;
	static final int S_CPOS_RAISE1   = 435;
	static final int S_CPOS_RAISE2   = 436;
	static final int S_CPOS_RAISE3   = 437;
	static final int S_CPOS_RAISE4   = 438;
	static final int S_CPOS_RAISE5   = 439;
	static final int S_CPOS_RAISE6   = 440;
	static final int S_CPOS_RAISE7   = 441;
	static final int S_TROO_STND     = 442;
	static final int S_TROO_STND2    = 443;
	static final int S_TROO_RUN1     = 444;
	static final int S_TROO_RUN2     = 445;
	static final int S_TROO_RUN3     = 446;
	static final int S_TROO_RUN4     = 447;
	static final int S_TROO_RUN5     = 448;
	static final int S_TROO_RUN6     = 449;
	static final int S_TROO_RUN7     = 450;
	static final int S_TROO_RUN8     = 451;
	static final int S_TROO_ATK1     = 452;
	static final int S_TROO_ATK2     = 453;
	static final int S_TROO_ATK3     = 454;
	static final int S_TROO_PAIN     = 455;
	static final int S_TROO_PAIN2    = 456;
	static final int S_TROO_DIE1     = 457;
	static final int S_TROO_DIE2     = 458;
	static final int S_TROO_DIE3     = 459;
	static final int S_TROO_DIE4     = 460;
	static final int S_TROO_DIE5     = 461;
	static final int S_TROO_XDIE1    = 462;
	static final int S_TROO_XDIE2    = 463;
	static final int S_TROO_XDIE3    = 464;
	static final int S_TROO_XDIE4    = 465;
	static final int S_TROO_XDIE5    = 466;
	static final int S_TROO_XDIE6    = 467;
	static final int S_TROO_XDIE7    = 468;
	static final int S_TROO_XDIE8    = 469;
	static final int S_TROO_RAISE1   = 470;
	static final int S_TROO_RAISE2   = 471;
	static final int S_TROO_RAISE3   = 472;
	static final int S_TROO_RAISE4   = 473;
	static final int S_TROO_RAISE5   = 474;
	static final int S_SARG_STND     = 475;
	static final int S_SARG_STND2    = 476;
	static final int S_SARG_RUN1     = 477;
	static final int S_SARG_RUN2     = 478;
	static final int S_SARG_RUN3     = 479;
	static final int S_SARG_RUN4     = 480;
	static final int S_SARG_RUN5     = 481;
	static final int S_SARG_RUN6     = 482;
	static final int S_SARG_RUN7     = 483;
	static final int S_SARG_RUN8     = 484;
	static final int S_SARG_ATK1     = 485;
	static final int S_SARG_ATK2     = 486;
	static final int S_SARG_ATK3     = 487;
	static final int S_SARG_PAIN     = 488;
	static final int S_SARG_PAIN2    = 489;
	static final int S_SARG_DIE1     = 490;
	static final int S_SARG_DIE2     = 491;
	static final int S_SARG_DIE3     = 492;
	static final int S_SARG_DIE4     = 493;
	static final int S_SARG_DIE5     = 494;
	static final int S_SARG_DIE6     = 495;
	static final int S_SARG_RAISE1   = 496;
	static final int S_SARG_RAISE2   = 497;
	static final int S_SARG_RAISE3   = 498;
	static final int S_SARG_RAISE4   = 499;
	static final int S_SARG_RAISE5   = 500;
	static final int S_SARG_RAISE6   = 501;
	static final int S_HEAD_STND     = 502;
	static final int S_HEAD_RUN1     = 503;
	static final int S_HEAD_ATK1     = 504;
	static final int S_HEAD_ATK2     = 505;
	static final int S_HEAD_ATK3     = 506;
	static final int S_HEAD_PAIN     = 507;
	static final int S_HEAD_PAIN2    = 508;
	static final int S_HEAD_PAIN3    = 509;
	static final int S_HEAD_DIE1     = 510;
	static final int S_HEAD_DIE2     = 511;
	static final int S_HEAD_DIE3     = 512;
	static final int S_HEAD_DIE4     = 513;
	static final int S_HEAD_DIE5     = 514;
	static final int S_HEAD_DIE6     = 515;
	static final int S_HEAD_RAISE1   = 516;
	static final int S_HEAD_RAISE2   = 517;
	static final int S_HEAD_RAISE3   = 518;
	static final int S_HEAD_RAISE4   = 519;
	static final int S_HEAD_RAISE5   = 520;
	static final int S_HEAD_RAISE6   = 521;
	static final int S_BRBALL1       = 522;
	static final int S_BRBALL2       = 523;
	static final int S_BRBALLX1      = 524;
	static final int S_BRBALLX2      = 525;
	static final int S_BRBALLX3      = 526;
	static final int S_BOSS_STND     = 527;
	static final int S_BOSS_STND2    = 528;
	static final int S_BOSS_RUN1     = 529;
	static final int S_BOSS_RUN2     = 530;
	static final int S_BOSS_RUN3     = 531;
	static final int S_BOSS_RUN4     = 532;
	static final int S_BOSS_RUN5     = 533;
	static final int S_BOSS_RUN6     = 534;
	static final int S_BOSS_RUN7     = 535;
	static final int S_BOSS_RUN8     = 536;
	static final int S_BOSS_ATK1     = 537;
	static final int S_BOSS_ATK2     = 538;
	static final int S_BOSS_ATK3     = 539;
	static final int S_BOSS_PAIN     = 540;
	static final int S_BOSS_PAIN2    = 541;
	static final int S_BOSS_DIE1     = 542;
	static final int S_BOSS_DIE2     = 543;
	static final int S_BOSS_DIE3     = 544;
	static final int S_BOSS_DIE4     = 545;
	static final int S_BOSS_DIE5     = 546;
	static final int S_BOSS_DIE6     = 547;
	static final int S_BOSS_DIE7     = 548;
	static final int S_BOSS_RAISE1   = 549;
	static final int S_BOSS_RAISE2   = 550;
	static final int S_BOSS_RAISE3   = 551;
	static final int S_BOSS_RAISE4   = 552;
	static final int S_BOSS_RAISE5   = 553;
	static final int S_BOSS_RAISE6   = 554;
	static final int S_BOSS_RAISE7   = 555;
	static final int S_BOS2_STND     = 556;
	static final int S_BOS2_STND2    = 557;
	static final int S_BOS2_RUN1     = 558;
	static final int S_BOS2_RUN2     = 559;
	static final int S_BOS2_RUN3     = 560;
	static final int S_BOS2_RUN4     = 561;
	static final int S_BOS2_RUN5     = 562;
	static final int S_BOS2_RUN6     = 563;
	static final int S_BOS2_RUN7     = 564;
	static final int S_BOS2_RUN8     = 565;
	static final int S_BOS2_ATK1     = 566;
	static final int S_BOS2_ATK2     = 567;
	static final int S_BOS2_ATK3     = 568;
	static final int S_BOS2_PAIN     = 569;
	static final int S_BOS2_PAIN2    = 570;
	static final int S_BOS2_DIE1     = 571;
	static final int S_BOS2_DIE2     = 572;
	static final int S_BOS2_DIE3     = 573;
	static final int S_BOS2_DIE4     = 574;
	static final int S_BOS2_DIE5     = 575;
	static final int S_BOS2_DIE6     = 576;
	static final int S_BOS2_DIE7     = 577;
	static final int S_BOS2_RAISE1   = 578;
	static final int S_BOS2_RAISE2   = 579;
	static final int S_BOS2_RAISE3   = 580;
	static final int S_BOS2_RAISE4   = 581;
	static final int S_BOS2_RAISE5   = 582;
	static final int S_BOS2_RAISE6   = 583;
	static final int S_BOS2_RAISE7   = 584;
	static final int S_SKULL_STND    = 585;
	static final int S_SKULL_STND2   = 586;
	static final int S_SKULL_RUN1    = 587;
	static final int S_SKULL_RUN2    = 588;
	static final int S_SKULL_ATK1    = 589;
	static final int S_SKULL_ATK2    = 590;
	static final int S_SKULL_ATK3    = 591;
	static final int S_SKULL_ATK4    = 592;
	static final int S_SKULL_PAIN    = 593;
	static final int S_SKULL_PAIN2   = 594;
	static final int S_SKULL_DIE1    = 595;
	static final int S_SKULL_DIE2    = 596;
	static final int S_SKULL_DIE3    = 597;
	static final int S_SKULL_DIE4    = 598;
	static final int S_SKULL_DIE5    = 599;
	static final int S_SKULL_DIE6    = 600;
	static final int S_SPID_STND     = 601;
	static final int S_SPID_STND2    = 602;
	static final int S_SPID_RUN1     = 603;
	static final int S_SPID_RUN2     = 604;
	static final int S_SPID_RUN3     = 605;
	static final int S_SPID_RUN4     = 606;
	static final int S_SPID_RUN5     = 607;
	static final int S_SPID_RUN6     = 608;
	static final int S_SPID_RUN7     = 609;
	static final int S_SPID_RUN8     = 610;
	static final int S_SPID_RUN9     = 611;
	static final int S_SPID_RUN10    = 612;
	static final int S_SPID_RUN11    = 613;
	static final int S_SPID_RUN12    = 614;
	static final int S_SPID_ATK1     = 615;
	static final int S_SPID_ATK2     = 616;
	static final int S_SPID_ATK3     = 617;
	static final int S_SPID_ATK4     = 618;
	static final int S_SPID_PAIN     = 619;
	static final int S_SPID_PAIN2    = 620;
	static final int S_SPID_DIE1     = 621;
	static final int S_SPID_DIE2     = 622;
	static final int S_SPID_DIE3     = 623;
	static final int S_SPID_DIE4     = 624;
	static final int S_SPID_DIE5     = 625;
	static final int S_SPID_DIE6     = 626;
	static final int S_SPID_DIE7     = 627;
	static final int S_SPID_DIE8     = 628;
	static final int S_SPID_DIE9     = 629;
	static final int S_SPID_DIE10    = 630;
	static final int S_SPID_DIE11    = 631;
	static final int S_BSPI_STND     = 632;
	static final int S_BSPI_STND2    = 633;
	static final int S_BSPI_SIGHT    = 634;
	static final int S_BSPI_RUN1     = 635;
	static final int S_BSPI_RUN2     = 636;
	static final int S_BSPI_RUN3     = 637;
	static final int S_BSPI_RUN4     = 638;
	static final int S_BSPI_RUN5     = 639;
	static final int S_BSPI_RUN6     = 640;
	static final int S_BSPI_RUN7     = 641;
	static final int S_BSPI_RUN8     = 642;
	static final int S_BSPI_RUN9     = 643;
	static final int S_BSPI_RUN10    = 644;
	static final int S_BSPI_RUN11    = 645;
	static final int S_BSPI_RUN12    = 646;
	static final int S_BSPI_ATK1     = 647;
	static final int S_BSPI_ATK2     = 648;
	static final int S_BSPI_ATK3     = 649;
	static final int S_BSPI_ATK4     = 650;
	static final int S_BSPI_PAIN     = 651;
	static final int S_BSPI_PAIN2    = 652;
	static final int S_BSPI_DIE1     = 653;
	static final int S_BSPI_DIE2     = 654;
	static final int S_BSPI_DIE3     = 655;
	static final int S_BSPI_DIE4     = 656;
	static final int S_BSPI_DIE5     = 657;
	static final int S_BSPI_DIE6     = 658;
	static final int S_BSPI_DIE7     = 659;
	static final int S_BSPI_RAISE1   = 660;
	static final int S_BSPI_RAISE2   = 661;
	static final int S_BSPI_RAISE3   = 662;
	static final int S_BSPI_RAISE4   = 663;
	static final int S_BSPI_RAISE5   = 664;
	static final int S_BSPI_RAISE6   = 665;
	static final int S_BSPI_RAISE7   = 666;
	static final int S_ARACH_PLAZ    = 667;
	static final int S_ARACH_PLAZ2   = 668;
	static final int S_ARACH_PLEX    = 669;
	static final int S_ARACH_PLEX2   = 670;
	static final int S_ARACH_PLEX3   = 671;
	static final int S_ARACH_PLEX4   = 672;
	static final int S_ARACH_PLEX5   = 673;
	static final int S_CYBER_STND    = 674;
	static final int S_CYBER_STND2   = 675;
	static final int S_CYBER_RUN1    = 676;
	static final int S_CYBER_RUN2    = 677;
	static final int S_CYBER_RUN3    = 678;
	static final int S_CYBER_RUN4    = 679;
	static final int S_CYBER_RUN5    = 680;
	static final int S_CYBER_RUN6    = 681;
	static final int S_CYBER_RUN7    = 682;
	static final int S_CYBER_RUN8    = 683;
	static final int S_CYBER_ATK1    = 684;
	static final int S_CYBER_ATK2    = 685;
	static final int S_CYBER_ATK3    = 686;
	static final int S_CYBER_ATK4    = 687;
	static final int S_CYBER_ATK5    = 688;
	static final int S_CYBER_ATK6    = 689;
	static final int S_CYBER_PAIN    = 690;
	static final int S_CYBER_DIE1    = 691;
	static final int S_CYBER_DIE2    = 692;
	static final int S_CYBER_DIE3    = 693;
	static final int S_CYBER_DIE4    = 694;
	static final int S_CYBER_DIE5    = 695;
	static final int S_CYBER_DIE6    = 696;
	static final int S_CYBER_DIE7    = 697;
	static final int S_CYBER_DIE8    = 698;
	static final int S_CYBER_DIE9    = 699;
	static final int S_CYBER_DIE10   = 700;
	static final int S_PAIN_STND     = 701;
	static final int S_PAIN_RUN1     = 702;
	static final int S_PAIN_RUN2     = 703;
	static final int S_PAIN_RUN3     = 704;
	static final int S_PAIN_RUN4     = 705;
	static final int S_PAIN_RUN5     = 706;
	static final int S_PAIN_RUN6     = 707;
	static final int S_PAIN_ATK1     = 708;
	static final int S_PAIN_ATK2     = 709;
	static final int S_PAIN_ATK3     = 710;
	static final int S_PAIN_ATK4     = 711;
	static final int S_PAIN_PAIN     = 712;
	static final int S_PAIN_PAIN2    = 713;
	static final int S_PAIN_DIE1     = 714;
	static final int S_PAIN_DIE2     = 715;
	static final int S_PAIN_DIE3     = 716;
	static final int S_PAIN_DIE4     = 717;
	static final int S_PAIN_DIE5     = 718;
	static final int S_PAIN_DIE6     = 719;
	static final int S_PAIN_RAISE1   = 720;
	static final int S_PAIN_RAISE2   = 721;
	static final int S_PAIN_RAISE3   = 722;
	static final int S_PAIN_RAISE4   = 723;
	static final int S_PAIN_RAISE5   = 724;
	static final int S_PAIN_RAISE6   = 725;
	static final int S_SSWV_STND     = 726;
	static final int S_SSWV_STND2    = 727;
	static final int S_SSWV_RUN1     = 728;
	static final int S_SSWV_RUN2     = 729;
	static final int S_SSWV_RUN3     = 730;
	static final int S_SSWV_RUN4     = 731;
	static final int S_SSWV_RUN5     = 732;
	static final int S_SSWV_RUN6     = 733;
	static final int S_SSWV_RUN7     = 734;
	static final int S_SSWV_RUN8     = 735;
	static final int S_SSWV_ATK1     = 736;
	static final int S_SSWV_ATK2     = 737;
	static final int S_SSWV_ATK3     = 738;
	static final int S_SSWV_ATK4     = 739;
	static final int S_SSWV_ATK5     = 740;
	static final int S_SSWV_ATK6     = 741;
	static final int S_SSWV_PAIN     = 742;
	static final int S_SSWV_PAIN2    = 743;
	static final int S_SSWV_DIE1     = 744;
	static final int S_SSWV_DIE2     = 745;
	static final int S_SSWV_DIE3     = 746;
	static final int S_SSWV_DIE4     = 747;
	static final int S_SSWV_DIE5     = 748;
	static final int S_SSWV_XDIE1    = 749;
	static final int S_SSWV_XDIE2    = 750;
	static final int S_SSWV_XDIE3    = 751;
	static final int S_SSWV_XDIE4    = 752;
	static final int S_SSWV_XDIE5    = 753;
	static final int S_SSWV_XDIE6    = 754;
	static final int S_SSWV_XDIE7    = 755;
	static final int S_SSWV_XDIE8    = 756;
	static final int S_SSWV_XDIE9    = 757;
	static final int S_SSWV_RAISE1   = 758;
	static final int S_SSWV_RAISE2   = 759;
	static final int S_SSWV_RAISE3   = 760;
	static final int S_SSWV_RAISE4   = 761;
	static final int S_SSWV_RAISE5   = 762;
	static final int S_KEENSTND      = 763;
	static final int S_COMMKEEN      = 764;
	static final int S_COMMKEEN2     = 765;
	static final int S_COMMKEEN3     = 766;
	static final int S_COMMKEEN4     = 767;
	static final int S_COMMKEEN5     = 768;
	static final int S_COMMKEEN6     = 769;
	static final int S_COMMKEEN7     = 770;
	static final int S_COMMKEEN8     = 771;
	static final int S_COMMKEEN9     = 772;
	static final int S_COMMKEEN10    = 773;
	static final int S_COMMKEEN11    = 774;
	static final int S_COMMKEEN12    = 775;
	static final int S_KEENPAIN      = 776;
	static final int S_KEENPAIN2     = 777;
	static final int S_BRAIN         = 778;
	static final int S_BRAIN_PAIN    = 779;
	static final int S_BRAIN_DIE1    = 780;
	static final int S_BRAIN_DIE2    = 781;
	static final int S_BRAIN_DIE3    = 782;
	static final int S_BRAIN_DIE4    = 783;
	static final int S_BRAINEYE      = 784;
	static final int S_BRAINEYESEE   = 785;
	static final int S_BRAINEYE1     = 786;
	static final int S_SPAWN1        = 787;
	static final int S_SPAWN2        = 788;
	static final int S_SPAWN3        = 789;
	static final int S_SPAWN4        = 790;
	static final int S_SPAWNFIRE1    = 791;
	static final int S_SPAWNFIRE2    = 792;
	static final int S_SPAWNFIRE3    = 793;
	static final int S_SPAWNFIRE4    = 794;
	static final int S_SPAWNFIRE5    = 795;
	static final int S_SPAWNFIRE6    = 796;
	static final int S_SPAWNFIRE7    = 797;
	static final int S_SPAWNFIRE8    = 798;
	static final int S_BRAINEXPLODE1 = 799;
	static final int S_BRAINEXPLODE2 = 800;
	static final int S_BRAINEXPLODE3 = 801;
	static final int S_ARM1          = 802;
	static final int S_ARM1A         = 803;
	static final int S_ARM2          = 804;
	static final int S_ARM2A         = 805;
	static final int S_BAR1          = 806;
	static final int S_BAR2          = 807;
	static final int S_BEXP          = 808;
	static final int S_BEXP2         = 809;
	static final int S_BEXP3         = 810;
	static final int S_BEXP4         = 811;
	static final int S_BEXP5         = 812;
	static final int S_BBAR1         = 813;
	static final int S_BBAR2         = 814;
	static final int S_BBAR3         = 815;
	static final int S_BON1          = 816;
	static final int S_BON1A         = 817;
	static final int S_BON1B         = 818;
	static final int S_BON1C         = 819;
	static final int S_BON1D         = 820;
	static final int S_BON1E         = 821;
	static final int S_BON2          = 822;
	static final int S_BON2A         = 823;
	static final int S_BON2B         = 824;
	static final int S_BON2C         = 825;
	static final int S_BON2D         = 826;
	static final int S_BON2E         = 827;
	static final int S_BKEY          = 828;
	static final int S_BKEY2         = 829;
	static final int S_RKEY          = 830;
	static final int S_RKEY2         = 831;
	static final int S_YKEY          = 832;
	static final int S_YKEY2         = 833;
	static final int S_BSKULL        = 834;
	static final int S_BSKULL2       = 835;
	static final int S_RSKULL        = 836;
	static final int S_RSKULL2       = 837;
	static final int S_YSKULL        = 838;
	static final int S_YSKULL2       = 839;
	static final int S_STIM          = 840;
	static final int S_MEDI          = 841;
	static final int S_SOUL          = 842;
	static final int S_SOUL2         = 843;
	static final int S_SOUL3         = 844;
	static final int S_SOUL4         = 845;
	static final int S_SOUL5         = 846;
	static final int S_SOUL6         = 847;
	static final int S_PINV          = 848;
	static final int S_PINV2         = 849;
	static final int S_PINV3         = 850;
	static final int S_PINV4         = 851;
	static final int S_PSTR          = 852;
	static final int S_PINS          = 853;
	static final int S_PINS2         = 854;
	static final int S_PINS3         = 855;
	static final int S_PINS4         = 856;
	static final int S_MEGA          = 857;
	static final int S_MEGA2         = 858;
	static final int S_MEGA3         = 859;
	static final int S_MEGA4         = 860;
	static final int S_SUIT          = 861;
	static final int S_PMAP          = 862;
	static final int S_PMAP2         = 863;
	static final int S_PMAP3         = 864;
	static final int S_PMAP4         = 865;
	static final int S_PMAP5         = 866;
	static final int S_PMAP6         = 867;
	static final int S_PVIS          = 868;
	static final int S_PVIS2         = 869;
	static final int S_CLIP          = 870;
	static final int S_AMMO          = 871;
	static final int S_ROCK          = 872;
	static final int S_BROK          = 873;
	static final int S_CELL          = 874;
	static final int S_CELP          = 875;
	static final int S_SHEL          = 876;
	static final int S_SBOX          = 877;
	static final int S_BPAK          = 878;
	static final int S_BFUG          = 879;
	static final int S_MGUN          = 880;
	static final int S_CSAW          = 881;
	static final int S_LAUN          = 882;
	static final int S_PLAS          = 883;
	static final int S_SHOT          = 884;
	static final int S_SHOT2         = 885;
	static final int S_COLU          = 886;
	static final int S_STALAG        = 887;
	static final int S_BLOODYTWITCH  = 888;
	static final int S_BLOODYTWITCH2 = 889;
	static final int S_BLOODYTWITCH3 = 890;
	static final int S_BLOODYTWITCH4 = 891;
	static final int S_DEADTORSO     = 892;
	static final int S_DEADBOTTOM    = 893;
	static final int S_HEADSONSTICK  = 894;
	static final int S_GIBS          = 895;
	static final int S_HEADONASTICK  = 896;
	static final int S_HEADCANDLES   = 897;
	static final int S_HEADCANDLES2  = 898;
	static final int S_DEADSTICK     = 899;
	static final int S_LIVESTICK     = 900;
	static final int S_LIVESTICK2    = 901;
	static final int S_MEAT2         = 902;
	static final int S_MEAT3         = 903;
	static final int S_MEAT4         = 904;
	static final int S_MEAT5         = 905;
	static final int S_STALAGTITE    = 906;
	static final int S_TALLGRNCOL    = 907;
	static final int S_SHRTGRNCOL    = 908;
	static final int S_TALLREDCOL    = 909;
	static final int S_SHRTREDCOL    = 910;
	static final int S_CANDLESTIK    = 911;
	static final int S_CANDELABRA    = 912;
	static final int S_SKULLCOL      = 913;
	static final int S_TORCHTREE     = 914;
	static final int S_BIGTREE       = 915;
	static final int S_TECHPILLAR    = 916;
	static final int S_EVILEYE       = 917;
	static final int S_EVILEYE2      = 918;
	static final int S_EVILEYE3      = 919;
	static final int S_EVILEYE4      = 920;
	static final int S_FLOATSKULL    = 921;
	static final int S_FLOATSKULL2   = 922;
	static final int S_FLOATSKULL3   = 923;
	static final int S_HEARTCOL      = 924;
	static final int S_HEARTCOL2     = 925;
	static final int S_BLUETORCH     = 926;
	static final int S_BLUETORCH2    = 927;
	static final int S_BLUETORCH3    = 928;
	static final int S_BLUETORCH4    = 929;
	static final int S_GREENTORCH    = 930;
	static final int S_GREENTORCH2   = 931;
	static final int S_GREENTORCH3   = 932;
	static final int S_GREENTORCH4   = 933;
	static final int S_REDTORCH      = 934;
	static final int S_REDTORCH2     = 935;
	static final int S_REDTORCH3     = 936;
	static final int S_REDTORCH4     = 937;
	static final int S_BTORCHSHRT    = 938;
	static final int S_BTORCHSHRT2   = 939;
	static final int S_BTORCHSHRT3   = 940;
	static final int S_BTORCHSHRT4   = 941;
	static final int S_GTORCHSHRT    = 942;
	static final int S_GTORCHSHRT2   = 943;
	static final int S_GTORCHSHRT3   = 944;
	static final int S_GTORCHSHRT4   = 945;
	static final int S_RTORCHSHRT    = 946;
	static final int S_RTORCHSHRT2   = 947;
	static final int S_RTORCHSHRT3   = 948;
	static final int S_RTORCHSHRT4   = 949;
	static final int S_HANGNOGUTS    = 950;
	static final int S_HANGBNOBRAIN  = 951;
	static final int S_HANGTLOOKDN   = 952;
	static final int S_HANGTSKULL    = 953;
	static final int S_HANGTLOOKUP   = 954;
	static final int S_HANGTNOBRAIN  = 955;
	static final int S_COLONGIBS     = 956;
	static final int S_SMALLPOOL     = 957;
	static final int S_BRAINSTEM     = 958;
	static final int S_TECHLAMP      = 959;
	static final int S_TECHLAMP2     = 960;
	static final int S_TECHLAMP3     = 961;
	static final int S_TECHLAMP4     = 962;
	static final int S_TECH2LAMP     = 963;
	static final int S_TECH2LAMP2    = 964;
	static final int S_TECH2LAMP3    = 965;
	static final int S_TECH2LAMP4    = 966;
	
	static final DEHMiscellany DEHMISC = 
		new DEHMiscellany();
	
	static final DEHAmmo[] DEHAMMO = 
	{
		(new DEHAmmo()).setName("Bullets")
			.setMax(200).setPickup(10),
		(new DEHAmmo()).setName("Shells")
			.setMax(50).setPickup(4),
		(new DEHAmmo()).setName("Cells")
			.setMax(300).setPickup(20),
		(new DEHAmmo()).setName("Rockets")
			.setMax(50).setPickup(1),
	};

	static final DEHWeapon[] DEHWEAPON = 
	{
		DEHWeapon.create("Fist",            Ammo.INFINITE, S_PUNCHUP, S_PUNCHDOWN, S_PUNCH, S_PUNCH1, S_NULL),
		DEHWeapon.create("Pistol",          Ammo.BULLETS,  S_PISTOLUP, S_PISTOLDOWN, S_PISTOL, S_PISTOL1, S_PISTOLFLASH),
		DEHWeapon.create("Shotgun",         Ammo.SHELLS,   S_SGUNUP, S_SGUNDOWN, S_SGUN, S_SGUN1, S_SGUNFLASH1),
		DEHWeapon.create("Chaingun",        Ammo.BULLETS,  S_CHAINUP, S_CHAINDOWN, S_CHAIN, S_CHAIN1, S_CHAINFLASH1),
		DEHWeapon.create("Rocket launcher", Ammo.ROCKETS,  S_MISSILEUP, S_MISSILEDOWN, S_MISSILE, S_MISSILE1, S_MISSILEFLASH1),
		DEHWeapon.create("Plasma rifle",    Ammo.CELLS,    S_PLASMAUP, S_PLASMADOWN, S_PLASMA, S_PLASMA1, S_PLASMAFLASH1),
		DEHWeapon.create("BFG9000",         Ammo.CELLS,    S_BFGUP, S_BFGDOWN, S_BFG, S_BFG1, S_BFGFLASH1),
		DEHWeapon.create("Chainsaw",        Ammo.INFINITE, S_SAWUP, S_SAWDOWN, S_SAW, S_SAW1, S_NULL),
		DEHWeapon.create("Super-shotgun",   Ammo.SHELLS,   S_DSGUNUP, S_DSGUNDOWN, S_DSGUN, S_DSGUN1, S_DSGUNFLASH1)
	};

	static final DEHSound[] DEHSOUND = 
	{
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(118, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(64, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(100, false),
		DEHSound.create(100, false),
		DEHSound.create(100, false),
		DEHSound.create(100, false),
		DEHSound.create(119, false),
		DEHSound.create(78, false),
		DEHSound.create(78, false),
		DEHSound.create(96, false),
		DEHSound.create(96, false),
		DEHSound.create(96, false),
		DEHSound.create(96, false),
		DEHSound.create(96, false),
		DEHSound.create(96, false),
		DEHSound.create(78, false),
		DEHSound.create(78, true),
		DEHSound.create(78, true),
		DEHSound.create(96, false),
		DEHSound.create(32, false),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(98, true),
		DEHSound.create(94, true),
		DEHSound.create(92, true),
		DEHSound.create(90, true),
		DEHSound.create(90, true),
		DEHSound.create(90, true),
		DEHSound.create(90, true),
		DEHSound.create(90, true),
		DEHSound.create(90, true),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(120, true),
		DEHSound.create(120, true),
		DEHSound.create(120, true),
		DEHSound.create(100, true),
		DEHSound.create(100, true),
		DEHSound.create(100, true),
		DEHSound.create(78, false),
		DEHSound.create(60, false),
		DEHSound.create(64, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(64, false),
		DEHSound.create(60, false),
		DEHSound.create(100, false),
		DEHSound.create(100, false),
		DEHSound.create(100, false),
		DEHSound.create(32, false),
		DEHSound.create(32, false),
		DEHSound.create(60, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(70, false),
		DEHSound.create(60, false)
	};

	static final DEHThing[] DEHTHING = 
	{
		(new DEHThing()).setName("Player")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(100)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(0)
			.setPainChance(255)
			.setFlags(33557510)
			.setMass(100)
			.setSpawnFrameIndex(149)
			.setWalkFrameIndex(150)
			.setPainFrameIndex(156)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(154)
			.setDeathFrameIndex(158)
			.setExtremeDeathFrameIndex(165)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(25)
			.setDeathSoundPosition(57)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Trooper")
			.setEditorNumber(3004)
			.setHealth(20)
			.setSpeed(8)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(200)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(174)
			.setWalkFrameIndex(176)
			.setPainFrameIndex(187)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(184)
			.setDeathFrameIndex(189)
			.setExtremeDeathFrameIndex(194)
			.setRaiseFrameIndex(203)
			.setSeeSoundPosition(36)
			.setAttackSoundPosition(1)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(59)
			.setActiveSoundPosition(75),
		(new DEHThing()).setName("Sargeant")
			.setEditorNumber(9)
			.setHealth(30)
			.setSpeed(8)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(170)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(207)
			.setWalkFrameIndex(209)
			.setPainFrameIndex(220)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(217)
			.setDeathFrameIndex(222)
			.setExtremeDeathFrameIndex(227)
			.setRaiseFrameIndex(236)
			.setSeeSoundPosition(37)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(60)
			.setActiveSoundPosition(75),
		(new DEHThing()).setName("Archvile")
			.setEditorNumber(64)
			.setHealth(700)
			.setSpeed(15)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(10)
			.setFlags(4194310)
			.setMass(500)
			.setSpawnFrameIndex(241)
			.setWalkFrameIndex(243)
			.setPainFrameIndex(269)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(255)
			.setDeathFrameIndex(271)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(48)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(28)
			.setDeathSoundPosition(71)
			.setActiveSoundPosition(80),
		(new DEHThing()).setName("Archvile attack")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(281)
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
		(new DEHThing()).setName("Revenant")
			.setEditorNumber(66)
			.setHealth(300)
			.setSpeed(10)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(100)
			.setFlags(4194310)
			.setMass(500)
			.setSpawnFrameIndex(321)
			.setWalkFrameIndex(323)
			.setPainFrameIndex(343)
			.setMeleeFrameIndex(335)
			.setMissileFrameIndex(339)
			.setDeathFrameIndex(345)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(351)
			.setSeeSoundPosition(106)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(74)
			.setActiveSoundPosition(105),
		(new DEHThing()).setName("Revenant fireball")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(655360)
			.setRadius(11)
			.setHeight(8)
			.setDamage(10)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(316)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(318)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(107)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(82)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Fireball trail")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(311)
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
		(new DEHThing()).setName("Mancubus")
			.setEditorNumber(67)
			.setHealth(600)
			.setSpeed(8)
			.setRadius(48)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(80)
			.setFlags(4194310)
			.setMass(1000)
			.setSpawnFrameIndex(362)
			.setWalkFrameIndex(364)
			.setPainFrameIndex(386)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(376)
			.setDeathFrameIndex(388)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(398)
			.setSeeSoundPosition(49)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(29)
			.setDeathSoundPosition(100)
			.setActiveSoundPosition(75),
		(new DEHThing()).setName("Mancubus fireball")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(1310720)
			.setRadius(6)
			.setHeight(8)
			.setDamage(8)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(357)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(359)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(16)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Chaingun Sargeant")
			.setEditorNumber(65)
			.setHealth(70)
			.setSpeed(8)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(170)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(406)
			.setWalkFrameIndex(408)
			.setPainFrameIndex(420)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(416)
			.setDeathFrameIndex(422)
			.setExtremeDeathFrameIndex(429)
			.setRaiseFrameIndex(435)
			.setSeeSoundPosition(37)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(60)
			.setActiveSoundPosition(75),
		(new DEHThing()).setName("Imp")
			.setEditorNumber(3001)
			.setHealth(60)
			.setSpeed(8)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(200)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(442)
			.setWalkFrameIndex(444)
			.setPainFrameIndex(455)
			.setMeleeFrameIndex(452)
			.setMissileFrameIndex(452)
			.setDeathFrameIndex(457)
			.setExtremeDeathFrameIndex(462)
			.setRaiseFrameIndex(470)
			.setSeeSoundPosition(39)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(62)
			.setActiveSoundPosition(76),
		(new DEHThing()).setName("Demon")
			.setEditorNumber(3002)
			.setHealth(150)
			.setSpeed(10)
			.setRadius(30)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(180)
			.setFlags(4194310)
			.setMass(400)
			.setSpawnFrameIndex(475)
			.setWalkFrameIndex(477)
			.setPainFrameIndex(488)
			.setMeleeFrameIndex(485)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(490)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(496)
			.setSeeSoundPosition(41)
			.setAttackSoundPosition(52)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(64)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Spectre")
			.setEditorNumber(58)
			.setHealth(150)
			.setSpeed(10)
			.setRadius(30)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(180)
			.setFlags(4456454)
			.setMass(400)
			.setSpawnFrameIndex(475)
			.setWalkFrameIndex(477)
			.setPainFrameIndex(488)
			.setMeleeFrameIndex(485)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(490)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(496)
			.setSeeSoundPosition(41)
			.setAttackSoundPosition(52)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(64)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Cacodemon")
			.setEditorNumber(3005)
			.setHealth(400)
			.setSpeed(8)
			.setRadius(31)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(128)
			.setFlags(4211206)
			.setMass(400)
			.setSpawnFrameIndex(502)
			.setWalkFrameIndex(503)
			.setPainFrameIndex(507)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(504)
			.setDeathFrameIndex(510)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(516)
			.setSeeSoundPosition(42)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(65)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Baron of Hell")
			.setEditorNumber(3003)
			.setHealth(1000)
			.setSpeed(8)
			.setRadius(24)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(50)
			.setFlags(4194310)
			.setMass(1000)
			.setSpawnFrameIndex(527)
			.setWalkFrameIndex(529)
			.setPainFrameIndex(540)
			.setMeleeFrameIndex(537)
			.setMissileFrameIndex(537)
			.setDeathFrameIndex(542)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(549)
			.setSeeSoundPosition(43)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(67)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Baron fireball")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(983040)
			.setRadius(6)
			.setHeight(8)
			.setDamage(8)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(522)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(524)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(16)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Hell Knight")
			.setEditorNumber(69)
			.setHealth(500)
			.setSpeed(8)
			.setRadius(24)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(50)
			.setFlags(4194310)
			.setMass(1000)
			.setSpawnFrameIndex(556)
			.setWalkFrameIndex(558)
			.setPainFrameIndex(569)
			.setMeleeFrameIndex(566)
			.setMissileFrameIndex(566)
			.setDeathFrameIndex(571)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(578)
			.setSeeSoundPosition(47)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(72)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Lost Soul")
			.setEditorNumber(3006)
			.setHealth(100)
			.setSpeed(8)
			.setRadius(16)
			.setHeight(56)
			.setDamage(3)
			.setReactionTime(8)
			.setPainChance(256)
			.setFlags(16902)
			.setMass(50)
			.setSpawnFrameIndex(585)
			.setWalkFrameIndex(587)
			.setPainFrameIndex(593)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(589)
			.setDeathFrameIndex(595)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(51)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Spiderdemon")
			.setEditorNumber(7)
			.setHealth(3000)
			.setSpeed(12)
			.setRadius(128)
			.setHeight(100)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(40)
			.setFlags(4194310)
			.setMass(1000)
			.setSpawnFrameIndex(601)
			.setWalkFrameIndex(603)
			.setPainFrameIndex(619)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(615)
			.setDeathFrameIndex(621)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(45)
			.setAttackSoundPosition(2)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(69)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Arachnotron")
			.setEditorNumber(68)
			.setHealth(500)
			.setSpeed(12)
			.setRadius(64)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(128)
			.setFlags(4194310)
			.setMass(600)
			.setSpawnFrameIndex(632)
			.setWalkFrameIndex(634)
			.setPainFrameIndex(651)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(647)
			.setDeathFrameIndex(653)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(660)
			.setSeeSoundPosition(46)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(70)
			.setActiveSoundPosition(78),
		(new DEHThing()).setName("Cyberdemon")
			.setEditorNumber(16)
			.setHealth(4000)
			.setSpeed(16)
			.setRadius(40)
			.setHeight(110)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(20)
			.setFlags(4194310)
			.setMass(1000)
			.setSpawnFrameIndex(674)
			.setWalkFrameIndex(676)
			.setPainFrameIndex(690)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(684)
			.setDeathFrameIndex(691)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(44)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(26)
			.setDeathSoundPosition(68)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("Pain Elemental")
			.setEditorNumber(71)
			.setHealth(400)
			.setSpeed(8)
			.setRadius(31)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(128)
			.setFlags(4211206)
			.setMass(400)
			.setSpawnFrameIndex(701)
			.setWalkFrameIndex(702)
			.setPainFrameIndex(712)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(708)
			.setDeathFrameIndex(714)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(720)
			.setSeeSoundPosition(50)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(30)
			.setDeathSoundPosition(73)
			.setActiveSoundPosition(77),
		(new DEHThing()).setName("SS Nazi")
			.setEditorNumber(84)
			.setHealth(50)
			.setSpeed(8)
			.setRadius(20)
			.setHeight(56)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(170)
			.setFlags(4194310)
			.setMass(100)
			.setSpawnFrameIndex(726)
			.setWalkFrameIndex(728)
			.setPainFrameIndex(742)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(736)
			.setDeathFrameIndex(744)
			.setExtremeDeathFrameIndex(749)
			.setRaiseFrameIndex(758)
			.setSeeSoundPosition(101)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(27)
			.setDeathSoundPosition(102)
			.setActiveSoundPosition(75),
		(new DEHThing()).setName("Commander Keen")
			.setEditorNumber(72)
			.setHealth(100)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(72)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(256)
			.setFlags(4195078)
			.setMass(10000000)
			.setSpawnFrameIndex(763)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(776)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(764)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(103)
			.setDeathSoundPosition(104)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Big Brain")
			.setEditorNumber(88)
			.setHealth(250)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(255)
			.setFlags(6)
			.setMass(10000000)
			.setSpawnFrameIndex(778)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(779)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(780)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(97)
			.setDeathSoundPosition(98)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Demon spawner")
			.setEditorNumber(89)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(32)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(24)
			.setMass(100)
			.setSpawnFrameIndex(784)
			.setWalkFrameIndex(785)
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
		(new DEHThing()).setName("Demon spawn spot")
			.setEditorNumber(87)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(32)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(24)
			.setMass(100)
			.setSpawnFrameIndex(DEHThing.FRAME_NULL)
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
		(new DEHThing()).setName("Demon spawn cube")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(655360)
			.setRadius(6)
			.setHeight(32)
			.setDamage(3)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(71184)
			.setMass(100)
			.setSpawnFrameIndex(787)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(DEHThing.FRAME_NULL)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(94)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Demon spawn fire")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(791)
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
		(new DEHThing()).setName("Barrel")
			.setEditorNumber(2035)
			.setHealth(20)
			.setSpeed(0)
			.setRadius(10)
			.setHeight(42)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(524294)
			.setMass(100)
			.setSpawnFrameIndex(806)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(808)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(82)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Imp fireball")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(655360)
			.setRadius(6)
			.setHeight(8)
			.setDamage(3)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(97)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(99)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(16)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Caco fireball")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(655360)
			.setRadius(6)
			.setHeight(8)
			.setDamage(5)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(102)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(104)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(16)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Rocket in flight")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(1310720)
			.setRadius(11)
			.setHeight(8)
			.setDamage(20)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(114)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(127)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(14)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(82)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Plasma projectile")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(1638400)
			.setRadius(13)
			.setHeight(8)
			.setDamage(5)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(107)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(109)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(8)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("BFG projectile")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(1638400)
			.setRadius(13)
			.setHeight(8)
			.setDamage(100)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(115)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(117)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(DEHThing.SOUND_NONE)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(15)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Arachnotron projectile")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(1638400)
			.setRadius(13)
			.setHeight(8)
			.setDamage(5)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(67088)
			.setMass(100)
			.setSpawnFrameIndex(667)
			.setWalkFrameIndex(DEHThing.FRAME_NULL)
			.setPainFrameIndex(DEHThing.FRAME_NULL)
			.setMeleeFrameIndex(DEHThing.FRAME_NULL)
			.setMissileFrameIndex(DEHThing.FRAME_NULL)
			.setDeathFrameIndex(669)
			.setExtremeDeathFrameIndex(DEHThing.FRAME_NULL)
			.setRaiseFrameIndex(DEHThing.FRAME_NULL)
			.setSeeSoundPosition(8)
			.setAttackSoundPosition(DEHThing.SOUND_NONE)
			.setPainSoundPosition(DEHThing.SOUND_NONE)
			.setDeathSoundPosition(17)
			.setActiveSoundPosition(DEHThing.SOUND_NONE),
		(new DEHThing()).setName("Bullet puff")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(93)
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
		(new DEHThing()).setName("Blood splat")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(16)
			.setMass(100)
			.setSpawnFrameIndex(90)
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
		(new DEHThing()).setName("Teleport fog")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(130)
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
		(new DEHThing()).setName("Item respawn fog")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(142)
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
		(new DEHThing()).setName("Teleport exit")
			.setEditorNumber(14)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(24)
			.setMass(100)
			.setSpawnFrameIndex(DEHThing.FRAME_NULL)
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
		(new DEHThing()).setName("BFG impact")
			.setEditorNumber(DEHThing.EDITORNUMBER_NONE)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(528)
			.setMass(100)
			.setSpawnFrameIndex(123)
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
		(new DEHThing()).setName("Green armor")
			.setEditorNumber(2018)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(802)
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
		(new DEHThing()).setName("Blue armor")
			.setEditorNumber(2019)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(804)
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
		(new DEHThing()).setName("Health potion")
			.setEditorNumber(2014)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(816)
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
		(new DEHThing()).setName("Armor helmet")
			.setEditorNumber(2015)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(822)
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
		(new DEHThing()).setName("Blue keycard")
			.setEditorNumber(5)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(828)
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
		(new DEHThing()).setName("Red keycard")
			.setEditorNumber(13)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(830)
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
		(new DEHThing()).setName("Yellow keycard")
			.setEditorNumber(6)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(832)
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
		(new DEHThing()).setName("Yellow skull key")
			.setEditorNumber(39)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(838)
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
		(new DEHThing()).setName("Red skull key")
			.setEditorNumber(38)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(836)
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
		(new DEHThing()).setName("Blue skull key")
			.setEditorNumber(40)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(33554433)
			.setMass(100)
			.setSpawnFrameIndex(834)
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
		(new DEHThing()).setName("Stimpack")
			.setEditorNumber(2011)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(840)
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
		(new DEHThing()).setName("Medical kit")
			.setEditorNumber(2012)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(841)
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
		(new DEHThing()).setName("Soul sphere")
			.setEditorNumber(2013)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(842)
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
		(new DEHThing()).setName("Invulnerability")
			.setEditorNumber(2022)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(848)
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
		(new DEHThing()).setName("Berserk sphere")
			.setEditorNumber(2023)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(852)
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
		(new DEHThing()).setName("Blur sphere")
			.setEditorNumber(2024)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(853)
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
		(new DEHThing()).setName("Radiation suit")
			.setEditorNumber(2025)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(861)
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
		(new DEHThing()).setName("Computer map")
			.setEditorNumber(2026)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(862)
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
		(new DEHThing()).setName("Lite amplification visor")
			.setEditorNumber(2045)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(868)
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
		(new DEHThing()).setName("Mega sphere")
			.setEditorNumber(83)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(8388609)
			.setMass(100)
			.setSpawnFrameIndex(857)
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
		(new DEHThing()).setName("Ammo clip")
			.setEditorNumber(2007)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(870)
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
		(new DEHThing()).setName("Box of ammo")
			.setEditorNumber(2048)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(871)
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
		(new DEHThing()).setName("Rocket")
			.setEditorNumber(2010)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(872)
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
		(new DEHThing()).setName("Box of rockets")
			.setEditorNumber(2046)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(873)
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
		(new DEHThing()).setName("Energy cell")
			.setEditorNumber(2047)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(874)
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
		(new DEHThing()).setName("Energy cell pack")
			.setEditorNumber(17)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(875)
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
		(new DEHThing()).setName("Shells")
			.setEditorNumber(2008)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(876)
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
		(new DEHThing()).setName("Box of shells")
			.setEditorNumber(2049)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(877)
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
		(new DEHThing()).setName("Backpack")
			.setEditorNumber(8)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(878)
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
		(new DEHThing()).setName("BFG 9000")
			.setEditorNumber(2006)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(879)
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
		(new DEHThing()).setName("Chaingun")
			.setEditorNumber(2002)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(880)
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
		(new DEHThing()).setName("Chainsaw")
			.setEditorNumber(2005)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(881)
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
		(new DEHThing()).setName("Rocket launcher")
			.setEditorNumber(2003)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(882)
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
		(new DEHThing()).setName("Plasma rifle")
			.setEditorNumber(2004)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(883)
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
		(new DEHThing()).setName("Shotgun")
			.setEditorNumber(2001)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(884)
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
		(new DEHThing()).setName("Super shotgun")
			.setEditorNumber(82)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(1)
			.setMass(100)
			.setSpawnFrameIndex(885)
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
		(new DEHThing()).setName("Tall lamp")
			.setEditorNumber(85)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(959)
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
		(new DEHThing()).setName("Tall lamp 2")
			.setEditorNumber(86)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(963)
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
		(new DEHThing()).setName("Short lamp")
			.setEditorNumber(2028)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(886)
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
		(new DEHThing()).setName("Tall green pillar")
			.setEditorNumber(30)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(907)
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
		(new DEHThing()).setName("Short green pillar")
			.setEditorNumber(31)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(908)
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
		(new DEHThing()).setName("Tall red pillar")
			.setEditorNumber(32)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(909)
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
		(new DEHThing()).setName("Short red pillar")
			.setEditorNumber(33)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(910)
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
		(new DEHThing()).setName("Pillar with skull")
			.setEditorNumber(37)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(913)
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
		(new DEHThing()).setName("Pillar with heart")
			.setEditorNumber(36)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(924)
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
		(new DEHThing()).setName("Eye in symbol")
			.setEditorNumber(41)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(917)
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
		(new DEHThing()).setName("Flaming skulls")
			.setEditorNumber(42)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(921)
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
		(new DEHThing()).setName("Grey tree")
			.setEditorNumber(43)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(914)
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
		(new DEHThing()).setName("Tall blue torch")
			.setEditorNumber(44)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(926)
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
		(new DEHThing()).setName("Tall green torch")
			.setEditorNumber(45)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(930)
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
		(new DEHThing()).setName("Tall red torch")
			.setEditorNumber(46)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(934)
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
		(new DEHThing()).setName("Small blue torch")
			.setEditorNumber(55)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(938)
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
		(new DEHThing()).setName("Small green torch")
			.setEditorNumber(56)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(942)
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
		(new DEHThing()).setName("Small red torch")
			.setEditorNumber(57)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(946)
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
		(new DEHThing()).setName("Brown stub")
			.setEditorNumber(47)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(906)
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
		(new DEHThing()).setName("Technical column")
			.setEditorNumber(48)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(916)
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
		(new DEHThing()).setName("Candle")
			.setEditorNumber(34)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(911)
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
		(new DEHThing()).setName("Candelabra")
			.setEditorNumber(35)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(912)
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
		(new DEHThing()).setName("Swaying body")
			.setEditorNumber(49)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(68)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(888)
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
		(new DEHThing()).setName("Hanging arms out")
			.setEditorNumber(50)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(84)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(902)
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
		(new DEHThing()).setName("One-legged body")
			.setEditorNumber(51)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(84)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(903)
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
		(new DEHThing()).setName("Hanging torso")
			.setEditorNumber(52)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(68)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(904)
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
		(new DEHThing()).setName("Hanging leg")
			.setEditorNumber(53)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(52)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(905)
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
		(new DEHThing()).setName("Hanging arms out 2")
			.setEditorNumber(59)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(84)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(768)
			.setMass(100)
			.setSpawnFrameIndex(902)
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
		(new DEHThing()).setName("Hanging torso 2")
			.setEditorNumber(60)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(68)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(768)
			.setMass(100)
			.setSpawnFrameIndex(904)
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
		(new DEHThing()).setName("One-legged body 2")
			.setEditorNumber(61)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(52)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(768)
			.setMass(100)
			.setSpawnFrameIndex(903)
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
		(new DEHThing()).setName("Hanging leg 2")
			.setEditorNumber(62)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(52)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(768)
			.setMass(100)
			.setSpawnFrameIndex(905)
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
		(new DEHThing()).setName("Swaying body 2")
			.setEditorNumber(63)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(68)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(768)
			.setMass(100)
			.setSpawnFrameIndex(888)
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
		(new DEHThing()).setName("Dead Cacodemon")
			.setEditorNumber(22)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(515)
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
		(new DEHThing()).setName("Dead Marine")
			.setEditorNumber(15)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(164)
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
		(new DEHThing()).setName("Dead Trooper")
			.setEditorNumber(18)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(193)
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
		(new DEHThing()).setName("Dead Demon")
			.setEditorNumber(21)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(495)
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
		(new DEHThing()).setName("Dead Lost Soul")
			.setEditorNumber(23)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(600)
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
		(new DEHThing()).setName("Dead Imp")
			.setEditorNumber(20)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(461)
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
		(new DEHThing()).setName("Dead Sargeant")
			.setEditorNumber(19)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(226)
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
		(new DEHThing()).setName("Guts and bones")
			.setEditorNumber(10)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(173)
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
		(new DEHThing()).setName("Guts and bones 2")
			.setEditorNumber(12)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(173)
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
		(new DEHThing()).setName("Skewered heads")
			.setEditorNumber(28)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(894)
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
		(new DEHThing()).setName("Pool of blood")
			.setEditorNumber(24)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(0)
			.setMass(100)
			.setSpawnFrameIndex(895)
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
		(new DEHThing()).setName("Pole with skull")
			.setEditorNumber(27)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(896)
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
		(new DEHThing()).setName("Pile of skulls")
			.setEditorNumber(29)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(897)
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
		(new DEHThing()).setName("Impaled body")
			.setEditorNumber(25)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(899)
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
		(new DEHThing()).setName("Twitching body")
			.setEditorNumber(26)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(900)
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
		(new DEHThing()).setName("Large tree")
			.setEditorNumber(54)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(32)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(915)
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
		(new DEHThing()).setName("Flaming barrel")
			.setEditorNumber(70)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(2)
			.setMass(100)
			.setSpawnFrameIndex(813)
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
		(new DEHThing()).setName("Hanging body 1")
			.setEditorNumber(73)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(88)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(950)
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
		(new DEHThing()).setName("Hanging body 2")
			.setEditorNumber(74)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(88)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(951)
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
		(new DEHThing()).setName("Hanging body 3")
			.setEditorNumber(75)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(952)
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
		(new DEHThing()).setName("Hanging body 4")
			.setEditorNumber(76)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(953)
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
		(new DEHThing()).setName("Hanging body 5")
			.setEditorNumber(77)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(954)
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
		(new DEHThing()).setName("Hanging body 6")
			.setEditorNumber(78)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(16)
			.setHeight(64)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(770)
			.setMass(100)
			.setSpawnFrameIndex(955)
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
		(new DEHThing()).setName("Pool of blood 1")
			.setEditorNumber(79)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(16)
			.setMass(100)
			.setSpawnFrameIndex(956)
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
		(new DEHThing()).setName("Pool of blood 2")
			.setEditorNumber(80)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(16)
			.setMass(100)
			.setSpawnFrameIndex(957)
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
		(new DEHThing()).setName("Brain")
			.setEditorNumber(81)
			.setHealth(1000)
			.setSpeed(0)
			.setRadius(20)
			.setHeight(16)
			.setDamage(0)
			.setReactionTime(8)
			.setPainChance(0)
			.setFlags(16)
			.setMass(100)
			.setSpawnFrameIndex(958)
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
