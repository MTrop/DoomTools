/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.separator;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.Environment;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.apps.data.WadTexExportSettings;
import net.mtrop.doom.tools.gui.apps.data.PatchExportSettings;
import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsTextOutputPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.InstancedFuture.Cancellable;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * Common application functions across more than one application.
 * @author Matthew Tropiano
 */
public final class AppCommon 
{
	public enum TexScanOutputMode
	{
		BOTH,
		TEXTURES,
		FLATS;
	}

	public enum GraphicsMode
	{
		GRAPHICS,
		FLATS,
		COLORMAPS,
		PALETTES;
	}

	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(AppCommon.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<AppCommon> INSTANCE = new SingletonProvider<>(() -> new AppCommon());
	
	/**
	 * @return the singleton instance of this common object.
	 */
	public static AppCommon get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private final DoomToolsGUIUtils utils;
	private final DoomToolsTaskManager tasks;
	private final DoomToolsLanguageManager language;
	private final DoomToolsProcessBuilder processes;

	private AppCommon() 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.processes = DoomToolsProcessBuilder.get();
	}

	public MenuNode[] getCommonHelpMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("doomtools.menu.help.item.opendocs", (i) -> openDocs()),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.opensettings", (i) -> openSettingsFolder()),
			separator(),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.openweb", (i) -> openWebsite()),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.openrepo", (i) -> openRepositorySite())
		);
	}
	
	private void openDocs()
	{
		final String path; 
		try {
			path = Environment.getDoomToolsDocsPath();
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (ObjectUtils.isEmpty(path))
		{
			SwingUtils.error(language.getText("doomtools.error.pathenvvar"));
			return;
		}
		
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.open"));
			return;
		}
		
		LOG.info("Opening the DoomTools documentation folder...");

		File docsDir = new File(path);
		if (!docsDir.exists())
		{
			SwingUtils.error(language.getText("doomtools.error.opendocs.notfound"));
		}
		else
		{
			try {
				Desktop.getDesktop().open(docsDir);
			} catch (IOException e) {
				SwingUtils.error(language.getText("doomtools.error.opendocs.io"));
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("doomtools.error.opendocs.security"));
			}
		}
	}
	
	private void openSettingsFolder()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.open"));
			return;
		}
		
		LOG.info("Opening the DoomTools settings folder...");
		
		File settingsDir = new File(Environment.getApplicationConfigPath());
		if (!settingsDir.exists())
		{
			SwingUtils.error(language.getText("doomtools.error.opensettings.notfound"));
			return;
		}
		
		try {
			Desktop.getDesktop().open(settingsDir);
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.opensettings.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.opensettings.security"));
		}
	}
	
	private void openWebsite()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.browse"));
			return;
		}
				
		LOG.info("Opening the DoomTools website...");

		try {
			Desktop.getDesktop().browse(new URI(DoomToolsMain.DOOMTOOLS_WEBSITE));
		} catch (URISyntaxException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.url"));
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.openweb.security"));
		}
	}
	
	private void openRepositorySite()
	{
		if (!Desktop.isDesktopSupported())
		{
			SwingUtils.error(language.getText("doomtools.error.desktop"));
			return;
		}

		if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			SwingUtils.error(language.getText("doomtools.error.desktop.browse"));
			return;
		}
				
		LOG.info("Opening the DoomTools code repository website...");

		try {
			Desktop.getDesktop().browse(new URI(DoomToolsMain.DOOMTOOLS_REPO_WEBSITE));
		} catch (URISyntaxException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.url"));
		} catch (IOException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.io"));
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("doomtools.error.openrepo.security"));
		}
	}

	private Cancellable<Integer> asCancellable(final ProcessCallable pc)
	{
		return new Cancellable<Integer>() 
		{
			@Override
			public Integer call() throws Exception 
			{
				return pc.call();
			}
			
			@Override
			public boolean isCancelled() 
			{
				return pc.isCancelled();
			}
			
			@Override
			public void cancel() 
			{
				pc.cancel();
			}
		};
	}

	/**
	 * Attempts to read a palette from an arbitrary file.
	 * @param paletteFile the file to read for the palette.
	 * @return a read palette, or null if no palette.
	 */
	public Palette readPaletteFromFile(File paletteFile)
	{
		if (paletteFile == null)
			return null;		
		
		boolean wadfile = false;
		try {
			wadfile = Wad.isWAD(paletteFile);
		} catch (IOException e) {
			SwingUtils.error(language.getText("palette.source.error.ioerror", paletteFile));
			return null;		
		}
		
		Palette pal = null;
	
		// If WAD, search for PlayPal
		if (wadfile)
		{
			try (WadFile wf = new WadFile(paletteFile)) {
				pal = wf.getDataAs("PLAYPAL", Palette.class);
				return pal;
			} catch (IOException e) {
				SwingUtils.error(language.getText("palette.source.error.ioerror", paletteFile));
				return null;		
			}
		}
		
		// read palette image.
		BufferedImage paletteImage;
		try {
			paletteImage = ImageIO.read(paletteFile);
		} catch (IOException e) {
			SwingUtils.error(language.getText("palette.source.error.ioerror", paletteFile));
			return null;		
		}
		
		if (paletteImage != null) // read an image
		{
			// SLADE PNG
			if (paletteImage.getWidth() == 128 && paletteImage.getHeight() == 128)
			{
				pal = new Palette();
				for (int y = 0; y < 16; y++)
					for (int x = 0; x < 16; x++)
						pal.setColor(x + (y * 16), paletteImage.getRGB(x * 8, y * 8));
				return pal;
			}
			// DoomTools Palette PNG
			else if (paletteImage.getWidth() >= 256)
			{
				pal = new Palette();
				for (int i = 0; i < 256; i++)
					pal.setColor(i, paletteImage.getRGB(i, 0));
				return pal;
			}
		}
		
		// else, attempt to load as raw palette.
		try {
			pal = BinaryObject.read(Palette.class, paletteFile);
		} catch (IOException e) {
			SwingUtils.error(language.getText("palette.source.error.notpal", paletteFile));
			return null;		
		}
		
		return pal;
	}
	
	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFile the script file to run.
	 * @param encoding the encoding of the source file.
	 * @param processSettings
	 */
	public void onExecuteDecoHack(Container parent, final DoomToolsStatusPanel statusPanel, File sourceFile, Charset encoding, PatchExportSettings processSettings)
	{
		File sourceOutputFile = processSettings.getSourceOutputFile();
		File outputFile = processSettings.getOutputFile(); 
		boolean budget = processSettings.isOutputBudget();
		
		utils.createProcessModal(
			parent, 
			language.getText("decohack.export.message.title"), 
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("decohack.export.message.running", sourceFile.getName()), 
				language.getText("decohack.export.message.success"), 
				language.getText("decohack.export.message.interrupt"), 
				language.getText("decohack.export.message.error"),
				asCancellable(processes.attachStreams(processes.createDecoHack(sourceFile, encoding, sourceOutputFile, outputFile, budget), stdout, stderr))
			) 
		).start(tasks);
	}
	
	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel.
	 * @param inputFile input file/dir for conversion.
	 * @param outputFile the output file, directory, or WAD.
	 * @param recursive if true, the input file is a directory, and the search is recursive.
	 * @param paletteSource the palette source file or WAD.
	 * @param mode the default mode.
	 * @param infoFileName the alternate name for the info file.
	 */
	public void onExecuteDImgConv(Container parent, final DoomToolsStatusPanel statusPanel, File inputFile, File outputFile, boolean recursive, File paletteSource, AppCommon.GraphicsMode mode, String infoFileName)
	{
		utils.createProcessModal(
			parent, 
			language.getText("dimgconv.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("dimgconv.status.message.running"), 
				language.getText("dimgconv.status.message.success"), 
				language.getText("dimgconv.status.message.interrupt"), 
				language.getText("dimgconv.status.message.error"), 
				asCancellable(processes.attachStreams(processes.createDImgConv(inputFile, outputFile, recursive, paletteSource, mode, infoFileName), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel.
	 * @param inputFiles the input sound files (will be exploded into individual files).
	 * @param ffmpegPath if not null, path to FFmpeg.
	 * @param ffmpegOnly if null, normal path. True, ffmpeg only. False, JSPI only.
	 * @param recursive if true, recurse through directories.
	 * @param outputDir if not null, output directory.
	 */
	public void onExecuteDMXConv(Container parent, final DoomToolsStatusPanel statusPanel, File[] inputFiles, File ffmpegPath, Boolean ffmpegOnly, Boolean recursive, File outputDir)
	{
		utils.createProcessModal(
			parent, 
			language.getText("dmxconv.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("dmxconv.status.message.running"), 
				language.getText("dmxconv.status.message.success"), 
				language.getText("dmxconv.status.message.interrupt"), 
				language.getText("dmxconv.status.message.error"), 
				asCancellable(processes.attachStreams(processes.createDMXConv(inputFiles, ffmpegPath, ffmpegOnly, recursive, outputDir), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel.
	 * @param projectDirectory the project directory.
	 * @param standardInFile the standard in.
	 * @param target the target name.
	 * @param args the script arguments, if any.
	 * @param agentOverride if true, override the agent warning. 
	 * @param onStart what to call on process start.
	 * @param onEnd what to call on process end.
	 */
	public void onExecuteDoomMake(
		Container parent, 
		final DoomToolsStatusPanel statusPanel, 
		final File projectDirectory, 
		final File standardInFile, 
		final String target, 
		final String[] args, 
		boolean agentOverride,
		Runnable onStart,
		Consumer<Integer> onEnd
	){
		onExecuteDoomMake(
			parent, 
			new DoomToolsTextOutputPanel(), 
			statusPanel, 
			false, 
			projectDirectory, 
			standardInFile, 
			target, 
			args, 
			agentOverride, 
			onStart, onEnd
		);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param outputPanel the output panel to output to.
	 * @param statusPanel the status panel.
	 * @param noModal if true, does not show a modal.
	 * @param projectDirectory the project directory.
	 * @param standardInFile the standard in.
	 * @param target the target name.
	 * @param args the script arguments, if any.
	 * @param agentOverride if true, override the agent warning. 
	 * @param onStart what to call on process start.
	 * @param onEnd what to call on process end.
	 */
	public void onExecuteDoomMake(
		Container parent, 
		final DoomToolsTextOutputPanel outputPanel,
		final DoomToolsStatusPanel statusPanel, 
		final boolean noModal,
		final File projectDirectory, 
		final File standardInFile, 
		final String target, 
		final String[] args, 
		boolean agentOverride,
		Runnable onStart,
		Consumer<Integer> onEnd
	){
		utils.createProcessModal(
			parent, 
			language.getText("wadscript.run.message.title"),
			standardInFile,
			outputPanel,
			noModal,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("doommake.project.build.message.running", target), 
				language.getText("doommake.project.build.message.success"), 
				language.getText("doommake.project.build.message.interrupt"), 
				language.getText("doommake.project.build.message.error"), 
				asCancellable(processes.attachStreams(processes.createDoomMake(projectDirectory, target, agentOverride, args), stdout, stderr, stdin))
			)
		).start(tasks, onStart, onEnd);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param scriptFile the script file to run.
	 * @param encoding the encoding of the script file.
	 * @param mergeSettings
	 */
	public void onExecuteWadMerge(Container parent, final DoomToolsStatusPanel statusPanel, File scriptFile, Charset encoding, MergeSettings mergeSettings)
	{
		final File workingDirectory = mergeSettings.getWorkingDirectory();
		final boolean verbose = mergeSettings.getVerboseOutput();
		final String[] args = mergeSettings.getArgs();
		
		utils.createProcessModal(
			parent, 
			language.getText("wadmerge.run.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wadmerge.run.message.running"), 
				language.getText("wadmerge.run.message.success"), 
				language.getText("wadmerge.run.message.interrupt"), 
				language.getText("wadmerge.run.message.error"), 
				asCancellable(processes.attachStreams(processes.createWadMerge(scriptFile, workingDirectory, verbose, encoding, args), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param scriptFile the script file to run.
	 * @param encoding the encoding of the script file.
	 * @param executionSettings
	 */
	public void onExecuteWadScript(Container parent, final DoomToolsStatusPanel statusPanel, File scriptFile, Charset encoding, ScriptExecutionSettings executionSettings)
	{
		final File workingDirectory = executionSettings.getWorkingDirectory();
		final File standardInPath = executionSettings.getStandardInPath();
		final String entryPoint = executionSettings.getEntryPoint();
		final String[] args = executionSettings.getArgs();
		
		utils.createProcessModal(
			parent, 
			language.getText("wadscript.run.message.title"),
			standardInPath,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wadscript.run.message.running", entryPoint), 
				language.getText("wadscript.run.message.success"), 
				language.getText("wadscript.run.message.interrupt"),
				language.getText("wadscript.run.message.error"),
				asCancellable(processes.attachStreams(processes.createWadScript(scriptFile, workingDirectory, entryPoint, encoding, args), stdout, stderr, stdin))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFile the source file.
	 * @param exportSettings
	 */
	public void onExecuteWadTex(Container parent, final DoomToolsStatusPanel statusPanel, final File sourceFile, WadTexExportSettings exportSettings)
	{
		final File outputWAD = exportSettings.getOutputWAD();
		final String nameOverride = exportSettings.getNameOverride();
		final boolean appendMode = exportSettings.getAppendMode();
		final boolean forceStrife = exportSettings.getForceStrife();
		
		utils.createProcessModal(
			parent, 
			language.getText("wadscript.run.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wadtex.export.message.running"), 
				language.getText("wadtex.export.message.success"), 
				language.getText("wadtex.export.message.interrupt"),
				language.getText("wadtex.export.message.error"),
				asCancellable(processes.attachStreams(processes.createWadTex(sourceFile, outputWAD, nameOverride, appendMode, forceStrife), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param scriptFile the script file to run.
	 * @param exportSettings the export settings.
	 */
	public void onExecuteWSwAnTbl(Container parent, final DoomToolsStatusPanel statusPanel, File scriptFile, DefSwAniExportSettings exportSettings)
	{
		final File outWAD = exportSettings.getOutputWAD();
		final boolean outputSource = exportSettings.isOutputSource();
		
		utils.createProcessModal(
			parent, 
			language.getText("wadmerge.run.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wswantbl.export.message.running"), 
				language.getText("wswantbl.export.message.success"), 
				language.getText("wswantbl.export.message.interrupt"), 
				language.getText("wswantbl.export.message.error"), 
				asCancellable(processes.attachStreams(processes.createWSwAnTbl(scriptFile, outWAD, outputSource), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
	 * @param outputMode 
	 * @param noMessages 
	 */
	public void onExecuteWTexList(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, AppCommon.TexScanOutputMode outputMode, boolean noMessages)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wtexlist.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wtexlist.status.message.running"), 
				language.getText("wtexlist.status.message.success"), 
				language.getText("wtexlist.status.message.interrupt"), 
				language.getText("wtexlist.status.message.error"), 
				asCancellable(processes.attachStreams(processes.createWTexList(sourceFiles, outputMode, noMessages), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
	 * @param mapInfoFiles 
	 * @param skydefsFiles 
	 * @param outputMode 
	 * @param noSkies 
	 * @param noMessages 
	 * @param mapName 
	 */
	public void onExecuteWTexScan(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, File[] mapInfoFiles, File[] skydefsFiles, AppCommon.TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wtexscan.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wtexscan.status.message.running"), 
				language.getText("wtexscan.status.message.success"), 
				language.getText("wtexscan.status.message.interrupt"), 
				language.getText("wtexscan.status.message.error"), 
				asCancellable(processes.attachStreams(processes.createWTexScan(sourceFiles, mapInfoFiles, skydefsFiles, outputMode, noSkies, noMessages, mapName), stdout, stderr))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param wTexScanList the input list from WTexScan.
	 * @param sourceTextureFiles 
	 * @param baseFile 
	 * @param outputFile 
	 * @param create 
	 * @param noAnim 
	 * @param noSwitch 
	 * @param nullTex 
	 */
	public void onExecuteWTExport(Container parent, final DoomToolsStatusPanel statusPanel, File wTexScanList, File[] sourceTextureFiles, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wtexport.status.message.title"),
			wTexScanList,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wtexport.status.message.running"), 
				language.getText("wtexport.status.message.success"), 
				language.getText("wtexport.status.message.interrupt"), 
				language.getText("wtexport.status.message.error"), 
				asCancellable(processes.attachStreams(processes.createWTExport(sourceTextureFiles, baseFile, outputFile, create, noAnim, noSwitch, nullTex), stdout, stderr, stdin))
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
	 * @param outputMode 
	 * @param noMessages 
	 * @param baseFile 
	 * @param outputFile 
	 * @param create 
	 * @param noAnim 
	 * @param noSwitch 
	 * @param nullTex 
	 */
	public void onExecuteWTexListToWTExport(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, AppCommon.TexScanOutputMode outputMode, boolean noMessages, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wtexport.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wtexport.status.message.running"), 
				language.getText("wtexport.status.message.success"), 
				language.getText("wtexport.status.message.interrupt"), 
				language.getText("wtexport.status.message.error"),
				asCancellable(processes.createWTexList(sourceFiles, outputMode, noMessages)
					.pipeOutTo(processes.attachStreams(processes.createWTExport(sourceFiles, baseFile, outputFile, create, noAnim, noSwitch, nullTex), stdout, stderr))) 
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
	 * @param mapInfoFiles 
	 * @param skydefsFiles 
	 * @param outputMode 
	 * @param noSkies 
	 * @param noMessages 
	 * @param mapName 
	 * @param sourceTextureFiles 
	 * @param baseFile 
	 * @param outputFile 
	 * @param create 
	 * @param noAnim 
	 * @param noSwitch 
	 * @param nullTex 
	 */
	public void onExecuteWTexScanToWTExport(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, File[] mapInfoFiles, File[] skydefsFiles, AppCommon.TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName, File[] sourceTextureFiles, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wtexport.status.message.title"),
			null,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("wtexport.status.message.running"), 
				language.getText("wtexport.status.message.success"), 
				language.getText("wtexport.status.message.interrupt"), 
				language.getText("wtexport.status.message.error"),
				asCancellable(processes.createWTexScan(sourceFiles, mapInfoFiles, skydefsFiles, outputMode, noSkies, noMessages, mapName)
					.pipeOutTo(processes.attachStreams(processes.createWTExport(sourceTextureFiles, baseFile, outputFile, create, noAnim, noSwitch, nullTex), stdout, stderr))) 
			)
		).start(tasks);
	}

	/**
	 * @param statusPanel the status panel of the primary app.
	 * @param activityMessage the message to display in the modal during execution.
	 * @param successMessage the message to display in the modal on successful finish.
	 * @param interruptMessage the message to display in the modal on interruption.
	 * @param errorMessage the message to display in the modal on error.
	 * @param cancellable the cancellable to execute.
	 * @return an InstancedFuture to queue in a task manager.
	 */
	public InstancedFuture<Integer> execute(
		final DoomToolsStatusPanel statusPanel, 
		final String activityMessage, 
		final String successMessage, 
		final String interruptMessage,
		final String errorMessage,
		final Cancellable<Integer> cancellable
	){
		return tasks.spawn(new Cancellable<Integer>() 
		{
			@Override
			public Integer call() throws Exception
			{
				Integer result = null;
				try
				{
					statusPanel.setActivityMessage(activityMessage);
					
					result = cancellable.call();
					
					if (cancellable.isCancelled())
					{
						statusPanel.setErrorMessage(interruptMessage);
						return result;
					}
					
					if (result != 0)
					{
						LOG.errorf(errorMessage);
						statusPanel.setErrorMessage(errorMessage);
						return result;
					}
					else
					{
						statusPanel.setSuccessMessage(successMessage);
					}

				} catch (InterruptedException e) {
					LOG.warnf(interruptMessage);
					statusPanel.setErrorMessage(interruptMessage);
				} catch (ExecutionException e) {
					LOG.errorf(e, errorMessage);
					statusPanel.setErrorMessage(errorMessage);
				}
				return result;
			}

			@Override
			public void cancel()
			{
				cancellable.cancel();
			}

			@Override
			public boolean isCancelled()
			{
				return cancellable.isCancelled();
			}
			
		});
	}
	
}
