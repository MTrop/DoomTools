package net.mtrop.doom.tools.gui.managers.parsing;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.ParameterUsage;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.TypeUsage;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.WadScriptMain.Resolver;
import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.HTMLWriter.Options;

/**
 * RookScript completion provider.
 * @author Matthew Tropiano
 */
public class RookScriptCompletionProvider extends DefaultCompletionProvider
{
	public RookScriptCompletionProvider()
	{
		super();
		for (Resolver r : WadScriptMain.getAllBaseResolvers())
			for (ScriptFunctionType type : r.resolver.getFunctions())
				addCompletion(new FunctionCompletion(this, r.namespace, type));
		
		for (RookScriptTemplateCompletion completion : RookScriptTemplateCompletion.values())
			addCompletion(completion.createCompletion(this));
	}
	
	/**
	 * A completion object for editor function.
	 */
	protected static class FunctionCompletion extends TemplateCompletion
	{
		/**
		 * Creates a RookScript function completion.
		 * @param parent the completion provider.
		 * @param namespace the function namespace. Can be null.
		 * @param type the function.
		 */
		protected FunctionCompletion(CompletionProvider parent, String namespace, ScriptFunctionType type) 
		{
			super(parent, 
				getInputText(namespace, type), 
				getInstructions(type.getUsage()), 
				getFullSignatureTemplate(type), 
				getInstructions(type.getUsage()), 
				getFunctionDescriptionHTML(namespace, type)
			);
		}
		
		@Override
		public String toString() 
		{
			return getInputText() + " - " + getShortDescription();
		}

		private static String getInputText(String namespace, ScriptFunctionType type)
		{
			return (namespace != null ? namespace.toLowerCase() + "::" : "") + type.name().toLowerCase();
		}

		private static String getInstructions(ScriptFunctionType.Usage usage)
		{
			String instructions = usage.getInstructions();
			int endidx = instructions.indexOf('.');
			instructions = endidx >= 0 ? instructions.substring(0, endidx + 1) : instructions;
			return instructions;
		}

		private static String getFullSignatureTemplate(ScriptFunctionType type)
		{
			StringBuilder sb = new StringBuilder();
			ScriptFunctionType.Usage usage = type.getUsage();
			
			sb.append(type.name().toLowerCase()).append("(");

			boolean first = true;
			for (ParameterUsage pusage : usage.getParameterInstructions())
			{
				if (!first)
					sb.append(", ");
				sb.append("${").append(pusage.getParameterName()).append("}");
				first = false;
			}
			sb.append(")");
			return sb.toString();
		}

		private static String getFunctionDescriptionHTML(String namespace, ScriptFunctionType type)
		{
			StringWriter out = new StringWriter(1024);
			try (HTMLWriter html = new HTMLWriter(out, Options.SLASHES_IN_SINGLE_TAGS)) 
			{
				html.push("html").push("body");
				writeFunctionUsageHTML(html, namespace, type.name().toLowerCase(), type.getUsage());
				html.end();
			} 
			catch (IOException e) 
			{
				// Do nothing - shouldn't be thrown.
			}
			return out.toString();
		}

		private static void writeFunctionTypeUsageHTML(HTMLWriter html, List<TypeUsage> typeUsages) throws IOException
		{
			html.push("ul");
			for (TypeUsage tusage : typeUsages)
			{
				html.push("li")
					.push("span")
						.tag("strong", tusage.getType() != null ? tusage.getType().name() : "ANY")
						.tag("em", tusage.getSubType() != null ? ":" + tusage.getSubType() : "")
						.html(" &mdash; ").text(tusage.getDescription())
					.pop()
				.pop();
			}
			html.pop();
		}

		private static void writeFunctionUsageHTML(HTMLWriter html, String namespace, String name, ScriptFunctionType.Usage usage) throws IOException
		{
			// Signature.
			html.push("div");
			html.push("strong");
			html.text((namespace != null ? namespace.toLowerCase() + "::" : "") + name);
			html.text(" (");
			boolean first = true;
			for (ParameterUsage pusage : usage.getParameterInstructions())
			{
				if (!first)
					html.text(", ");
				html.tag("em", pusage.getParameterName());
				first = false;
			}
			html.text(")");
			html.pop();
			html.pop();
		
			// Full instructions.
			html.push("div").text(usage.getInstructions()).pop();
			
			// Parameters
			if (!usage.getParameterInstructions().isEmpty())
			{
				html.push("div");
				for (ParameterUsage pusage : usage.getParameterInstructions())
				{
					html.push("div")
						.tag("strong", pusage.getParameterName())
					.pop();
					
					writeFunctionTypeUsageHTML(html, pusage.getTypes());			
				}
				html.pop();
			}
			
			html.push("div")
				.tag("strong", "Returns:")
			.pop();
		
			writeFunctionTypeUsageHTML(html, usage.getReturnTypes());			
		}
		
	}
	
}
