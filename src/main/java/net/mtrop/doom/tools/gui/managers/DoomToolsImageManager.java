package net.mtrop.doom.tools.gui.managers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.mtrop.doom.tools.struct.Loader;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomTools GUI image loader singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsImageManager 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsImageManager.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsImageManager> INSTANCE = new SingletonProvider<>(() -> new DoomToolsImageManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsImageManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private Loader<BufferedImage> imageLoader;
	
	private DoomToolsImageManager()
	{
		this.imageLoader = new Loader<>(Loader.createResourceLoader("gui/images/", (path, in) -> {
			try {
				BufferedImage out = ImageIO.read(in);
				LOG.debugf("Loaded image: %s", path);
				return out;
			} catch (IOException e) {
				throw new RuntimeException("Could not load expected resource: " + path);
			}
		}));
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the image to load.
	 * @return the image.
	 */
	public BufferedImage getImage(String name)
	{
		return imageLoader.getObject(name);
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the image to load.
	 * @return a future to get the image from.
	 */
	public LoaderFuture<BufferedImage> getImageAsync(String name)
	{
		return imageLoader.getObjectAsync(name);
	}
	
}
