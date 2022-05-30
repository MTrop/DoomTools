package net.mtrop.doom.tools.gui.managers.parsing;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

/**
 * An enumeration of RookScript Templates. 
 * @author Matthew Tropiano
 */
public enum RookScriptTemplateCompletion
{
	FORL("A \"for\" loop that iterates through a list.", (
		"for (${i} = 0; ${i} < length(${list}); ${i} += 1) {\n" +
		"\t${element} = ${list}[${i}];\n" +
		"\t${cursor}\n" +
		"}"
	)),

	EACHL("An \"each\" loop that iterates through an object.", (
		"each (${i} : ${object}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	EACHM("An \"each\" loop that iterates through a map.", (
		"each (${k}, ${v} : ${map}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),
	
	WHILE("A \"while\" loop expression.", (
		"while (${expression}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),
	
	ENTRY("An entry point.", (
		"entry ${name}() {\n" +
		"\t${cursor}\n" +
		"}"
	)),
	
	ENTRYARGS("An entry point with an argument array.", (
		"entry ${name}(args) {\n" +
		"\t${cursor}\n" +
		"}"
	)),
	
	FUNCTION("A function with no arguments.", (
		"function ${name}() {\n" +
		"\t${cursor}\n" +
		"}"
	)),
		
	FUNCTION1("A function with one argument.", (
		"function ${name}(${param}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	FUNCTION2("A function with two arguments.", (
		"function ${name}(${param0}, ${param1}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	FUNCTION3("A function with three arguments.", (
		"function ${name}(${param0}, ${param1}, ${param2}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	FUNCTION4("A function with four arguments.", (
		"function ${name}(${param0}, ${param1}, ${param2}, ${param3}) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	MAIN("The main entry point.", (
		"entry main(args) {\n" +
		"\t${cursor}\n" +
		"}"
	)),

	;
	
	private final String inputText;
	private final String description;
	private final String templateString;
	
	private RookScriptTemplateCompletion(String description, String templateString)
	{
		this.inputText = name();
		this.description = description;
		this.templateString = templateString;
	}
	
	private RookScriptTemplateCompletion(String inputText, String description, String templateString)
	{
		this.inputText = inputText;
		this.description = description;
		this.templateString = templateString;
	}
	
	/**
	 * Creates a template completion for this definition.
	 * @param parent the parent provider.
	 * @return the generated completion.
	 */
	public final TemplateCompletion createCompletion(CompletionProvider parent)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("<div>").append(description).append("</div>");
		sb.append("<pre>").append(templateString).append("</pre>");
		sb.append("</body></html>");
		return new RookScriptTemplate(parent, inputText, templateString, description, sb.toString());
	}
	
	/**
	 * RookScript template.
	 */
	private static class RookScriptTemplate extends TemplateCompletion
	{
		private RookScriptTemplate(CompletionProvider provider, String inputText, String template, String shortDescription, String summary)
		{
			super(provider, inputText, shortDescription, template, shortDescription, summary);
		}
		
		@Override
		public String toString() 
		{
			return getInputText() + " (Template) " + getShortDescription();
		}
	}
	
}
