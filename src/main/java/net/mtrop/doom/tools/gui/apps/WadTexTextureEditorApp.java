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
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.WadMap;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.object.GraphicObject;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.ParseException;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexTextureEditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.adapters.MouseControlAdapter;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.PatchDisplayCanvas;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchGraphic;
import net.mtrop.doom.tools.gui.swing.panels.WadTexTextureEditorCanvas.PatchListModel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doom.tools.struct.swing.LayoutFactory.Flow;
import net.mtrop.doom.tools.struct.swing.ImageUtils;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.EncodingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;
import net.mtrop.doom.util.NameUtils;
import net.mtrop.doom.util.TextureUtils;
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

	private static final String FILE_HEADER = "; File written by WadTex Texture Editor v" + Version.WADTEX + " by Matt Tropiano";

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
	
	private Properties projectProperties;
	private Map<String, File> projectPatchSources;
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
	
	private Action saveAction;
	private Action refreshTextureListAction;
	
	private Action textureAddAction;
	private Action textureAddAction2;
	private Action textureRemoveAction;
	private Action textureRemoveAction2;
	private Action textureMoveUpAction;
	private Action textureMoveDownAction;
	private Action patchAddAction;
	private Action patchAddAction2;
	private Action patchRemoveAction;
	private Action patchRemoveAction2;
	private Action patchMoveUpAction;
	private Action patchMoveDownAction;
	private Action patchCloneAction;
	private Action patchRenameAction;
	private Action helpAction;

	private Action copyTextureAction;
	private Action renameTextureAction;
	
	private File currentTextureFile;
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
		
		this.projectPatchSources = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.projectPatchNames = new ArrayList<>();

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
		
		this.iwadSourceField = fileField(settings.getLastIWAD(), 
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.iwad.source.browse.title"), 
				language.getText("wadtex.texture.editor.iwad.source.browse.accept"),
				() -> current != null ? current : settings.getLastIWAD(),
				settings::setLastIWAD,
				utils.createWADFileFilter()
			), 
			this::onIWADFileSelect
		);
	
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
		
		this.saveAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.file.item.save", (a) -> onSaveTextureFile());
		this.refreshTextureListAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.file.item.refresh.texture", (a) -> onRefreshTextureList());
		
		this.textureAddAction = actionItem(icons.getImage("add.png"), (a) -> onTextureAdd());
		this.textureRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onTextureRemove());
		this.textureMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onTextureMoveUp());
		this.textureMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onTextureMoveDown());
		this.patchAddAction = actionItem(icons.getImage("add.png"), (a) -> onPatchAdd());
		this.patchRemoveAction = actionItem(icons.getImage("remove.png"), (a) -> onPatchRemove());
		this.patchMoveUpAction = actionItem(icons.getImage("up-arrow.png"), (a) -> onPatchMoveUp());
		this.patchMoveDownAction = actionItem(icons.getImage("down-arrow.png"), (a) -> onPatchMoveDown());
		this.helpAction = actionItem(icons.getImage("help.png"), (a) -> onHelpDialog());

		this.textureAddAction2 = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.texture.add", (a) -> onTextureAdd());
		this.textureRemoveAction2 = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.texture.remove", (a) -> onTextureRemove());
		this.copyTextureAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.texture.copy", (a) -> onTextureCopy());
		this.renameTextureAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.texture.rename", (a) -> onTextureRename());
		this.patchAddAction2 = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.patch.add", (a) -> onPatchAdd());
		this.patchRemoveAction2 = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.patch.remove", (a) -> onPatchRemove());
		this.patchCloneAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.patch.clone", (a) -> onPatchClone());
		this.patchRenameAction = utils.createActionFromLanguageKey("wadtex.texture.editor.menu.popup.patch.rename", (a) -> onPatchRename());
		
		this.textureList.setComponentPopupMenu(popupMenu(
			menuItem(textureAddAction2),
			menuItem(textureRemoveAction2),
			separator(),
			menuItem(copyTextureAction),
			menuItem(renameTextureAction)
		));
		this.patchList.setComponentPopupMenu(popupMenu(
			menuItem(patchAddAction2),
			menuItem(patchRemoveAction2),
			separator(),
			menuItem(patchCloneAction),
			menuItem(patchRenameAction)
		));
		
		this.currentTextureFile = null;
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
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState(); 
		
		state.put("textureState", textureListModel.createState());
		
		state.put("projectDir", projectDirectoryField.getValue() == null ? "" : projectDirectoryField.getValue().getAbsolutePath());
		state.put("baseWad", iwadSourceField.getValue() == null ? "" : iwadSourceField.getValue().getAbsolutePath());
		state.put("palette", paletteSourceField.getValue() == null ? "" : paletteSourceField.getValue().getAbsolutePath());
		
		state.put("zoom.factor", String.valueOf(zoomFactorField.getValue()));
		
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		super.setApplicationState(state);
		
		textureListModel.restoreState(state.getOrDefault("textureState", ""));
		
		paletteSourceField.setValue(ValueUtils.parse(state.get("palette"), (s) -> s == null ? null : new File(s)));
		iwadSourceField.setValue(ValueUtils.parse(state.get("baseWad"), (s) -> s == null ? null : new File(s)));
		projectDirectoryField.setValue(ValueUtils.parse(state.get("projectDir"), (s) -> s == null ? null : new File(s)));
		
		zoomFactorField.setValue(ValueUtils.parseDouble(state.get("zoom.factor"), 1));
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
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.openwad", (i) -> onOpenFromWADTextureFile()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.save", saveAction),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.saveas", (i) -> onSaveTextureFileAs()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.textedit", (i) -> onTextOpen()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.refresh.texture", refreshTextureListAction),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.refresh.patch", (i) -> onRefreshPatchList()),
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
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.openwad", (i) -> onOpenFromWADTextureFile()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.save", saveAction),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.saveas", (i) -> onSaveTextureFileAs()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.textedit", (i) -> onTextOpen()),
				separator(),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.refresh.texture", refreshTextureListAction),
				utils.createItemFromLanguageKey("wadtex.texture.editor.menu.file.item.refresh.patch", (i) -> onRefreshPatchList())
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
	
	private void commitTextureSet(TextureSet textureSet)
	{
		onSwitchToTexture(null);
		patchListModel.clearPatches();
		textureListModel.clearTextures();
		if (textureSet != null && !textureSet.isEmpty())
			textureListModel.setTextures(textureSet);
		currentHasChanged = false;
		currentTexture = null;
		currentPatch = null;
		updateActionsAndFields();
	}

	private GraphicObject fetchGraphicObjectForName(String name)
	{
		File file = projectPatchSources.get(name);
		if (file != null)
		{
			try {
				if (Wad.isWAD(file))
				{
					try (WadFile wf = new WadFile(file))
					{
						byte[] data = wf.getData(name);
						if (data == null)
							return null;
						return getGraphicObject(data);
					}
				}
				else
				{
					return getGraphicObject(IOUtils.getBinaryContents(file));
				}
			} catch (IOException e) {
				LOG.error(e, "Could not load patch from file: " + file.getPath());
			}
		}
		
		return null;
	}
	
	private GraphicObject getGraphicObject(byte[] data)
	{
		try {
			if (ArrayUtils.startsWith(data, PNG_SIGNATURE)) // png?
			{
				return BinaryObject.create(PNGPicture.class, data);
			}
			else // other?
			{
				BufferedImage image;
				PNGPicture png = new PNGPicture();
				try {
					image = ImageIO.read(new ByteArrayInputStream(data));
					if (image != null)
					{
						png.setImage(image);
						return png;
					}
				} catch (IOException e) {}
				
				int w, h, ox, oy;
				try (InputStream ins = new ByteArrayInputStream(data))
				{
					SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
					w = sr.readShort(ins);
					h = sr.readShort(ins);
					ox = sr.readShort(ins);
					oy = sr.readShort(ins);
				}

				// test if acceptable or reasonable bounds
				if (checkPictureBounds(w, h, ox, oy))
				{
					return BinaryObject.create(Picture.class, data);
				}
				else
				{
					return null;
				}
			}
		} catch (IOException e) {
			return null;
		}
	}
	
	private static boolean checkPictureBounds(int w, int h, int ox, int oy)
	{
		return w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024;
	}

	private void refreshGraphicData(final TextureSet.Texture texture)
	{
		for (TextureSet.Patch patch : texture)
		{
			GraphicObject gobj = fetchGraphicObjectForName(patch.getName());
			if (gobj != null)
			{
				if (gobj instanceof Picture)
					createPatch(patch, (Picture)gobj);
				else
					createPatch(patch, (PNGPicture)gobj);
			}
			else
			{
				createPatch(patch, NO_PATCH);
			}
		}
	}

	private void refreshPatchSources()
	{
		LOG.debugf("Start patch source refresh...");
		projectPatchSources.clear();
		
		if (projectDirectoryField.getValue() != null)
		{
			File value = projectDirectoryField.getValue();

			File srcDir = Common.getProjectPropertyPath(value, projectProperties, "doommake.dir.src", "src");

			File[] dirs = {
				new File(srcDir.getPath() + "/textures/patches"),
				new File(srcDir.getPath() + "/textures/texture1"),
				new File(srcDir.getPath() + "/textures/texture2"),
				new File(srcDir.getPath() + "/convert/patches"),
				new File(srcDir.getPath() + "/convert/texture1"),
				new File(srcDir.getPath() + "/convert/texture2")
			};
			
			for (File dir : dirs)
			{
				File[] fileList = FileUtils.explodeFiles(dir);
				if (fileList != null)
				{
					for (File f : fileList)
						projectPatchSources.put(NameUtils.toValidTextureName(FileUtils.getFileNameWithoutExtension(f)), f);
				}
			}
		}
		
		if (iwadSourceField.getValue() != null)
		{
			File value = iwadSourceField.getValue();
			
			try {
				if (!Wad.isWAD(value))
					SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.iwad.error.notwad", value.getPath()));
			} catch (IOException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.iwad.error.notwad", value.getPath()));
			}

			try {
				WadMap iwadMap = new WadMap(value);
				WadEntry[] patchEntries = WadUtils.getEntriesInNamespace(iwadMap, "P", Pattern.compile("P[1-9]_(START|END)"));
				for (int i = 0; i < patchEntries.length; i++)
					projectPatchSources.put(patchEntries[i].getName(), value);
			} catch (IOException e) {
				LOG.error(e, "Could not read IWAD " + value.getPath() + " for patch entries.");
			}
		}

		LOG.debugf("End patch source refresh.");
		refeshPatchNameList();
	}
	
	private void refeshPatchNameList()
	{
		LOG.debugf("Start patch name refresh...");
		projectPatchNames.clear();

		for (Map.Entry<String, ?> entry : projectPatchSources.entrySet())
			projectPatchNames.add(entry.getKey());
		
		projectPatchNames.sort(String.CASE_INSENSITIVE_ORDER);
		LOG.debugf("Patch name refresh finished.");
	}

	private void updateActionsAndFields()
	{
		List<TextureSet.Texture> selectedTextures = textureList.getSelectedValuesList();
		List<PatchGraphic> selectedPatches = patchList.getSelectedValuesList();
		
		saveAction.setEnabled(currentTextureFile != null);
		refreshTextureListAction.setEnabled(currentTextureFile != null);
		
		textureAddAction.setEnabled(true);
		textureRemoveAction.setEnabled(!selectedTextures.isEmpty());
		textureAddAction2.setEnabled(true);
		textureRemoveAction2.setEnabled(!selectedTextures.isEmpty());
		textureMoveUpAction.setEnabled(currentTexture != null);
		textureMoveDownAction.setEnabled(currentTexture != null);

		patchAddAction.setEnabled(currentTexture != null);
		patchRemoveAction.setEnabled(currentPatch != null);
		patchAddAction2.setEnabled(currentTexture != null);
		patchRemoveAction2.setEnabled(currentPatch != null);
		patchMoveUpAction.setEnabled(currentPatch != null);
		patchMoveDownAction.setEnabled(currentPatch != null);
		patchCloneAction.setEnabled(!selectedPatches.isEmpty());
		patchRenameAction.setEnabled(currentPatch != null);
		
		textureWidthField.setEnabled(currentTexture != null);
		textureHeightField.setEnabled(currentTexture != null);

		patchXField.setEnabled(currentPatch != null);
		patchYField.setEnabled(currentPatch != null);

		copyTextureAction.setEnabled(currentTexture != null);
		renameTextureAction.setEnabled(currentTexture != null);
	}
	
	private void doReopenFile(File openedFile)
	{
		TextureSet textureSet;
		
		try (BufferedReader br = IOUtils.openTextFile(openedFile))
		{
			textureSet = Utility.readDEUTEXFile(br);
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
		
		commitTextureSet(textureSet);
		currentTextureFile = openedFile;
	}

	private void onNewTextureFile()
	{
		if (currentHasChanged)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.new.file")))
				return;
		}
		
		commitTextureSet(null);
		currentTextureFile = null;
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
			utils.createWadTexFileFilter()
		);
		
		if (openedFile == null)
			return;
		
		doReopenFile(openedFile);
	}

	private void onOpenFromWADTextureFile()
	{
		if (currentHasChanged)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.open.file")))
				return;
		}
		
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wadtex.open.wad.title"), 
			language.getText("wadtex.open.wad.accept"),
			settings::getLastOpenedWAD,
			settings::setLastOpenedWAD,
			utils.createWADFileFilter()
		);
		
		if (file == null)
			return;
		
		boolean isWad;
		try {
			isWad = Wad.isWAD(file);
		} catch (FileNotFoundException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.notfound", file.getAbsolutePath()));
			return;
		} catch (IOException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.security", file.getAbsolutePath()));
			return;
		}
	
		if (!isWad)
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		
		TextureSet texture1 = null;
		TextureSet texture2 = null;
		
		try (WadFile wad = new WadFile(file))
		{
			int texture1Index = wad.indexOf("TEXTURE1");
			int texture2Index = wad.indexOf("TEXTURE2");
			int pnamesIndex =   wad.indexOf("PNAMES");
			
			if (pnamesIndex < 0 || (pnamesIndex >= 0 && texture1Index < 0 && texture2Index < 0))
			{
				SwingUtils.error(language.getText("wadtex.open.wad.error.nodata", file.getAbsolutePath()));
				return;
			}
			
			PatchNames pnames = wad.getDataAs(pnamesIndex, PatchNames.class);
			byte[] texture1data = texture1Index >= 0 ? wad.getData(texture1Index) : null;
			byte[] texture2data = texture2Index >= 0 ? wad.getData(texture2Index) : null;
			
			CommonTextureList<?> texture1List = null;
			if (texture1data != null)
			{
				if (TextureUtils.isStrifeTextureData(texture1data))
					texture1List = BinaryObject.create(StrifeTextureList.class, texture1data);
				else
					texture1List = BinaryObject.create(DoomTextureList.class, texture1data);
			}

			CommonTextureList<?> texture2List = null;
			if (texture2data != null)
			{
				if (TextureUtils.isStrifeTextureData(texture2data))
					texture2List = BinaryObject.create(StrifeTextureList.class, texture2data);
				else
					texture2List = BinaryObject.create(DoomTextureList.class, texture2data);
			}

			texture1 = texture1List != null ? new TextureSet(pnames, texture1List) : null;
			texture2 = texture2List != null ? new TextureSet(pnames, texture2List) : null;
		}
		catch (WadException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		catch (IOException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.security", file.getAbsolutePath()));
			return;
		}

		TextureSet selectedTextureSet;
		
		if (texture1 != null && texture2 != null)
		{
			JRadioButton tex1button = radio("TEXTURE1", true);
			JRadioButton tex2button = radio("TEXTURE2", false);
			group(tex1button, tex2button);
			
			Boolean ok = modal(
				getApplicationContainer(),
				language.getText("wadtex.texture.editor.open.texture.lump.title"),
				containerOf(gridLayout(2, 1, 4, 4),
					node(tex1button),
					node(tex2button)
				),
				utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
				utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
			).openThenDispose();
			
			if (ok != Boolean.TRUE)
				return;
			
			if (tex1button.isSelected())
				selectedTextureSet = texture1;
			else
				selectedTextureSet = texture2;
		}
		else if (texture1 != null)
		{
			selectedTextureSet = texture1;
		}
		else
		{
			selectedTextureSet = texture2;
		}

		commitTextureSet(selectedTextureSet);
		currentTextureFile = null; // must save to TXT
	}

	private void onSaveTextureFile()
	{
		if (currentTextureFile == null)
			onSaveTextureFileAs();
		else
		{
			commitCurrentTexture();
			
			TextureSet textureSet = new TextureSet();
			for (TextureSet.Texture tex : textureListModel.textures)
				textureSet.addTexture(tex);
			
			try (PrintWriter pw = new PrintWriter(new FileOutputStream(currentTextureFile))) 
			{
				Utility.writeDEUTEXFile(textureSet, FILE_HEADER, pw);
				SwingUtils.info(getApplicationContainer(), language.getText("wadtex.texture.editor.save.file", currentTextureFile.getPath()));
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
	}
	
	private void onSaveTextureFileAs()
	{
		final FileFilter txtFilter = utils.createWadTexFileFilter();
		
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
		
		if (saveFile.exists())
		{
			if (SwingUtils.noTo(language.getText("wadtex.texture.editor.save.exists", saveFile.getName())))
				return;
		}
		
		currentTextureFile = saveFile;
		onSaveTextureFile();
		updateActionsAndFields();
	}

	private void onRefreshTextureList() 
	{
		if (currentHasChanged)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.refresh.file")))
				return;
		}
		
		doReopenFile(currentTextureFile);
	}

	private void onRefreshPatchList()
	{
		refreshPatchSources();
		SwingUtils.info(language.getText("wadtex.texture.editor.refresh.patches"));
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
			canvas.repaint();
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
	
	private void onProjectDirectorySelect(File directory)
	{
		if (directory != null)
		{
			try {
				projectProperties = Common.createProjectProperties(directory);
			} catch (IOException e) {
				throw new RuntimeException("Could not initialize texture editor app project properties.", e);
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
	
				File paletteFile = FileUtils.searchDirectory(new File(directory.getPath() + "/" + build + "/convert/palettes"), "PLAYPAL", true, false);
				if (paletteFile != null)
				{
					paletteSourceField.setValue(paletteFile);
				}
				else
				{
					paletteFile = FileUtils.searchDirectory(new File(directory.getPath() + "/" + source + "/assets/palettes"), "PLAYPAL", true, false);
					if (paletteFile != null)
						paletteSourceField.setValue(paletteFile);
					else if (iwadSourceField.getValue() != null)
						paletteSourceField.setValue(iwadSourceField.getValue());
				}
			}
		}
		
		if (currentTexture != null)
		{
			refreshGraphicData(currentTexture);
			canvas.repaint();
		}
		
		refreshPatchSources();
	}

	private void onIWADFileSelect(File value)
	{
		refreshPatchSources();
		onSwitchToTexture(currentTexture);
	}
	
	private void onPaletteFileSelect(File selectedFile)
	{
		canvas.setPalette(AppCommon.get().readPaletteFromFile(selectedFile));
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

		String value;
		
		do {
			
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
			
			value = nameField.getValue();
			
			if (ObjectUtils.isEmpty(value))
				return;

			value = NameUtils.toValidTextureName(value.replace(" ", ""));
			
			if (textureListModel.containsTexture(value))
			{
				SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.texture.add.exists", value));
			}

		} while (textureListModel.containsTexture(value));
		
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

	private void onTextureCopy()
	{
		commitCurrentTexture();
		
		TextureSet.Texture selected = textureList.getSelectedValue();
		if (selected != null)
		{
			final JFormField<String> nameField = stringField(selected.getName(), false, true); 
			
			String value;
			
			do {
				
				Boolean ok = modal(
					getApplicationContainer(),
					language.getText("wadtex.texture.editor.texture.copy"),
					containerOf(borderLayout(),
						node(BorderLayout.CENTER, utils.createForm(
							form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.texture.copy.labelwidth")),
							utils.formField("wadtex.texture.editor.texture.copy.name", nameField)
						))
					),
					utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
					utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
				).openThenDispose();
				
				if (ok != Boolean.TRUE)
					return;
				
				value = nameField.getValue();
				
				if (ObjectUtils.isEmpty(value))
					return;

				value = NameUtils.toValidTextureName(value.replace(" ", ""));
				
				if (textureListModel.containsTexture(value))
				{
					SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.texture.copy.exists", value));
				}
				
			} while (textureListModel.containsTexture(value));
			
			// do copy
			TextureSet.Texture added = textureListModel.addTexture(value);
			added.setWidth(selected.getWidth());
			added.setHeight(selected.getHeight());
			for (TextureSet.Patch p : selected)
			{
				TextureSet.Patch ap = added.createPatch(p.getName());
				ap.setOriginX(p.getOriginX());
				ap.setOriginY(p.getOriginY());
			}
		}
	}

	private void onTextureRename()
	{
		commitCurrentTexture();

		int selectedIndex = textureList.getSelectedIndex();
		if (selectedIndex >= 0)
		{
			TextureSet.Texture selected = textureList.getSelectedValue();
			final JFormField<String> nameField = stringField(selected.getName(), false, true); 
			
			String value;
			
			do {
				
				Boolean ok = modal(
					getApplicationContainer(),
					language.getText("wadtex.texture.editor.texture.rename"),
					containerOf(borderLayout(),
						node(BorderLayout.CENTER, utils.createForm(
							form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.texture.rename.labelwidth")),
							utils.formField("wadtex.texture.editor.texture.rename.name", nameField)
						))
					),
					utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
					utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
				).openThenDispose();
				
				if (ok != Boolean.TRUE)
					return;
				
				value = nameField.getValue();
				
				if (ObjectUtils.isEmpty(value))
					return;

				value = NameUtils.toValidTextureName(value.replace(" ", ""));
				
				if (textureListModel.containsTexture(value))
				{
					SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.texture.rename.exists", value));
				}
				
			} while (textureListModel.containsTexture(value));
			
			// do rename
			TextureSet.Texture removed = textureListModel.removeTexture(selectedIndex);
			TextureSet.Texture added = textureListModel.addTexture(value);
			added.setWidth(removed.getWidth());
			added.setHeight(removed.getHeight());
			for (TextureSet.Patch p : removed)
			{
				TextureSet.Patch ap = added.createPatch(p.getName());
				ap.setOriginX(p.getOriginX());
				ap.setOriginY(p.getOriginY());
			}
		}
	}

	private void onPatchAdd()
	{
		PatchDisplayCanvas displayCanvas = new PatchDisplayCanvas();
		displayCanvas.setZoomFactor((float)(double)zoomFactorField.getValue());
		displayCanvas.setPalette(canvas.getPalette());
		
		displayCanvas.addKeyListener(new KeyAdapter() 
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				switch (e.getKeyCode())
				{
					case KeyEvent.VK_MINUS:
						displayCanvas.setZoomFactor((float)(double)Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, displayCanvas.getZoomFactor() - ZOOMFACTOR_STEP)));
						break;
					case KeyEvent.VK_EQUALS:
						displayCanvas.setZoomFactor((float)(double)Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, displayCanvas.getZoomFactor() + ZOOMFACTOR_STEP)));
						break;
				}
			}
		});
		
		displayCanvas.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
				{
					displayCanvas.setZoomFactor((float)(double)Math.max(ZOOMFACTOR_MIN, Math.min(ZOOMFACTOR_MAX, displayCanvas.getZoomFactor() + (e.getUnitsToScroll() > 0 ? -ZOOMFACTOR_STEP : ZOOMFACTOR_STEP))));
				}
			}
		});
		
		JList<String> patchList = list(projectPatchNames, ListSelectionMode.MULTIPLE_INTERVAL, (selected, adjusting) ->
		{
			if (selected.isEmpty() || selected.size() > 1)
				displayCanvas.clearPictures();
			else
			{
				String sel = selected.get(0);
				GraphicObject go = fetchGraphicObjectForName(sel);
				if (go != null)
				{
					if (go instanceof Picture)
						displayCanvas.setPicture((Picture)go);
					else
						displayCanvas.setPNGPicture((PNGPicture)go);
				}
				else
				{
					displayCanvas.clearPictures();
				}
			}
		});

		Boolean ok = modal(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.patch.add"),
			containerOf(borderLayout(), 
				node(BorderLayout.WEST, dimension(128, 512), scroll(patchList)),
				node(BorderLayout.CENTER, dimension(512, 512), displayCanvas)
			),
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		List<String> patchNames = patchList.getSelectedValuesList();
		if (patchNames.isEmpty())
			return;
		
		for (String patchName : patchNames)
		{
			GraphicObject gobj = fetchGraphicObjectForName(patchName);
			TextureSet.Patch patch = currentTexture.createPatch(patchName);
			if (gobj != null)
			{
				if (gobj instanceof Picture)
					createPatch(patch, (Picture)gobj);
				else
					createPatch(patch, (PNGPicture)gobj);
			}
			else
			{
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

	private void onPatchClone()
	{
		for (PatchGraphic pg : patchList.getSelectedValuesList())
		{
			TextureSet.Patch origpatch = pg.getPatch();
			TextureSet.Patch patch = currentTexture.createPatch(origpatch.getName());
			patch.setOriginX(origpatch.getOriginX());
			patch.setOriginY(origpatch.getOriginY());
			patchListModel.addPatch(pg.cloneUsing(patch));
		}
		patchList.setSelectedIndex(patchListModel.getSize() - 1);
		
	}

	private void onPatchRename()
	{
		int selectedIndex = patchList.getSelectedIndex();
		PatchGraphic pg = patchList.getSelectedValuesList().get(0);
		
		final JFormField<String> nameField = stringField(pg.getPatch().getName(), false, true); 

		String value;
		
		Boolean ok = modal(
			getApplicationContainer(),
			language.getText("wadtex.texture.editor.patch.rename"),
			containerOf(borderLayout(),
				node(BorderLayout.CENTER, utils.createForm(
					form(LabelSide.LEADING, language.getInteger("wadtex.texture.editor.patch.rename.labelwidth")),
					utils.formField("wadtex.texture.editor.patch.rename.name", nameField)
				))
			),
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		value = nameField.getValue();
		
		if (!projectPatchSources.containsKey(value))
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wadtex.texture.editor.patch.rename.notexist")))
				return;
		}
		
		// do "rename"
		TextureSet.Patch origPatch = pg.getPatch();
		patchListModel.removePatch(selectedIndex);
		
		GraphicObject gobj = fetchGraphicObjectForName(value);
		TextureSet.Patch patch = currentTexture.createPatch(value);
		patch.setOriginX(origPatch.getOriginX());
		patch.setOriginY(origPatch.getOriginY());
		if (gobj != null)
		{
			if (gobj instanceof Picture)
				createPatch(patch, (Picture)gobj, selectedIndex);
			else
				createPatch(patch, (PNGPicture)gobj, selectedIndex);
		}
		else
		{
			createPatch(patch, NO_PATCH, selectedIndex);
		}
	}

	private void onTextOpen()
	{
		Modal<Boolean> editorModal = null;
		
		EditorMultiFilePanel editorPanel =  new EditorMultiFilePanel(new EditorMultiFilePanel.Options()
		{
			@Override
			public boolean hideTreeActions()
			{
				return true;
			}
			
			@Override
			public boolean hideStyleChangePanel()
			{
				return true;
			}
			
			@Override
			public boolean forbidClose() 
			{
				return true;
			}
		});
		
		editorModal = modal(getApplicationContainer(),
			language.getText("wadtex.texture.editor.texteditor.title"),
			containerOf(dimension(640, 480), node(editorPanel)),
			utils.createChoiceFromLanguageKey("wadtex.texture.editor.texteditor.commit", Boolean.TRUE)
		);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 16);
		try (PrintWriter pw = new PrintWriter(bos)) 
		{
			Utility.writeDEUTEXFile(textureListModel.textures, "; " + language.getText("wadtex.texture.editor.texteditor.comment"), pw);
		} 
		catch (IOException e)
		{
			// Not happening.
			throw new RuntimeException("INTERNAL ERROR: BAD INTERNAL BUFFER", e); 
		}
		
		String editorContent = new String(bos.toByteArray());
		boolean parseOkay = false;
		editorPanel.newEditor("TEXTURESET", "");
		TextureSet textureSet = textureListModel.textures;
		Boolean result;
		
		do {
			EditorHandle handle = editorPanel.getEditorByIndex(0);
			handle.changeStyleName(DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX);
			handle.setContent(editorContent);
			result = editorModal.open();
			
			if (result != Boolean.TRUE) // DON'T CHANGE
				return;
			
			editorContent = handle.getContent();
			
			try (BufferedReader br = new BufferedReader(new StringReader(editorContent))) 
			{
				textureSet = Utility.readDEUTEXFile(br);
				parseOkay = true;
			} 
			catch (IOException e)
			{
				// Not happening.
				throw new RuntimeException("INTERNAL ERROR: BAD INTERNAL BUFFER", e); 
			} 
			catch (ParseException e) 
			{
				SwingUtils.error(getApplicationContainer(), language.getText("wadtex.texture.editor.texteditor.error.parse", e.getLocalizedMessage()));
			}
			
		} while (!parseOkay);
		
		editorModal.dispose();
		
		commitTextureSet(textureSet);
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
		return createPatch(patch, p, patchListModel.getSize());
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param p the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, PNGPicture p)
	{
		return createPatch(patch, p, patchListModel.getSize());
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param image the patch picture.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, Image image)
	{
		return createPatch(patch, image, patchListModel.getSize());
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param p the patch picture.
	 * @param index the target index.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, Picture p, int index)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setPicture(p);
		patchListModel.addPatch(index, newPatch);
		patchList.setSelectedIndex(index);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param p the patch picture.
	 * @param index the target index.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, PNGPicture p, int index)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setPNGPicture(p);
		patchListModel.addPatch(index, newPatch);
		patchList.setSelectedIndex(index);
		return newPatch;
	}

	/**
	 * Adds a patch to this texture canvas.
	 * @param patch the patch.
	 * @param image the patch picture.
	 * @param index the target index.
	 * @return a reference to the created patch.
	 */
	public PatchGraphic createPatch(TextureSet.Patch patch, Image image, int index)
	{
		PatchGraphic newPatch = canvas.new PatchGraphic(patch);
		newPatch.setImage(image);
		patchListModel.addPatch(index, newPatch);
		patchList.setSelectedIndex(index);
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
		 * Creates a state to load from later.
		 * @return a state string.
		 */
		public String createState()
		{
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
				PrintWriter pw = new PrintWriter(bos);
				Utility.writeDEUTEXFile(textures, "; State metadata", pw);
				byte[] zippedBytes = EncodingUtils.gzipBytes(bos.toByteArray());
				return EncodingUtils.asBase64(zippedBytes);
			} catch (IOException e) {
				LOG.error(e, "Error creating texture set state.");
				return null;
			}
		}
		
		/**
		 * Restores a state created by {@link #createState()}.
		 * @param state the state string.
		 */
		public void restoreState(String state)
		{
			try {
				byte[] zippedBytes = EncodingUtils.fromBase64(state);
				String deuTex = new String(EncodingUtils.gunzipBytes(zippedBytes));
				setTextures(Utility.readDEUTEXFile(new BufferedReader(new StringReader(deuTex))));
			} catch (IOException e) {
				LOG.error(e, "Error restoring texture set state.");
			} catch (ParseException e) {
				LOG.error(e, "Parse Error restoring texture set state.");
			}
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
		 * Checks if a texture exists by name.
		 * @param name the name of the texture.
		 * @return true if so, false if not.
		 */
		public boolean containsTexture(String name) 
		{
			return textures.getTextureByName(name) != null;
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
