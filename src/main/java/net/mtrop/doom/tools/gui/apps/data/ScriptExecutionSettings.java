package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Execution settings for WadScript.
 * @author Matthew Tropiano
 */
public class ScriptExecutionSettings
{
	private File workingDirectory;
	private File standardInPath;
	private String entryPoint;
	private String[] args;
	
	public ScriptExecutionSettings()
	{
		this(null);
	}
	
	public ScriptExecutionSettings(File workingDirectory)
	{
		this.workingDirectory = workingDirectory;
		this.standardInPath = null;
		this.entryPoint = "main";
		this.args = new String[0];
	}
	
	public void setWorkingDirectory(File workingDirectory) 
	{
		this.workingDirectory = workingDirectory;
	}
	
	public File getWorkingDirectory() 
	{
		return workingDirectory;
	}
	
	public void setStandardInPath(File standardInPath) 
	{
		this.standardInPath = standardInPath;
	}
	
	public File getStandardInPath() 
	{
		return standardInPath;
	}
	
	public void setEntryPoint(String entryPoint) 
	{
		this.entryPoint = entryPoint;
	}
	
	public String getEntryPoint() 
	{
		return entryPoint;
	}
	
	public void setArgs(String ... args) 
	{
		this.args = ArrayUtils.arrayOf(args);
	}
	
	public String[] getArgs() 
	{
		return args;
	}
	
}

