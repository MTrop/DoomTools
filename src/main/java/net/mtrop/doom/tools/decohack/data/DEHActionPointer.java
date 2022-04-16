/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public interface DEHActionPointer
{
	/** The NULL pointer. */
	static DEHActionPointer NULL = new DEHActionPointer() 
	{
		final DEHActionPointerParamType[] NO_PARAMS = new DEHActionPointerParamType[0];

		@Override
		public int getFrame()
		{
			return 0;
		}

		@Override
		public boolean isWeapon() 
		{
			return false;
		}
		
		@Override
		public DEHActionPointerType getType() 
		{
			return DEHActionPointerType.DOOM19;
		}
		
		@Override
		public DEHActionPointerParamType[] getParams() 
		{
			return NO_PARAMS;
		}
		
		@Override
		public DEHActionPointerParamType getParam(int index) 
		{
			return null;
		}
		
		@Override
		public String getMnemonic()
		{
			return "NULL";
		}
	}; 
	
	/**
	 * Gets the originating frame for an action pointer.
	 * This will return <code>-1</code> for pointers not in the original Doom.
	 * @return the original frame, or -1.
	 */
	int getFrame();
	
	/**
	 * @return true if this is a weapon action pointer.
	 */
	boolean isWeapon();
	
	/**
	 * @return the mnemonic string for this pointer.
	 */
	String getMnemonic();
	
	/**
	 * @return the feature set category for this pointer.
	 */
	DEHActionPointerType getType();
	
	/**
	 * @return the parameter list.
	 */
	DEHActionPointerParamType[] getParams();

	/**
	 * Gets a specific param using the param index.
	 * @param index the parameter index.
	 * @return the corresponding parameter, or <code>null</code> if no parameter.
	 */
	DEHActionPointerParamType getParam(int index);

	/**
	 * Checks if two pointers are semantically equal.
	 * This only checks the mnemonics case-insensitively.
	 * @param pointer the other pointer.
	 * @return true if equal, false if not.
	 * @see #getMnemonic()
	 */
	default boolean equals(DEHActionPointer pointer)
	{
		return pointer != null && getMnemonic().equalsIgnoreCase(pointer.getMnemonic());
	}
	
}
