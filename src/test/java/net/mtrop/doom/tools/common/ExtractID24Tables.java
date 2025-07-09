package net.mtrop.doom.tools.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.mtrop.doom.tools.decohack.data.enums.DEHThingDoom19Flag;
import net.mtrop.doom.tools.struct.Lexer;
import net.mtrop.doom.tools.struct.util.IOUtils;

public final class ExtractID24Tables 
{
	public static void main(String[] args) 
	{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(IOUtils.openResource("decohack/id24junk.txt"))))
		{
			System.out.println((new Parser(br)).go());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final class Parser extends Lexer.Parser
	{
		public Parser(Reader reader)
		{
			super(new Lexer(new Kernel(), reader));
		}
		
		public boolean go()
		{
			nextToken(); // prime lexer
			
			// SPRITES
			
			if (!matchIdents("static", "const", "spriteinfo_t", "id24sprites"))
				return false;
			
			if (!matchTypeSequence(Kernel.TYPE_LBRACK, Kernel.TYPE_RBRACK, Kernel.TYPE_EQUAL))
				return false;
			
			if (matchType(Kernel.TYPE_LBRACE))
			{
				if (!matchSpriteList())
					return false;
			}

			if (!matchType(Kernel.TYPE_SEMI))
				return false;

			if (!matchIdents("static", "const", "state_t", "id24states"))
				return false;
			
			if (!matchTypeSequence(Kernel.TYPE_LBRACK, Kernel.TYPE_RBRACK, Kernel.TYPE_EQUAL))
				return false;
			
			if (matchType(Kernel.TYPE_LBRACE))
			{
				if (!matchStateList())
					return false;
			}

			if (!matchType(Kernel.TYPE_SEMI))
				return false;

			if (!matchIdents("static", "const", "mobjinfo_t", "id24mobjs"))
				return false;
			
			if (!matchTypeSequence(Kernel.TYPE_LBRACK, Kernel.TYPE_RBRACK, Kernel.TYPE_EQUAL))
				return false;

			if (matchType(Kernel.TYPE_LBRACE))
			{
				if (!matchThingList())
					return false;
			}

			return true;
		}

		private boolean matchThingList()
		{
			System.out.println("// [THINGS]");
			
			do {
				if (currentType(Kernel.TYPE_RBRACE))
					break;
				
				if (!matchType(Kernel.TYPE_LBRACE))
					return false;
				
				Integer thingnum = matchNumber(true);
				matchString(true); // features_id24
				
				Integer doomednum = matchNumber(true);
				Integer spawnstate = matchNumber(true);
				Integer spawnhealth = matchNumber(true);
				Integer seestate = matchNumber(true);
				Integer seesound = matchNumber(true);
				Integer reactiontime = matchNumber(true);
				Integer attacksound = matchNumber(true);
				Integer painstate = matchNumber(true);
				Integer painchance = matchNumber(true);
				Integer painsound = matchNumber(true);
				Integer meleestate = matchNumber(true);
				Integer missilestate = matchNumber(true);
				Integer deathstate = matchNumber(true);
				Integer xdeathstate = matchNumber(true);
				Integer deathsound = matchNumber(true);

				Integer speed = matchNumber(true);
				Integer radius = matchFixedToNumber(true);
				Integer height = matchFixedToNumber(true);
				Integer mass = matchNumber(true);
				Integer damage = matchNumber(true);
				Integer activesound = matchNumber(true);
				Integer flags = matchNumber(true);
				Integer raisestate = matchNumber(true);
				
				if ((flags & DEHThingDoom19Flag.MISSILE.getValue()) != 0)
					speed = speed >> 16;
				
				Integer fastspeed = matchNumber(true);
				Integer meleerange = matchFixedToNumber(true);
				Integer infightinggroup = matchNumber(true);
				Integer projectilegroup = matchNumber(true);
				Integer splashgroup = matchNumber(true);
				Integer mbf21flags = matchNumber(true);
				Integer ripsound = matchNumber(true);
				
				Integer id24flags = matchNumber(true);
				Integer minrespawntics = matchNumber(true);
				Integer respawndice = matchNumber(true);
				Integer dropthing = matchNumber(true);
				Integer pickupammotype = matchNumber(true);
				Integer pickupammocategory = matchNumber(true);
				Integer pickupweapontype = matchNumber(true);
				Integer pickupitemtype = matchNumber(true);
				Integer pickupbonuscount = matchNumber(true);
				Integer pickupsound = matchNumber(true);
				String pickupstringmnemonic = matchString(true);
				String translationlump = matchString(true);

				if (!matchType(Kernel.TYPE_RBRACE))
					return false;

				StringBuilder sb = new StringBuilder();
				sb.append("put(").append(thingnum).append(", (new DEHThing()).name(\"UNNAMED\")\n");
				sb.append("\t.setEditorNumber(").append(doomednum).append(")\n");
				sb.append("\t.setHealth(").append(spawnhealth).append(")\n");
				sb.append("\t.setSpeed(").append(speed).append(")\n");
				sb.append("\t.setRadius(").append(radius).append(")\n");
				sb.append("\t.setHeight(").append(height).append(")\n");
				sb.append("\t.setDamage(").append(damage).append(")\n");
				sb.append("\t.setReactionTime(").append(reactiontime).append(")\n");
				sb.append("\t.setPainChance(").append(painchance).append(")\n");
				sb.append("\t.setFlags(").append(flags).append(")\n");
				sb.append("\t.setMass(").append(mass).append(")\n");
				sb.append("\t.setSpawnFrameIndex(").append(spawnstate).append(")\n");
				sb.append("\t.setWalkFrameIndex(").append(seestate).append(")\n");
				sb.append("\t.setPainFrameIndex(").append(painstate).append(")\n");
				sb.append("\t.setMeleeFrameIndex(").append(meleestate).append(")\n");
				sb.append("\t.setMissileFrameIndex(").append(missilestate).append(")\n");
				sb.append("\t.setDeathFrameIndex(").append(deathstate).append(")\n");
				sb.append("\t.setExtremeDeathFrameIndex(").append(xdeathstate).append(")\n");
				sb.append("\t.setRaiseFrameIndex(").append(raisestate).append(")\n");
				sb.append("\t.setSeeSoundPosition(").append(seesound).append(")\n");
				sb.append("\t.setAttackSoundPosition(").append(attacksound).append(")\n");
				sb.append("\t.setPainSoundPosition(").append(painsound).append(")\n");
				sb.append("\t.setDeathSoundPosition(").append(deathsound).append(")\n");
				sb.append("\t.setActiveSoundPosition(").append(activesound).append(")\n");

				sb.append("\t.setDropThing(").append(dropthing).append(")\n");
				
				sb.append("\t.setMBF21Flags(").append(mbf21flags).append(")\n");
				sb.append("\t.setFastSpeed(").append(fastspeed).append(")\n");
				sb.append("\t.setMeleeRange(").append(meleerange).append(")\n");
				sb.append("\t.setInfightingGroup(").append(infightinggroup).append(")\n");
				sb.append("\t.setProjectileGroup(").append(projectilegroup).append(")\n");
				sb.append("\t.setSplashGroup(").append(splashgroup).append(")\n");
				sb.append("\t.setRipSoundPosition(").append(ripsound).append(")\n");

				sb.append("\t.setID24Flags(").append(id24flags).append(")\n");
				sb.append("\t.setMinRespawnTics(").append(minrespawntics).append(")\n");
				sb.append("\t.setRespawnDice(").append(respawndice).append(")\n");
				sb.append("\t.setPickupAmmoType(").append(pickupammotype).append(")\n");
				sb.append("\t.setPickupAmmoCategory(").append(pickupammocategory).append(")\n");
				sb.append("\t.setPickupWeaponType(").append(pickupweapontype).append(")\n");
				sb.append("\t.setPickupItemType(").append(pickupitemtype).append(")\n");
				sb.append("\t.setPickupBonusCount(").append(pickupbonuscount).append(")\n");
				sb.append("\t.setPickupSoundPosition(").append(pickupsound).append(")\n");

				sb.append("\t.setPickupStringMnemonic(").append(pickupstringmnemonic != null ? '"' + pickupstringmnemonic + '"' : null).append(")\n");
				sb.append("\t.setTranslation(").append(translationlump != null ? '"' + translationlump + '"' : null).append(")\n");

				sb.append(");\n");
				System.out.println(sb.toString());

			} while (matchType(Kernel.TYPE_COMMA));
			
			return matchType(Kernel.TYPE_RBRACE);
		}
		
		private boolean matchStateList()
		{
			System.out.println("// [STATES]");
			
			do {
				if (currentType(Kernel.TYPE_RBRACE))
					break;
				
				if (!matchType(Kernel.TYPE_LBRACE))
					return false;

				Integer statenum = matchNumber(true);
				matchString(true); // features_id24				
				Integer sprite = matchNumber(true);

				Integer frame = matchNumber(true);
				boolean bright = (frame & (0x08000)) != 0;
				frame = frame & ~0x08000;
				
				Integer tics = matchNumber(true);
				String actionMnemonic = matchBracedString(true); 
				actionMnemonic = actionMnemonic != null ? actionMnemonic.substring(2).toUpperCase() : null;
				Integer nextstate = matchNumber(true);
				Integer misc1 = matchBracedNumber(true); 
				Integer misc2 = matchBracedNumber(true); 
				Integer mbf21flags = matchNumber(true); 
				Integer arg1 = matchBracedNumber(true); 
				Integer arg2 = matchBracedNumber(true); 
				Integer arg3 = matchBracedNumber(true); 
				Integer arg4 = matchBracedNumber(true); 
				Integer arg5 = matchBracedNumber(true); 
				Integer arg6 = matchBracedNumber(true); 
				Integer arg7 = matchBracedNumber(true); 
				Integer arg8 = matchBracedNumber(true); 
				String tranmap = matchString(true);
				
				if (!matchType(Kernel.TYPE_RBRACE))
					return false;

				StringBuilder sb = new StringBuilder();
				sb.append("put(").append(statenum).append(", ");
				sb.append("PatchBoom.State.create(");
				sb.append("DEHState.create(");
					sb.append(sprite).append(", ");
					sb.append(frame).append(", ");
					sb.append(bright).append(", ");
					sb.append(nextstate).append(", ");
					sb.append(tics).append(", ");
					sb.append(misc1).append(", ");
					sb.append(misc2).append(", ");
					sb.append("new int[]{")
						.append(arg1).append(", ")
						.append(arg2).append(", ")
						.append(arg3).append(", ")
						.append(arg4).append(", ")
						.append(arg5).append(", ")
						.append(arg6).append(", ")
						.append(arg7).append(", ")
						.append(arg8)
					.append("}, ");
					sb.append(mbf21flags).append(", ");
					sb.append(tranmap != null ? '"' + tranmap + '"' : "null");
				sb.append("), ");
				sb.append(actionMnemonic);
				sb.append(")");
				sb.append(");");
				System.out.println(sb.toString());
				
			} while (matchType(Kernel.TYPE_COMMA));
			
			return matchType(Kernel.TYPE_RBRACE);
		}
		
		private boolean matchSpriteList()
		{
			System.out.println("// [SPRITES]");
			
			do {
				if (currentType(Kernel.TYPE_RBRACE))
					break;
				
				Integer index;
				String spritename;
				
				if (!matchType(Kernel.TYPE_LBRACE))
					return false;

				index = matchNumber(true);
				matchString(true); // features_id24
				spritename = matchString(true);
				
				System.out.printf("put(%d, \"%s\");\n", index, spritename);
				
				if (!matchType(Kernel.TYPE_RBRACE))
					return false;
				
			} while (matchType(Kernel.TYPE_COMMA));
			
			return matchType(Kernel.TYPE_RBRACE);
		}
		
		private Integer matchBracedNumber(boolean comma)
		{
			if (!matchType(Kernel.TYPE_LBRACE))
				return null;
			Integer out = matchNumber(false); 
			if (!matchType(Kernel.TYPE_RBRACE))
				return null;
			
			if (comma)
				if (!matchType(Kernel.TYPE_COMMA))
					return null;
			
			return out;
		}
		
		private String matchBracedString(boolean comma)
		{
			if (!matchType(Kernel.TYPE_LBRACE))
				return null;
			String out = matchString(false); 
			if (!matchType(Kernel.TYPE_RBRACE))
				return null;

			if (comma)
				if (!matchType(Kernel.TYPE_COMMA))
					return null;
			
			return out;
		}
		
		private String matchString(boolean comma)
		{
			if (currentType(Kernel.TYPE_IDENTIFIER, Kernel.TYPE_STRING))
			{
				String out = currentLexeme();
				nextToken();

				if (comma)
					if (!matchType(Kernel.TYPE_COMMA))
						return null;
				
				return out.equals("nullptr") ? null : out;
			}
			return null;
		}

		private boolean matchIdent(String identLexeme, boolean comma)
		{
			if (currentType(Kernel.TYPE_IDENTIFIER) && currentLexeme().equalsIgnoreCase(identLexeme))
			{
				nextToken();

				if (comma)
					if (!matchType(Kernel.TYPE_COMMA))
						return false;
				
				return true;
			}
			return false;
		}

		private boolean matchTypeSequence(int ... types)
		{
			for (int t : types)
				if (!matchType(t))
					return false;
			return true;
		}

		private boolean matchIdents(String ... identLexemes)
		{
			for (String s : identLexemes)
				if (!matchIdent(s, false))
					return false;
			return true;
		}

		private Integer matchNumber(boolean comma)
		{
			Integer value = null;
			if (matchType(Kernel.TYPE_MINUS))
			{
				if (currentType(Kernel.TYPE_NUMBER))
				{
					value = -Integer.parseInt(currentLexeme());
					nextToken();

					if (comma)
						if (!matchType(Kernel.TYPE_COMMA))
							return null;
				}
			}
			else
			{
				if (currentType(Kernel.TYPE_NUMBER))
				{
					value = Integer.parseInt(currentLexeme());
					nextToken();

					if (comma)
						if (!matchType(Kernel.TYPE_COMMA))
							return null;
				}
			}
			return value;
		}

		private Integer matchFixedToNumber(boolean comma)
		{
			Integer value = null;
			if (matchType(Kernel.TYPE_MINUS))
			{
				if (currentType(Kernel.TYPE_NUMBER))
				{
					value = -Integer.parseInt(currentLexeme());
					nextToken();

					if (comma)
						if (!matchType(Kernel.TYPE_COMMA))
							return null;
				}
			}
			else
			{
				if (currentType(Kernel.TYPE_NUMBER))
				{
					value = Integer.parseInt(currentLexeme());
					nextToken();

					if (comma)
						if (!matchType(Kernel.TYPE_COMMA))
							return null;
				}
			}
			return value >> 16;
		}

	}
	
	private static final class Kernel extends Lexer.Kernel
	{
		public static final int TYPE_LBRACK = 0;
		public static final int TYPE_RBRACK = 1;
		public static final int TYPE_LPAREN = 2;
		public static final int TYPE_RPAREN = 3;
		public static final int TYPE_LBRACE = 4;
		public static final int TYPE_RBRACE = 5;
		public static final int TYPE_EQUAL =  6;
		public static final int TYPE_SEMI =   7;
		public static final int TYPE_COMMA =  8;
		public static final int TYPE_MINUS =  9;
		
		public Kernel()
		{
			setDecimalSeparator('.');
			addStringDelimiter('"', '"');
			addCommentLineDelimiter("//");
			addCommentDelimiter("/*", "*/");
			addDelimiter("[", TYPE_LBRACK);
			addDelimiter("]", TYPE_RBRACK);
			addDelimiter("(", TYPE_LPAREN);
			addDelimiter(")", TYPE_RPAREN);
			addDelimiter("{", TYPE_LBRACE);
			addDelimiter("}", TYPE_RBRACE);
			addDelimiter("=", TYPE_EQUAL);
			addDelimiter(";", TYPE_SEMI);
			addDelimiter(",", TYPE_COMMA);
			addDelimiter("-", TYPE_MINUS);
		}
	}
	
}
