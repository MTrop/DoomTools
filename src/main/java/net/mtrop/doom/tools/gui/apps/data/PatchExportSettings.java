/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

/**
 * DECOHack export settings.
 * @author Matthew Tropiano
 */
public class PatchExportSettings
{
	private File outputFile;
	private File sourceOutputFile;
	private boolean outputBudget;
	
	public PatchExportSettings()
	{
		this.outputFile = null;
		this.sourceOutputFile = null;
		this.outputBudget = false;
	}
	
	public PatchExportSettings(File sourceFile)
	{
		this.outputFile = new File(sourceFile.getParent() + File.separator + "dehacked.deh");
		this.sourceOutputFile = null;
		this.outputBudget = false;
	}
	
	public File getOutputFile() 
	{
		return outputFile;
	}
	
	public void setOutputFile(File value)
	{
		this.outputFile = value;
	}
	
	public File getSourceOutputFile() 
	{
		return sourceOutputFile;
	}
	
	public void setSourceOutputFile(File value) 
	{
		this.sourceOutputFile = value;
	}
	
	public boolean isOutputBudget() 
	{
		return outputBudget;
	}
	
	public void setOutputBudget(boolean value) 
	{
		this.outputBudget = value;
	}
	
}

