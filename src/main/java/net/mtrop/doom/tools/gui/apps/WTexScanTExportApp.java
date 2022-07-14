package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.AppCommon.TexScanOutputMode;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.settings.WTexScanSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTExportParametersPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WTexScan-WTexport pipe GUI application.
 * @author Matthew Tropiano
 */
public class WTexScanTExportApp extends DoomToolsApplicationInstance
{
	private WTexScanSettingsManager settings;
	
    /** Parameter panel. */
    private WTexScanParametersPanel texScanParametersPanel;
    /** Parameter panel. */
    private WTExportParametersPanel texportParametersPanel;
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
	/**
	 * Creates a new WTexScan app instance.
	 */
	public WTexScanTExportApp() 
	{
		this.settings = WTexScanSettingsManager.get();
		
		this.texScanParametersPanel = new WTexScanParametersPanel();
		this.texportParametersPanel = new WTExportParametersPanel();
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle()
	{
		return getLanguage().getText("wtextex.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();

		File[] files = texScanParametersPanel.getFiles();
		TexScanOutputMode mode = texScanParametersPanel.getOutputMode();
		boolean noSkies = texScanParametersPanel.getSkipSkies();
		boolean noMessages = texScanParametersPanel.getNoCommentMessages();
		String mapName = texScanParametersPanel.getMapName();

		state.put("wtexscan.files.count", String.valueOf(files.length));
		for (int i = 0; i < files.length; i++) 
			state.put("wtexscan.files." + i, files[i].getAbsolutePath());
			
		state.put("wtexscan.mode", mode.name());
		state.put("wtexscan.noskies", String.valueOf(noSkies));
		state.put("wtexscan.nomessages", String.valueOf(noMessages));
		state.put("wtexscan.mapname", mapName != null ? mapName : "");
		
		File[] textureWads = texportParametersPanel.getTextureWads();
		File baseWad = texportParametersPanel.getBaseWad();
		File outputWad = texportParametersPanel.getOutputWad();
		boolean create = texportParametersPanel.getCreate();
		String nullTexture = texportParametersPanel.getNullTexture();
		boolean noAnim = texportParametersPanel.getNoAnimated();
		boolean noSwitch = texportParametersPanel.getNoSwitches();

		state.put("wtexport.files.count", String.valueOf(textureWads.length));
		for (int i = 0; i < textureWads.length; i++) 
			state.put("wtexport.files." + i, textureWads[i].getAbsolutePath());
			
		state.put("wtexport.basewad", baseWad.getAbsolutePath());
		state.put("wtexport.outputwad", outputWad.getAbsolutePath());
		state.put("wtexport.create", String.valueOf(create));
		state.put("wtexport.nulltexture", nullTexture != null ? nullTexture : "");
		state.put("wtexport.noanim", String.valueOf(noAnim));
		state.put("wtexport.noswitch", String.valueOf(noSwitch));

		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		int texScanCount = ValueUtils.parseInt(state.get("wtexscan.files.count"), 0);

		File[] texScanFiles = new File[texScanCount];
		
		final Function<String, File> fileParse = (input) -> new File(input);
		final Function<String, TexScanOutputMode> modeParse = (input) -> EnumUtils.getEnumInstance(input, TexScanOutputMode.class);
		
		for (int i = 0; i < texScanFiles.length; i++) 
			texScanFiles[i] = ValueUtils.parse(state.get("wtexscan.files." + i), fileParse);

		texScanParametersPanel.setFiles(texScanFiles);
		texScanParametersPanel.setOutputMode(ValueUtils.parse(state.get("wtexscan.mode"), modeParse));
		texScanParametersPanel.setSkipSkies(ValueUtils.parseBoolean(state.get("wtexscan.noskies"), false));
		texScanParametersPanel.setNoCommentMessages(ValueUtils.parseBoolean(state.get("wtexscan.nomessages"), false));
		texScanParametersPanel.setMapName(state.get("wtexscan.mapname"));
		
		int texportCount = ValueUtils.parseInt(state.get("wtexport.files.count"), 0);
		File[] texportFiles = new File[texportCount];
		for (int i = 0; i < texportFiles.length; i++) 
			texportFiles[i] = ValueUtils.parse(state.get("wtexport.files." + i), fileParse);

		texScanParametersPanel.setFiles(texportFiles);
		texportParametersPanel.setBaseWad(ValueUtils.parse(state.get("wtexport.basewad"), fileParse));
		texportParametersPanel.setOutputWad(ValueUtils.parse(state.get("wtexport.outputwad"), fileParse));
		texportParametersPanel.setCreate(ValueUtils.parseBoolean(state.get("wtexport.create"), false));
		texportParametersPanel.setNullTexture(ValueUtils.parse(state.get("wtexport.nulltexture"), (input) -> (ObjectUtils.isEmpty(input) ? null : input)));
		texportParametersPanel.setNoAnimated(ValueUtils.parseBoolean(state.get("wtexport.noanim"), false));
		texportParametersPanel.setNoSwitches(ValueUtils.parseBoolean(state.get("wtexport.noswitch"), false));
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		DoomToolsGUIUtils utils = getUtils();
		
		return menuBar(
			utils.createMenuFromLanguageKey("wtextex.menu.file",
				utils.createItemFromLanguageKey("wtextex.menu.file.item.exit", (c, e) -> attemptClose())
			),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(createHelpMenu());
	}

	@Override
	public Container createContentPane()
	{
		return containerOf(dimension(640, 450), borderLayout(0, 4),
			node(BorderLayout.CENTER, containerOf(gridLayout(1, 2, 4, 0),
				node(getUtils().createTitlePanel(getLanguage().getText("wtexscan.title"), texScanParametersPanel)),
				node(getUtils().createTitlePanel(getLanguage().getText("wtexport.title"), texportParametersPanel))
			)),
			node(BorderLayout.SOUTH, containerOf(
				node(BorderLayout.CENTER, statusPanel),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.TRAILING),
					node(getUtils().createButtonFromLanguageKey("wtextex.button.start", (c, e) -> onDoPipe()))
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
		statusPanel.setSuccessMessage(getLanguage().getText("wtextex.status.message.ready"));
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
	
	private void onDoPipe() 
	{
		File[] files = texScanParametersPanel.getFiles();
		TexScanOutputMode mode = texScanParametersPanel.getOutputMode();
		boolean noSkies = texScanParametersPanel.getSkipSkies();
		boolean noMessages = texScanParametersPanel.getNoCommentMessages();
		String mapName = texScanParametersPanel.getMapName();
		
		File[] textureWads = texportParametersPanel.getTextureWads();
		File baseWad = texportParametersPanel.getBaseWad();
		File outputWad = texportParametersPanel.getOutputWad();
		boolean create = texportParametersPanel.getCreate();
		String nullTexture = texportParametersPanel.getNullTexture();
		boolean noAnim = texportParametersPanel.getNoAnimated();
		boolean noSwitch = texportParametersPanel.getNoSwitches();
		
		if (outputWad.exists() && create)
		{
			if (SwingUtils.noTo(getApplicationContainer(), getLanguage().getText("wtexport.overwrite.message", outputWad.getName())))
				return;
		}

		getCommon().onExecuteWTexScanToWTExport(getApplicationContainer(), statusPanel, 
			files, 
			mode, 
			noSkies, 
			noMessages,
			mapName,
			textureWads, 
			baseWad, 
			outputWad, 
			create,
			noAnim,
			noSwitch,
			nullTexture
		);	
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		DoomToolsGUIUtils utils = getUtils();
	
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (c, e) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		getUtils().createHelpModal(
			getUtils().helpResource("docs/changelogs/CHANGELOG-wtexscan.md"),
			getUtils().helpResource("docs/changelogs/CHANGELOG-wtexport.md")
		).open();
	}
	
}
