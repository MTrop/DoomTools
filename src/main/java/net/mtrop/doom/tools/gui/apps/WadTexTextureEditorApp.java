/*******************************************************************************
 * Copyright (c) 2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
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
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.ListModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexTextureEditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchGraphic;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchListModel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.LayoutFactory.Flow;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
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
	private Properties projectProperties;

	private JFormField<Double> zoomFactorField;
	private JFormField<File> iwadSourceField;
	private JFormField<File> paletteSourceField;

	private WadTexTextureEditorCanvas canvas;
	private PatchListModel patchListModel;
	private JList<PatchGraphic> patchList;
	private TextureListModel textureListModel;
	private JList<TextureSet.Texture> textureList;

	private JLabel textureInfoLabel;
	
	private Action textureAddAction;
	private Action textureRemoveAction;
	private Action textureMoveUpAction;
	private Action textureMoveDownAction;
	private Action patchAddAction;
	private Action patchRemoveAction;
	private Action patchMoveUpAction;
	private Action patchMoveDownAction;
	private Action patchMoveToFrontAction;
	private Action patchMoveToBackAction;

	private File currentTextureFile;
	
	public WadTexTextureEditorApp(File projectDirectory, File baseIwadPath, File paletteWadPath)
	{
		this();
		
		this.projectDirectory = projectDirectory;
		try {
			this.projectProperties = DoomMakeMain.createProjectProperties(projectDirectory);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize texture editor app.", e);
		}
		this.iwadSourceField.setValue(baseIwadPath != null ? baseIwadPath : settings.getLastOpenedWAD());
		this.paletteSourceField.setValue(paletteWadPath != null ? paletteWadPath : settings.getLastPaletteFile());
	}

	public WadTexTextureEditorApp()
	{
		this.icons = DoomToolsIconManager.get();
		this.settings = WadTexTextureEditorSettingsManager.get();
		
		this.zoomFactorField = spinnerField(spinner(spinnerModel(ZOOMFACTOR_MIN, ZOOMFACTOR_MIN, ZOOMFACTOR_MAX, ZOOMFACTOR_STEP), (c) -> onZoomFactorChanged((Double)c.getValue())));
		
		this.iwadSourceField = fileField(settings.getLastOpenedWAD(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.iwad.source.browse.title"), 
				language.getText("wadtex.texture.editor.iwad.source.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createWADFileFilter()
			), 
			this::onIWADFileSelect
		);
		onIWADFileSelect(iwadSourceField.getValue());
	
		this.paletteSourceField = fileField(settings.getLastPaletteFile(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.palette.source.browse.title"), 
				language.getText("wadtex.texture.editor.palette.source.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createAllFilesFilter()
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
		this.textureList = list(textureListModel, ListSelectionMode.MULTIPLE_INTERVAL, (list, b) -> {
			TextureSet.Texture tex = list.isEmpty() || list.size() > 1 ? null : list.get(0);
			onSwitchToTexture(tex);	
		});
		
		this.textureInfoLabel = label("");
		
		this.textureAddAction = actionItem(icons.getImage("add.png"), (a) -> onTextureAdd());
		this.textureRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onTextureRemove());
		this.textureMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onTextureMoveUp());
		this.textureMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onTextureMoveDown());
		this.patchAddAction = actionItem(icons.getImage("add.png"), (a) -> onPatchAdd());
		this.patchRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onPatchRemove());
		this.patchMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onPatchMoveUp());
		this.patchMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onPatchMoveDown());
		this.patchMoveToFrontAction = actionItem(icons.getImage("foreground.png"), (a) -> onPatchMoveToFront());
		this.patchMoveToBackAction = actionItem(icons.getImage("background.png"), (a) -> onPatchMoveToBack());
		
		this.currentTextureFile = null;
	}

	public static WadTexTextureEditorApp openAndCreate(Component parent)
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get();
		WadTexTextureEditorSettingsManager settings = WadTexTextureEditorSettingsManager.get();
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
		File projectDir = utils.chooseDirectory(
			parent,
			language.getText("wadtex.texture.editor.open.directory.title"),
			language.getText("wadtex.texture.editor.open.directory.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile
		);
		
		if (projectDir == null)
			return null;
		
		if (!projectDir.isDirectory())
			return null;

		if (!DoomMakeOpenProjectApp.isProjectDirectory(projectDir))
		{
			SwingUtils.error(language.getText("wadtex.texture.editor.open.directory.error", projectDir.getPath()));
			return null;
		}
		
		return new WadTexTextureEditorApp(projectDir, null, null);
	}

	@Override
	public String getTitle()
	{
		return language.getText("wadtex.texture.editor.title");
	}

	@Override
	public Container createContentPane()
	{
		Container textureListPanel = containerOf(borderLayout(4, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
				node(BorderLayout.WEST, label(language.getText("wadtex.texture.editor.list.textures"))),
				node(BorderLayout.CENTER, containerOf(flowLayout(Flow.LEFT, 4, 0),
					node(button(textureAddAction)),
					node(button(textureRemoveAction)),
					node(button(textureMoveUpAction)),
					node(button(textureMoveDownAction))
				))
			)),
			node(BorderLayout.CENTER, containerOf(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
				node(textureList)
			))
		);
		
		Container patchListPanel = containerOf(borderLayout(4, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
				node(BorderLayout.WEST, label(language.getText("wadtex.texture.editor.list.patches"))),
				node(BorderLayout.CENTER, containerOf(flowLayout(Flow.LEFT, 4, 0),
					node(button(patchAddAction)),
					node(button(patchRemoveAction)),
					node(button(patchMoveUpAction)),
					node(button(patchMoveDownAction)),
					node(button(patchMoveToFrontAction)),
					node(button(patchMoveToBackAction))
				))
			)),
			node(BorderLayout.CENTER, containerOf(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
				node(patchList)
			))
		);
		
		return containerOf(dimension(820, 560), borderLayout(4, 4),
			node(BorderLayout.WEST, containerOf(borderLayout(4, 4),
				node(BorderLayout.WEST, textureListPanel),
				node(BorderLayout.EAST, patchListPanel)
			)),
			node(BorderLayout.CENTER, containerOf(borderLayout(4, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
					node(BorderLayout.WEST, containerOf(flowLayout(Flow.LEADING, 4, 0),
						node(label(language.getText("wadtex.texture.editor.zoom"))),
						node(zoomFactorField)
					)),
					node(BorderLayout.CENTER, containerOf(gridLayout(1, 2, 4, 4),
						node(containerOf(borderLayout(4, 4),
							node(BorderLayout.LINE_START, label(language.getText("wadtex.texture.editor.iwad"))),
							node(BorderLayout.CENTER, iwadSourceField)
						)),
						node(containerOf(borderLayout(4, 4),
							node(BorderLayout.LINE_START, label(language.getText("wadtex.texture.editor.palette.source"))),
							node(BorderLayout.CENTER, paletteSourceField)
						))
					))
				)),
				node(BorderLayout.CENTER, canvas)
			)),
			node(BorderLayout.SOUTH, textureInfoLabel)
		);
	}
	
	@Override
	public JMenuBar createDesktopMenuBar()
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadtex.texture.editor.menu.file",
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.new", (i) -> onNewTextureFile()),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.open", (i) -> onOpenTextureFile()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.save", (i) -> onSaveTextureFile()),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.saveas", (i) -> onSaveTextureFileAs()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.exit", (i) -> attemptClose())
			)
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadtex.texture.editor.menu.file",
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.new", (i) -> onNewTextureFile()),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.open", (i) -> onOpenTextureFile()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.save", (i) -> onSaveTextureFile()),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.saveas", (i) -> onSaveTextureFileAs())
			)
		);
	}
	
	@Override
	public void onResize(Object frame) 
	{
		canvas.repaint();
	}

	private File fetchPatchFileForName(String name)
	{
		Map<String, File> fileMap = new HashMap<>();

		File srcDir = DoomMakeMain.getProjectPropertyPath(projectDirectory, projectProperties, "doommake.dir.src", "src");
		
		// try src/convert/patches
		File convertPatchDir = new File(srcDir.getPath() + "/convert/patches");
		File[] fileList = convertPatchDir.listFiles();
		if (fileList != null)
		{
			for (File f : fileList)
				fileMap.put(FileUtils.getFilePathWithoutExtension(f), f);

			File file = fileMap.get(convertPatchDir.getPath() + "/" + name);
			if (file != null)
				return file;
		}
		
		// try src/textures/patches
		File texturePatchDir = new File(srcDir.getPath() + "/textures/patches");
		fileList = texturePatchDir.listFiles();
		if (fileList != null)
		{
			for (File f : fileList)
				fileMap.put(FileUtils.getFilePathWithoutExtension(f), f);

			File file = fileMap.get(texturePatchDir.getPath() + "/" + name);
			if (file != null)
				return file;
		}
		
		return null;
	}
	
	private Picture fetchIWADPatchForName(String name)
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
	
	private boolean addFoundPatch(TextureSet.Patch patch, Picture picture)
	{
		createPatch(patch, picture);
		return true;
	}
	
	private boolean addFoundPatchFile(TextureSet.Patch patch, File selected)
	{
		try {
			if (FileUtils.matchMagicNumber(selected, PNG_SIGNATURE)) // png?
			{
				PNGPicture p = BinaryObject.read(PNGPicture.class, selected);
				createPatch(patch, p);
				return true;
			}
			else // other?
			{
				BufferedImage image;
				PNGPicture png = new PNGPicture();
				try {
					image = ImageIO.read(selected);
					png.setImage(image);
					createPatch(patch, png);
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
					createPatch(patch, p);
					return true;
				}
				else
				{
					createPatch(patch, NO_PATCH);
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
	
	private void onNewTextureFile()
	{
		// TODO Auto-generated method stub
	}

	private void onOpenTextureFile()
	{
		// TODO Auto-generated method stub
	}

	public void onSaveTextureFile()
	{
		// TODO: Finish this.
	}

	private void onSaveTextureFileAs() 
	{
		// TODO: Finish this.
	}

	private void onSwitchToTexture(TextureSet.Texture texture)
	{
		// TODO: Finish this.
	}

	private void onSwitchToPatch(PatchGraphic patch)
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

	private void onTextureAdd()
	{
		// TODO: Finish this.
	}

	private void onTextureRemove()
	{
		// TODO: Finish this.
	}

	private void onTextureMoveUp()
	{
		// TODO: Finish this.
	}

	private void onTextureMoveDown()
	{
		// TODO: Finish this.
	}

	private void onPatchAdd()
	{
		// TODO: Finish this.
	}

	private void onPatchRemove()
	{
		// TODO: Finish this.
	}

	private void onPatchMoveUp()
	{
		// TODO: Finish this.
	}

	private void onPatchMoveDown()
	{
		// TODO: Finish this.
	}

	private void onPatchMoveToFront()
	{
		// TODO: Finish this.
	}

	private void onPatchMoveToBack()
	{
		// TODO: Finish this.
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param p the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, Picture p)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setPicture(p);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param p the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, PNGPicture p)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setPNGPicture(p);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param image the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, Image image)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setImage(image);
		patchListModel.addPatch(newPatch);
		return newPatch;
	}

	/**
	 * The texture list model for the textures.
	 */
	public static class TextureListModel implements ListModel<TextureSet.Texture>
	{
		private List<TextureSet.Texture> textures;
		private final List<ListDataListener> listeners;
	
		public TextureListModel() 
		{
			this.textures = Collections.synchronizedList(new ArrayList<TextureSet.Texture>(4));
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}
		
		@Override
		public int getSize()
		{
			return textures.size();
		}
	
		@Override
		public TextureSet.Texture getElementAt(int index)
		{
			return textures.get(index);
		}
	
		/**
		 * Adds a texture to this texture canvas.
		 * @param tex the texture graphic to add.
		 */
		public void addTexture(TextureSet.Texture tex)
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
		public void addTexture(int index, TextureSet.Texture tex)
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
		public boolean removeTexture(TextureSet.Texture tex)
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
		public TextureSet.Texture removeTexture(int index)
		{
			TextureSet.Texture out;
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
		public TextureSet.Texture getTexture(int index)
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
