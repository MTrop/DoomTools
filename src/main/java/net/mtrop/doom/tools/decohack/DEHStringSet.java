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

	/**
	 * Gets the key that corresponds to a string.
	 * @param value the string value for lookup.
	 * @return the corresponding key, or null if not found.
	 */
	K getKey(String value);

}
