package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

/**
 * DEFSWANI export settings for WSwAnTbl.
 * @author Matthew Tropiano
 */
public class DefSwAniExportSettings
{
	private File outputWAD;
	private boolean outputSource;
	
	public DefSwAniExportSettings()
	{
		this.outputWAD = null;
		this.outputSource = false;
	}
	
	public File getOutputWAD() 
	{
		return outputWAD;
	}
	
	public void setOutputWAD(File value)
	{
		this.outputWAD = value;
	}
	
	public boolean isOutputSource() 
	{
		return outputSource;
	}
	
	public void setOutputSource(boolean value) 
	{
		this.outputSource = value;
	}
	
}

