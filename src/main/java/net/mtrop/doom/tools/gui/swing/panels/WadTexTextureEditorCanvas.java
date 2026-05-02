package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.GraphicUtils;

/**
 * The main canvas for the texture editor.
 */
public class WadTexTextureEditorCanvas extends Canvas
{
	private static final long serialVersionUID = -7510942462707150155L;
	
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
	
	private static final Composite PATCH_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f);
	
	private float zoomFactor;
	private Palette palette;

	private Dimension textureDimensions;
	private PatchListModel patchListModel;

	private VolatileImage canvasImage;
	private BufferedImage backgroundImage;
	
	public WadTexTextureEditorCanvas()
	{
		this.zoomFactor = 1.0f;
		this.palette = BLACK_PALETTE;

		this.textureDimensions = new Dimension(128, 128);
		this.patchListModel = new PatchListModel();

		this.backgroundImage = null;
		
		rebuildImages();
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
	 * Sets the current texture dimensions.
	 * @param textureDimensions the new dimensions.
	 */
	public void setTextureDimensions(Dimension textureDimensions) 
	{
		this.textureDimensions = textureDimensions;
		repaint();
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
		rebuildImages();
		repaint();
	}
	
	/**
	 * @return the list model for the patches in this texture canvas.
	 */
	public PatchListModel getPatchListModel() 
	{
		return patchListModel;
	}
	
	private void rebuildImages()
	{
		for (int i = 0; i < patchListModel.getSize(); i++)
			patchListModel.getElementAt(i).rebuildImage();
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
		if (canvasImage != null)
		{
			Graphics2D canvasGraphics = canvasImage.createGraphics();
			
			// force nearest rendering mode.
			ImageUtils.ResamplingType.NEAREST.setHints(canvasGraphics);
			
			drawBackground(canvasGraphics);
			drawTexture(canvasGraphics);
			drawGuides(canvasGraphics);
			
			canvasGraphics.dispose();

			graphics.drawImage(canvasImage, 0, 0, null);
		}
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
		
		// start at midpoint, then draw texture rectangle.
		int originX = (int)(getWidth() / 2 - (textureDimensions.width * zoomFactor));
		int originY = (int)(getHeight() / 2 - (textureDimensions.height * zoomFactor));
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(originX, originY, (int)(textureDimensions.width * zoomFactor), (int)(textureDimensions.height * zoomFactor));
		g2d.drawRect(originX - 1, originY - 1, (int)(textureDimensions.width * zoomFactor) + 2, (int)(textureDimensions.height * zoomFactor) + 2);
		
		g2d.setColor(prevColor);
	}

	protected void drawTexture(Graphics2D g2d)
	{
		Composite prevComposite = g2d.getComposite();
		g2d.setComposite(PATCH_COMPOSITE);

		for (int i = 0; i < patchListModel.getSize(); i++)
		{
			PatchGraphic pg = patchListModel.getElementAt(i);
			
			if (zoomFactor > 0)
			{
				// start top-left corner at HUD edge.
				int originX = (int)(getWidth() / 2 - (textureDimensions.width / 2 * zoomFactor));
				int originY = (int)(getHeight() / 2 - (textureDimensions.height / 2 * zoomFactor));
				
				originX += pg.offsetX * zoomFactor;
				originY += pg.offsetY * zoomFactor;

				g2d.drawImage(pg.renderedImage, 
					originX, originY, 
					(int)(pg.renderedImage.getWidth(null) * zoomFactor), 
					(int)(pg.renderedImage.getHeight(null) * zoomFactor), 
					null
				);
			}
		}
		
		g2d.setComposite(prevComposite);
	}

	/**
	 * A single patch graphic.
	 */
	public class PatchGraphic
	{
		private String name;
		
		private Image renderedImage;
		
		private Picture picture;
		private PNGPicture pngPicture;
		
		private int offsetX;
		private int offsetY;
		
		public PatchGraphic(String name)
		{
			this.name = name;
			
			this.renderedImage = null;
			
			this.picture = null;
			this.pngPicture = null;
			
			this.offsetX = 0;
			this.offsetY = 0;
		}
		
		/**
		 * Sets the patch's picture (unsets PNGPicture).
		 * @param p the picture.
		 */
		public void setPicture(Picture p)
		{
			this.picture = p;
			this.pngPicture = null;
			rebuildImage();
		}
		
		/**
		 * Sets the patch's PNG picture (unsets Picture).
		 * @param p the picture.
		 */
		public void setPNGPicture(PNGPicture p)
		{
			this.picture = null;
			this.pngPicture = p;
			rebuildImage();
		}
		
		/**
		 * Sets this patch's offsets.
		 * @param x the x-coordinate.
		 * @param y the y-coordinate.
		 */
		public void setOffsets(int x, int y)
		{
			this.offsetX = x;
			this.offsetY = y;
		}
		
		/**
		 * Translate's this patch's offsets.
		 * @param x the x-coordinate.
		 * @param y the y-coordinate.
		 */
		public void translate(int x, int y)
		{
			this.offsetX += x;
			this.offsetY += y;
		}
		
		@Override
		public String toString() 
		{
			return name;
		}
		
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
			}
		}
		
	}

	/**
	 * The patch list model for the patches in this canvas.
	 */
	public static class PatchListModel implements ListModel<WadTexTextureEditorCanvas.PatchGraphic>
	{
		private List<PatchGraphic> patches;
		private final List<ListDataListener> listeners;
	
		public PatchListModel() 
		{
			this.patches = Collections.synchronizedList(new ArrayList<PatchGraphic>(4));
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}
		
		@Override
		public int getSize()
		{
			return patches.size();
		}
	
		@Override
		public PatchGraphic getElementAt(int index)
		{
			return patches.get(index);
		}
	
		/**
		 * Adds a patch to this texture canvas.
		 * @param pg the patch graphic to add.
		 */
		public void addPatch(PatchGraphic pg)
		{
			int index = patches.size();
			patches.add(pg);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}
	
		/**
		 * Adds a patch to this texture canvas.
		 * @param index the position to add the patch to.
		 * @param pg the patch graphic to add.
		 */
		public void addPatch(int index, PatchGraphic pg)
		{
			patches.add(index, pg);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}
	
		/**
		 * Removes a patch from this texture canvas.
		 * @param pg the patch graphic to remove.
		 * @return true of removed, false if not.
		 */
		public boolean removePatch(PatchGraphic pg)
		{
			int index = patches.indexOf(pg);
			if (patches.remove(pg))
			{
				listeners.forEach((listener) -> listener.intervalRemoved(
					new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)
				));
				return true;
			}
			return false;
		}
	
		/**
		 * Removes a patch from this texture canvas.
		 * @param index the index of the patch to remove.
		 * @return true of removed, false if not.
		 */
		public PatchGraphic removePatch(int index)
		{
			PatchGraphic out;
			out = patches.remove(index);
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)
			));
			return out;
		}
	
		/**
		 * Removes all patches from the model.
		 */
		public void clearPatches()
		{
			int amount = patches.size();
			patches.clear();
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, amount - 1)
			));
		}
		
		/**
		 * Gets a patch from this texture canvas.
		 * @param index the index of the patch to get.
		 * @return the corresponding patch.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public PatchGraphic getPatch(int index)
		{
			return patches.get(index);
		}
	
		/**
		 * Shifts a patch index up one index.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftUp(int index)
		{
			if (index > 0)
			{
				addPatch(index - 1, removePatch(index));
			}
		}
	
		/**
		 * Shifts a patch index up to the beginning of the list.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftToBack(int index)
		{
			if (index > 0)
			{
				addPatch(0, removePatch(index));
			}
		}
	
		/**
		 * Shifts a patch index down one index.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftDown(int index)
		{
			if (index < patches.size() - 1)
			{
				if (index + 1 == patches.size())
					addPatch(removePatch(index));
				else
					addPatch(index + 1, removePatch(index));
			}
		}
	
		/**
		 * Shifts a patch index to the end of the list.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftToFront(int index)
		{
			if (index < patches.size() - 1)
			{
				addPatch(removePatch(index));
			}
		}
	
		@Override
		public void addListDataListener(ListDataListener l) 
		{
			listeners.add(l);
		}
		
		@Override
		public void removeListDataListener(ListDataListener l) 
		{
			listeners.remove(l);
		}
	
	}
	
}
