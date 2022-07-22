/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.parsing;

import com.blackrook.rookscript.lang.ScriptFunctionType;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.WadScriptMain.Resolver;

/** 
 * WadScript Completion Provider.
 * @author Matthew Tropiano
 */
public class WadScriptCompletionProvider extends RookScriptCompletionProvider
{
	public WadScriptCompletionProvider()
	{
		for (Resolver r : WadScriptMain.getAllWadScriptResolvers())
			for (ScriptFunctionType type : r.resolver.getFunctions())
				addCompletion(new FunctionCompletion(this, r.namespace, type));
	}
}

