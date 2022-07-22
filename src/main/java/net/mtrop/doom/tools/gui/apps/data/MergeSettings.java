/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps.data;

import java.io.File;

import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Execution settings for WadMerge.
 * @author Matthew Tropiano
 */
public class MergeSettings
{
	private File workingDirectory;
	private String[] args;
	
	public MergeSettings()
	{
		this(null);
	}
	
	public MergeSettings(File workingDirectory)
	{
		this.workingDirectory = workingDirectory;
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
	
	public void setArgs(String ... args) 
	{
		this.args = ArrayUtils.arrayOf(args);
	}
	
	public String[] getArgs() 
	{
		return args;
	}
	
}

