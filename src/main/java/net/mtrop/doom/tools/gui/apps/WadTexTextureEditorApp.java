/*******************************************************************************
 * Copyright (c) 2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.WadMap;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.ParseException;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexTextureEditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.adapters.MouseControlAdapter;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchGraphic;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchListModel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doom.tools.struct.swing.LayoutFactory.Flow;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.WadUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;

public class WadTexTextureEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(WadTexTextureEditorApp.class); 

	private static final byte[] PNG_SIGNATURE = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

	private static final String FILE_HEADER = "; File written by WadTex v" + Version.WADTEX + " by Matt Tropiano";

	private static final double ZOOMFACTOR_MIN = .5;
	private static final double ZOOMFACTOR_MAX = 8;
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
	private Map<String, File> projectConvertFiles;
	private Map<String, File> projectPatchFiles;
	private List<String> projectPatchNames;

	private JFormField<File> projectDirectoryField;
	private JFormField<Double> zoomFactorField;
	private JFormField<File> iwadSourceField;
	private JFormField<File> paletteSourceField;
	private JFormField<Short> textureWidthField;
	private JFormField<Short> textureHeightField;
	private JFormField<Short> patchXField;
	private JFormField<Short> patchYField;

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
	private Action helpAction;

	private boolean currentHasChanged;
	private TextureSet.Texture currentTexture;
	private TextureSet.Patch currentPatch;
	
	public WadTexTextureEditorApp(File projectDirectory, File baseIwadPath, File paletteWadPath)
	{
		this();
		
		this.projectDirectoryField.setValue(projectDirectory);
		this.iwadSourceField.setValue(baseIwadPath != null ? baseIwadPath : settings.getLastOpenedWAD());
		this.paletteSourceField.setValue(paletteWadPath != null ? paletteWadPath : settings.getLastPaletteFile());
	}

	public WadTexTextureEditorApp()
	{
		this.icons = DoomToolsIconManager.get();
		this.settings = WadTexTextureEditorSettingsManager.get();
		
		this.zoomFactorField = spinnerField(spinner(spinnerModel(1, ZOOMFACTOR_MIN, ZOOMFACTOR_MAX, ZOOMFACTOR_STEP), (c) -> onZoomFactorChanged((Double)c.getValue())));
		
		this.projectConvertFiles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.projectPatchFiles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.projectPatchNames = new ArrayList<>();

		this.projectDirectoryField = fileField(null, 
			(current) -> utils.chooseDirectory(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.project.source.browse.title"), 
				language.getText("wadtex.texture.editor.project.source.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile
			), 
			this::onProjectDirectorySelect
		);
	
		this.iwadSourceField = fileField(settings.getLastOpenedWAD(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.iwad.source.browse.title"), 
				language.getText("wadtex.texture.editor.iwad.source.browse.accept"),
				() -> current != null ? current : settings.getLastOpenedWAD(),
				settings::setLastOpenedWAD,
				utils.createWADFileFilter()
			), 
			this::onIWADFileSelect
		);
	
		this.paletteSourceField = fileField(settings.getLastPaletteFile(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.palette.source.browse.title"), 
				language.getText("wadtex.texture.editor.palette.source.browse.accept"),
				() -> current != null ? current : settings.getLastPaletteFile(),
				settings::setLastPaletteFile,
				utils.createAllFilesFilter()
			), 
			this::onPaletteFileSelect
		);
	
		this.textureWidthField = shortField((short)0, this::onTextureWidthChanged);
		this.textureHeightField = shortField((short)0, this::onTextureHeightChanged);
		this.textureWidthField.setEnabled(false);
		this.textureHeightField.setEnabled(false);
		this.patchXField = shortField((short)0, this::onPatchOffsetXChange);
		this.patchYField = shortField((short)0, this::onPatchOffsetYChange);
		this.patchXField.setEnabled(false);
		this.patchYField.setEnabled(false);
		
		this.canvas = new WadTexTextureEditorCanvas();
		
		this.patchListModel = canvas.getPatchListModel();
		this.patchList = list(patchListModel, listLabelRenderer((i) -> i.toString()), ListSelectionMode.SINGLE, (list, b) -> {
			PatchGraphic pg = list.isEmpty() ? null : list.get(0);
			onSwitchToPatch(pg);	
		});
		
		this.textureListModel = new TextureListModel();
		this.textureList = list(textureListModel, listLabelRenderer((i) -> i.getName()), ListSelectionMode.MULTIPLE_INTERVAL, (list, b) -> {
			TextureSet.Texture tex = list.isEmpty() || list.size() > 1 ? null : list.get(0);
			onSwitchToTexture(tex);	
		});
		
		this.textureInfoLabel = label(" ");
		
		this.textureAddAction = actionItem(icons.getImage("add.png"), (a) -> onTextureAdd());
		this.textureRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onTextureRemove());
		this.textureMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onTextureMoveUp());
		this.textureMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onTextureMoveDown());
		this.patchAddAction = actionItem(icons.getImage("add.png"), (a) -> onPatchAdd());
		this.patchRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onPatchRemove());
		this.patchMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onPatchMoveUp());
		this.patchMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onPatchMoveDown());
		this.helpAction = actionItem(icons.getImage("help.png"), (a) -> onHelpDialog());
		
		this.currentHasChanged = false;
		this.currentTexture = null;
		this.currentPatch = null;

		MouseControlAdapter canvasMouseAdapter = new MouseControlAdapter()
		{
			private int lastX = -1; 
			private int lastY = -1;
	
			@Override
			public void mousePressed(MouseEvent e) 
			{
				lastX = e.getX();
				lastY = e.getY();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) 
			{
				lastX = -1;
				lastY = -1;
			}
			
			@Override
			public void mouseDragged(MouseEvent e) 
			{
				if (currentPatch == null)
					return;
				onPatchTranslate(-(lastX - e.getX()), -(lastY - e.getY()));
				lastX = e.getX();
				lastY = e.getY();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
				{
					zoomFactorField.setValue(Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, zoomFactorField.getValue() + (e.getUnitsToScroll() > 0 ? -ZOOMFACTOR_STEP : ZOOMFACTOR_STEP))));
				}
			}
		};
		
		KeyListener canvasKeyboardAdapter = new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				switch (e.getKeyCode())
				{
					case KeyEvent.VK_LEFT:
						if (currentPatch == null)
							return;
						onPatchTranslate(-1, 0);
						break;
					case KeyEvent.VK_RIGHT:
						if (currentPatch == null)
							return;
						onPatchTranslate(1, 0);
						break;
					case KeyEvent.VK_UP:
						if (currentPatch == null)
							return;
						onPatchTranslate(0, -1);
						break;
					case KeyEvent.VK_DOWN:
						if (currentPatch == null)
							return;
						onPatchTranslate(0, 1);
						break;
					case KeyEvent.VK_MINUS:
						zoomFactorField.setValue(Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, zoomFactorField.getValue() - ZOOMFACTOR_STEP)));
						break;
					case KeyEvent.VK_EQUALS:
						zoomFactorField.setValue(Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, zoomFactorField.getValue() + ZOOMFACTOR_STEP)));
						break;
				}
			}
		};
		
		canvas.addMouseListener(canvasMouseAdapter);
		canvas.addMouseMotionListener(canvasMouseAdapter);
		canvas.addMouseWheelListener(canvasMouseAdapter);
		canvas.addKeyListener(canvasKeyboardAdapter);

		onIWADFileSelect(iwadSourceField.getValue());
		onPaletteFileSelect(paletteSourceField.getValue());
	}

	@Override
	public String getTitle()
	{
		return language.getText("wadtex.texture.editor.title");
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		// TODO Auto-generated method stub
		super.setApplicationState(state);
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		// TODO Auto-generated method stub
		return super.getApplicationState();
	}
	
	@Override
	public Container createContentPane()
	{
		Container textureListPanel = containerOf(borderLayout(4, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
				node(BorderLayout.CENTER, label(language.getText("wadtex.texture.editor.list.textures"))),
				node(BorderLayout.EAST, containerOf(flowLayout(Flow.LEFT, 4, 0),
					node(button(textureAddAction)),
					node(button(textureRemoveAction)),
					node(button(textureMoveUpAction)),
					node(button(textureMoveDownAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(textureList))
		);
		
		Container patchListPanel = containerOf(borderLayout(4, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
				node(BorderLayout.CENTER, label(language.getText("wadtex.texture.editor.list.patches"))),
				node(BorderLayout.EAST, containerOf(flowLayout(Flow.LEFT, 4, 0),
					node(button(patchAddAction)),
					node(button(patchRemoveAction)),
					node(button(patchMoveUpAction)),
					node(button(patchMoveDownAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(patchList))
		);
		
		JPanel tcPanel = new JPanel();
		Container textureCharacteristicsPanel = containerOf(tcPanel, BorderFactory.createTitledBorder(language.getText("wadtex.texture.editor.texture.info")), boxLayout(tcPanel, BoxAxis.Y_AXIS),
			node(utils.createForm(form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.texture.labelwidth")), 
				utils.formField("wadtex.texture.editor.texture.width", textureWidthField),
				utils.formField("wadtex.texture.editor.texture.height", textureHeightField)
			))
		);
		JPanel pcPanel = new JPanel();
		Container patchCharacteristicsPanel = containerOf(pcPanel, BorderFactory.createTitledBorder(language.getText("wadtex.texture.editor.patch.info")), boxLayout(pcPanel, BoxAxis.Y_AXIS),
			node(utils.createForm(form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.patch.labelwidth")), 
				utils.formField("wadtex.texture.editor.patch.x", patchXField),
				utils.formField("wadtex.texture.editor.patch.y", patchYField)
			))
		);
		
		return containerOf(borderLayout(4, 4),
			node(BorderLayout.WEST, containerOf(borderLayout(4, 4),
				node(BorderLayout.WEST, dimension(160, 560), textureListPanel),
				node(BorderLayout.EAST, containerOf(borderLayout(4, 4),
					node(BorderLayout.NORTH, dimension(160, 200), patchListPanel),
					node(BorderLayout.CENTER, dimension(160, 360), containerOf(borderLayout(),
						node(BorderLayout.NORTH, patchCharacteristicsPanel),
						node(BorderLayout.CENTER, containerOf()),
						node(BorderLayout.SOUTH, textureCharacteristicsPanel)
					))
				))
			)),
			node(BorderLayout.CENTER, containerOf(dimension(560, 560), borderLayout(4, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(4, 4),
					node(BorderLayout.WEST, containerOf(flowLayout(Flow.LEADING, 4, 0),
						node(label(language.getText("wadtex.texture.editor.zoom"))),
						node(zoomFactorField)
					)),
					node(BorderLayout.CENTER, containerOf()),
					node(BorderLayout.EAST, containerOf(flowLayout(Flow.TRAILING),
						node(button(helpAction))
					))
				)),
				node(BorderLayout.CENTER, canvas),
				node(BorderLayout.SOUTH, containerOf(borderLayout(),
					node(BorderLayout.NORTH, containerOf(BorderFactory.createTitledBorder(language.getText("wadtex.texture.editor.files.title")),
						node(utils.createForm(form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.files.labelwidth")),
							utils.formField("wadtex.texture.editor.project", projectDirectoryField),
							utils.formField("wadtex.texture.editor.iwad", iwadSourceField)
						))
					)),
					node(BorderLayout.SOUTH, containerOf(BorderFactory.createTitledBorder(language.getText("wadtex.texture.editor.files.title.palette")),
						node(utils.createForm(form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.files.labelwidth")),
							utils.formField("wadtex.texture.editor.palette.source", paletteSourceField)
						))
					))
				))
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
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.save", (i) -> onSaveTextureFile())
			)
		);
	}
	
	@Override
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		return fromWorkspaceClear || SwingUtils.yesTo(language.getText("doomtools.application.close"));
	}
	
	@Override
	public void onResize(Object frame) 
	{
		canvas.repaint();
	}

	private void onProjectDirectorySelect(File directory)
	{
		projectDirectory = directory;
		projectConvertFiles.clear();
		projectPatchFiles.clear();

		if (projectDirectory != null)
		{
			try {
				projectProperties = Common.createProjectProperties(projectDirectory);
			} catch (IOException e) {
				throw new RuntimeException("Could not initialize texture editor app project properties.", e);
			}
	
			File srcDir = Common.getProjectPropertyPath(projectDirectory, projectProperties, "doommake.dir.src", "src");
	
			File convertPatchDir = new File(srcDir.getPath() + "/convert/patches");
			File texturePatchDir = new File(srcDir.getPath() + "/textures/patches");
			
			File[] fileList = convertPatchDir.listFiles();
			if (fileList != null)
			{
				for (File f : fileList)
					projectConvertFiles.put(FileUtils.getFilePathWithoutExtension(f), f);
			}
	
			fileList = texturePatchDir.listFiles();
			if (fileList != null)
			{
				for (File f : fileList)
					projectPatchFiles.put(FileUtils.getFilePathWithoutExtension(f), f);
			}

			if (iwadSourceField.getValue() == null)
			{
				String iwadPath = projectProperties.getProperty("doommake.iwad");
				File file;
				if (!ObjectUtils.isEmpty(iwadPath) && (file = new File(iwadPath)).exists())
					iwadSourceField.setValue(file);
			}
			
			if (paletteSourceField.getValue() == null)
			{
				String build = projectProperties.getProperty("doommake.dir.build");
				if (ObjectUtils.isEmpty(build))
					build = "build";
				String source = projectProperties.getProperty("doommake.dir.src");
				if (ObjectUtils.isEmpty(source))
					source = "src";

				File paletteFile = FileUtils.searchDirectory(new File(projectDirectory.getPath() + "/" + build + "/convert/palettes"), "PLAYPAL", true, false);
				if (paletteFile != null)
				{
					paletteSourceField.setValue(paletteFile);
				}
				else
				{
					paletteFile = FileUtils.searchDirectory(new File(projectDirectory.getPath() + "/" + source + "/assets/palettes"), "PLAYPAL", true, false);
					if (paletteFile != null)
						paletteSourceField.setValue(paletteFile);
					else if (iwadSourceField.getValue() != null)
						paletteSourceField.setValue(iwadSourceField.getValue());
				}
			}
			
			if (currentTexture != null)
			{
				refreshGraphicData(currentTexture);
				canvas.repaint();
			}
		}
		
		refeshPatchNameList();
	}

	private void commitCurrentTexture()
	{
		if (currentTexture == null)
			return;
		
		while (!currentTexture.isEmpty())
			currentTexture.removePatch(0);
		
		for (int i = 0; i < patchListModel.getSize(); i++)
		{
			PatchGraphic pg = patchListModel.getPatch(i);
			TextureSet.Patch srcPatch = pg.getPatch();
			TextureSet.Patch patch = currentTexture.createPatch(srcPatch.getName());
			patch.setOriginX(srcPatch.getOriginX());
			patch.setOriginY(srcPatch.getOriginY());
		}
	}
	
	private File fetchPatchFileForName(String name)
	{
		if (projectDirectory == null)
			return null;
		
		File srcDir = Common.getProjectPropertyPath(projectDirectory, projectProperties, "doommake.dir.src", "src");
		File convertPatchDir = new File(srcDir.getPath() + File.separator + "convert" + File.separator + "patches");
		File texturePatchDir = new File(srcDir.getPath() + File.separator + "textures" + File.separator + "patches");

		File file;

		// try src/convert/patches
		file = projectConvertFiles.get(convertPatchDir.getPath() + File.separator + name);
		if (file != null)
			return file;

		// try src/textures/patches
		file = projectPatchFiles.get(texturePatchDir.getPath() + File.separator + name);
		if (file != null)
			return file;
		
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
					if (image != null)
					{
						png.setImage(image);
						createPatch(patch, png);
						return true;
					}
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

	private void refreshGraphicData(final TextureSet.Texture texture)
	{
		for (TextureSet.Patch patch : texture)
		{
			File patchFile = fetchPatchFileForName(patch.getName());
			if (patchFile != null)
			{
				addFoundPatchFile(patch, patchFile);
			}
			else
			{
				Picture picture = fetchIWADPatchForName(patch.getName());
				if (picture != null)
					addFoundPatch(patch, picture);
				else
					createPatch(patch, NO_PATCH);
			}
		}
	}

	private void refeshPatchNameList()
	{
		LOG.debugf("Start patch name refresh...");
		projectPatchNames.clear();
		
		for (Map.Entry<String, ?> entry : projectConvertFiles.entrySet())
		{
			String pname = FileUtils.getFileNameWithoutExtension((new File(entry.getKey())).getName()).toUpperCase(); 
			projectPatchNames.add(pname);
		}
		
		for (Map.Entry<String, ?> entry : projectPatchFiles.entrySet())
		{
			String pname = FileUtils.getFileNameWithoutExtension((new File(entry.getKey())).getName()).toUpperCase(); 
			projectPatchNames.add(pname);
		}
		
		if (iwadSourceField.getValue() != null)
		{
			try {
				WadMap wad = new WadMap(iwadSourceField.getValue());
				for (WadEntry entry : WadUtils.getEntriesInNamespace(wad, "P"))
				{
					if (!entry.isMarker())
						projectPatchNames.add(entry.getName());
				}
				for (WadEntry entry : WadUtils.getEntriesInNamespace(wad, "PP"))
				{
					if (!entry.isMarker())
						projectPatchNames.add(entry.getName());
				}
			} catch (IOException e) {
				// Eat exception.
			}
		}
		
		projectPatchNames.sort(String.CASE_INSENSITIVE_ORDER);
		LOG.debugf("Patch name refresh finished.");
	}

	private boolean checkPictureBounds(int w, int h, int ox, int oy)
	{
		return w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024;
	}
	
	private void updateActionsAndFields()
	{
		textureAddAction.setEnabled(true);
		textureRemoveAction.setEnabled(currentTexture != null);
		textureMoveUpAction.setEnabled(currentTexture != null);
		textureMoveDownAction.setEnabled(currentTexture != null);

		patchAddAction.setEnabled(currentTexture != null);
		patchRemoveAction.setEnabled(currentPatch != null);
		patchMoveUpAction.setEnabled(currentPatch != null);
		patchMoveDownAction.setEnabled(currentPatch != null);

		textureWidthField.setEnabled(currentTexture != null);
		textureHeightField.setEnabled(currentTexture != null);
		
		patchXField.setEnabled(currentPatch != null);
		patchYField.setEnabled(currentPatch != null);
	}
	
	private void onNewTextureFile()
	{
		if (currentHasChanged)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.new.file")))
				return;
		}
		
		onSwitchToTexture(null);
		patchListModel.clearPatches();
		textureListModel.clearTextures();
		currentHasChanged = false;
		currentTexture = null;
		currentPatch = null;
	}

	private void onOpenTextureFile()
	{
		if (currentHasChanged)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.open.file")))
				return;
		}
		
		File openedFile = utils.chooseFile(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.open.file.title"), 
			language.getText("wadtex.texture.editor.open.file.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.createTextFileFilter()
		);
		
		if (openedFile == null)
			return;
		
		PatchNames pnames = new PatchNames();
		DoomTextureList textures = new DoomTextureList();
		TextureSet textureSet;
		
		try (BufferedReader br = IOUtils.openTextFile(openedFile))
		{
			textureSet = Utility.readDEUTEXFile(br, pnames, textures);
		}
		catch (IOException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.open.file.error.io", openedFile.getPath()));
			return;
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.open.file.error.security", openedFile.getPath()));
			return;
		} 
		catch (ParseException e)
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.open.file.error.parse", openedFile.getPath(), e.getLocalizedMessage()));
			return;
		}
		
		onSwitchToTexture(null);
		patchListModel.clearPatches();
		textureListModel.clearTextures();
		textureListModel.setTextures(textureSet);
		currentHasChanged = false;
		currentTexture = null;
		currentPatch = null;
	}

	private void onSaveTextureFile()
	{
		final FileFilter txtFilter = utils.createTextFileFilter();
		
		File saveFile = utils.chooseFile(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.save.file.title"), 
			language.getText("wadtex.texture.editor.save.file.accept"),
			() -> (settings.getLastTouchedFile().getParentFile()),
			settings::setLastTouchedFile,
			(filter, selected) -> FileUtils.addMissingExtension(selected, filter == txtFilter ? "txt" : ""),
			txtFilter
		);
		
		if (saveFile == null)
			return;
		
		commitCurrentTexture();
		
		TextureSet textureSet = new TextureSet();
		for (TextureSet.Texture tex : textureListModel.textures)
			textureSet.addTexture(tex);
		
		try (PrintWriter pw = new PrintWriter(new FileOutputStream(saveFile))) 
		{
			Utility.writeDEUTEXFile(textureSet, FILE_HEADER, pw);
			SwingUtils.info(getApplicationContainer(), language.getText("wadtex.texture.editor.save.file", saveFile.getPath()));
		} 
		catch (IOException e)
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.save.error.io"));
			return;
		}
		catch (SecurityException e)
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.save.error.security"));
			return;
		}
		
		currentHasChanged = false;
	}

	private void onSwitchToTexture(final TextureSet.Texture texture)
	{
		commitCurrentTexture();
		patchListModel.clearPatches();
		
		if (texture == null)
		{
			SwingUtils.invoke(() -> {
				textureInfoLabel.setText(" ");
			});
			currentTexture = null;
			updateActionsAndFields();
			return;
		}

		refreshGraphicData(texture);
		
		SwingUtils.invoke(() -> {
			textureInfoLabel.setText(texture.getName() + " (" + texture.getWidth() + " x " + texture.getHeight() + ")");
		});
		
		currentTexture = texture;
		onSwitchToPatch(null);
		
		textureWidthField.setValue((short)texture.getWidth());
		textureHeightField.setValue((short)texture.getHeight());
		updateActionsAndFields();
	}

	private void onSwitchToPatch(PatchGraphic patch)
	{
		if (patch == null)
		{
			currentPatch = null;
			updateActionsAndFields();
			return;
		}
		
		currentPatch = patch.getPatch();
		patchXField.setValue((short)currentPatch.getOriginX());
		patchYField.setValue((short)currentPatch.getOriginY());
		updateActionsAndFields();
	}

	private void onZoomFactorChanged(double zoomFactor)
	{
		canvas.setZoomFactor((float)zoomFactor);
	}
	
	private void onPaletteFileSelect(File selectedFile)
	{
		if (selectedFile == null)
		{
			canvas.setPalette(null);
			return;
		}
		
		boolean wadfile = false;
		try {
			wadfile = Wad.isWAD(selectedFile);
		} catch (IOException e) {
			SwingUtils.error(language.getText("wadtex.texture.editor.palette.source.error.ioerror", selectedFile));
			return;
		}
		
		Palette pal = null;

		// If WAD, search for PlayPal
		if (wadfile)
		{
			try (WadFile wf = new WadFile(selectedFile)) {
				pal = wf.getDataAs("PLAYPAL", Palette.class);
			} catch (IOException e) {
				SwingUtils.error(language.getText("wadtex.texture.editor.palette.source.error.ioerror", selectedFile));
				return;
			}
		}
		// else, attempt to load as palette.
		else
		{
			try {
				pal = BinaryObject.read(Palette.class, selectedFile);
			} catch (IOException e) {
				SwingUtils.error(language.getText("wadtex.texture.editor.palette.source.error.notpal", selectedFile));
				return;
			}
		}
		
		if (pal != null)
			settings.setLastPaletteFile(selectedFile);
		
		canvas.setPalette(pal);
	}

	private void onIWADFileSelect(File value)
	{
		if (value != null)
		{
			try {
				if (!Wad.isWAD(value))
					SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.iwad.error.notwad", value.getPath()));
			} catch (IOException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.iwad.error.notwad", value.getPath()));
			}
		}

		onSwitchToTexture(currentTexture);
		refeshPatchNameList();
	}
	
	private void onTextureWidthChanged(Short value)
	{
		currentTexture.setWidth(value);
		canvas.setTextureDimensions(dimension(currentTexture.getWidth(), currentTexture.getHeight()));
		currentHasChanged = true;
	}

	private void onTextureHeightChanged(Short value)
	{
		currentTexture.setHeight(value);
		canvas.setTextureDimensions(dimension(currentTexture.getWidth(), currentTexture.getHeight()));
		currentHasChanged = true;
	}

	private void onTextureAdd()
	{
		final JFormField<String> nameField = stringField(false, true); 
		final JFormField<Short> widthField = shortField((short)128, false); 
		final JFormField<Short> heightField = shortField((short)128, false); 
		
		Boolean ok = modal(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.texture.add"),
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, utils.createForm(
					form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.texture.add.labelwidth")),
					utils.formField("wadtex.texture.editor.texture.add.name", nameField),
					utils.formField("wadtex.texture.editor.texture.add.width", widthField),
					utils.formField("wadtex.texture.editor.texture.add.height", heightField)
				))
			),
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		String value = nameField.getValue().toUpperCase();
		if (value.isEmpty())
			return;
		
		if (value.length() > 8)
			value = value.substring(0, 8);
		
		TextureSet.Texture added = textureListModel.addTexture(value);
		added.setWidth(widthField.getValue());
		added.setHeight(heightField.getValue());
		
		textureList.setSelectedIndex(textureListModel.getSize() - 1);
		currentHasChanged = true;
	}

	private void onTextureRemove()
	{
		int[] selectedIndices = textureList.getSelectedIndices();
		
		if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.texture.remove", selectedIndices.length)))
			return;
		
		textureListModel.removeTextures(selectedIndices);
		
		currentHasChanged = true;
	}

	private void onTextureMoveUp()
	{
		int index = textureList.getSelectedIndex();
		if (index > 0)
		{
			textureListModel.shift(index, index - 1);
			currentHasChanged = true;
			textureList.setSelectedIndex(index - 1);
		}
	}

	private void onTextureMoveDown()
	{
		int index = textureList.getSelectedIndex();
		if (index < textureListModel.getSize() - 1)
		{
			textureListModel.shift(index, index + 1);
			currentHasChanged = true;
			textureList.setSelectedIndex(index + 1);
		}
	}

	private void onPatchAdd()
	{
		JList<String> patchList = list(projectPatchNames, ListSelectionMode.SINGLE);
		
		Boolean ok = modal(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.patch.add"),
			containerOf(borderLayout(), node(dimension(128, 256), scroll(patchList))),
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		String patchName = patchList.getSelectedValue();
		if (patchName == null)
			return;
		
		File patchFile = fetchPatchFileForName(patchName);
		if (patchFile != null)
		{
			TextureSet.Patch patch = currentTexture.createPatch(patchName);
			addFoundPatchFile(patch, patchFile);
		}
		else
		{
			Picture picture = fetchIWADPatchForName(patchName);
			if (picture != null)
			{
				TextureSet.Patch patch = currentTexture.createPatch(patchName);
				addFoundPatch(patch, picture);
			}
			else
			{
				TextureSet.Patch patch = currentTexture.createPatch(patchName);
				createPatch(patch, NO_PATCH);
			}
		}
		
		canvas.repaint();
		currentHasChanged = true;
	}

	private void onPatchRemove()
	{
		patchListModel.removePatch(patchList.getSelectedIndex());
		onSwitchToPatch(null);
		canvas.repaint();
		currentHasChanged = true;
	}

	private void onPatchMoveUp()
	{
		int index = patchList.getSelectedIndex();
		if (index > 0)
		{
			patchListModel.shiftUp(index);
			patchList.setSelectedIndex(index - 1);
			canvas.repaint();
			currentHasChanged = true;
		}
	}

	private void onPatchMoveDown()
	{
		int index = patchList.getSelectedIndex();
		if (index < patchListModel.getSize() - 1)
		{
			patchListModel.shiftDown(index);
			patchList.setSelectedIndex(index + 1);
			canvas.repaint();
			currentHasChanged = true;
		}
	}

	private void onPatchOffsetXChange(Short x)
	{
		if (currentPatch == null)
			return;
		currentPatch.setOriginX(x);
		canvas.repaint();
		currentHasChanged = true;
	}

	private void onPatchOffsetYChange(Short y)
	{
		if (currentPatch == null)
			return;
		currentPatch.setOriginY(y);
		canvas.repaint();
		currentHasChanged = true;
	}

	private void onPatchTranslate(int x, int y)
	{
		patchXField.setValue((short)(currentPatch.getOriginX() + x));
		patchYField.setValue((short)(currentPatch.getOriginY() + y));
		canvas.repaint();
		currentHasChanged = true;
	}

	private void onHelpDialog()
	{
		modal(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.help.title"),
			containerOf(node(label(language.getHTML("wadtex.texture.editor.help.content")))),
			utils.createChoiceFromLanguageKey("doomtools.ok")
		).openThenDispose();
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
		private TextureSet textures;
		private final List<ListDataListener> listeners;
	
		public TextureListModel() 
		{
			this.textures = new TextureSet();
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
			return textures.getTexture(index);
		}
	
		/**
		 * Sets the textures on this texture set.
		 * @param textures the textures to add.
		 */
		public void setTextures(TextureSet textures)
		{
			clearTextures();
			this.textures = textures;
			int index = textures.size();
			if (index == 0)
				return;
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, index - 1)
			));
		}
	
		/**
		 * Adds a texture to this texture set.
		 * @param name the name of the texture to add. 
		 * @return the created texture.
		 */
		public TextureSet.Texture addTexture(String name)
		{
			int index = textures.size();
			TextureSet.Texture out = textures.createTexture(name);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
			return out;
		}
	
		/**
		 * Removes a texture from this texture set.
		 * @param index the index of the texture to remove.
		 * @return the removed texture.
		 */
		public TextureSet.Texture removeTexture(int index)
		{
			TextureSet.Texture out;
			out = textures.removeTexture(index);
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)
			));
			return out;
		}
	
		/**
		 * Removes textures from this texture set.
		 * @param indices the indices of the textures to remove.
		 */
		public void removeTextures(int[] indices)
		{
			int offset = 0;
			for (int i = 0; i < indices.length; i++)
			{
				int index = indices[i] - offset;
				removeTexture(index);
				offset++;
			}
		}
	
		/**
		 * Removes all textures from the model.
		 */
		public void clearTextures()
		{
			int amount = textures.size();
			if (amount == 0)
				return;
			this.textures = new TextureSet();
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, amount - 1)
			));
		}
		
		/**
		 * Shifts a texture by index.
		 * @param startIndex the starting index.
		 * @param targetIndex the target index.
		 * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= count.
		 */
		public void shift(int startIndex, int targetIndex)
		{
			if (startIndex == targetIndex)
				return;
			textures.shiftTexture(startIndex, targetIndex);
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, startIndex, startIndex)
			));
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, targetIndex, targetIndex)
			));
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
