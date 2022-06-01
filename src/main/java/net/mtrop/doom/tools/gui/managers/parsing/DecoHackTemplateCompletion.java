package net.mtrop.doom.tools.gui.managers.parsing;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

/**
 * An enumeration of DECOHack Templates. 
 * @author Matthew Tropiano
 */
public enum DecoHackTemplateCompletion
{
	AUTOTHING("Adds an auto-thing.", (
		"auto thing ${thingalias} \"${thingname}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
	
	AUTOTHINGI("Adds an auto-thing copied from another thing.", (
		"auto thing ${thingalias} \"${thingname}\" : thing ${sourcething} {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	AMMO("Creates an ammo block.", (
		"ammo ${SlotNumber} \"${AmmoName}\" {\n" +
		"\tmax ${MaxAmount}\n" +
		"\tpickup ${PickupAmount}\n" +
		"}\n"
	)),
		
	SOUND("Creates a sound block.", (
		"ammo ${SlotNumber} \"${AmmoName}\" {\n" +
		"\tmax ${MaxAmount}\n" +
		"\tpickup ${PickupAmount}\n" +
		"}\n"
	)),
			
	STRINGS("Creates a strings block.", (
		"strings {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	;
	
	private final String inputText;
	private final String description;
	private final String templateString;
	
	private DecoHackTemplateCompletion(String description, String templateString)
	{
		this.inputText = name();
		this.description = description;
		this.templateString = templateString;
	}
	
	private DecoHackTemplateCompletion(String inputText, String description, String templateString)
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
		return new DecoHackTemplate(parent, inputText, templateString, description, sb.toString());
	}
	
	/**
	 * DECOHack template.
	 */
	private static class DecoHackTemplate extends TemplateCompletion
	{
		private DecoHackTemplate(CompletionProvider provider, String inputText, String template, String shortDescription, String summary)
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
