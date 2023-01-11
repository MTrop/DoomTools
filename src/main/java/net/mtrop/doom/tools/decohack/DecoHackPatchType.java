/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack;

import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.decohack.contexts.PatchDoomUnityContext;

import java.util.Map;

import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchMBFContext;
import net.mtrop.doom.tools.decohack.contexts.PatchExtendedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchMBF21Context;
import net.mtrop.doom.tools.decohack.contexts.PatchDSDHackedContext;

/**
 * Enumeration of Patch types.
 * @author Matthew Tropiano
 */
public enum DecoHackPatchType
{
	DOOM19("doom19", PatchDoom19Context.class),
	UDOOM19("udoom19", PatchUltimateDoom19Context.class),
	DOOMUNITY("doomunity", PatchDoomUnityContext.class),
	BOOM("boom", PatchBoomContext.class),
	MBF("mbf", PatchMBFContext.class),
	EXTENDED("extended", PatchExtendedContext.class),
	MBF21("mbf21", PatchMBF21Context.class),
	DSDHACKED("dsdhacked", PatchDSDHackedContext.class);
	
	private final String keyword;
	private final Class<?> patchClass;
	
	private static final Map<String, DecoHackPatchType> VALUEMAP = EnumUtils.createCaseInsensitiveEnumMap(DecoHackPatchType.class, (o, e) -> e.getKeyword());
	
	private <A extends AbstractPatchContext<?>> DecoHackPatchType(String keyword, Class<A> patchClass)
	{
		this.keyword = keyword;
		this.patchClass = patchClass;
	}
	
	public static DecoHackPatchType getByKeyword(String keyword)
	{
		return VALUEMAP.get(keyword);
	}
	
	public String getKeyword() 
	{
		return keyword;
	}
	
	@SuppressWarnings("unchecked")
	public <A extends AbstractPatchContext<?>> Class<A> getPatchClass() 
	{
		return (Class<A>)patchClass;
	}
	
}
