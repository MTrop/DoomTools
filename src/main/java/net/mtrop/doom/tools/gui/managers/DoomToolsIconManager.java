package net.mtrop.doom.tools.gui.managers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;

import net.mtrop.doom.tools.struct.Loader;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * DoomMake GUI icon loader singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsIconManager 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsIconManager.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsIconManager> INSTANCE = new SingletonProvider<>(() -> new DoomToolsIconManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsIconManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private Loader<ImageIcon> iconLoader;
	
	private DoomToolsIconManager()
	{
		this.iconLoader = new Loader<>(Loader.createResourceLoader("gui/images/", (path, in) -> {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.relay(in, bos);
				LOG.debugf("Loaded icon: %s", path);
				return new ImageIcon(bos.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException("Could not load expected resource: " + path);
			}
		}));
	}
	
	/**
	 * Gets an icon by name.
	 * @param name the name of the icon to load.
	 * @return the icon.
	 */
	public ImageIcon getImage(String name)
	{
		return iconLoader.getObject(name);
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the icon to load.
	 * @return a future to get the icon from.
	 */
	public LoaderFuture<ImageIcon> getImageAsync(String name)
	{
		return iconLoader.getObjectAsync(name);
	}
	
}
