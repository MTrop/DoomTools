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
		"auto thing ${ThingAlias} \"${ThingName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
	
	AUTOTHINGCOPY("Adds an auto-thing copied from another thing.", (
		"auto thing ${ThingAlias} : thing ${SourceThing} \"${ThingName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	AMMOBLOCK("Adds an ammo block.", (
		"ammo ${SlotNumber} \"${AmmoName}\" {\n" +
		"\tmax ${MaxAmount}\n" +
		"\tpickup ${PickupAmount}\n" +
		"}\n"
	)),
		
	EACHTHINGFROM("Adds an \"each thing\" range block.", (
		"each thing from ${ThingIDStart} to ${ThingIDEnd} {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	EACHTHINGIN("Adds an \"each thing\" set block.", (
		"each thing in (${ThingList}) {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	EACHWEAPONFROM("Adds an \"each weapon\" range block.", (
		"each weapon from ${WeaponIDStart} to ${WeaponIDEnd} {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	EACHWEAPONIN("Adds an \"each weapon\" set block.", (
		"each weapon in (${WeaponList}) {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	SOUNDBLOCK("Adds a sound block.", (
		"sound \"${SoundName}\" {\n" +
		"\tpriority ${Priority}\n" +
		"\tsingular ${Singular}\n" +
		"}\n"
	)),
			
	STATEFREE1("Adds a \"state free\" clause.", (
		"state free ${StateID}\n"
	)),

	STATEFREE2("Adds a \"state free\" range clause.", (
		"state free ${StateIDStart} to ${StateIDEnd}\n"
	)),

	STATEFREE3("Adds a \"state free\" from a starting state clause.", (
		"state free from ${StateIDStart}\n"
	)),

	STATEFILL("Adds a \"state fill\" block.", (
		"state fill ${StateIDStart} {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	STRINGSBLOCK("Adds a strings block.", (
		"strings {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),
		
	THINGBLOCK("Adds a thing block.", (
		"thing ${ThingID} \"${ThingName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	THINGBLOCKCOPY("Adds a thing block copied from another thing.", (
		"thing ${ThingID} : thing ${SourceThing} \"${ThingName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	THINGFREE1("Adds a \"Thing free\" clause.", (
		"thing free ${ThingID}\n"
	)),

	THINGFREE2("Adds a \"Thing free\" range clause.", (
		"thing free ${ThingIDStart} to ${ThingIDEnd}\n"
	)),

	THINGFREESTATES("Adds a \"free a thing's connected states\" clause.", (
		"thing ${ThingID} free states\n"
	)),

	THINGFREESTATES2("Adds a \"free a thing's specific connected states\" clause.", (
		"thing ${ThingID} free #{StateLabel}\n"
	)),

	THINGSWAP("Adds a thing swap clause.", (
		"thing ${Thing1} swap with ${Thing2}\n"
	)),

	WEAPONBLOCK("Adds a weapon block.", (
		"weapon ${WeaponID} \"${WeaponName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	WEAPONBLOCKCOPY("Adds a weapon block copied from another weapon.", (
		"weapon ${WeaponID} : weapon ${SourceWeapon} \"${WeaponName}\" {\n" +
		"\t${cursor}\n" +
		"}\n"
	)),

	WEAPONFREESTATES("Adds a \"free a weapon's connected states\" clause.", (
		"weapon ${WeaponID} free states\n"
	)),

	WEAPONFREESTATES2("Adds a \"free a weapon's specific connected states\" clause.", (
		"weapon ${WeaponID} free #{StateLabel}\n"
	)),

	WEAPONSWAP("Adds a weapon's swap clause.", (
		"weapon ${Weapon1} swap with ${Weapon2}\n"
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
