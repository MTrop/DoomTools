package net.mtrop.doom.tools.gui.managers;

import java.awt.Container;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import net.mtrop.doom.tools.DMXConvertMain;
import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.WTExportMain;
import net.mtrop.doom.tools.WTexScanMain;
import net.mtrop.doom.tools.WadMergeMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.apps.data.PatchExportSettings;
import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * Common application functions across more than one application.
 * @author Matthew Tropiano
 */
public final class AppCommon 
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(AppCommon.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<AppCommon> INSTANCE = new SingletonProvider<>(() -> new AppCommon());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static AppCommon get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private static final AtomicLong DEFAULT_THREADFACTORY_ID = new AtomicLong(0L);
	private static final ThreadFactory DEFAULT_THREADFACTORY = 
		(runnable) -> new Thread(runnable, "AppThread-" + DEFAULT_THREADFACTORY_ID.getAndIncrement());

	private DoomToolsGUIUtils utils;
	private DoomToolsTaskManager tasks;
	private DoomToolsLanguageManager language;

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

	private AppCommon() 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
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
				callDecoHack(sourceFile, encoding, sourceOutputFile, outputFile, budget, stdout, stderr)
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
	public void onExecuteDImgConv(Container parent, final DoomToolsStatusPanel statusPanel, File inputFile, File outputFile, boolean recursive, File paletteSource, GraphicsMode mode, String infoFileName)
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
				callDImgConv(inputFile, outputFile, recursive, paletteSource, mode, infoFileName, stdout, stderr)
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
	 * @param outputDir if not null, output directory.
	 */
	public void onExecuteDMXConv(Container parent, final DoomToolsStatusPanel statusPanel, File[] inputFiles, File ffmpegPath, Boolean ffmpegOnly, File outputDir)
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
				callDMXConv(inputFiles, ffmpegPath, ffmpegOnly, outputDir, stdout, stderr)
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
	 */
	public void onExecuteDoomMake(Container parent, final DoomToolsStatusPanel statusPanel, final File projectDirectory, final File standardInFile, final String target, final String[] args, boolean agentOverride)
	{
		utils.createProcessModal(
			parent, 
			language.getText("wadscript.run.message.title"),
			standardInFile,
			(stdout, stderr, stdin) -> execute(
				statusPanel,
				language.getText("doommake.project.build.message.running", target), 
				language.getText("doommake.project.build.message.success"), 
				language.getText("doommake.project.build.message.interrupt"), 
				language.getText("doommake.project.build.message.error"), 
				callDoomMake(projectDirectory, target, agentOverride, args, stdout, stderr, stdin)
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
				callWadScript(scriptFile, workingDirectory, entryPoint, encoding, args, stdout, stderr, stdin)
			)
		).start(tasks);
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
				callWadMerge(scriptFile, workingDirectory, encoding, args, stdout, stderr)
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
				callWSwAnTbl(scriptFile, outWAD, outputSource, stdout, stderr)
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
	 * @param outputMode 
	 * @param noSkies 
	 * @param noMessages 
	 * @param mapName 
	 */
	public void onExecuteWTexScan(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName)
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
				callWTexScan(sourceFiles, outputMode, noSkies, noMessages, mapName, stdout, stderr)
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
				callWTExport(sourceTextureFiles, baseFile, outputFile, create, noAnim, noSwitch, nullTex, stdout, stderr, stdin)
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFiles 
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
	public void onExecuteWTexScanToWTExport(Container parent, final DoomToolsStatusPanel statusPanel, File[] sourceFiles, TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName, File[] sourceTextureFiles, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex)
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
				InstancedFuture.spawn(() -> {
					int result;
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream(16 * 1024);
					PrintStream byteOut = new PrintStream(bos);
					
					result = callWTexScan(sourceFiles, outputMode, noSkies, noMessages, mapName, byteOut, stderr).result();
					if (result != 0)
						return result;
					
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					
					return callWTExport(sourceTextureFiles, baseFile, outputFile, create, noAnim, noSwitch, nullTex, stdout, stderr, bis).result();
				})
			)
		).start(tasks);
	}

	/**
	 * 
	 * @param statusPanel the status panel of the primary app.
	 * @param activityMessage the message to display in the modal during execution.
	 * @param successMessage the message to display in the modal on successful finish.
	 * @param interruptMessage the message to display in the modal on interruption.
	 * @param errorMessage the message to display in the modal on error.
	 * @param task the instanced future to execute.
	 * @return
	 */
	private InstancedFuture<Integer> execute(
		final DoomToolsStatusPanel statusPanel, 
		final String activityMessage, 
		final String successMessage, 
		final String interruptMessage,
		final String errorMessage,
		final InstancedFuture<Integer> task
	){
		return tasks.spawn(() -> {
			Integer result = null;
			try
			{
				statusPanel.setActivityMessage(activityMessage);
				result = task.get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(successMessage);
				}
				else
				{
					LOG.errorf(errorMessage);
					statusPanel.setErrorMessage(errorMessage);
				}
			} catch (InterruptedException e) {
				LOG.warnf(interruptMessage);
				statusPanel.setErrorMessage(interruptMessage);
			} catch (ExecutionException e) {
				LOG.errorf(e, errorMessage);
				statusPanel.setErrorMessage(errorMessage);
			}
			return result;
		});
	}

	/**
	 * 
	 * @param scriptFile
	 * @param encoding
	 * @param outSourceFile
	 * @param outTargetFile
	 * @param budget
	 * @param stdout
	 * @param stderr
	 * @return the future task
	 */
	public InstancedFuture<Integer> callDecoHack(File scriptFile, Charset encoding, File outSourceFile, File outTargetFile, boolean budget, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(DecoHackMain.class).setWorkingDirectory(scriptFile.getParentFile());
		
		callable.arg(scriptFile.getAbsolutePath());
		callable.arg(DecoHackMain.SWITCH_OUTPUT).arg(outTargetFile.getAbsolutePath());
		callable.arg(DecoHackMain.SWITCH_CHARSET1).arg(encoding.displayName());
		
		if (outSourceFile != null)
			callable.arg(DecoHackMain.SWITCH_SOURCE_OUTPUT).arg(outSourceFile.getAbsolutePath());
	
		if (budget)
			callable.arg(DecoHackMain.SWITCH_BUDGET);
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(IOUtils.getNullInputStream())
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on DECOHack STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on DECOHack STDERR."));
		
		LOG.infof("Calling DECOHack (%s).", scriptFile);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	/**
	 * Calls DImgConv.
	 * @param inputFile input file/dir for conversion.
	 * @param outputFile the output file, directory, or WAD.
	 * @param recursive if true, the input file is a directory, and the search is recursive.
	 * @param paletteSource the palette source file or WAD.
	 * @param mode the default mode.
	 * @param infoFileName the alternate name for the info file.
	 * @param stdout
	 * @param stderr
	 * @return the future task
	 */
	public InstancedFuture<Integer> callDImgConv(File inputFile, File outputFile, boolean recursive, File paletteSource, GraphicsMode mode, String infoFileName, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(DoomImageConvertMain.class);
		
		if (inputFile != null)
			callable.arg(inputFile.getAbsolutePath());
		
		if (outputFile != null)
			callable.arg(DoomImageConvertMain.SWITCH_OUTPUT).arg(outputFile.getAbsolutePath());

		if (recursive)
			callable.arg(DoomImageConvertMain.SWITCH_RECURSIVE);

		if (paletteSource != null)
			callable.arg(DoomImageConvertMain.SWITCH_PALETTE).arg(paletteSource.getAbsolutePath());
		
		switch (mode)
		{
			case PALETTES:
				callable.arg(DoomImageConvertMain.SWITCH_MODE_PALETTES);
				break;
			case COLORMAPS:
				callable.arg(DoomImageConvertMain.SWITCH_MODE_COLORMAPS);
				break;
			case FLATS:
				callable.arg(DoomImageConvertMain.SWITCH_MODE_FLATS);
				break;
			default:
			case GRAPHICS:
				break;
		}
		
		if (!ObjectUtils.isEmpty(infoFileName))
			callable.arg(DoomImageConvertMain.SWITCH_METAINFOFILE).arg(infoFileName);
		
		callable.arg(DoomImageConvertMain.SWITCH_VERBOSE);
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on DImgConv STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on DImgConv STDERR."))
		;

		LOG.infof("Calling DMXConv.");
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	/**
	 * Calls DMXConvert.
	 * @param inputFiles
	 * @param ffmpegPath 
	 * @param ffmpegOnly 
	 * @param outputDir 
	 * @param stdout
	 * @param stderr
	 * @return the future task
	 */
	public InstancedFuture<Integer> callDMXConv(File[] inputFiles, File ffmpegPath, Boolean ffmpegOnly, File outputDir, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(DMXConvertMain.class);
		
		inputFiles = FileUtils.explodeFiles(inputFiles);
		for (int i = 0; i < inputFiles.length; i++) 
			callable.arg(inputFiles[i].getAbsolutePath());	
		
		if (ffmpegPath != null)
			callable.arg(DMXConvertMain.SWITCH_FFMPEG_PATH).arg(ffmpegPath.getAbsolutePath());
		
		if (ffmpegOnly != null)
		{
			if (ffmpegOnly)
				callable.arg(DMXConvertMain.SWITCH_FFMPEG_ONLY);
			else
				callable.arg(DMXConvertMain.SWITCH_JSPI_ONLY);
		}
		
		if (outputDir != null)
			callable.arg(DMXConvertMain.SWITCH_OUTPUTDIR).arg(outputDir.getAbsolutePath());

		callable
			.setOut(stdout)
			.setErr(stderr)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on DMXConv STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on DMXConv STDERR."))
		;

		LOG.infof("Calling DMXConv.");
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	/**
	 * Calls a DoomMake project target.
	 * @param projectDirectory the project directory.
	 * @param stdout the standard out stream. 
	 * @param stderr the standard error stream. 
	 * @param targetName the target name.
	 * @param agentOverride if true, bypasses agent detection.
	 * @param args script arguments.
	 * @param stdin standard in.
	 * @return the list of project targets.
	 */
	public InstancedFuture<Integer> callDoomMake(File projectDirectory, String targetName, boolean agentOverride, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		ProcessCallable callable = Common.spawnJava(DoomMakeMain.class).setWorkingDirectory(projectDirectory);
		if (agentOverride)
			callable.arg(DoomMakeMain.SWITCH_AGENT_BYPASS);
		
		callable.arg(targetName)
			.args(args)
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on DoomMake STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on DoomMake STDERR."))
			.setInListener((exception) -> LOG.errorf(exception, "Exception occurred on DoomMake STDIN."));
		
		LOG.infof("Calling DoomMake (%s).", targetName);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	public InstancedFuture<Integer> callWadScript(final File scriptFile, final File workingDirectory, String entryPoint, Charset encoding, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		ProcessCallable callable = Common.spawnJava(WadScriptMain.class).setWorkingDirectory(workingDirectory);
		callable.arg(scriptFile.getAbsolutePath())
			.arg(WadScriptMain.SWITCH_ENTRY1).arg(entryPoint)
			.arg(WadScriptMain.SWITCH_CHARSET1).arg(encoding.displayName())
			.args(args)
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDERR."))
			.setInListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDIN."));
		
		LOG.infof("Calling WadScript (%s:%s).", scriptFile, entryPoint);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	public InstancedFuture<Integer> callWadMerge(final File scriptFile, final File workingDirectory, Charset encoding, String[] args, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(WadMergeMain.class).setWorkingDirectory(workingDirectory);
		callable.arg(scriptFile.getAbsolutePath())
			.arg(WadMergeMain.SWITCH_CHARSET1).arg(encoding.displayName())
			.args(args)
			.setOut(stdout)
			.setErr(stderr)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WadMerge STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WadMerge STDERR."));
		
		LOG.infof("Calling WadMerge (%s).", scriptFile);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}

	public InstancedFuture<Integer> callWSwAnTbl(File sourceFile, File outWAD, boolean addSource, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(WSwAnTablesMain.class)
			.setWorkingDirectory(sourceFile.getParentFile()); // unnecessary, but do it anyway
		
		callable.arg(outWAD.getAbsolutePath());

		callable.arg(WSwAnTablesMain.SWITCH_VERBOSE1);
		callable.arg(WSwAnTablesMain.SWITCH_IMPORT1).arg(sourceFile.getAbsolutePath());
		
		if (addSource)
			callable.arg(WSwAnTablesMain.SWITCH_ADDSOURCE1);
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(IOUtils.getNullInputStream())
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WSwAnTbl STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WSwAnTbl STDERR."));
		
		LOG.infof("Calling WSwAnTbl (%s).", sourceFile);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}
	
	public InstancedFuture<Integer> callWTexScan(File[] sourceFiles, TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(WTexScanMain.class);
		
		sourceFiles = FileUtils.explodeFiles(sourceFiles);
		
		for (int i = 0; i < sourceFiles.length; i++) 
			callable.arg(sourceFiles[i].getAbsolutePath());	

		switch (outputMode)
		{
			default:
			case BOTH:
				break;
			case TEXTURES:
				callable.arg(WTexScanMain.SWITCH_TEXTURES);
				break;
			case FLATS:
				callable.arg(WTexScanMain.SWITCH_FLATS);
				break;
		}
		
		if (noSkies)
			callable.arg(WTexScanMain.SWITCH_NOSKIES);
		
		if (noMessages)
			callable.arg(WTexScanMain.SWITCH_QUIET);

		if (mapName != null)
			callable.arg(WTexScanMain.SWITCH_MAP).arg(mapName);
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(IOUtils.getNullInputStream())
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WTexScan STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WTexScan STDERR."));
		
		LOG.infof("Calling WTexScan.");
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}
	
	public InstancedFuture<Integer> callWTExport(File[] sourceTextureFiles, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		ProcessCallable callable = Common.spawnJava(WTExportMain.class);
		
		for (int i = 0; i < sourceTextureFiles.length; i++) 
			callable.arg(sourceTextureFiles[i].getAbsolutePath());	

		if (baseFile != null)
			callable.arg(WTExportMain.SWITCH_BASE).arg(baseFile.getAbsolutePath());

		if (outputFile != null)
			callable.arg(WTExportMain.SWITCH_OUTPUT).arg(outputFile.getAbsolutePath());
		
		if (create)
			callable.arg(WTExportMain.SWITCH_CREATE);
		else
			callable.arg(WTExportMain.SWITCH_ADDITIVE);
		
		if (noAnim)
			callable.arg(WTExportMain.SWITCH_NOANIMATED);
		if (noSwitch)
			callable.arg(WTExportMain.SWITCH_NOSWITCH);
		
		if (nullTex != null)
			callable.arg(WTExportMain.SWITCH_NULLTEX).arg(nullTex);
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WTExport STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WTExport STDERR."))
			.setInListener((exception) -> LOG.errorf(exception, "Exception occurred on WTExport STDIN."));
		
		LOG.infof("Calling WTExport.");
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}
	
}
