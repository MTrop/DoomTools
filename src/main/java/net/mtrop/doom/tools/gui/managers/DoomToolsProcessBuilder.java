package net.mtrop.doom.tools.gui.managers;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import net.mtrop.doom.tools.DMXConvertMain;
import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.WADTexMain;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.WTExportMain;
import net.mtrop.doom.tools.WTexListMain;
import net.mtrop.doom.tools.WTexScanMain;
import net.mtrop.doom.tools.WadMergeMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * The process builder for utility processes.
 * 
 */
public final class DoomToolsProcessBuilder
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsProcessBuilder.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<DoomToolsProcessBuilder> INSTANCE = new SingletonProvider<>(() -> new DoomToolsProcessBuilder());
	
	/**
	 * @return the singleton instance of this common object.
	 */
	public static DoomToolsProcessBuilder get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */

	private DoomToolsProcessBuilder() {	}
	
	/**
	 * 
	 * @param scriptFile
	 * @param encoding
	 * @param outSourceFile
	 * @param outTargetFile
	 * @param budget
	 * @return the future task
	 */
	public ProcessCallable createDecoHack(File scriptFile, Charset encoding, File outSourceFile, File outTargetFile, boolean budget)
	{
		ProcessCallable callable = Common.spawnJava(DecoHackMain.class).setWorkingDirectory(scriptFile.getParentFile());
		
		callable.arg(scriptFile.getAbsolutePath());
		callable.arg(DecoHackMain.SWITCH_OUTPUT).arg(outTargetFile.getAbsolutePath());
		callable.arg(DecoHackMain.SWITCH_CHARSET1).arg(encoding.displayName());
		
		if (outSourceFile != null)
			callable.arg(DecoHackMain.SWITCH_SOURCE_OUTPUT).arg(outSourceFile.getAbsolutePath());
	
		if (budget)
			callable.arg(DecoHackMain.SWITCH_BUDGET);
		
		LOG.infof("Creating DECOHack (%s).", scriptFile);
		return callable;
	}

	/**
	 * Calls DImgConv.
	 * @param inputFile input file/dir for conversion.
	 * @param outputFile the output file, directory, or WAD.
	 * @param recursive if true, the input file is a directory, and the search is recursive.
	 * @param paletteSource the palette source file or WAD.
	 * @param mode the default mode.
	 * @param infoFileName the alternate name for the info file.
	 * @return the future task
	 */
	public ProcessCallable createDImgConv(File inputFile, File outputFile, boolean recursive, File paletteSource, AppCommon.GraphicsMode mode, String infoFileName)
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
		
		LOG.infof("Creating DMXConv.");
		return callable;
	}

	/**
	 * Calls DMXConvert.
	 * @param inputFiles
	 * @param ffmpegPath 
	 * @param ffmpegOnly 
	 * @param recursive
	 * @param outputDir 
	 * @return the future task
	 */
	public ProcessCallable createDMXConv(File[] inputFiles, File ffmpegPath, Boolean ffmpegOnly, Boolean recursive, File outputDir)
	{
		ProcessCallable callable = Common.spawnJava(DMXConvertMain.class);
		
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
		
		if (recursive == Boolean.TRUE)
			callable.arg(DMXConvertMain.SWITCH_RECURSIVE);
		
		if (outputDir != null)
			callable.arg(DMXConvertMain.SWITCH_OUTPUTDIR).arg(outputDir.getAbsolutePath());
	
		LOG.infof("Creating DMXConv.");
		return callable;
	}

	/**
	 * Calls a DoomMake project target.
	 * @param projectDirectory the project directory.
	 * @param targetName the target name.
	 * @param agentOverride if true, bypasses agent detection.
	 * @param args script arguments.
	 * @return the list of project targets.
	 */
	public ProcessCallable createDoomMake(File projectDirectory, String targetName, boolean agentOverride, String[] args)
	{
		ProcessCallable callable = Common.spawnJava(DoomMakeMain.class).setWorkingDirectory(projectDirectory);
		if (agentOverride)
			callable.arg(DoomMakeMain.SWITCH_AGENT_BYPASS);
		
		callable.arg(targetName).args(args);
		
		LOG.infof("Creating DoomMake (%s).", targetName);
		return callable;
	}

	public ProcessCallable createWadMerge(File scriptFile, File workingDirectory, boolean verbose, Charset encoding, String[] args)
	{
		ProcessCallable callable = Common.spawnJava(WadMergeMain.class).setWorkingDirectory(workingDirectory);
		callable.arg(scriptFile.getAbsolutePath())
			.arg(WadMergeMain.SWITCH_CHARSET1).arg(encoding.displayName());
		
		if (verbose)
			callable.arg(WadMergeMain.SWITCH_VERBOSE);
		
		callable.args(args);
		
		LOG.infof("Creating WadMerge (%s).", scriptFile.getAbsolutePath());
		return callable;
	}

	public ProcessCallable createWadScript(File scriptFile, File workingDirectory, String entryPoint, Charset encoding, String[] args)
	{
		ProcessCallable callable = Common.spawnJava(WadScriptMain.class).setWorkingDirectory(workingDirectory);
		callable.arg(scriptFile.getAbsolutePath())
			.arg(WadScriptMain.SWITCH_ENTRY1).arg(entryPoint)
			.arg(WadScriptMain.SWITCH_CHARSET1).arg(encoding.displayName())
			.args(args)
		;
		
		LOG.infof("Creating WadScript (%s:%s).", scriptFile, entryPoint);
		return callable;
	}

	public ProcessCallable createWadTex(File sourceFile, File outputWAD, String nameOverride, boolean appendMode, boolean forceStrife)
	{
		ProcessCallable callable = Common.spawnJava(WADTexMain.class);
		callable.arg(outputWAD.getAbsolutePath())
			.arg(WADTexMain.SWITCH_IMPORT1).arg(sourceFile.getAbsolutePath());
		
		if (!ObjectUtils.isEmpty(nameOverride))
			callable.arg(WADTexMain.SWITCH_NAME1).arg(nameOverride);
	
		if (appendMode)
			callable.arg(WADTexMain.SWITCH_ADDITIVE1);
	
		if (forceStrife)
			callable.arg(WADTexMain.SWITCH_STRIFE);
	
		callable.arg(WADTexMain.SWITCH_VERBOSE1);
		
		LOG.infof("Creating WadTex (%s <- %s).", outputWAD.getAbsolutePath(), sourceFile.getAbsolutePath());
		return callable;
	}

	public ProcessCallable createWSwAnTbl(File sourceFile, File outWAD, boolean addSource)
	{
		ProcessCallable callable = Common.spawnJava(WSwAnTablesMain.class)
			.setWorkingDirectory(sourceFile.getParentFile()); // unnecessary, but do it anyway
		
		callable.arg(outWAD.getAbsolutePath());
	
		callable.arg(WSwAnTablesMain.SWITCH_VERBOSE1);
		callable.arg(WSwAnTablesMain.SWITCH_IMPORT1).arg(sourceFile.getAbsolutePath());
		
		if (addSource)
			callable.arg(WSwAnTablesMain.SWITCH_ADDSOURCE1);
		
		LOG.infof("Creating WSwAnTbl (%s).", sourceFile);
		return callable;
	}

	public ProcessCallable createWTexList(File[] sourceFiles, AppCommon.TexScanOutputMode outputMode, boolean noMessages)
	{
		ProcessCallable callable = Common.spawnJava(WTexListMain.class);
		
		sourceFiles = FileUtils.explodeFiles(sourceFiles);
		
		for (int i = 0; i < sourceFiles.length; i++) 
			callable.arg(sourceFiles[i].getAbsolutePath());	
	
		switch (outputMode)
		{
			default:
			case BOTH:
				break;
			case TEXTURES:
				callable.arg(WTexListMain.SWITCH_TEXTURES);
				break;
			case FLATS:
				callable.arg(WTexListMain.SWITCH_FLATS);
				break;
		}
		
		if (noMessages)
			callable.arg(WTexListMain.SWITCH_QUIET);
	
		LOG.infof("Creating WTexList.");
		return callable;
	}

	public ProcessCallable createWTexScan(File[] sourceFiles, File[] mapInfoFiles, File[] skydefsFiles, AppCommon.TexScanOutputMode outputMode, boolean noSkies, boolean noMessages, String mapName)
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
		
		for (File f : mapInfoFiles)
			callable.arg(WTexScanMain.SWITCH_MAPINFO).arg(f.getAbsolutePath());
		
		for (File f : skydefsFiles)
			callable.arg(WTexScanMain.SWITCH_SKYDEFS).arg(f.getAbsolutePath());
	
		LOG.infof("Creating WTexScan.");
		return callable;
	}

	public ProcessCallable createWTExport(File[] sourceTextureFiles, File baseFile, File outputFile, boolean create, boolean noAnim, boolean noSwitch, String nullTex)
	{
		ProcessCallable callable = Common.spawnJava(WTExportMain.class);
		
		for (int i = 0; i < sourceTextureFiles.length; i++) 
			callable.arg(sourceTextureFiles[i].getAbsolutePath());	
	
		if (baseFile != null)
			callable.arg(WTExportMain.SWITCH_BASE1).arg(baseFile.getAbsolutePath());
	
		if (outputFile != null)
			callable.arg(WTExportMain.SWITCH_OUTPUT1).arg(outputFile.getAbsolutePath());
		
		if (create)
			callable.arg(WTExportMain.SWITCH_CREATE1);
		else
			callable.arg(WTExportMain.SWITCH_ADDITIVE1);
		
		if (noAnim)
			callable.arg(WTExportMain.SWITCH_NOANIMATED);
		if (noSwitch)
			callable.arg(WTExportMain.SWITCH_NOSWITCH);
		
		if (nullTex != null)
			callable.arg(WTExportMain.SWITCH_NULLTEX).arg(nullTex);
		
		LOG.infof("Creating WTExport.");
		return callable;
	}
	
	/**
	 * Attaches streams to a callable process.
	 * @param callable the callable process.
	 * @param stdout the standard out stream.
	 * @param stderr the standard error stream.
	 * @return the passed-in callable.
	 */
	public ProcessCallable attachStreams(ProcessCallable callable, PrintStream stdout, PrintStream stderr)
	{
		final String callableName = callable.getCallName(); 
		return callable
			.setOut(stdout)
			.setErr(stderr)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on " + callableName + " STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on " + callableName + " STDERR."))
		;
	}

	/**
	 * Attaches streams to a callable process.
	 * @param callable the callable process.
	 * @param stdout the standard out stream.
	 * @param stderr the standard error stream.
	 * @param stdin the standard in stream.
	 * @return the passed-in callable.
	 */
	public ProcessCallable attachStreams(ProcessCallable callable, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		final String callableName = callable.getCallName(); 
		return callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on " + callableName + " STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on " + callableName + " STDERR."))
			.setInListener((exception) ->  LOG.errorf(exception, "Exception occurred on " + callableName + " STDIN."))
		;
	}
	
}
