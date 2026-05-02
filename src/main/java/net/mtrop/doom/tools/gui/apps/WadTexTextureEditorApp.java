/*******************************************************************************
 * Copyright (c) 2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.texture.CommonPatch;
import net.mtrop.doom.texture.CommonTexture;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexTextureEditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchGraphic;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchListModel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.fileField;
import static net.mtrop.doom.tools.struct.swing.FormFactory.spinnerField;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;

public class WadTexTextureEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(WadTexTextureEditorApp.class); 

	private static final byte[] PNG_SIGNATURE = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

	private static final int ZOOMFACTOR_MIN = 1;
	private static final int ZOOMFACTOR_MAX = 8;
	private static final double ZOOMFACTOR_STEP = .5;

	private static final BufferedImage NO_PATCH = ImageUtils.imageBuilder(32, 32, BufferedImage.TYPE_INT_ARGB, (image) -> {
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.MAGENTA);
		g2d.fillRect(0, 0, 16, 16);
		g2d.fillRect(16, 16, 16, 16);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 16, 16, 16);
		g2d.fillRect(16, 0, 16, 16);
		g2d.dispose();
	});
	
	private final DoomToolsIconManager icons;
	private final WadTexTextureEditorSettingsManager settings;
	
	private File projectDirectory;

	private JFormField<Double> zoomFactorField;
	private JFormField<File> iwadSourceField;
	private JFormField<File> paletteSourceField;

	private WadTexTextureEditorCanvas canvas;
	private PatchListModel patchListModel;
	private JList<PatchGraphic> patchList;
	private TextureListModel textureListModel;
	private JList<CommonTexture<CommonPatch>> textureList;
	
	public WadTexTextureEditorApp(File projectDirectory, String baseIwadPath, String paletteWadPath, String ... textureFilePaths)
	{
		this();
		
		this.projectDirectory = projectDirectory;
		
		this.zoomFactorField = spinnerField(spinner(spinnerModel(ZOOMFACTOR_MIN, ZOOMFACTOR_MIN, ZOOMFACTOR_MAX, ZOOMFACTOR_STEP), (c) -> onZoomFactorChanged((Double)c.getValue())));
		
		this.iwadSourceField = fileField(settings.getLastPaletteFile(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("dimgconv.offsetter.palette.source.browse.title"), 
				language.getText("dimgconv.offsetter.palette.source.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createWADFileFilter()
			), 
			this::onIWADFileSelect
		);
		onIWADFileSelect(paletteSourceField.getValue());
	
		this.paletteSourceField = fileField(settings.getLastPaletteFile(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("dimgconv.offsetter.palette.source.browse.title"), 
				language.getText("dimgconv.offsetter.palette.source.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createWADFileFilter()
			), 
			this::onPaletteFileSelect
		);
		onPaletteFileSelect(paletteSourceField.getValue());
	
		this.canvas = new WadTexTextureEditorCanvas();
		
		this.patchListModel = canvas.getPatchListModel();
		this.patchList = list(patchListModel, ListSelectionMode.SINGLE, (list, b) -> {
			PatchGraphic pg = list.isEmpty() ? null : list.get(0);
			onSwitchToPatch(pg);	
		});
		
		this.textureListModel = new TextureListModel();
		this.textureList = list(textureListModel, ListSelectionMode.SINGLE, (list, b) -> {
			CommonTexture<CommonPatch> tex = list.isEmpty() ? null : list.get(0);
			onSwitchToTexture(tex);	
		});
		
		// TODO: Finish this.
	}

	public WadTexTextureEditorApp()
	{
		this.icons = DoomToolsIconManager.get();
		this.settings = WadTexTextureEditorSettingsManager.get();
		
		// TODO: Finish this.
	}

	public static WadTexTextureEditorApp openAndCreate(Component parent)
	{
		// TODO: Finish this.
		return null;
	}

	@Override
	public String getTitle()
	{
		return language.getText("wadtex.texture.title");
	}

	@Override
	public Container createContentPane()
	{
		// TODO: Finish this.
		return containerOf();
	}
	
	public void onExportTextures()
	{
		// TODO: Finish this.
	}

	private File fetchPatchFileForName(String name)
	{
		Map<String, File> fileMap = new HashMap<>();

		// try src/convert/patches
		File convertPatchDir = new File(projectDirectory.getPath() + "/src/convert/patches/");
		File[] fileList = convertPatchDir.listFiles();
		if (fileList != null)
		{
			for (File f : fileList)
				fileMap.put(FileUtils.getFilePathWithoutExtension(f), f);

			File file = fileMap.get(projectDirectory.getPath() + "/src/convert/patches/" + name);
			if (file != null)
				return file;
		}
		
		// try src/textures/patches
		File texturePatchDir = new File(projectDirectory.getPath() + "/src/textures/patches/");
		fileList = texturePatchDir.listFiles();
		if (fileList != null)
		{
			for (File f : fileList)
				fileMap.put(FileUtils.getFilePathWithoutExtension(f), f);

			File file = fileMap.get(projectDirectory.getPath() + "/src/textures/patches/" + name);
			if (file != null)
				return file;
		}
		
		return null;
	}
	
	private Picture fetchPatchForName(String name)
	{
		// search IWAD
		File iwad = iwadSourceField.getValue();
		if (iwad == null || !iwad.exists())
			return null;
		
		try (WadFile wad = new WadFile(iwad))
		{
			int pidx = wad.indexOf("P_START");
			if (pidx < 0)
				pidx = wad.indexOf("PP_START");
			if (pidx < 0)
				return null;
			return wad.getDataAs(name, pidx, Picture.class);
		} 
		catch (IOException e)
		{
			return null;
		}
	}
	
	private boolean addFoundPatch(String name, Picture picture)
	{
		createPatch(name.toUpperCase(), picture);
		return true;
	}
	
	private boolean addFoundPatchFile(File selected)
	{
		try {
			if (FileUtils.matchMagicNumber(selected, PNG_SIGNATURE)) // png?
			{
				PNGPicture p = BinaryObject.read(PNGPicture.class, selected);
				createPatch(FileUtils.getFileNameWithoutExtension(selected).toUpperCase(), p);
				return true;
			}
			else // other?
			{
				BufferedImage image;
				PNGPicture png = new PNGPicture();
				try {
					image = ImageIO.read(selected);
					png.setImage(image);
					createPatch(FileUtils.getFileNameWithoutExtension(selected).toUpperCase(), png);
					return true;
				} catch (IOException e) {
					image = null;
				}
				
				int w, h, ox, oy;
				try (FileInputStream fis = new FileInputStream(selected))
				{
					SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
					w = sr.readShort(fis);
					h = sr.readShort(fis);
					ox = sr.readShort(fis);
					oy = sr.readShort(fis);
				}

				// test if acceptable or reasonable bounds
				if (checkPictureBounds(w, h, ox, oy))
				{
					Picture p = BinaryObject.read(Picture.class, selected);
					createPatch(FileUtils.getFileNameWithoutExtension(selected).toUpperCase(), p);
					return true;
				}
				else
				{
					createPatch(FileUtils.getFileNameWithoutExtension(selected).toUpperCase(), NO_PATCH);
					return false;
				}
			}
		} catch (IOException e) {
			return false;
		}
	}

	private boolean checkPictureBounds(int w, int h, int ox, int oy)
	{
		return w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024;
	}
	
	private void onSwitchToPatch(PatchGraphic patch)
	{
		// TODO: Finish this.
	}

	private void onSwitchToTexture(CommonTexture<CommonPatch> texture)
	{
		// TODO: Finish this.
	}

	private void onZoomFactorChanged(double zoomFactor)
	{
		canvas.setZoomFactor((float)zoomFactor);
	}
	
	private void onPaletteFileSelect(File value)
	{
		// TODO: Finish this.
	}

	private void onIWADFileSelect(File value)
	{
		// TODO: Finish this.
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param name the patch name.
	 * @param p the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(String name, Picture p)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(name);
		newPatch.setPicture(p);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param name the patch name.
	 * @param p the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(String name, PNGPicture p)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(name);
		newPatch.setPNGPicture(p);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param name the patch name.
	 * @param image the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(String name, Image image)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(name);
		newPatch.setImage(image);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * The texture list model for the textures.
	 */
	public static class TextureListModel implements ListModel<CommonTexture<CommonPatch>>
	{
		private List<CommonTexture<CommonPatch>> textures;
		private final List<ListDataListener> listeners;
	
		public TextureListModel() 
		{
			this.textures = Collections.synchronizedList(new ArrayList<CommonTexture<CommonPatch>>(4));
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}
		
		@Override
		public int getSize()
		{
			return textures.size();
		}
	
		@Override
		public CommonTexture<CommonPatch> getElementAt(int index)
		{
			return textures.get(index);
		}
	
		/**
		 * Adds a texture to this texture canvas.
		 * @param tex the texture graphic to add.
		 */
		public void addTexture(CommonTexture<CommonPatch> tex)
		{
			int index = textures.size();
			textures.add(tex);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}
	
		/**
		 * Adds a texture to this texture canvas.
		 * @param index the position to add the texture to.
		 * @param tex the texture graphic to add.
		 */
		public void addTexture(int index, CommonTexture<CommonPatch> tex)
		{
			textures.add(index, tex);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}
	
		/**
		 * Removes a texture from this texture canvas.
		 * @param tex the texture graphic to remove.
		 * @return true of removed, false if not.
		 */
		public boolean removeTexture(CommonTexture<CommonPatch> tex)
		{
			int index = textures.indexOf(tex);
			if (textures.remove(tex))
			{
				listeners.forEach((listener) -> listener.intervalRemoved(
					new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)
				));
				return true;
			}
			return false;
		}
	
		/**
		 * Removes a texture from this texture canvas.
		 * @param index the index of the texture to remove.
		 * @return true of removed, false if not.
		 */
		public CommonTexture<CommonPatch> removeTexture(int index)
		{
			CommonTexture<CommonPatch> out;
			out = textures.remove(index);
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)
			));
			return out;
		}
	
		/**
		 * Removes all textures from the model.
		 */
		public void clearTextures()
		{
			int amount = textures.size();
			textures.clear();
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, amount - 1)
			));
		}
		
		/**
		 * Gets a texture from this texture canvas.
		 * @param index the index of the texture to get.
		 * @return the corresponding texture.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public CommonTexture<?> getTexture(int index)
		{
			return textures.get(index);
		}
	
		/**
		 * Shifts a texture index up one index.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftUp(int index)
		{
			if (index > 0)
			{
				addTexture(index - 1, removeTexture(index));
			}
		}
	
		/**
		 * Shifts a texture index down one index.
		 * @param index the index to shift.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shiftDown(int index)
		{
			if (index < textures.size() - 1)
			{
				if (index + 1 == textures.size())
					addTexture(removeTexture(index));
				else
					addTexture(index + 1, removeTexture(index));
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
