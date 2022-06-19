package net.mtrop.doom.tools.gui.managers.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

import net.mtrop.doom.tools.decohack.DecoHackPatchType;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer.Usage.PointerParameter;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;


/** 
 * DECOHack Completion Provider.
 * @author Matthew Tropiano
 */
public class DecoHackCompletionProvider extends CommonCompletionProvider
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DecoHackCompletionProvider.class); 

    private static final Map<String, IOConsumer<HTMLWriter>> THING_HARDCODE_DOCS = ObjectUtils.apply(new HashMap<>(), (map) -> {
		map.put("1", (html) -> {
			html.tag("div", "This is the player actor. It is created several times per player, especially during deathmatch and co-op.");
		});
		map.put("2", (html) -> {
			html.tag("div", "On death, this spawns MT_CLIP (slot 64) with a DROPPED flag.");
		});
		map.put("3", (html) -> {
			html.tag("div", "On death, this spawns a MT_SHOTGUN (slot 78) with a DROPPED flag.");
		});
		map.put("4", (html) -> {
			html.tag("div", "Has a shortened missile attack range (2048 units). This can be changed in MBF21 (or later) patches via the SHORTMRANGE flag.");
			html.tag("div", "Is ignored by other monsters for infighting (others will not fight it). This too can be changed in MBF21 (or later) patches via the DMGIGNORED flag.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Spawnable by A_SpawnFly (0.78% chance).");
		});
		map.put("5", (html) -> {
			html.tag("div", "Spawned by A_VileTarget.");
		});
		map.put("6", (html) -> {
			html.tag("div", "Has a longer melee range check. This can be changed in MBF21 (or later) patches via the LONGMELEE flag.");
			html.tag("div", "Has an increased chance to do a missile attack at shorter ranges. This too can be changed in MBF21 (or later) patches via the RANGEHALF flag.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Spawnable by A_SpawnFly (3.91% chance).");
		});
		map.put("7", (html) -> {
			html.tag("div", "Spawned by A_SkelMissile.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Can use A_Tracer, since A_SkelMissile sets the tracer reference.");
		});
		map.put("8", (html) -> {
			html.tag("div", "Spawned on a seeking A_Tracer call.");
		});
		map.put("9", (html) -> {
			html.tag("div", "Spawnable by A_SpawnFly (11.72% chance).");
			html.push("div").html("&nbsp").pop();
			html.push("div")
				.text("When this calls A_BossDeath and all MT_FATSOs (slot 9) are dead:")
				.push("ul")
					.tag("li", "On MAP07, Floor_LowerToLowest all sectors tagged 666.")
				.pop()
			.pop();
			html.tag("div", "This can be changed in MBF21 (or later) patches via the MAP07BOSS1 flag.");
		});
		map.put("10", (html) -> {
			html.tag("div", "Spawned by A_FatAttack1, A_FatAttack2, and A_FatAttack3, in different angles.");
		});
		map.put("11", (html) -> {
			html.tag("div", "On death, this spawns MT_CLIP (slot 64) with a DROPPED flag.");
		});
		map.put("12", (html) -> {
			html.tag("div", "Spawnable by A_SpawnFly (19.53% chance).");
		});
		map.put("13", (html) -> {
			html.tag("div", "Spawnable by A_SpawnFly (15.63% chance).");
		});
		map.put("14", (html) -> {
			html.tag("div", "Spawnable by A_SpawnFly (11.72% chance).");
		});
		map.put("15", (html) -> {
			html.tag("div", "Spawnable by A_SpawnFly (11.72% chance).");
		});
		map.put("16", (html) -> {
			html.tag("div", "Shares a \"species\" with MT_KNIGHT (slot 18) to prevent infighting.");
			html.push("div")
				.text("When this calls A_BossDeath and all MT_BRUISERs (slot 16) are dead:")
				.push("ul")
					.tag("li", "On E1M8, Floor_LowerToLowest all sectors tagged 666.")
				.pop()
			.pop();
			html.tag("div", "This can be changed in MBF21 (or later) patches via the E1M8BOSS flag.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Spawnable by A_SpawnFly (3.91% chance).");
		});
		map.put("17", (html) -> {
			html.tag("div", "Spawned by A_BruisAttack.");
		});
		map.put("18", (html) -> {
			html.tag("div", "Shares a \"species\" with MT_BRUISER (slot 16) to prevent infighting.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Spawnable by A_SpawnFly (9.38% chance).");
		});
		map.put("19", (html) -> {
			html.tag("div", "Has an increased chance to do a missile attack at shorter ranges. This can be changed in MBF21 (or later) patches via the RANGEHALF flag.");
			html.push("div").html("&nbsp").pop();
			html.tag("div", "Spawned by A_PainAttack (one, already flying) and A_PainDie (three, also flying).");
		});
		map.put("20", (html) -> {
			html.tag("div", "Sight and Death sounds are played at full volume always. This can be changed in MBF21 (or later) patches via the FULLVOLSOUNDS flag.");
			html.tag("div", "Immune to radius damage. This too can be changed in MBF21 (or later) patches via the NORADIUSDMG flag.");
			html.tag("div", "Has an increased chance to do a missile attack at shorter ranges. This too can be changed in MBF21 (or later) patches via the RANGEHALF flag.");
			html.push("div").html("&nbsp").pop();
			html.push("div")
				.text("When this calls A_BossDeath and all MT_SPIDERs (slot 20) are dead:")
				.push("ul")
					.tag("li", "On E3M8, exit the map.")
					.tag("li", "On E4M8, Floor_LowerToLowest all sectors tagged 666.")
				.pop()
			.pop();
			html.tag("div", "This can be changed in MBF21 (or later) patches via the E3M8BOSS and E4M8BOSS flags, respectively.");
		});
		// TODO: Finish this.
	});

	private static final Map<String, IOConsumer<HTMLWriter>> WEAPON_HARDCODE_DOCS = ObjectUtils.apply(new HashMap<>(), (map) -> {
		
		final IOConsumer<HTMLWriter> NOFIRE_NOTES = (html) -> {
			html.tag("div", "This weapon must require a FIRE button press to start - it will not fire as it is brought up if FIRE is held.");
			html.tag("div", "The no-fire behavior can be overridden in an MBF21 (or higher) patch format.");
		};
		
		final IOConsumer<HTMLWriter> NOAUTOSWITCH_NOTES = (html) -> {
			html.tag("div", "This weapon will not be autoswitched to.");
		};

		final IOConsumer<HTMLWriter> AUTOSWITCHFROM_NOTES = (html) -> {
			html.tag("div", "This weapon will be autoswitched away if the player finds ammo for another weapon.");
		};
		
		map.put("0", (html) -> {
			AUTOSWITCHFROM_NOTES.accept(html);
			NOAUTOSWITCH_NOTES.accept(html);
			html.tag("div", "The autoswitch behavior can be overridden in an MBF21 (or higher) patch format.");
		});

		map.put("1", (html) -> {
			AUTOSWITCHFROM_NOTES.accept(html);
			html.tag("div", "The autoswitch behavior can be overridden in an MBF21 (or higher) patch format.");
		});

		map.put("4", NOFIRE_NOTES);
		map.put("6", NOFIRE_NOTES);
		
		map.put("7", (html) -> {
			NOAUTOSWITCH_NOTES.accept(html);
			html.tag("div", "The autoswitch behavior can be overridden in an MBF21 (or higher) patch format.");
			html.tag("div", "Calling A_WeaponReady while this as the current weapon on state S_SAW (slot 67) will play the sound \"SAWIDL\".");
			html.tag("div", "When this weapon starts to raise, the sound \"SAWUP\" is played from the switching player.");
		});
	});
	
	private static final Map<String, IOConsumer<HTMLWriter>> STATE_HARDCODE_DOCS = ObjectUtils.apply(new HashMap<>(), (map) -> {
		map.put("0", (html) -> {
			html.tag("div", "NULL state. Reserved.");
			html.push("div")
				.text("Any actor or object that jumps to this frame is removed.")
				.text("The \"stop\" keyword in a state definition is a jump to this state.")
			.pop();
		});
		map.put("1", (html) -> {
			html.tag("div", "State that resets the player's \"extra light\" level to 0. Reserved for convenience.");
			html.tag("div", "The \"goto lightdone\" clause in a state definition is a jump to this state.");
		});
		map.put("52", (html) -> {
			html.tag("div", "Chaingun's first firing state.");
			html.tag("div", "This state is used as a reference point in A_FireCGun to determine which state to jump to on each fire.");
			html.tag("div", "See A_FireCGun for details.");
		});
		map.put("67", (html) -> {
			html.tag("div", "If A_WeaponReady is called on this frame while WP_CHAINSAW (slot 7) is the active weapon, the chainsaw sound is played.");
		});
		map.put("91", (html) -> {
			html.tag("div", "The blood map object, MT_BLOOD (slot 39), is immediately set to this state for a bleeding hitscan attack that does between 9 to 12 damage, inclusive.");
			html.tag("div", "Damage strictly greater than 12 goes to MT_BLOOD's SPAWN state.");
		});
		map.put("92", (html) -> {
			html.tag("div", "The blood map object, MT_BLOOD (slot 39), is immediately set to this state for a bleeding hitscan attack that does strictly less than 9 damage.");
		});
		map.put("154", (html) -> {
			html.tag("div", "The player's firing frame.");
		});
		map.put("155", (html) -> {
			html.tag("div", "The player's firing frame (muzzle flash).");
			html.tag("div", "This is jumped to on calls to A_GunFlash or other weapon functions.");
		});
		map.put("266", (html) -> {
			html.tag("div", "An actor that calls A_VileChase and encounters a ressurrectable actor jumps to this state on \"healing\".");
			html.tag("div", "See A_VileChase for more info.");
		});
		
		final IOConsumer<HTMLWriter> FASTMONSTER_NOTES = (html) -> {
			html.tag("div", "If \"Fast Monsters\" is enabled, this state has its duration halved.");
			html.tag("div", "MBF21 (and higher) patches can clear this behavior with the \"NotFast\" state flag for this specific state.");
		};
		
		for (int i = 477; i <= 487; i++)
			map.put(String.valueOf(i), FASTMONSTER_NOTES);
		
		map.put("799", (html) -> {
			html.tag("div", "For A_BrainExplode, an MT_ROCKET (slot 34) projectile is spwaned, but immediately set to this state.");
			html.tag("div", "It would be wise to then call A_BrainExplode at the end of the state animation sequence to make the next explosion occur.");
			html.tag("div", "See A_BrainExplode for more info.");
		});

		map.put("895", (html) -> {
			html.tag("div", "If a crushable actor is \"crushed\", it is set to this state.");
		});
		
		map.put("966", (html) -> {
			html.tag("div", "This is the very final frame in UDOOM19/DOOM19/DOOMUNITY patches.");
			html.tag("div", "If changed, DeHackEd 3.0 (the DOS editor) will overwrite part of the weapon table on EXE patching due to a bug.");
			html.tag("div", "Otherwise, changing this state has NO issues in any other port or editor.");
		});
	});
	
	public DecoHackCompletionProvider()
	{
		super();
		for (DEHActionPointerDoom19 pointer : DEHActionPointerDoom19.values())
		{
			if (pointer == DEHActionPointerDoom19.NULL)
				continue;
			addCompletion(new PointerCompletion(this, pointer));
		}
		
		for (DEHActionPointerMBF pointer : DEHActionPointerMBF.values())
			addCompletion(new PointerCompletion(this, pointer));
		for (DEHActionPointerMBF21 pointer : DEHActionPointerMBF21.values())
			addCompletion(new PointerCompletion(this, pointer));

		for (DecoHackTemplateCompletion completion : DecoHackTemplateCompletion.values())
			addCompletion(completion.createCompletion(this));
		
		addDefineCompletions(DecoHackPatchType.DOOM19,   "Thing Slot", "decohack/constants/doom19/things.dh",   THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.BOOM,     "Thing Slot", "decohack/constants/boom/things.dh",     THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.MBF,      "Thing Slot", "decohack/constants/mbf/things.dh",      THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.EXTENDED, "Thing Slot", "decohack/constants/extended/things.dh", THING_HARDCODE_DOCS);
		
		addDefineCompletions(DecoHackPatchType.DOOM19,   "Thing Slot (Friendly Macro)", "decohack/constants/doom19/friendly_things.dh",   THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.BOOM,     "Thing Slot (Friendly Macro)", "decohack/constants/boom/friendly_things.dh",     THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.MBF,      "Thing Slot (Friendly Macro)", "decohack/constants/mbf/friendly_things.dh",      THING_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.EXTENDED, "Thing Slot (Friendly Macro)", "decohack/constants/extended/friendly_things.dh", THING_HARDCODE_DOCS);

		addDefineCompletions(DecoHackPatchType.DOOM19,   "Weapon Slot", "decohack/constants/doom19/weapons.dh", WEAPON_HARDCODE_DOCS);

		addDefineCompletions(DecoHackPatchType.DOOM19,   "State Slot", "decohack/constants/doom19/states.dh",   STATE_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.BOOM,     "State Slot", "decohack/constants/boom/states.dh",     STATE_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.MBF,      "State Slot", "decohack/constants/mbf/states.dh",      STATE_HARDCODE_DOCS);
		addDefineCompletions(DecoHackPatchType.EXTENDED, "State Slot", "decohack/constants/extended/states.dh", STATE_HARDCODE_DOCS);

		// TODO: Add thing aliases (with associated hardcode info for certain slots).
		// TODO: Add weapon aliases (with associated hardcode info for certain slots).
		
		// TODO: Add thing flags.
		// TODO: Add MBF21 thing flags.
		// TODO: Add MBF21 weapon flags.
		
		// TODO: Add ammo defines.
		// TODO: Add string defines.
	}

	/**
	 * Adds define completions from parsing a resource.
	 * @param type the patch type.
	 * @param category the category.
	 * @param resourcePath the resource path.
	 * @param valueToNotesLookup lookup for summaries for specific defines.
	 */
	private void addDefineCompletions(DecoHackPatchType type, final String category, String resourcePath, Map<String, IOConsumer<HTMLWriter>> valueToNotesLookup)
	{
		final String DEFINE = "#define";
		
		try (BufferedReader reader = IOUtils.openTextStream(IOUtils.openResource(resourcePath), StandardCharsets.UTF_8))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.length() < DEFINE.length())
					continue;
				if (!line.substring(0, DEFINE.length()).equalsIgnoreCase(DEFINE))
					continue;
				StringTokenizer tokenizer = new StringTokenizer(line);
				tokenizer.nextToken();
				
				String token, value, summary;
				
				if (!tokenizer.hasMoreTokens())
					continue;
				
				token = tokenizer.nextToken();

				if (!tokenizer.hasMoreTokens())
					continue;

				value = tokenizer.nextToken();
				
				final IOConsumer<HTMLWriter> addendum = valueToNotesLookup.get(value);
				
				summary = writeHTML((html) -> {
					html.push("div")
						.tag("strong", token)
						.text(" = ")
						.tag("span", value)
					.pop();
					html.push("div")
						.tag("em", category)
						.text(", ")
						.tag("span", type.name())
					.pop();
					
					if (addendum != null)
					{
						html.push("div").html("&nbsp").pop();
						html.push("div").tag("strong", "Hardcode Notes:").pop();
						html.push("div").html("&nbsp").pop();
						html.push("div").html(writeHTML(addendum)).pop();
					}
				});
				
				addCompletion(new DefineCompletion(this, type, token, value, addendum != null, summary));
			}
		} 
		catch (IOException e) 
		{
			LOG.error(e, "An error occurred trying to parse define completions!");
		}
	}
	
	/**
	 * A completion object for defines.
	 */
	protected static class DefineCompletion extends BasicCompletion
	{
		/**
		 * Creates a define completion.
		 * @param parent the completion provider.
		 * @param type the patch type associated with the define.
		 * @param token the define token.
		 * @param value the define value.
		 * @param hardcode has hardcode notes.
		 * @param summary the summary.
		 */
		public DefineCompletion(CompletionProvider parent, DecoHackPatchType type, String token, String value, boolean hardcode, String summary) 
		{
			super(parent, token);
			setShortDescription("(" + type.name() + ") " + value + (hardcode ? " [Hardcode Warning]" : ""));
			setSummary(summary);
		}
		
		@Override
		public String toString() 
		{
			return getReplacementText() + " - " + getShortDescription();
		}

	}
	
	/**
	 * A completion object for editor function.
	 */
	protected static class PointerCompletion extends TemplateCompletion
	{
		/**
		 * Creates a pointer completion.
		 * @param parent the completion provider.
		 * @param pointer the pointer.
		 */
		protected PointerCompletion(CompletionProvider parent, DEHActionPointer pointer) 
		{
			super(parent, 
				getInputText(pointer), 
				getInstructions(pointer.getUsage()), 
				getFullSignatureTemplate(pointer), 
				getTypeText(pointer) + " " + getInstructions(pointer.getUsage()), 
				getFunctionDescriptionHTML(pointer)
			);
		}
		
		@Override
		public String toString() 
		{
			return getInputText() + " - " + getShortDescription();
		}

		private static String getInputText(DEHActionPointer pointer)
		{
			return "A_" + pointer.getMnemonic();
		}

		private static String getTypeText(DEHActionPointer pointer)
		{
			return "(" + pointer.getType().name() + ", " + (pointer.isWeapon() ? "Weapon" : "Thing") + ")";
		}

		private static String getInstructions(DEHActionPointer.Usage usage)
		{
			String instructions = getFullInstructions(usage.getInstructions(), true);
			int endidx = instructions.indexOf('.');
			instructions = endidx >= 0 ? instructions.substring(0, endidx + 1) : instructions;
			return instructions;
		}

		private static String getSummaryInstructions(DEHActionPointer.Usage usage)
		{
			String instructions = getFullInstructions(usage.getInstructions(), false);
			int endidx = instructions.indexOf('.');
			instructions = endidx >= 0 && endidx + 2 <= instructions.length() ? instructions.substring(endidx + 2) : "";
			return instructions;
		}

		private static String getFullInstructions(Iterable<String> instructions, boolean stopAtPeriod)
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String str : instructions)
			{
				if (!first)
					sb.append(" ");
				sb.append(str);
				first = false;
				if (stopAtPeriod && str.indexOf('.') >= 0)
					break;
			}
			return sb.toString();
		}

		private static String getFullSignatureTemplate(DEHActionPointer pointer)
		{
			StringBuilder sb = new StringBuilder();
			DEHActionPointer.Usage usage = pointer.getUsage();
			
			sb.append("A_" + pointer.getMnemonic()).append("(");

			boolean first = true;
			for (PointerParameter pusage : usage.getParameters())
			{
				if (!first)
					sb.append(", ");
				sb.append("${").append(pusage.getName()).append("}");
				first = false;
			}
			sb.append(")${cursor}");
			return sb.toString();
		}

		private static String getFunctionDescriptionHTML(final DEHActionPointer pointer)
		{
			return writeHTML((html) -> writeFunctionUsageHTML(html, pointer));
		}

		private static void writeFunctionUsageHTML(HTMLWriter html, DEHActionPointer pointer) throws IOException
		{
			DEHActionPointer.Usage usage = pointer.getUsage();
		
			// Signature.
			html.push("div");
			html.push("strong");
			html.text("A_" + pointer.getMnemonic());
			html.text(" (");
			boolean first = true;
			for (PointerParameter pusage : pointer.getUsage().getParameters())
			{
				if (!first)
					html.text(", ");
				html.tag("em", pusage.getName());
				first = false;
			}
			html.text(")");
			html.pop();
			html.pop();

			// Type
			html.push("div")
				.tag("strong", pointer.getType().name())
				.text(" ")
				.tag("em", (pointer.isWeapon() ? "Weapon" : "Thing") + " Pointer")
			.pop();

			html.push("div").html("&nbsp;").pop();
			
			// Full instructions.

			String instructions = getInstructions(usage);
			String summaryInstructions = getSummaryInstructions(usage);
			
			if (instructions.trim().length() > 0)
			{
				html.tag("div", getInstructions(usage));
			}
			
			if (summaryInstructions.trim().length() > 0)
			{
				boolean inList = false;
				for (String divText : getSummaryInstructions(usage).split("\\n+"))
				{
					if (divText.startsWith("* "))
					{
						if (!inList)
							html.push("ul");
						html.tag("li", divText.substring(2));
						inList = true;
					}
					else if (inList)
					{
						html.pop(); // pop <ul>
						inList = false;
					}
					
					if (!inList)
					{
						html.push("div").html("&nbsp;").pop();
						html.tag("div", divText);
					}
				}
			}
		
			if (usage.hasParameters())
			{
				html.push("div").html("&nbsp;").pop();
				html.push("div").tag("strong", "Parameters:").pop();
				writeFunctionTypeUsageHTML(html, usage.getParameters());			
			}
		}

		private static void writeFunctionTypeUsageHTML(HTMLWriter html, Iterable<PointerParameter> parameterUsages) throws IOException
		{
			html.push("ul");
			for (PointerParameter tusage : parameterUsages)
			{
				html.push("li")
					.push("span")
						.tag("strong", tusage.getName())
						.text(" ")
						.tag("span", "(" + tusage.getType().name() + ")")
						.html(" &mdash; ")
						.text(getFullInstructions(tusage.getInstructions(), false))
					.pop()
				.pop();
			}
			html.pop();
		}
		
	}
	
}
