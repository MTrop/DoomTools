/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

/**
 * Export settings for WadTex.
 * @author Matthew Tropiano
 */
public class WadTexExportSettings
{
	private File outputWAD;
	private boolean appendMode;
	private boolean forceStrife;
	private String nameOverride;
	
	public WadTexExportSettings()
	{
		this.outputWAD = null;
		this.appendMode = false;
		this.forceStrife = false;
		this.nameOverride = "TEXTURE1";
	}

	public void setOutputWAD(File outputWAD) 
	{
		this.outputWAD = outputWAD;
	}

	public File getOutputWAD() 
	{
		return outputWAD;
	}

	public void setAppendMode(boolean appendMode) 
	{
		this.appendMode = appendMode;
	}

	public boolean getAppendMode()
	{
		return appendMode;
	}

	public void setForceStrife(boolean forceStrife) 
	{
		this.forceStrife = forceStrife;
	}

	public boolean getForceStrife() 
	{
		return forceStrife;
	}

	public void setNameOverride(String nameOverride) 
	{
		this.nameOverride = nameOverride;
	}

	public String getNameOverride() 
	{
		return nameOverride;
	}
	
	
	
}

