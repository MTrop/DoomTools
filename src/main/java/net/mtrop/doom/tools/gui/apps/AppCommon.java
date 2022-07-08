package net.mtrop.doom.tools.gui.apps;

import java.awt.Container;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.WadMergeMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.apps.data.PatchExportSettings;
import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.IOUtils;

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
				language.getText("wswantbl.run.message.running"), 
				language.getText("wswantbl.run.message.success"), 
				language.getText("wswantbl.run.message.interrupt"), 
				language.getText("wswantbl.run.message.error"), 
				callWSwAnTbl(scriptFile, outWAD, outputSource, stdout, stderr)
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

	public static InstancedFuture<Integer> callDecoHack(File scriptFile, Charset encoding, File outSourceFile, File outTargetFile, boolean budget, PrintStream stdout, PrintStream stderr)
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
	public static InstancedFuture<Integer> callDoomMake(File projectDirectory, String targetName, boolean agentOverride, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
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

	public static InstancedFuture<Integer> callWadScript(final File scriptFile, final File workingDirectory, String entryPoint, Charset encoding, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
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

	public static InstancedFuture<Integer> callWadMerge(final File scriptFile, final File workingDirectory, Charset encoding, String[] args, PrintStream stdout, PrintStream stderr)
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

	public static InstancedFuture<Integer> callWSwAnTbl(File sourceFile, File outWAD, boolean addSource, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(WSwAnTablesMain.class)
			.setWorkingDirectory(sourceFile.getParentFile()); // unnecessary, but do it anyway
		
		callable.arg(outWAD.getAbsolutePath());

		callable.arg(WSwAnTablesMain.SWITCH_VERBOSE);
		callable.arg(WSwAnTablesMain.SWITCH_IMPORT).arg(sourceFile.getAbsolutePath());
		
		callable
			.setOut(stdout)
			.setErr(stderr)
			.setIn(IOUtils.getNullInputStream())
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WSwAnTbl STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WSwAnTbl STDERR."));
		
		LOG.infof("Calling WSwAnTbl (%s).", sourceFile);
		return InstancedFuture.instance(callable).spawn(DEFAULT_THREADFACTORY);
	}
	
}
