/*******************************************************************************
 * Copyright (c) 2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JList;

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
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
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

	private final DoomToolsIconManager icons;
	private final WadTexTextureEditorSettingsManager settings;
	
	private File projectDirectory;

	private JFormField<File> paletteField;
	private JFormField<File> paletteSourceField;
	private JFormField<Double> zoomFactorField;

	private WadTexTextureEditorCanvas canvas;
	private JList<CommonTexture<CommonPatch>> textureList;
	private JList<PatchGraphic> patchGraphicList;
	private PatchListModel patchGraphicListModel;  
	
	public WadTexTextureEditorApp(File projectDirectory, String baseIwadPath, String paletteWadPath, String ... textureFilePaths)
	{
		this();
		
		this.projectDirectory = projectDirectory;
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
	

	public void onSwitchToTexture(CommonTexture<CommonPatch> texture)
	{
		// TODO: Finish this.
	}

	public void onExportTextures()
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
		patchGraphicListModel.addPatch(newPatch);
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
		patchGraphicListModel.addPatch(newPatch);
		return newPatch;
	}

}
