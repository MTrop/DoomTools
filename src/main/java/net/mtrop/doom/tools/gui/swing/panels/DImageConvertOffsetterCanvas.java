package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.GraphicUtils;

/**
 * The main canvas for the image offsetter.
 */
public class DImageConvertOffsetterCanvas extends Canvas
{
	private static final long serialVersionUID = 7705527681845540943L;
	
	/** First fill color for background. */
	private static final Color FILL_COLOR_0 = new Color(64, 64, 80, 255);
	/** Second fill color for background. */
	private static final Color FILL_COLOR_1 = new Color(80, 80, 96, 255);
	
	/** Placeholder palette if none used. */
	private static final Palette BLACK_PALETTE = new Palette();
	/** Blank renedered image. */
	private static final Image BLANK_IMAGE = ObjectUtils.apply(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), (image) -> 
	{
		image.setRGB(0, 0, 0xff0ff000);
	});
	/** Fill image for background. */
	private static final Image FILL_IMAGE = ObjectUtils.apply(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), (image) -> 
	{
		int hw = image.getWidth() / 2;
		int hh = image.getHeight() / 2;
		
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(FILL_COLOR_0);
		g2d.fillRect( 0,  0, hw, hh);
		g2d.fillRect(hw, hh, hw, hh);
		g2d.setColor(FILL_COLOR_1);
		g2d.fillRect( 0, hh, hw, hh);
		g2d.fillRect(hw,  0, hw, hh);
		g2d.dispose();
	});
	
	public enum GuideMode
	{
		SPRITE,
		HUD;
	}
	
	/** The image buffer to write to during the current frame. */
	private VolatileImage writableBuffer;
	/** Graphics context. */
	private Graphics2D writableGraphics;

	private int zoomFactor;
	private Palette palette;
	private Picture picture;
	private PNGPicture pngPicture;
	private Point currentOffset;
	private GuideMode guideMode;
	
	private Image renderedImage;
	
	public DImageConvertOffsetterCanvas()
	{
		this.zoomFactor = 1;
		this.palette = BLACK_PALETTE;
		this.picture = null;
		this.pngPicture = null;
		this.currentOffset = new Point();
		this.guideMode = GuideMode.SPRITE;
		rebuildImage();
	}
	
	/**
	 * Sets this canvas's zoom factor.
	 * @param zoomFactor the new zoom factor.
	 */
	public void setZoomFactor(int zoomFactor) 
	{
		this.zoomFactor = zoomFactor;
		repaint();
	}
	
	/**
	 * @return this canvas's zoom factor.
	 */
	public int getZoomFactor() 
	{
		return zoomFactor;
	}
	
	/**
	 * Sets the palette to use when rendering a Picture.
	 * @param p the new palette.
	 */
	public void setPalette(Palette p)
	{
		if (p == null)
			palette = BLACK_PALETTE;
		else
			palette = p;
		rebuildImage();
		repaint();
	}
	
	/**
	 * Sets the picture to use.
	 * If this is set, then the current PNG Picture is cleared.
	 * @param p the picture to set.
	 */
	public void setPicture(Picture p)
	{
		picture = p;
		pngPicture = null;
		rebuildImage();
		repaint();
	}
	
	/**
	 * Sets the picture to use.
	 * If this is set, then the current PNG Picture is cleared.
	 * @param p the picture to set.
	 */
	public void setPNGPicture(PNGPicture p)
	{
		pngPicture = p;
		picture = null;
		rebuildImage();
		repaint();
	}
	
	/**
	 * Sets the current offsets for the image.
	 * @param x the x offset.
	 * @param y the y offset.
	 */
	public void setOffsets(int x, int y)
	{
		currentOffset.setLocation(x, y);
		repaint();
	}
	
	/**
	 * Translates from the current offsets for the image.
	 * @param x the x offset movement.
	 * @param y the y offset movement.
	 */
	public void translate(int x, int y)
	{
		currentOffset.translate(x, y);
		repaint();
	}
	
	/**
	 * Sets the current guide mode.
	 * @param guideMode the guide mode.
	 */
	public void setGuideMode(GuideMode guideMode) 
	{
		this.guideMode = guideMode;
		repaint();
	}
	
	// Rebuilds the renderable image and starting offsets.
	private void rebuildImage()
	{
		if (picture != null)
		{
			renderedImage = GraphicUtils.createImage(picture, palette);
			currentOffset.x = picture.getOffsetX();
			currentOffset.y = picture.getOffsetY();
		}
		else if (pngPicture != null)
		{
			renderedImage = pngPicture.getImage();
			currentOffset.x = pngPicture.getOffsetX();
			currentOffset.y = pngPicture.getOffsetY();
		}
		else
		{
			renderedImage = BLANK_IMAGE;
			currentOffset.x = 0;
			currentOffset.y = 0;
		}
	}
	
	@Override
	public void update(Graphics g)
	{
		// do not clear rect - just paint over.
		paint(g);
	}
	
	@Override
	public void paint(Graphics g) 
	{
		if (writableGraphics != null)
		{
			writableGraphics.dispose();
			writableGraphics = null;
		}
		
		// 2D accelerated buffer
		writableBuffer = recreateVolatileImage(writableBuffer, getWidth(), getHeight(), Transparency.TRANSLUCENT);
		
		Graphics2D g2d = writableBuffer.createGraphics();
		
		// force nearest rendering mode.
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
		AffineTransform prevTransform = g2d.getTransform();
		
		drawBackground(g2d);
		drawGuides(g2d);
		drawImage(g2d);
		
		g2d.setTransform(prevTransform);
		
		Graphics2D thisCanvasGraphics = (Graphics2D)g;
		thisCanvasGraphics.drawImage(writableBuffer, 0, 0, getWidth(), getHeight(), null);
		g2d.dispose();
	}
	
	protected void drawBackground(Graphics2D g2d)
	{
		int canvasWidth = getWidth();
		int canvasHeight = getHeight();
		
		for (int y = 0; y < canvasHeight; y += FILL_IMAGE.getHeight(null))
			for (int x = 0; x < canvasWidth; x += FILL_IMAGE.getWidth(null))
				g2d.drawImage(FILL_IMAGE, x, y, null);
	}

	protected void drawGuides(Graphics2D g2d)
	{
		Color prevColor = g2d.getColor();
		
		switch (guideMode)
		{
			case HUD:
			{
				// start at midpoint, then draw HUD rectangle.
				int originX = getWidth() / 2 - (160 * zoomFactor);
				int originY = getHeight() / 2 - (100 * zoomFactor);
				
				g2d.setColor(Color.BLACK);
				g2d.drawRect(originX, originY, 320 * zoomFactor, 200 * zoomFactor);
			}
			// fall through, draw midpoint guides.
			
			default:
			case SPRITE:
			{
				int hw = getWidth() / 2;
				int hh = getHeight() / 2;
				
				g2d.setColor(Color.BLACK);
				g2d.drawLine(hw, 0, hw, getHeight());
				g2d.drawLine(0, hh, getWidth(), hh);
			}
			break;
		}
		
		g2d.setColor(prevColor);
	}

	protected void drawImage(Graphics g2d)
	{
		switch (guideMode)
		{
			case HUD:
			{
				// start top-left corner at HUD edge.
				int originX = getWidth() / 2 - (160 * zoomFactor);
				int originY = getHeight() / 2 - (100 * zoomFactor);
				
				originX -= currentOffset.x * zoomFactor;
				originY -= currentOffset.y * zoomFactor;

				if (zoomFactor > 0)
				{
					g2d.drawImage(renderedImage, 
						originX, originY, 
						renderedImage.getWidth(null) * zoomFactor, 
						renderedImage.getHeight(null) * zoomFactor, 
						null
					);
				}
			}
			break;
			
			default:
			case SPRITE:
			{
				// start top-left corner at midpoint.
				int originX = getWidth() / 2;
				int originY = getHeight() / 2;
				
				originX -= currentOffset.x * zoomFactor;
				originY -= currentOffset.y * zoomFactor;

				if (zoomFactor > 0)
				{
					g2d.drawImage(renderedImage, 
						originX, originY, 
						renderedImage.getWidth(null) * zoomFactor, 
						renderedImage.getHeight(null) * zoomFactor, 
						null
					);
				}
			}
			break;
		}
	}
	
	/**
	 * Creates/Recreates a new volatile image.
	 * A new VolatileImage, compatible with the current local graphics environment is returned
	 * if the current image passed into the method is invalid or incompatible. The image is deallocated
	 * if it is incompatible. If it is compatible or has different dimensions, nothing is reallocated, and
	 * it is returned.
	 * @param currentImage the previous VolatileImage to validate.
	 * @param width the width of the new image.
	 * @param height the height of the new image.
	 * @param transparency the transparency mode (from {@link Transparency}).
	 * @return a new or same volatile image. if null, can't be created for some reason.
	 */
	private static VolatileImage recreateVolatileImage(VolatileImage currentImage, int width, int height, int transparency)
	{
		GraphicsConfiguration gconfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		if (!revalidate(gconfig, currentImage, width, height))
		{
			if (currentImage != null)
				currentImage.flush();
			currentImage = null; // expedite Garbage Collection maybe why not
			
			if (width > 0 && height > 0)
				currentImage = gconfig.createCompatibleVolatileImage(width, height, transparency);
		}
		
		return currentImage;
	}
	
	// Revalidates a VolatileImage.
	private static boolean revalidate(GraphicsConfiguration gc, VolatileImage currentImage, int width, int height)
	{
		if (currentImage == null)
			return false;
		
		int valid = currentImage.validate(gc);
		
		switch (valid)
		{
			default:
			case VolatileImage.IMAGE_INCOMPATIBLE:
				return false;
			case VolatileImage.IMAGE_RESTORED:
			case VolatileImage.IMAGE_OK:
				return currentImage.getWidth() == width && currentImage.getHeight() == height;
		}
	}
}
