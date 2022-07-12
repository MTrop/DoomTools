package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.settings.WTexScanSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel.OutputMode;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.menuBar;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WTexScanApp GUI application.
 * @author Matthew Tropiano
 */
public class WTexScanApp extends DoomToolsApplicationInstance
{
	private WTexScanSettingsManager settings;
	
    /** Parameter panel. */
    private WTexScanParametersPanel parametersPanel;
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
	/**
	 * Creates a new WTexScan app instance.
	 */
	public WTexScanApp() 
	{
		this.settings = WTexScanSettingsManager.get();
		
		this.parametersPanel = new WTexScanParametersPanel();
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle()
	{
		return getLanguage().getText("wtexscan.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();

		File[] files = parametersPanel.getFiles();
		OutputMode mode = parametersPanel.getOutputMode();
		boolean noSkies = parametersPanel.getSkipSkies();
		boolean noMessages = parametersPanel.getNoCommentMessages();
		String mapName = parametersPanel.getMapName();

		state.put("files.count", String.valueOf(files.length));
		for (int i = 0; i < files.length; i++) 
			state.put("files." + i, files[i].getAbsolutePath());
			
		state.put("mode", mode.name());
		state.put("noskies", String.valueOf(noSkies));
		state.put("nomessages", String.valueOf(noMessages));
		state.put("mapname", mapName != null ? mapName : "");
		
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		int count = ValueUtils.parseInt(state.get("files.count"), 0);

		File[] files = new File[count];
		
		final Function<String, File> fileParse = (input) -> new File(input);
		final Function<String, OutputMode> modeParse = (input) -> EnumUtils.getEnumInstance(input, OutputMode.class);
		
		for (int i = 0; i < files.length; i++) 
			files[i] = ValueUtils.parse(state.get("files." + i), fileParse);

		parametersPanel.setFiles(files);
		parametersPanel.setOutputMode(ValueUtils.parse(state.get("mode"), modeParse));
		parametersPanel.setSkipSkies(ValueUtils.parseBoolean(state.get("noskies"), false));
		parametersPanel.setNoCommentMessages(ValueUtils.parseBoolean(state.get("nomessages"), false));
		parametersPanel.setMapName(state.get("mapname"));
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		DoomToolsGUIUtils utils = getUtils();
		
		return menuBar(
			utils.createMenuFromLanguageKey("wtexscan.menu.file",
				utils.createItemFromLanguageKey("wtexscan.menu.file.item.exit", (c, e) -> attemptClose())
			)
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return null;
	}

	@Override
	public Container createContentPane()
	{
		return containerOf(borderLayout(0, 4),
			node(BorderLayout.CENTER, parametersPanel),
			node(BorderLayout.SOUTH, containerOf(
				node(BorderLayout.CENTER, statusPanel),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.TRAILING),
					node(getUtils().createButtonFromLanguageKey("wtexscan.button.start", (c, e) -> onScanMaps()))
				))
			))
		);
	}

	@Override
	public void onCreate(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			Rectangle bounds = settings.getBounds();
			boolean maximized = settings.getBoundsMaximized();
			f.setBounds(bounds);
			if (maximized)
				f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	@Override
	public void onOpen(Object frame) 
	{
		statusPanel.setSuccessMessage(getLanguage().getText("wtexscan.status.message.ready"));
	}

	@Override
	public void onClose(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			settings.setBounds(f);
		}
	}
	
	private void onScanMaps() 
	{
		File[] files = parametersPanel.getFiles();
		OutputMode mode = parametersPanel.getOutputMode();
		boolean noSkies = parametersPanel.getSkipSkies();
		boolean noMessages = parametersPanel.getNoCommentMessages();
		String mapName = parametersPanel.getMapName();
		
		getCommon().onExecuteWTexScan(getApplicationContainer(), statusPanel, 
			files, 
			mode, 
			noSkies, 
			noMessages,
			mapName
		);	
	}
	
}
