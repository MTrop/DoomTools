package net.mtrop.doom.tools.decohack.data;

import net.mtrop.doom.tools.decohack.data.enums.DEHValueType;

/**
 * DeHackEd property/custom property.
 * @author Matthew Tropiano
 */
public class DEHProperty
{
	/** In-DECOHack keyword. */
	private String keyword;
	/** In-DeHackEd label. */
	private String dehackedLabel;
	/** Parameter type. */
	private DEHValueType type;
	
	/**
	 * Creates a new property.
	 * @param keyword the keyword.
	 * @param dehackedLabel the label to write to DeHackEd.
	 * @param type the parameter type to check for.
	 */
	public DEHProperty(String keyword, String dehackedLabel, DEHValueType type)
	{
		this.keyword = keyword;
		this.dehackedLabel = dehackedLabel;
		this.type = type;
	}
	
	public String getKeyword() 
	{
		return keyword;
	}
	
	public String getDeHackEdLabel() 
	{
		return dehackedLabel;
	}
	
	public DEHValueType getType() 
	{
		return type;
	}
	
	public int hashCode()
	{
		return keyword.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof DEHProperty)
			return keyword.equalsIgnoreCase(((DEHProperty)obj).keyword);
		return super.equals(obj);
	}
	
}
