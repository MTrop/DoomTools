package net.mtrop.doom.tools.decohack.patches;

/**
 * Patch implementation for ID24, extending DSDHacked.
 * @author Matthew Tropiano
 */
public class PatchID24 extends PatchDSDHacked 
{
	public static final String USERSTRING_PREFIX = "USER_";

	/**
	 * Checks if the provided string key is a userstring.
	 * @param key the string key.
	 * @return true if so, false if not.
	 */
	public static boolean isUserStringKey(String key)
	{
		return key.startsWith(USERSTRING_PREFIX);
	}
	
	@Override
	public String getString(String key)
	{
		String str = super.getString(key);
		if ("".equals(str))
			return "";
		else if (isUserStringKey(key))
			return "";
		else
			return null; 
	}

	@Override
	public boolean isValidStringKey(String key)
	{
		return super.isValidStringKey(key) || isUserStringKey(key);
	}
	
}
