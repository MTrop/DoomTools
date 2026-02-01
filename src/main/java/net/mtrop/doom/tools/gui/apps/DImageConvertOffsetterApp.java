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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(DImageConvertOffsetterApp.class); 

	private static final byte[] PNG_SIGNATURE = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	
	private DoomToolsIconManager icons;
	private DImageConvertOffsetterSettingsManager settings;
	
	private JFormField<File> paletteSourceField;
	private JFormField<Double> zoomFactorField;
	private DImageConvertOffsetterCanvas canvas;
	private JFormField<Short> offsetXField;
	private JFormField<Short> offsetYField;
	private JComboBox<GuideMode> guideModeField;
	private JFormField<Boolean> autosaveField;
	private Action autoAlignAction;
	private Action autoAlignBulkAction;
	
	private JPopupMenu filePopupMenu;
	
	private JList<File> fileList;
	private DirectoryListModel fileListModel;
	
	private File currentDirectory;
	private JLabel currentDirectoryLabel;
	private File currentFile;
	private boolean autoSave;

	private DoomToolsStatusPanel statusPanel;
	
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

		this.zoomFactorField = spinnerField(spinner(spinnerModel(1, 1, 4, .5), (c) -> onZoomFactorChanged((Double)c.getValue())));
		this.offsetXField = shortField((short)0, this::onOffsetXChanged);
		this.offsetYField = shortField((short)0, this::onOffsetYChanged);
		this.guideModeField = comboBox(Arrays.asList(GuideMode.SPRITE, GuideMode.HUD), this::onGuideModeChange);
		this.autosaveField = checkBoxField(checkBox(language.getText("dimgconv.offsetter.autosave"), false, this::onAutoSaveChange));
		this.autoAlignAction = actionItem(language.getText("dimgconv.offsetter.offset.auto"), (e) -> onAutoAlign());
		this.autoAlignBulkAction = actionItem(language.getText("dimgconv.offsetter.offset.auto.bulk"), (e) -> onAutoAlignBulk());
		
		this.filePopupMenu = popupMenu(
			menuItem(autoAlignBulkAction)
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
					zoomFactorField.setValue(Math.max(1, Math.min(4, zoomFactorField.getValue() + (e.getUnitsToScroll() > 0 ? -.5 : .5))));
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
					doPopupTrigger(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(MouseEvent e) 
			{
				if (e.isPopupTrigger())
					doPopupTrigger(e.getComponent(), e.getX(), e.getY());
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
						doPopupTrigger(e.getComponent(), point.x, point.y);
				}
			}
		});
		
		canvas.addMouseListener(canvasMouseAdapter);
		canvas.addMouseMotionListener(canvasMouseAdapter);
		canvas.addMouseWheelListener(canvasMouseAdapter);
		canvas.addKeyListener(canvasKeyboardAdapter);
		
		autoAlignAction.setEnabled(false);
		autoAlignBulkAction.setEnabled(false);
	}
	
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
					node(zoomFactorField)
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
					node(button(autoAlignAction))
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

		if (files.size() == 1)
		{
			File selected = files.get(0);
			
			autoAlignAction.setEnabled(true);
			autoAlignBulkAction.setEnabled(true);
			
			// load next picture
			try {
				if (FileUtils.matchMagicNumber(selected, PNG_SIGNATURE)) // png?
				{
					PNGPicture p = BinaryObject.read(PNGPicture.class, selected);
					canvas.setPNGPicture(p);
					offsetXField.setValue((short)p.getOffsetX());
					offsetYField.setValue((short)p.getOffsetY());
					currentFile = selected;
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
					if (w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024)
					{
						Picture p = BinaryObject.read(Picture.class, selected);
						canvas.setPicture(p);
						offsetXField.setValue((short)p.getOffsetX());
						offsetYField.setValue((short)p.getOffsetY());
						currentFile = selected;
					}
					else
					{
						canvas.clearPicture();
						offsetXField.setValue((short)0);
						offsetYField.setValue((short)0);
						currentFile = null;
					}
				}
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", selected.getName()));
				statusPanel.setErrorMessage(language.getText("dimgconv.offsetter.status.readfile.error", currentFile.getName()));
				canvas.clearPicture();
				currentFile = null;
			}
		}
		else
		{
			autoAlignAction.setEnabled(false);
			autoAlignBulkAction.setEnabled(!files.isEmpty());

			canvas.clearPicture();
			offsetXField.setValue((short)0);
			offsetYField.setValue((short)0);
			currentFile = null;
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
					count++;
				}
				else
				{
					Picture p = BinaryObject.read(Picture.class, file);
					mode.alignGraphic(p);
					savePicture(p, (short)p.getOffsetX(), (short)p.getOffsetY(), file, statusPanel);
					count++;
				}
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.security", file.getName()));
			} catch (IOException e) {
				SwingUtils.error(language.getText("dimgconv.offsetter.file.ioerror", file.getName()));
			}
		}
		
		SwingUtils.info(language.getText("dimgconv.offsetter.offset.auto.bulk.count", count));
	}
	
	private void doPopupTrigger(Component c, int x, int y)
	{
		filePopupMenu.show(c, x, y);
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
		private final List<ListDataListener> listeners;
		
		private DirectoryListModel()
		{
			this.fileList = new ArrayList<>();
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}

		public void setDirectory(File dir)
		{
			fileList.clear();
			for (File f : dir.listFiles((f) -> !f.isHidden() && !f.isDirectory()))
				fileList.add(f);
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}
		
		@Override
		public int getSize() 
		{
			return fileList.size();
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
