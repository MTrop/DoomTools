package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.object.GraphicObject;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertOffsetterSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DImageConvertOffsetterCanvas;
import net.mtrop.doom.tools.gui.swing.panels.DImageConvertOffsetterCanvas.GuideMode;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.swing.ClipboardUtils;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ListSelectionMode;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.LayoutFactory.Flow;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;

/**
 * DImageConvert Offsetter program.
 * Bulk-offset graphics in Doom.
 * @author Matthew Tropiano
 */
public class DImageConvertOffsetterApp extends DoomToolsApplicationInstance
{
	private static final String DIMGCONV_OFFSETTER_FILEHEADER = "*DIMGCONV-Offsetter";

	private static final int ZOOMFACTOR_MIN = 1;
	private static final int ZOOMFACTOR_MAX = 8;
	private static final double ZOOMFACTOR_STEP = .5;

	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(DImageConvertOffsetterApp.class); 

	private static final byte[] PNG_SIGNATURE = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	
	private DoomToolsIconManager icons;
	private DImageConvertOffsetterSettingsManager settings;
	
	private JFormField<File> paletteSourceField;
	private JFormField<Double> zoomFactorField;
	private JFormField<Boolean> onionSkinField;
	private DImageConvertOffsetterCanvas canvas;
	private JFormField<Short> offsetXField;
	private JFormField<Short> offsetYField;
	private JComboBox<GuideMode> guideModeField;
	private JFormField<Boolean> autosaveField;
	
	private Action autoAlignAction;
	private Action adjustAlignAction;
	private Action copyOffsetsAction;
	private Action pasteOffsetsAction;
	private Action autoAlignBulkAction;
	private Action adjustAlignBulkAction;
	private Action setAlignBulkAction;
	private Action importOffsetsAction;
	private Action exportOffsetsAction;
	private Action sortByNameAction;
	private Action sortByFrameAction;
	
	private JPopupMenu filePopupMenu;
	private JPopupMenu offsetPopupMenu;
	
	private JList<File> fileList;
	private DirectoryListModel fileListModel;
	
	private File currentDirectory;
	private JLabel currentDirectoryLabel;
	private File currentFile;
	private boolean onionSkin;
	private boolean autoSave;

	private DoomToolsStatusPanel statusPanel;
	
	public DImageConvertOffsetterApp(File startingDirectory, String paletteWadPath)
	{
		this();
		
		if (!startingDirectory.isDirectory())
			startingDirectory = startingDirectory.getParentFile(); 
		
		File paletteFile = paletteWadPath != null ? new File(paletteWadPath) : settings.getLastPaletteFile();
		
		LOG.infof("Using palette file: %s", paletteFile != null ? paletteFile.getPath() : "null");
		
		onPaletteFileSelect(paletteFile);
		onDirectoryChange(startingDirectory);
	}
	
