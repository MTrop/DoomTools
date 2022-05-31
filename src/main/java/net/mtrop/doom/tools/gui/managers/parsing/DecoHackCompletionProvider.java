package net.mtrop.doom.tools.gui.managers.parsing;

import java.io.IOException;
import java.io.StringWriter;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer.Usage.PointerParameter;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21;
import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.HTMLWriter.Options;

/** 
 * DECOHack Completion Provider.
 * @author Matthew Tropiano
 */
public class DecoHackCompletionProvider extends DefaultCompletionProvider
{
	public DecoHackCompletionProvider()
	{
		super();
		for (DEHActionPointerDoom19 pointer : DEHActionPointerDoom19.values())
			addCompletion(new PointerCompletion(this, pointer));
		for (DEHActionPointerMBF pointer : DEHActionPointerMBF.values())
			addCompletion(new PointerCompletion(this, pointer));
		for (DEHActionPointerMBF21 pointer : DEHActionPointerMBF21.values())
			addCompletion(new PointerCompletion(this, pointer));

		for (DecoHackTemplateCompletion completion : DecoHackTemplateCompletion.values())
			addCompletion(completion.createCompletion(this));
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
				getInstructions(pointer.getUsage()), 
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
			sb.append(")");
			return sb.toString();
		}

		private static String getFunctionDescriptionHTML(DEHActionPointer pointer)
		{
			StringWriter out = new StringWriter(1024);
			try (HTMLWriter html = new HTMLWriter(out, Options.SLASHES_IN_SINGLE_TAGS)) 
			{
				html.push("html").push("body");
				writeFunctionUsageHTML(html, pointer);
				html.end();
			} 
			catch (IOException e)
			{
				// Do nothing - shouldn't be thrown.
			}
			return out.toString();
		}

		private static void writeFunctionUsageHTML(HTMLWriter html, DEHActionPointer pointer) throws IOException
		{
			DEHActionPointer.Usage usage = pointer.getUsage();
		
			// Signature.
			html.push("div");
			html.push("strong");
			html.text("A_" + pointer.getMnemonic());
			html.text("(");
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
		
			// Full instructions.
			
			String instructions = getInstructions(usage);
			String summaryInstructions = getInstructions(usage);
			
			if (instructions.trim().length() > 0)
				html.tag("div", getInstructions(usage));
			if (summaryInstructions.trim().length() > 0)
				html.tag("div", getSummaryInstructions(usage));
		
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
						.tag("strong", tusage.getType().name().toLowerCase())
						.html(" &mdash; ").text(getFullInstructions(tusage.getInstructions(), false))
					.pop()
				.pop();
			}
			html.pop();
		}
		
	}
	
}
