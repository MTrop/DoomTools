/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;

/**
 * Describes all DeHackEd objects and how to write them.
 * @author Matthew Tropiano
 * @param <SELF> this object's class.
 */
public abstract class DEHObject<SELF>
{
	/** Custom properties. */
	private Map<DEHProperty, String> customProperties;
	
	/** Force output. */
	protected boolean forceOutput;
	
	protected DEHObject()
	{
		this.customProperties = new HashMap<>();
	}
	
	/**
	 * Sets a custom property value.
	 * @param property the property.
	 * @param value the value.
	 */
	public void setCustomPropertyValue(DEHProperty property, String value)
	{
		customProperties.put(property, value);
	}

	/**
	 * Clears the custom properties.
	 */
	public void clearCustomPropertyValues()
	{
		customProperties.clear();
	}
	
	/**
	 * @return true if this has custom properties defined on it, false if not.
	 */
	public boolean hasCustomProperties()
	{
		return !customProperties.isEmpty();
	}
	
	/**
	 * @return the set of all custom properties.
	 */
	protected Set<Map.Entry<DEHProperty, String>> getCustomPropertySet()
	{
		return customProperties.entrySet();
	}
	
	/**
	 * Writes the custom properties out to DeHackEd.
	 * @param writer the writer to use.
	 * @throws IOException if a write error occurs.
	 */
	public void writeCustomProperties(Writer writer) throws IOException
	{
		for (Map.Entry<DEHProperty, String> property : customProperties.entrySet())
			writer.append(property.getKey().getDeHackEdLabel()).append(" = ").append(property.getValue()).append("\r\n");
	}
	
	/**
	 * Copies this object's values/properties.
	 * @param source the source object.
	 * @return this object.
	 */
	public abstract SELF copyFrom(SELF source);

	/**
	 * Sets if this object is fully output even if no fields change.
	 * @param enabled true to enable, false to disable.
	 * @return this object.
	 */
	@SuppressWarnings("unchecked")
	public final SELF setForceOutput(boolean enabled)
	{
		this.forceOutput = enabled;
		return (SELF)this;
	}
	
	public final boolean isForceOutput()
	{
		return forceOutput;
	}
	
	/**
	 * Writes this object to a DeHackEd file stream.
	 * @param writer the writer to write to.
	 * @param original the original object to compare to for writing changed fields.
	 * @param level the highest feature level to export for.
	 * @throws IOException if a write error occurs.
	 */
	public abstract void writeObject(Writer writer, SELF original, DEHFeatureLevel level) throws IOException;
	
}
