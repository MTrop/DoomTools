package net.mtrop.doom.tools.doommake.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.mtrop.doom.tools.struct.Loader;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomMake GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeImageManager 
{
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomMakeImageManager> INSTANCE = new SingletonProvider<>(() -> new DoomMakeImageManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeImageManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private Loader<BufferedImage> imageLoader;
	
	private DoomMakeImageManager()
	{
		this.imageLoader = new Loader<>(Loader.createResourceLoader("doommake/gui/images/", (in) -> {
			try {
				return ImageIO.read(in);
			} catch (IOException e) {
				throw new RuntimeException("Could not load expected resource.");
			}
		}));
	}
	
	/**
	 * Gets an image by name.
	 * @param name the name of the image to load.
	 * @return a future to get the image from.
	 */
	public LoaderFuture<BufferedImage> getImage(String name)
	{
		return imageLoader.getObjectAsync(name);
	}
	
}