	public DImageConvertOffsetterApp()
	{
		this.icons = DoomToolsIconManager.get();
		this.settings = DImageConvertOffsetterSettingsManager.get();
		this.canvas = new DImageConvertOffsetterCanvas();
		
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
	
		this.zoomFactorField = spinnerField(spinner(spinnerModel(ZOOMFACTOR_MIN, ZOOMFACTOR_MIN, ZOOMFACTOR_MAX, ZOOMFACTOR_STEP), (c) -> onZoomFactorChanged((Double)c.getValue())));
		this.onionSkinField = checkBoxField(checkBox(language.getText("dimgconv.offsetter.onion"), false, this::onOnionSkinChange));
		this.offsetXField = shortField((short)0, this::onOffsetXChanged);
		this.offsetYField = shortField((short)0, this::onOffsetYChanged);
		this.guideModeField = comboBox(Arrays.asList(GuideMode.SPRITE, GuideMode.HUD), this::onGuideModeChange);
		this.autosaveField = checkBoxField(checkBox(language.getText("dimgconv.offsetter.autosave"), false, this::onAutoSaveChange));
		
		this.autoAlignAction = actionItem(language.getText("dimgconv.offsetter.offset.auto"), (e) -> onAutoAlign());
		this.adjustAlignAction = actionItem(language.getText("dimgconv.offsetter.offset.adjust"), (e) -> onAdjustAlign());
		this.copyOffsetsAction = actionItem(language.getText("dimgconv.offsetter.offset.copy"), (e) -> onCopyOffsets());
		this.pasteOffsetsAction = actionItem(language.getText("dimgconv.offsetter.offset.paste"), (e) -> onPasteOffsets());
		this.autoAlignBulkAction = actionItem(language.getText("dimgconv.offsetter.offset.auto.bulk"), (e) -> onAutoAlignBulk());
		this.adjustAlignBulkAction = actionItem(language.getText("dimgconv.offsetter.offset.adjust.bulk"), (e) -> onAdjustAlignBulk());
		this.setAlignBulkAction = actionItem(language.getText("dimgconv.offsetter.offset.set.bulk"), (e) -> onSetAlignBulk());
		this.importOffsetsAction = actionItem(language.getText("dimgconv.offsetter.offset.import"), (e) -> onImportOffsets());
		this.exportOffsetsAction = actionItem(language.getText("dimgconv.offsetter.offset.export"), (e) -> onExportOffsets());
		this.sortByNameAction = actionItem(language.getText("dimgconv.offsetter.sort.name"), (e) -> onSortByName());
		this.sortByFrameAction = actionItem(language.getText("dimgconv.offsetter.sort.frame"), (e) -> onSortByFrame());
		
		this.filePopupMenu = popupMenu(
			menuItem(autoAlignBulkAction),
			menuItem(setAlignBulkAction),
			menuItem(adjustAlignBulkAction),
			separator(),
			menuItem(importOffsetsAction),
			menuItem(exportOffsetsAction),
			separator(),
			menuItem(sortByNameAction),
			menuItem(sortByFrameAction)
		);
		
		this.offsetPopupMenu = popupMenu(
			menuItem(autoAlignAction),
			menuItem(adjustAlignAction),
			separator(),
			menuItem(copyOffsetsAction),
			menuItem(pasteOffsetsAction)
		);
		
		this.fileListModel = new DirectoryListModel();
		this.fileList = list(fileListModel, ListSelectionMode.MULTIPLE_INTERVAL, this::onFileSelect);
		this.fileList.setCellRenderer(new FileListRenderer());
		
		this.currentDirectory = null;
		this.currentDirectoryLabel = label("");
		this.currentFile = null;
		this.autoSave = false;
		
		this.statusPanel = new DoomToolsStatusPanel();
		this.statusPanel.setSuccessMessage(language.getText("dimgconv.offsetter.status.ready"));
	
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
				offsetXField.setValue((short)(
					offsetXField.getValue() + (lastX - e.getX())
				));
				offsetYField.setValue((short)(
					offsetYField.getValue() + (lastY - e.getY())
				));
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
						offsetXField.setValue((short)(offsetXField.getValue() + 1));
						break;
					case KeyEvent.VK_RIGHT:
						offsetXField.setValue((short)(offsetXField.getValue() - 1));
						break;
					case KeyEvent.VK_UP:
						offsetYField.setValue((short)(offsetYField.getValue() + 1));
						break;
					case KeyEvent.VK_DOWN:
						offsetYField.setValue((short)(offsetYField.getValue() - 1));
						break;
				}
			}
		};
		
		fileList.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				if (e.isPopupTrigger())
					doFilePopupTrigger(e.getComponent(), e.getX(), e.getY());
			}
	
			@Override
			public void mouseReleased(MouseEvent e) 
			{
				if (e.isPopupTrigger())
					doFilePopupTrigger(e.getComponent(), e.getX(), e.getY());
			}
		});
		fileList.addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
				{
					Point point = e.getComponent().getMousePosition();
					if (point != null)
						doFilePopupTrigger(e.getComponent(), point.x, point.y);
				}
			}
		});
		
		canvas.addMouseListener(canvasMouseAdapter);
		canvas.addMouseMotionListener(canvasMouseAdapter);
		canvas.addMouseWheelListener(canvasMouseAdapter);
		canvas.addKeyListener(canvasKeyboardAdapter);
		
		updateActions();
		onSortByName();
	}

	@Override
	public String getTitle() 
	{
		return language.getText("dimgconv.offsetter.title");
	}

	@Override
	public Container createContentPane() 
	{
		JComponent directoryPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 0),
				node(BorderLayout.WEST, button(icons.getImage("folder.png"), (b) -> onDirectorySelect())),
				node(BorderLayout.CENTER, currentDirectoryLabel)
			)),
			node(BorderLayout.CENTER, scroll(fileList)),
			node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.LEFT),
				node(autosaveField)
			))
		);
		
		JComponent canvasPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 0),
				node(BorderLayout.WEST, containerOf(flowLayout(Flow.LEADING, 4, 0),
					node(label(language.getText("dimgconv.offsetter.zoom"))),
					node(zoomFactorField),
					node(onionSkinField)
				)),
				node(BorderLayout.CENTER, containerOf(borderLayout(4, 0),
					node(BorderLayout.WEST, label(language.getText("dimgconv.offsetter.palette.source"))),
					node(BorderLayout.CENTER, paletteSourceField)
				))
			)),
			node(BorderLayout.CENTER, canvas),
			node(BorderLayout.SOUTH, containerOf(borderLayout(),
				node(BorderLayout.WEST, containerOf(flowLayout(Flow.LEADING, 4, 0),
					node(label(language.getText("dimgconv.offsetter.offset"))),
					node(label(language.getText("dimgconv.offsetter.offset.x"))),
					node(offsetXField),
					node(label(language.getText("dimgconv.offsetter.offset.y"))),
					node(offsetYField),
					node(button("\u25bc" /* Black Down-pointing Triangle */, (b) -> doOffsetPopupTrigger(b, b.getWidth(), 0)))
				)),
				node(BorderLayout.EAST, containerOf(flowLayout(Flow.LEADING, 4, 0),
					node(label(language.getText("dimgconv.offsetter.guidemode"))),
					node(guideModeField)
				))
			))
		);
				
		return containerOf(borderLayout(4, 4),
			node(BorderLayout.WEST, dimension(150, 1), directoryPanel), 
			node(BorderLayout.CENTER, dimension(512, 400), canvasPanel),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState(); 
		
		state.put("palette.path", paletteSourceField.getValue() == null ? "" : paletteSourceField.getValue().getPath());
		state.put("selected.directory", currentDirectory == null ? "" : currentDirectory.getAbsolutePath());
		state.put("selected.file", fileList.getSelectedValue() == null ? "" : fileList.getSelectedValue().getPath());
		
		state.put("zoom.factor", String.valueOf(zoomFactorField.getValue()));
		state.put("offset.x", String.valueOf(offsetXField.getValue()));
		state.put("offset.y", String.valueOf(offsetYField.getValue()));
		state.put("guide.mode", String.valueOf(guideModeField.getSelectedItem()));
		
		state.put("autosave", String.valueOf(autosaveField.getValue()));
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		super.setApplicationState(state);
		paletteSourceField.setValue(ValueUtils.parse(state.get("palette.path"), (s) -> s == null ? null : new File(s)));
		currentDirectory = ValueUtils.parse(state.get("selected.directory"), (s) -> s == null ? null : new File(s));
		onDirectoryChange(currentDirectory);
		fileList.setSelectedValue(ValueUtils.parse(state.get("selected.file"),(s) -> s == null ? null : new File(s)), true);
		
		zoomFactorField.setValue(ValueUtils.parseDouble(state.get("zoom.factor"), 1));
		offsetXField.setValue(ValueUtils.parseShort(state.get("offset.x"), (short)0));
		offsetYField.setValue(ValueUtils.parseShort(state.get("offset.y"), (short)0));
		guideModeField.setSelectedItem(ValueUtils.parse(state.get("guide.mode"), (s) -> s == null ? GuideMode.SPRITE : GuideMode.MAP_VALUES.get(s)));
		
		autosaveField.setValue(ValueUtils.parseBoolean("autosave", false));
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

	/**
	 * Opens a dialog for opening a directory and returns an application instance. 
	 * @param parent the parent window for the dialog.
	 * @return a new app instance, or null if bad directory selected.
	 */
	public static DImageConvertOffsetterApp openAndCreate(Component parent) 
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get();
		DImageConvertOffsetterSettingsManager settings = DImageConvertOffsetterSettingsManager.get();
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
		File projectDir = utils.chooseDirectory(
			parent,
			language.getText("dimgconv.offsetter.open.directory.title"),
			language.getText("dimgconv.offsetter.open.directory.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile
		);
		
		if (projectDir == null)
			return null;
		
		if (!projectDir.isDirectory())
			return null;
		
		return new DImageConvertOffsetterApp(projectDir, null);
	}

	private void onDirectoryChange(File directory)
	{
		currentDirectory = directory;
		fileListModel.setDirectory(directory);
		SwingUtils.invoke(() -> {
			currentDirectoryLabel.setText(currentDirectory.getName());
		});
	}
	
	private void onDirectorySelect()
	{
		File projectDir = utils.chooseDirectory(
			getApplicationContainer(),
			language.getText("dimgconv.offsetter.open.directory.title"),
			language.getText("dimgconv.offsetter.open.directory.accept"),
			() -> currentDirectory,
			settings::setLastTouchedFile
		);
		
		if (projectDir != null && projectDir.isDirectory())
		{
			onDirectoryChange(projectDir);
		}
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
			SwingUtils.error(language.getText("dimgconv.offsetter.palette.source.error.ioerror", selectedFile));
			return;
		}
		
		Palette pal = null;

		// If WAD, search for PlayPal
		if (wadfile)
		{
			try (WadFile wf = new WadFile(selectedFile)) {
				pal = wf.getDataAs("PLAYPAL", Palette.class);
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.palette.source.error.ioerror", selectedFile));
				return;
			}
		}
		// else, attempt to load as palette.
		else
		{
			try {
				pal = BinaryObject.read(Palette.class, selectedFile);
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.palette.source.error.notpal", selectedFile));
				return;
			}
		}
		
		if (pal != null)
			settings.setLastPaletteFile(selectedFile);
		
		canvas.setPalette(pal);
	}
	
	private void onZoomFactorChanged(double zoomFactor)
	{
		canvas.setZoomFactor((float)zoomFactor);
	}
	
	private void onOffsetXChanged(short val)
	{
		canvas.setOffsets(val, offsetYField.getValue());
	}
	
	private void onOffsetYChanged(short val)
	{
		canvas.setOffsets(offsetXField.getValue(), val);
	}
	
	private void onGuideModeChange(GuideMode mode)
	{
		canvas.setGuideMode(mode);
	}
	
	private void onOnionSkinChange(boolean selected)
	{
		onionSkin = selected;
		updateOnionSkinFile();
	}
	
	private void onAutoSaveChange(boolean selected)
	{
		autoSave = selected;
	}
	
	private void onFileSelect(List<File> files, boolean adjusting)
	{
		// save previous
		if (currentFile != null && canvas.offsetsDiffer())
		{
			if (autoSave || SwingUtils.yesTo(language.getText("dimgconv.offsetter.save.changes", currentFile.getName())))
			{
				statusPanel.setActivityMessage(language.getText("dimgconv.offsetter.status.savingfile"));
				if (canvas.getPicture() != null)
				{
					savePicture(canvas.getPicture(), offsetXField.getValue(), offsetYField.getValue(), currentFile, statusPanel);
				}
				else if (canvas.getPNGPicture() != null)
				{
					savePNGPicture(canvas.getPNGPicture(), offsetXField.getValue(), offsetYField.getValue(), currentFile, statusPanel);
				}
				else
				{
					// Do nothing.
				}
			}
		}
		
		updateActions();

		if (files.size() == 1)
		{
			// load next picture
			File selected = files.get(0);
			currentFile = updateNextFile(selected);
		}
		else
		{
			canvas.clearPictures();
			offsetXField.setValue((short)0);
			offsetYField.setValue((short)0);
			currentFile = null;
		}

		updateOnionSkinFile();
	}

	private File updateNextFile(File selected) 
	{
		try {
			if (FileUtils.matchMagicNumber(selected, PNG_SIGNATURE)) // png?
			{
				PNGPicture p = BinaryObject.read(PNGPicture.class, selected);
				canvas.setPNGPicture(p);
				offsetXField.setValue((short)p.getOffsetX());
				offsetYField.setValue((short)p.getOffsetY());
				return selected;
			}
			else // picture?
			{
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
					canvas.setPicture(p);
					offsetXField.setValue((short)p.getOffsetX());
					offsetYField.setValue((short)p.getOffsetY());
					return selected;
				}
				else
				{
					canvas.clearPictures();
					offsetXField.setValue((short)0);
					offsetYField.setValue((short)0);
					return null;
				}
			}
		} catch (IOException e) {
			SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", selected.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.readfile.error", currentFile.getName()));
			canvas.clearPictures();
			return null;
		}
	}
	
	private File updateNextOnionSkinFile(File selected) 
	{
		try {
			if (FileUtils.matchMagicNumber(selected, PNG_SIGNATURE)) // png?
			{
				PNGPicture p = BinaryObject.read(PNGPicture.class, selected);
				canvas.setOnionSkinPNGPicture(p);
				return selected;
			}
			else // picture?
			{
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
					canvas.setOnionSkinPicture(p);
					return selected;
				}
				else
				{
					canvas.clearOnionSkinPicture();
					return null;
				}
			}
		} catch (IOException e) {
			SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", selected.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.readfile.error", currentFile.getName()));
			canvas.clearOnionSkinPicture();
			return null;
		}
	}

	private boolean checkPictureBounds(int w, int h, int ox, int oy)
	{
		return w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024;
	}
	
	private void updateOnionSkinFile()
	{
		if (!onionSkin)
		{
			canvas.clearOnionSkinPicture();
			return;
		}
		
		int index;
		int[] selectedIndices = fileList.getSelectedIndices();
		if (selectedIndices.length != 1)
		{
			canvas.clearOnionSkinPicture();
			return;
		}
		
		index = selectedIndices[0];
		
		if (index > 0)
		{
			updateNextOnionSkinFile(fileList.getModel().getElementAt(index - 1));
			currentFile = updateNextFile(currentFile);
		}
	}

	private void updateActions()
	{
		sortByNameAction.setEnabled(true);
		sortByFrameAction.setEnabled(true);
		importOffsetsAction.setEnabled(true);
		
		List<File> files = fileList.getSelectedValuesList();
		
		if (files.size() == 1)
		{
			autoAlignAction.setEnabled(true);
			adjustAlignAction.setEnabled(true);
			copyOffsetsAction.setEnabled(true);
			pasteOffsetsAction.setEnabled(true);
			autoAlignBulkAction.setEnabled(true);
			adjustAlignBulkAction.setEnabled(true);
			setAlignBulkAction.setEnabled(true);
			exportOffsetsAction.setEnabled(true);
		}
		else
		{
			autoAlignAction.setEnabled(false);
			adjustAlignAction.setEnabled(false);
			copyOffsetsAction.setEnabled(false);
			pasteOffsetsAction.setEnabled(false);
			autoAlignBulkAction.setEnabled(!files.isEmpty());
			adjustAlignBulkAction.setEnabled(!files.isEmpty());
			setAlignBulkAction.setEnabled(!files.isEmpty());
			exportOffsetsAction.setEnabled(!files.isEmpty());
		}
	}
	
	private void onAutoAlign()
	{
		AutoAlignMode mode = selectAutoAlignMode();
		if (mode == null)
			return;
		
		if (canvas.getPicture() != null)
		{
			Picture p = canvas.getPicture();
			mode.alignGraphic(p);
			offsetXField.setValue((short)p.getOffsetX());
			offsetYField.setValue((short)p.getOffsetY());
		}
		else if (canvas.getPNGPicture() != null)
		{
			PNGPicture p = canvas.getPNGPicture();
			mode.alignGraphic(p);
			offsetXField.setValue((short)p.getOffsetX());
			offsetYField.setValue((short)p.getOffsetY());
		}
	}

	private void onAutoAlignBulk()
	{
		AutoAlignMode mode = selectAutoAlignMode();
		if (mode == null)
			return;
		
		int count = 0;
		for (File file : fileList.getSelectedValuesList())
		{
			try {
				if (FileUtils.matchMagicNumber(file, PNG_SIGNATURE)) // png?
				{
					PNGPicture p = BinaryObject.read(PNGPicture.class, file);
					mode.alignGraphic(p);
					savePNGPicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
				}
				else
				{
					Picture p = BinaryObject.read(Picture.class, file);
					mode.alignGraphic(p);
					savePicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
				}
				count++;
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.security", file.getName()));
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", file.getName()));
			}
		}
		
		SwingUtils.info(language.getText("dimgconv.offsetter.offset.auto.bulk.count", count));
	}
	
	private void onAdjustAlign()
	{
		Point offset = selectAdjustAlign();
		if (offset == null)
			return;
		
		if (canvas.getPicture() != null)
		{
			Picture p = canvas.getPicture();
			p.setOffsetX(p.getOffsetX() + offset.x);
			p.setOffsetY(p.getOffsetY() + offset.y);
			offsetXField.setValue((short)p.getOffsetX());
			offsetYField.setValue((short)p.getOffsetY());
		}
		else if (canvas.getPNGPicture() != null)
		{
			PNGPicture p = canvas.getPNGPicture();
			p.setOffsetX(p.getOffsetX() + offset.x);
			p.setOffsetY(p.getOffsetY() + offset.y);
			offsetXField.setValue((short)p.getOffsetX());
			offsetYField.setValue((short)p.getOffsetY());
		}
	}

	private void onAdjustAlignBulk()
	{
		Point offset = selectAdjustAlign();
		if (offset == null)
			return;

		int count = 0;
		for (File file : fileList.getSelectedValuesList())
		{
			try {
				if (FileUtils.matchMagicNumber(file, PNG_SIGNATURE)) // png?
				{
					PNGPicture p = BinaryObject.read(PNGPicture.class, file);
					p.setOffsetX(p.getOffsetX() + offset.x);
					p.setOffsetY(p.getOffsetY() + offset.y);
					savePNGPicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
				}
				else
				{
					Picture p = BinaryObject.read(Picture.class, file);
					p.setOffsetX(p.getOffsetX() + offset.x);
					p.setOffsetY(p.getOffsetY() + offset.y);
					savePicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
				}
				count++;
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.security", file.getName()));
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", file.getName()));
			}
		}
		
		SwingUtils.info(language.getText("dimgconv.offsetter.offset.adjust.bulk.count", count));
	}

	private void onSetAlignBulk() 
	{
		Point offset = selectSetAlign();
		if (offset == null)
			return;

		int count = 0;
		for (File file : fileList.getSelectedValuesList())
		{
			try {
				setAlignmentOnFile(file, offset);
				count++;
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.security", file.getName()));
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", file.getName()));
			}
		}
		
		SwingUtils.info(language.getText("dimgconv.offsetter.offset.adjust.bulk.count", count));
	}

	private void setAlignmentOnFile(File file, Point offsets) throws IOException 
	{
		if (FileUtils.matchMagicNumber(file, PNG_SIGNATURE)) // png?
		{
			PNGPicture p = BinaryObject.read(PNGPicture.class, file);
			p.setOffsetX(offsets.x);
			p.setOffsetY(offsets.y);
			savePNGPicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
		}
		else
		{
			Picture p = BinaryObject.read(Picture.class, file);
			p.setOffsetX(offsets.x);
			p.setOffsetY(offsets.y);
			savePicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
		}
	}

	private void onCopyOffsets()
	{
		ClipboardUtils.sendStringToClipboard("DIMGCONV:Offset:" + offsetXField.getValue() + ":" + offsetYField.getValue());
	}

	private void onPasteOffsets()
	{
		String clipboardContents = ClipboardUtils.getStringFromClipboard();
		String[] tokens = clipboardContents.split(":");
		
		if (tokens.length < 4)
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.paste.parserror"));
			return;
		}

		if (!tokens[0].equals("DIMGCONV") || !tokens[1].equals("Offset"))
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.paste.parserror"));
			return;
		}

		short x = Short.parseShort(tokens[2]);
		short y = Short.parseShort(tokens[3]);

		offsetXField.setValue(x);
		offsetYField.setValue(y);
	}

	private void onImportOffsets() 
	{
		JFormField<File> sourceFile = fileField("...",
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("dimgconv.offsetter.offset.import.file.browse"), 
				language.getText("dimgconv.offsetter.offset.import.file.select"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createTextFileFilter()
			) 
		);
		
		Boolean ok = modal(language.getText("dimgconv.offsetter.offset.import.title"),
			containerOf(node(utils.createForm(form(language.getInteger("dimgconv.offsetter.offset.import.labelwidth")),
				utils.formField("dimgconv.offsetter.offset.import.file", sourceFile)
			))),
			choice(language.getText("doomtools.ok"), Boolean.TRUE),
			choice(language.getText("doomtools.cancel"), Boolean.FALSE)
		).openThenDispose();

		if (ok != Boolean.TRUE)
			return;
		

		int count = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile.getValue()))))
		{
			String fileName;
			short offsetX, offsetY;
			String line = reader.readLine();
			
			if (!DIMGCONV_OFFSETTER_FILEHEADER.equals(line))
			{
				SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.import.file.badfile"));
				return;
			}
			
			while ((line = reader.readLine()) != null)
			{
				try (TokenScanner scanner = new TokenScanner(line)) 
				{
					fileName = scanner.nextString();
					offsetX = scanner.nextShort();
					offsetY = scanner.nextShort();
					
					File foundFile = fileListModel.findElement(fileName);
					
					if (foundFile != null)
					{
						setAlignmentOnFile(foundFile, new Point(offsetX, offsetY));
						count++;
					}
				} 
				catch (NoSuchElementException e) 
				{
					// skip bad lines.
					continue;
				}
			}
			
		} 
		catch (FileNotFoundException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.import.file.notfound"));
			return;
		} 
		catch (IOException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.import.file.ioerror"));
			return;
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.import.file.security"));
			return;
		}
		
		SwingUtils.info(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.import.file.count", count));
	}
	
	private void onExportOffsets()
	{
		FileFilter filter = utils.createTextFileFilter();
		JFormField<File> targetFile = fileField("...",
			(current) -> utils.chooseFile(
				getApplicationContainer(),
				language.getText("dimgconv.offsetter.offset.export.file.browse"), 
				language.getText("dimgconv.offsetter.offset.export.file.select"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				(f, input) -> (f == filter ? FileUtils.addMissingExtension(input, "txt") : input),
				filter
			) 
		);
		
		Boolean ok = modal(language.getText("dimgconv.offsetter.offset.export.title"),
			containerOf(node(utils.createForm(form(language.getInteger("dimgconv.offsetter.offset.export.labelwidth")),
				utils.formField("dimgconv.offsetter.offset.export.file", targetFile)
			))),
			choice(language.getText("doomtools.ok"), Boolean.TRUE),
			choice(language.getText("doomtools.cancel"), Boolean.FALSE)
		).openThenDispose();
		
		if (ok != Boolean.TRUE || targetFile.getValue() == null)
			return;
		
		int count = 0;
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getValue()))))
		{
			writer.append(DIMGCONV_OFFSETTER_FILEHEADER).append('\n');
			for (File f : fileList.getSelectedValuesList())
			{
				GraphicObject graphic;
				if (FileUtils.matchMagicNumber(f, PNG_SIGNATURE)) // png?
					graphic = BinaryObject.read(PNGPicture.class, f);
				else
					graphic = BinaryObject.read(Picture.class, f);
				
				writer.append(f.getName()).append(' ')
					.append(String.valueOf(graphic.getOffsetX())).append(' ')
					.append(String.valueOf(graphic.getOffsetY()))
					.append('\n');
				count++;
			}
		} 
		catch (IOException e)
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.export.file.ioerror"));
			return;
		} 
		catch (SecurityException e) 
		{
			SwingUtils.error(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.export.file.security"));
			return;
		}
		
		SwingUtils.info(getApplicationContainer(), language.getText("dimgconv.offsetter.offset.export.file.count", count));
	}

	private void onSortByName()
	{
		fileListModel.setSort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
		onFileSelect(fileList.getSelectedValuesList(), false);
	}
	
	private void onSortByFrame()
	{
		fileListModel.setSort((a, b) -> 
		{
			String aname = FileUtils.getFileNameWithoutExtension(a.getName());
			String bname = FileUtils.getFileNameWithoutExtension(b.getName());
			
			String asprite = aname.substring(0, Math.min(aname.length(), 4));
			String bsprite = bname.substring(0, Math.min(bname.length(), 4));
			
			String aframe = "";
			String bframe = "";
			String arot = "";
			String brot = "";
			if (aname.length() > 4)
				aframe = Character.toString(aname.charAt(4));
			if (aname.length() > 5)
				arot = Character.toString(aname.charAt(5));
			if (bname.length() > 4)
				bframe = Character.toString(bname.charAt(4));
			if (bname.length() > 5)
				brot = Character.toString(bname.charAt(5));
			
			int rotCompare = arot.compareToIgnoreCase(brot);
			int frameCompare = aframe.compareToIgnoreCase(bframe);
			int spriteCompare = asprite.compareToIgnoreCase(bsprite);
			
			return spriteCompare == 0 
				? (rotCompare == 0 ? frameCompare : rotCompare)
				: spriteCompare
			;
		});
		onFileSelect(fileList.getSelectedValuesList(), false);
	}

	private void doFilePopupTrigger(Component c, int x, int y)
	{
		filePopupMenu.show(c, x, y);
	}

	private void doOffsetPopupTrigger(Component c, int x, int y)
	{
		offsetPopupMenu.show(c, x, y);
	}

	private static AutoAlignMode selectAutoAlignMode()
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get(); 

		JComboBox<AutoAlignMode> combo = comboBox(comboBoxModel(Arrays.asList(AutoAlignMode.values())));
		
		Boolean ok = modal(language.getText("dimgconv.offsetter.offset.auto.title"),
			containerOf(node(combo)),
			choice(language.getText("doomtools.ok"), Boolean.TRUE),
			choice(language.getText("doomtools.cancel"), Boolean.FALSE)
		).openThenDispose();
		
		return ok == Boolean.TRUE ? combo.getItemAt(combo.getSelectedIndex()) : null; 
	}

	private static Point selectOffsetDialog(String titleKey)
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get(); 
	
		JFormField<Short> offsetX = shortField((short)0, false);
		JFormField<Short> offsetY = shortField((short)0, false);
		
		Boolean ok = modal(language.getText(titleKey),
			containerOf(flowLayout(Flow.LEADING, 4, 0),
				node(label(language.getText("dimgconv.offsetter.offset.x"))),
				node(offsetX),
				node(label(language.getText("dimgconv.offsetter.offset.y"))),
				node(offsetY)
			),
			choice(language.getText("doomtools.ok"), Boolean.TRUE),
			choice(language.getText("doomtools.cancel"), Boolean.FALSE)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return null;
		
		return new Point(offsetX.getValue(), offsetY.getValue());
	}

	private static Point selectAdjustAlign()
	{
		String titleKey = "dimgconv.offsetter.offset.adjust.bulk.title";
		return selectOffsetDialog(titleKey);
	}

	private static Point selectSetAlign()
	{
		String titleKey = "dimgconv.offsetter.offset.set.bulk.title";
		return selectOffsetDialog(titleKey);
	}

	private static void savePicture(Picture picture, short offsetX, short offsetY, File destinationFile, DoomToolsStatusPanel statusPanel) 
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get(); 
		
		picture.setOffsetX(offsetX);
		picture.setOffsetY(offsetY);
		try (FileOutputStream fos = new FileOutputStream(destinationFile))
		{
			picture.writeBytes(fos);
			statusPanel.setSuccessMessage(language.getText("dimgconv.offsetter.status.savefile", destinationFile.getName()));
		} 
		catch (FileNotFoundException e) 
		{
			SwingUtils.error(language.getText("dimgconv.offsetter.save.changes.notfound", destinationFile.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.savefile.error", destinationFile.getName()));
		} 
		catch (IOException e) 
		{
			SwingUtils.error(language.getText("dimgconv.offsetter.save.changes.ioerror", destinationFile.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.savefile.error", destinationFile.getName()));
		}
	}

	private static void savePNGPicture(PNGPicture picture, short offsetX, short offsetY, File destinationFile, DoomToolsStatusPanel statusPanel) 
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get(); 
		
		picture.setOffsetX(offsetX);
		picture.setOffsetY(offsetY);
		try (FileOutputStream fos = new FileOutputStream(destinationFile))
		{
			picture.writeBytes(fos);
			statusPanel.setSuccessMessage(language.getText("dimgconv.offsetter.status.savefile", destinationFile.getName()));
		} 
		catch (FileNotFoundException e) 
		{
			SwingUtils.error(language.getText("dimgconv.offsetter.save.changes.notfound", destinationFile.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.savefile.error", destinationFile.getName()));
		} 
		catch (IOException e) 
		{
			SwingUtils.error(language.getText("dimgconv.offsetter.save.changes.ioerror", destinationFile.getName()));
			statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.savefile.error", destinationFile.getName()));
		}
	}

	private enum AutoAlignMode
	{
		OBJECT
		{
			@Override
			protected void alignGraphic(GraphicObject graphic)
			{
				graphic.setOffsetX(graphic.getWidth() / 2);
				graphic.setOffsetY(graphic.getHeight());
			}
		},
		WEAPON
		{
			@Override
			protected void alignGraphic(GraphicObject graphic)
			{
				graphic.setOffsetX(-(320 - graphic.getWidth() - ((320 - graphic.getWidth()) / 2)));
				graphic.setOffsetY(-(200 - 32 - graphic.getHeight()));
			}
		},
		PROJECTILE
		{
			@Override
			protected void alignGraphic(GraphicObject graphic) 
			{
				graphic.setOffsetX(graphic.getWidth() / 2);
				graphic.setOffsetY(graphic.getHeight() / 2);
			}
		};
		
		protected abstract void alignGraphic(GraphicObject graphic);
		
	}
	
	private static class DirectoryListModel implements ListModel<File>
	{
		private List<File> fileList;
		private Map<String, File> fileMap;
		private final List<ListDataListener> listeners;
		
		private DirectoryListModel()
		{
			this.fileList = new ArrayList<>();
			this.fileMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}

		public void setDirectory(File dir)
		{
			int amount = fileList.size();
			
			fileList.clear();
			fileMap.clear();
			
			if (amount > 0)
			{
				listeners.forEach((listener) -> listener.intervalRemoved(
					new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, amount - 1)
				));
			}
			
			for (File f : dir.listFiles((f) -> !f.isHidden() && !f.isDirectory()))
			{
				fileList.add(f);
				fileMap.put(f.getName(), f);
			}
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, fileList.size() - 1)
			));
		}
		
		@Override
		public int getSize() 
		{
			return fileList.size();
		}
		
		/**
		 * Attempts to find and retrieve a file element by its name.
		 * @param fileName the file's name.
		 * @return the corresponding file or null if no file by that name.
		 */
		public File findElement(String fileName)
		{
			return fileMap.get(fileName);
		}

		@Override
		public File getElementAt(int index)
		{
			if (index < 0 || index > fileList.size())
				return null;
			return fileList.get(index);
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
		
		/**
		 * Sorts the list in a different way.
		 * @param comparator the comparator to use.
		 */
		public void setSort(Comparator<File> comparator)
		{
			fileList.sort(comparator);
			listeners.forEach((listener) -> listener.contentsChanged(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, fileList.size() - 1)
			));
		}
	}
	
	private static class FileListRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 2182340892102124806L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
		{
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof File)
				label.setText(((File)value).getName());
			return label;
		}
	}
	
	private static class MouseControlAdapter implements MouseInputListener, MouseWheelListener
	{
		public MouseControlAdapter()
		{
			// Do nothing.
		}
		
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mousePressed(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseReleased(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseEntered(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseExited(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseDragged(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseMoved(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			// Do nothing.
		}
		
	}
	
}
