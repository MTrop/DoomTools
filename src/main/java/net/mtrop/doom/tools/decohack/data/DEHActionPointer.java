/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.LinkedList;
import java.util.List;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public interface DEHActionPointer
{
	static Usage BLANK_USAGE = usage();
	
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
	 * @return the Usage documentation for this pointer.
	 */
	default Usage getUsage()
	{
		return BLANK_USAGE;
	}
	
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
	
	/**
	 * Creates a Usage object. 
	 * Usage can be built from these.
	 * @param instructions a list of instructions strings, representing paragraphs.
	 * @return a new Usage object.
	 */
	static Usage usage(String ... instructions)
	{
		Usage out = new Usage();
		for (int i = 0; i < instructions.length; i++)
			out.instructions.add(instructions[i]);
		return out;
	}
	
	/**
	 * Encapsulates a set of parameters.
	 * Just for code aesthetics.
	 * @param types the parameter types.
	 * @return the array of types.
	 */
	static DEHActionPointerParamType[] params(DEHActionPointerParamType ... types)
	{
		return ArrayUtils.arrayOf(types);
	}

	/**
	 * Usage info for an action pointer.
	 */
	public static class Usage
	{
		private List<String> instructions;
		private List<PointerParameter> parameters;
		
		public static class PointerParameter
		{
			private String name;
			private DEHActionPointerParamType type;
			private List<String> instructions;
			
			private PointerParameter(String name, DEHActionPointerParamType type)
			{
				this.name = name;
				this.type = type;
				this.instructions = new LinkedList<>();
			}
			
			/**
			 * @return the parameter name.
			 */
			public String getName() 
			{
				return name;
			}
			
			/**
			 * @return the parameter's expected type.
			 */
			public DEHActionPointerParamType getType() 
			{
				return type;
			}
			
			/**
			 * @return the usage instructions paragraphs.
			 */
			public Iterable<String> getInstructions() 
			{
				return instructions;
			}
			
		}
		
		private Usage()
		{
			this.instructions = new LinkedList<>();
			this.parameters = new LinkedList<>();
		}
		
		/**
		 * Adds a parameter usage.
		 * @param name the parameter name.
		 * @param type the parameter type.
		 * @param instructions a list of instructions strings, representing paragraphs.
		 * @return this Usage object.
		 */
		public Usage parameter(String name, DEHActionPointerParamType type, String ... instructions)
		{
			PointerParameter out;
			parameters.add(out = new PointerParameter(name, type));
			for (int i = 0; i < instructions.length; i++)
				out.instructions.add(instructions[i]);
			return this;
		}
		
		/**
		 * @return the usage instructions paragraphs.
		 */
		public Iterable<String> getInstructions() 
		{
			return instructions;
		}
		
		/**
		 * @return the action pointer parameters.
		 */
		public Iterable<PointerParameter> getParameters()
		{
			return parameters;
		}
		
		/**
		 * @return true if this has parameters.
		 */
		public boolean hasParameters()
		{
			return !parameters.isEmpty();
		}
		
	}
	
}
