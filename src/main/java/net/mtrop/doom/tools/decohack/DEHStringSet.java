package net.mtrop.doom.tools.decohack;

/**
 * DeHackEd strings bank.
 * @param <K> the key type.
 * @author Matthew Tropiano
 */
public interface DEHStringSet<K>
{
	/**
	 * Gets the string that corresponds to a key.
	 * @param key the key for lookup.
	 * @return the corresponding string, or null if no string.
	 */
	String getString(K key);
	
	/**
	 * Gets the original string that corresponds to a key.
	 * @param key the key for lookup.
	 * @return the corresponding string, or null if no string.
	 */
	String getOriginalString(K key);

}
