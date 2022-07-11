package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.util.Map;
import java.util.function.Function;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel.OutputMode;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WTexScanApp GUI application.
 * @author Matthew Tropiano
 */
public class WTexScanApp extends DoomToolsApplicationInstance
{
	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
    /** App Common */
    private AppCommon appCommon;
	
    /** Parameter panel. */
    private WTexScanParametersPanel parametersPanel;
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
	/**
	 * Creates a new New Project app instance.
	 */
	public WTexScanApp() 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.appCommon = AppCommon.get();
		
		this.parametersPanel = new WTexScanParametersPanel();
		this.statusPanel = new DoomToolsStatusPanel();
		this.statusPanel.setSuccessMessage(language.getText("wtexscan.status.message.ready"));
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("wtexscan.title");
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
	public Container createContentPane()
	{
		return containerOf(borderLayout(0, 4),
			node(BorderLayout.CENTER, parametersPanel),
			node(BorderLayout.SOUTH, containerOf(
				node(BorderLayout.CENTER, statusPanel),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.TRAILING),
					node(utils.createButtonFromLanguageKey("wtexscan.button.start", (c, e) -> onScanMaps()))
				))
			))
		);
	}

	private void onScanMaps() 
	{
		File[] files = parametersPanel.getFiles();
		OutputMode mode = parametersPanel.getOutputMode();
		boolean noSkies = parametersPanel.getSkipSkies();
		boolean noMessages = parametersPanel.getNoCommentMessages();
		String mapName = parametersPanel.getMapName();
		
		appCommon.onExecuteWTexScan(getApplicationContainer(), statusPanel, 
			files, 
			mode, 
			noSkies, 
			noMessages,
			mapName
		);	
	}
	
}
