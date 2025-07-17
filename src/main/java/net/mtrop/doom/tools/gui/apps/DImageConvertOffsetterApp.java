package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.event.MouseInputListener;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertOffsetterSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DImageConvertOffsetterCanvas;
import net.mtrop.doom.tools.gui.swing.panels.DImageConvertOffsetterCanvas.GuideMode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * DImageConvert Offsetter program.
 * Bulk-offset graphics in Doom.
 * @author Matthew Tropiano
 */
public class DImageConvertOffsetterApp extends DoomToolsApplicationInstance
{
	private DImageConvertOffsetterSettingsManager settings;
	
	private JFormField<File> paletteSourceField;
	private JFormField<Integer> zoomFactorField;
	private DImageConvertOffsetterCanvas canvas;
	private JFormField<Short> offsetXField;
	private JFormField<Short> offsetYField;
	private JComboBox<GuideMode> guideModeField;
	
	public DImageConvertOffsetterApp(File startingDirectory)
	{
		if (!startingDirectory.isDirectory())
			startingDirectory = startingDirectory.getParentFile(); 
		
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

		this.zoomFactorField = spinnerField(spinner(spinnerModel(1, 1, 4, 1), (c) -> onZoomFactorChanged((Integer)c.getValue())));
		this.offsetXField = shortField((short)0, (value) -> onOffsetXChanged(value));
		this.offsetYField = shortField((short)0, (value) -> onOffsetYChanged(value));
		this.guideModeField = comboBox(ObjectUtils.createList(GuideMode.SPRITE, GuideMode.HUD), this::onGuideModeChange);

		MouseControlAdapter canvasAdapter = new MouseControlAdapter()
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
				offsetXField.setValue((short)(offsetXField.getValue() + lastX - e.getX()));
				offsetYField.setValue((short)(offsetYField.getValue() + lastY - e.getY()));
				lastX = e.getX();
				lastY = e.getY();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
				{
					onZoomFactorChanged(Math.max(1, Math.min(4, canvas.getZoomFactor() + e.getUnitsToScroll())));
				}
			}
		};
		
		canvas.addMouseListener(canvasAdapter);
		canvas.addMouseMotionListener(canvasAdapter);
		canvas.addMouseWheelListener(canvasAdapter);
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("dimgconv.offsetter.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(borderLayout(0, 4),
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
			node(BorderLayout.CENTER, dimension(320, 200), canvas),
			node(BorderLayout.SOUTH, containerOf(borderLayout(),
				node(BorderLayout.WEST, containerOf(flowLayout(Flow.LEADING, 4, 0),
					node(label(language.getText("dimgconv.offsetter.offset"))),
					node(label(language.getText("dimgconv.offsetter.offset.x"))),
					node(offsetXField),
					node(label(language.getText("dimgconv.offsetter.offset.y"))),
					node(offsetYField)
				)),
				node(BorderLayout.EAST, containerOf(flowLayout(Flow.LEADING, 4, 0),
					node(label(language.getText("dimgconv.offsetter.guidemode"))),
					node(guideModeField)
				))
			))
		);
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		// TODO Finish this.
		return super.getApplicationState();
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		// TODO Finish this.
		super.setApplicationState(state);
	}

	@Override
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		return fromWorkspaceClear || SwingUtils.yesTo(language.getText("doomtools.application.close"));
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
		
		canvas.setPalette(pal);
	}
	
	private void onZoomFactorChanged(int zoomFactor)
	{
		canvas.setZoomFactor(zoomFactor);
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
		
		return new DImageConvertOffsetterApp(projectDir);
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
