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
		// TODO: Finish this.
	});

	private static final Map<String, IOConsumer<HTMLWriter>> WEAPON_HARDCODE_DOCS = ObjectUtils.apply(new HashMap<>(), (map) -> {
		// TODO: Finish this.
	});
	
	private static final Map<String, IOConsumer<HTMLWriter>> STATE_HARDCODE_DOCS = ObjectUtils.apply(new HashMap<>(), (map) -> {
		// TODO: Finish this.
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
						html.tag("div", writeHTML(addendum));
					}
				});
				
				addCompletion(new DefineCompletion(this, type, token, value, summary));
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
		 * @param summary the summary.
		 */
		public DefineCompletion(CompletionProvider parent, DecoHackPatchType type, String token, String value, String summary) 
		{
			super(parent, token);
			setShortDescription("(" + type.name() + ") " + value);
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
				sb.append(str.trim());
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
				html.push("div").html("&nbsp;").pop();
			}
			
			if (summaryInstructions.trim().length() > 0)
			{
				html.tag("div", getSummaryInstructions(usage));
				html.push("div").html("&nbsp;").pop();
			}
		
			if (usage.hasParameters())
			{
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
