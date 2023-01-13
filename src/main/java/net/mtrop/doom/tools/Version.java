/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import net.mtrop.doom.tools.common.Common;

/**
 * One stop shop for all application versions.
 * @author Matthew Tropiano
 */
public interface Version 
{
	String DOOMTOOLS = Common.getVersionString("doomtools");

	String JSON = Common.getVersionString("json");
	String DOOMSTRUCT = Common.getVersionString("doom");
	String ROOKSCRIPT = Common.getVersionString("rookscript");
	String ROOKSCRIPT_DESKTOP = Common.getVersionString("rookscript-desktop");

	String FLATLAF = Common.getVersionString("flatlaf");
	String RSYNTAXTEXTAREA = Common.getVersionString("rsyntaxtextarea");
	String AUTOCOMPLETE = Common.getVersionString("autocomplete");
	String COMMONMARK = Common.getVersionString("commonmark");

	String DECOHACK = Common.getVersionString("decohack");
	String DIMGCONV = Common.getVersionString("dimgconv");
	String DMXCONV = Common.getVersionString("dmxconv");
	String DOOMFETCH = Common.getVersionString("doomfetch");
	String DOOMMAKE = Common.getVersionString("doommake");
	String WADMERGE = Common.getVersionString("wadmerge");
	String WADSCRIPT = Common.getVersionString("wadscript");
	String WADTEX = Common.getVersionString("wadtex");
	String WSWANTBL = Common.getVersionString("wswantbl");
	String WTEXPORT = Common.getVersionString("wtexport");
	String WTEXSCAN = Common.getVersionString("wtexscan");
}
