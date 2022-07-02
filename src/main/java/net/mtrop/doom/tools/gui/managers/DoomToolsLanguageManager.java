package net.mtrop.doom.tools.gui.managers;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import javax.swing.KeyStroke;

import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * DoomTools Language Manager.
 * @author Matthew Tropiano
 */
public final class DoomToolsLanguageManager
{
	/** Default language. */
	private static final String DEFAULT_LANGUAGE = "default";
	/** OS: Windows. */
	private static final String OS_WINDOWS = "win";
	/** OS: OSX/macOS. */
	private static final String OS_MACOS = "mac";
	/** OS: Default Linux. */
	private static final String OS_LINUX = "linux";
	
	/** Missing text. */
	private static final String MISSING_TEXT = "[[NO LANGUAGE MATCH]]";
	
	/** Class loader. */
	private static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();
	
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsLanguageManager.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsLanguageManager> INSTANCE = new SingletonProvider<>(() -> new DoomToolsLanguageManager());

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsLanguageManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	/** Resource path. */
	private String resourcePath;
	/** Key map. */
	private Properties languageMap;
	
	/**
	 * Creates the language manager with the specific language.
	 */
	private DoomToolsLanguageManager()
	{
		final String iso3language = Locale.getDefault().getISO3Language().toLowerCase();
		final String operatingSystem = 
			OSUtils.isWindows() ? OS_WINDOWS :
			OSUtils.isOSX()     ? OS_MACOS :
			OSUtils.isLinux()   ? OS_LINUX : 
			null;
		
		this.resourcePath = "gui/language/";
		this.languageMap = new Properties();
		
		loadIntoMap(DEFAULT_LANGUAGE, null);
		if (operatingSystem != null)
			loadIntoMap(DEFAULT_LANGUAGE, operatingSystem);
		loadIntoMap(iso3language, null);
		if (operatingSystem != null)
			loadIntoMap(iso3language, operatingSystem);
	}

	private void loadIntoMap(String iso3language, String operatingSystem)
	{
		Objects.requireNonNull(iso3language);
		
		String resourceName;
		InputStream in = LOADER.getResourceAsStream(resourceName = 
			resourcePath + 
			iso3language + 
			(operatingSystem != null ? "." + operatingSystem : "") + 
			".properties"
		);
		if (in != null)
		{
			try {
				languageMap.load(new InputStreamReader(in));
				LOG.infof("Loaded language resource: %s", resourceName);
			} catch (IOException e) {
				SwingUtils.error("Could not load language file! Language " + iso3language);
			} finally {
				IOUtils.close(in);
			}
		}
		else
		{
			LOG.infof("No language resource loaded: %s not found.", resourceName);
		}
	}
	
	/**
	 * Checks if a key is present in the language map.
	 * @param key the language key.
	 * @return true if so, false if not.
	 */
	public boolean hasKey(String key)
	{
		return languageMap.getProperty(key) != null;
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param defaultValue the value to use if the key isn't found.
	 * @param modifier the function called after fetch to optionally modify the incoming value. Incoming value may be null!
	 * @return the desired value, or the provided default value if not found or parseable as an integer.
	 */
	public Integer getInteger(String key, Integer defaultValue, Function<Integer, Integer> modifier)
	{
		Integer out;
		String str = languageMap.getProperty(key);
		if (str == null)
		{
			out = defaultValue;
		}
		else
		{
			try {
				out = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				out = defaultValue;
			}
		}
		
		return modifier != null ? modifier.apply(out) : out;
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param modifier the function called after fetch to optionally modify the incoming value. Incoming value may be null!
	 * @return the desired value, after the modifier is called.
	 */
	public Integer getInteger(String key, Function<Integer, Integer> modifier)
	{
		return getInteger(key, null, modifier);
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param defaultValue the value to use if the key isn't found.
	 * @return the desired value, or the provided default value if not found or parseable as an integer.
	 */
	public Integer getInteger(String key, Integer defaultValue)
	{
		return getInteger(key, defaultValue, null);
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @return the desired value, or null if not found.
	 */
	public Integer getInteger(String key)
	{
		return getInteger(key, null, null);
	}
	
	/**
	 * Gets text using a text key.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the desired text, or a macro if it is not found. 
	 */
	public String getText(String key, Object ... args)
	{
		String out = languageMap.getProperty(key);
		return String.format(out != null ? out : MISSING_TEXT, args);
	}
	
	/**
	 * Attempts to parse a mnemonic value from the results of a language lookup.
	 * @param key the language key.
	 * @return the corresponding {@link KeyEvent} VK value, or {@link KeyEvent#VK_UNDEFINED} if not found.
	 */
	public int getMnemonicValue(String key)
	{
		if (!hasKey(key))
			return KeyEvent.VK_UNDEFINED;
		
		char keyname = Character.toUpperCase(getText(key).charAt(0));
		try {
			Field f;
			if ((f = KeyEvent.class.getField("VK_" + keyname)) == null)
				return KeyEvent.VK_UNDEFINED;
			else
				return f.getInt(null);
		} catch (NoSuchFieldException | SecurityException e) {
			return KeyEvent.VK_UNDEFINED;
		} catch (IllegalArgumentException e) {
			return KeyEvent.VK_UNDEFINED;
		} catch (IllegalAccessException e) {
			return KeyEvent.VK_UNDEFINED;
		}
	}
	
	/**
	 * Attempts to parse a keystroke value from the results of a language lookup.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the corresponding keystroke, or null if not found.
	 */
	public KeyStroke getKeyStroke(String key, Object ... args)
	{
		if (!hasKey(key))
			return null;
		
		String value = getText(key, args);
		KeyStroke out = KeyStroke.getKeyStroke(value);
		if (out == null)
			LOG.info("Language key " + key + " was not parseable: " + value);
		return out;
	}
	
	/**
	 * Gets text wrapped in HTML tags using a text key.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the desired text, or a macro if it is not found. 
	 */
	public String getHTML(String key, Object ... args)
	{
		return "<html>" + getText(key, args) + "<html>";
	}
	
}
