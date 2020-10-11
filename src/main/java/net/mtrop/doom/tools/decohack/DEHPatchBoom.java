package net.mtrop.doom.tools.decohack;

import java.util.Set;

/**
 * Common DeHackEd Patch interface for Boom/BEX.
 * @author Matthew Tropiano
 */
public interface DEHPatchBoom extends DEHPatch
{
	/**
	 * Gets a string by its macro key.
	 * @param key the key.
	 * @return the corresponding string.
	 */
	String getString(String key);
	
	/**
	 * @return all possible string lookup keys.
	 */
	Set<String> getStringKeys();
	
}
