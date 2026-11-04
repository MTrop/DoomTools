package net.mtrop.doom.tools.decohack.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.mtrop.doom.tools.decohack.data.enums.DEHFlag;
import net.mtrop.doom.tools.struct.util.SetUtils;

/**
 * A flag set for patch state.
 * @author Matthew Tropiano
 */
public class DEHFlagSet implements Iterable<DEHFlag>
{
	private Set<DEHFlag> flagSet;
	
	/**
	 * Creates an empty flag set.
	 */
	public DEHFlagSet()
	{
		this.flagSet = new HashSet<>();
	}
	
	/**
	 * Creates a flag set from a group of flags.
	 * @param flags the flags.
	 */
	public DEHFlagSet(DEHFlag ... flags)
	{
		this();
		setFlags(flags);
	}
	
	/**
	 * Creates a flag set from a group of flags.
	 * @param flags the flags to potentially add/lookup.
	 * @param flagBits the flag bits.
	 */
	public DEHFlagSet(Collection<DEHFlag> flags, final int flagBits)
	{
		this();
		setFlags(flags, flagBits);
	}

	/**
	 * Sets flags from a group of flags.
	 * @param flags the flags to potentially add/lookup.
	 * @param flagBits the flag bits.
	 */
	public void setFlags(Collection<DEHFlag> flags, final int flagBits)
	{
		flagSet.clear();
		for (DEHFlag flag : flags)
		{
			if ((flag.getValue() & flagBits) != 0)
				addFlag(flag);
		}
	}
	
	/**
	 * Sets the flags on this set, clearing it first.
	 * @param flags the flags to set.
	 */
	public void setFlags(DEHFlag ... flags) 
	{
		flagSet.clear();
		for (DEHFlag flag : flags)
			flagSet.add(flag);
	}

	/**
	 * Adds a flag to the set.
	 * @param flag the flag to add.
	 */
	public void addFlag(DEHFlag flag)
	{
		flagSet.add(flag);
	}
	
	/**
	 * Removes a flag from the set.
	 * @param flag the flag to remove.
	 */
	public void removeFlag(DEHFlag flag)
	{
		flagSet.remove(flag);
	}
	
	/**
	 * Checks if this set contains a specific flag.
	 * @param flag the flag.
	 * @return true if so, false if not.
	 */
	public boolean containsFlag(DEHFlag flag)
	{
		return flagSet.contains(flag);
	}

	/**
	 * @return an array of the flags in this set.
	 */
	public DEHFlag[] flags()
	{
		return flagSet.toArray(new DEHFlag[flagSet.size()]);
	}
	
	/**
	 * @return the integer value fo this set.
	 */
	public int getValue()
	{
		int out = 0;
		for (DEHFlag flag : flagSet)
			out |= flag.getValue();
		return out;
	}
	
	/**
	 * Checks if this set is empty.
	 * @return true if so, false if not.
	 */
	public boolean isEmpty() 
	{
		return flagSet.isEmpty();
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHFlagSet)
			return equals((DEHFlagSet)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHFlagSet obj) 
	{
		return SetUtils.equals(this.flagSet, obj.flagSet);
	}
	
	/**
	 * @return the mnemonic string for this set.
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (DEHFlag flag : flagSet)
		{
			if (sb.length() > 0)
				sb.append("+");
			sb.append(flag.name());
		}
		return sb.toString();
	}

	@Override
	public Iterator<DEHFlag> iterator()
	{
		return flagSet.iterator();
	}
	
}
