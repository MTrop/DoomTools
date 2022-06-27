package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

/**
 * DECOHack export settings for WadScript.
 * @author Matthew Tropiano
 */
public class ExportSettings
{
	private File outputFile;
	private File sourceOutputFile;
	private boolean outputBudget;
	
	public ExportSettings()
	{
		this.outputFile = null;
		this.sourceOutputFile = null;
		this.outputBudget = false;
	}
	
	public ExportSettings(File sourceFile)
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

