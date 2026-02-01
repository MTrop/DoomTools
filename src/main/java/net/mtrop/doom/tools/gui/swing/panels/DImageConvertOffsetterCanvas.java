package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Map;

import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
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
		image.setRGB(0, 0, 0x00000000);
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
		
		public static final Map<String, GuideMode> MAP_VALUES = EnumUtils.createCaseInsensitiveNameMap(GuideMode.class);
	}
	
	private float zoomFactor;
	private Palette palette;
	private Picture picture;
	private PNGPicture pngPicture;
	private Point currentOffset;
	private Point originalOffset;
	private GuideMode guideMode;
	
	private VolatileImage canvasImage;
	private BufferedImage backgroundImage;
	private Image renderedImage;
	
	public DImageConvertOffsetterCanvas()
	{
		this.zoomFactor = 1.0f;
		this.palette = BLACK_PALETTE;
		this.picture = null;
		this.pngPicture = null;
		this.currentOffset = new Point();
		this.originalOffset = new Point();
		this.guideMode = GuideMode.SPRITE;
		
		this.backgroundImage = null;
		this.renderedImage = null;
		
		clearPicture();
		rebuildImage();
	}
	
	/**
	 * Sets this canvas's zoom factor.
	 * @param zoomFactor the new zoom factor.
	 */
	public void setZoomFactor(float zoomFactor) 
	{
		this.zoomFactor = zoomFactor;
		repaint();
	}
	
	/**
	 * @return this canvas's zoom factor.
	 */
	public float getZoomFactor() 
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
		originalOffset.x = picture.getOffsetX();
		originalOffset.y = picture.getOffsetY();
		currentOffset.x = picture.getOffsetX();
		currentOffset.y = picture.getOffsetY();
		rebuildImage();
		repaint();
	}
	
	/**
	 * @return the current Picture, if any set.
	 */
	public Picture getPicture() 
	{
		return picture;
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
		originalOffset.x = pngPicture.getOffsetX();
		originalOffset.y = pngPicture.getOffsetY();
		currentOffset.x = pngPicture.getOffsetX();
		currentOffset.y = pngPicture.getOffsetY();
		rebuildImage();
		repaint();
	}
	
	/**
	 * @return the current PNGPicture, if any set.
	 */
	public PNGPicture getPNGPicture() 
	{
		return pngPicture;
	}
	
	/**
	 * Set no picture.
	 */
	public void clearPicture()
	{
		picture = null;
		pngPicture = null;
		originalOffset.x = 0;
		originalOffset.y = 0;
		currentOffset.x = 0;
		currentOffset.y = 0;
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
	
	/**
	 * Tests if the current offsets are different than the original ones.
	 * @return true if so, false if not.
	 */
	public boolean offsetsDiffer()
	{
		return !originalOffset.equals(currentOffset);
	}
	
	// Rebuilds the renderable image and starting offsets.
	private void rebuildImage()
	{
		if (picture != null)
		{
			renderedImage = GraphicUtils.createImage(picture, palette);
		}
		else if (pngPicture != null)
		{
			renderedImage = pngPicture.getImage();
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
		Graphics2D graphics = (Graphics2D)g;

		canvasImage = ImageUtils.recreateVolatileImage(canvasImage, getWidth(), getHeight(), Transparency.OPAQUE);
		
		Graphics2D canvasGraphics = canvasImage.createGraphics();
		
		// force nearest rendering mode.
		ImageUtils.ResamplingType.NEAREST.setHints(canvasGraphics);
		
		AffineTransform prevTransform = canvasGraphics.getTransform();
		
		drawBackground(canvasGraphics);
		drawGuides(canvasGraphics);
		drawImage(canvasGraphics);
		
		canvasGraphics.setTransform(prevTransform);
		canvasGraphics.dispose();
		
		graphics.drawImage(canvasImage, 0, 0, null);
	}
	
	protected void drawBackground(Graphics2D g2d)
	{
		int canvasWidth = getWidth();
		int canvasHeight = getHeight();
		
		if (backgroundImage == null || backgroundImage.getWidth(null) != canvasWidth || backgroundImage.getHeight(null) != canvasHeight)
		{
			backgroundImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D image2D = backgroundImage.createGraphics();
			for (int y = 0; y < canvasHeight; y += FILL_IMAGE.getHeight(null))
				for (int x = 0; x < canvasWidth; x += FILL_IMAGE.getWidth(null))
					image2D.drawImage(FILL_IMAGE, x, y, null);
		}
		
		g2d.drawImage(backgroundImage, 0, 0, null);
	}

	protected void drawGuides(Graphics2D g2d)
	{
		Color prevColor = g2d.getColor();
		
		switch (guideMode)
		{
			case HUD:
			{
				// start at midpoint, then draw HUD rectangle.
				int originX = (int)(getWidth() / 2 - (160 * zoomFactor));
				int originY = (int)(getHeight() / 2 - (100 * zoomFactor));
				
				g2d.setColor(Color.BLACK);
				g2d.drawRect(originX, originY, (int)(320 * zoomFactor), (int)(200 * zoomFactor));
				g2d.drawRect(originX, originY, (int)(320 * zoomFactor), (int)((200 - 32) * zoomFactor));
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
		Color prevColor = g2d.getColor();
		
		switch (guideMode)
		{
			case HUD:
			{
				// start top-left corner at HUD edge.
				int originX = (int)(getWidth() / 2 - (160 * zoomFactor));
				int originY = (int)(getHeight() / 2 - (100 * zoomFactor));
				
				originX -= currentOffset.x * zoomFactor;
				originY -= currentOffset.y * zoomFactor;

				if (zoomFactor > 0)
				{
					g2d.setColor(Color.BLACK);
					g2d.drawRect( 
						originX, originY, 
						(int)(renderedImage.getWidth(null) * zoomFactor), 
						(int)(renderedImage.getHeight(null) * zoomFactor) 
					);
					g2d.drawImage(renderedImage, 
						originX, originY, 
						(int)(renderedImage.getWidth(null) * zoomFactor), 
						(int)(renderedImage.getHeight(null) * zoomFactor), 
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
					g2d.setColor(Color.BLACK);
					g2d.drawRect( 
						originX, originY, 
						(int)(renderedImage.getWidth(null) * zoomFactor), 
						(int)(renderedImage.getHeight(null) * zoomFactor) 
					);
					g2d.drawImage(renderedImage, 
						originX, originY, 
						(int)(renderedImage.getWidth(null) * zoomFactor), 
						(int)(renderedImage.getHeight(null) * zoomFactor), 
						null
					);
				}
			}
			break;
		}
		
		g2d.setColor(prevColor);
	}
	
}
