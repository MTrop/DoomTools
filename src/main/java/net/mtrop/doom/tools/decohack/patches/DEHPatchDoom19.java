package net.mtrop.doom.tools.decohack.patches;

/**
 * Common DeHackEd Patch interface for specific Doom v1.9 executable patches.
 * @author Matthew Tropiano
 */
public interface DEHPatchDoom19 extends DEHPatch
{
	/**
	 * @return the amount of strings.
	 */
	int getStringCount();
	
	/**
	 * Gets a string.
	 * @param index the index.
	 * @return the corresponding string.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see #getStringCount() 
	 */
	String getString(int index);
	
	/**
	 * Gets the original frame number for an action pointer. 
	 * @param index the action pointer index.
	 * @return the original frame index.
	 */
	int getActionPointerFrame(int index);
}
