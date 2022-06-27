package net.mtrop.doom.tools.gui.apps;

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.apps.data.ExecutionSettings;
import net.mtrop.doom.tools.gui.apps.data.ExportSettings;
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
	 * @param scriptFile the script file to run.
	 * @param executionSettings
	 */
	public void onExecuteWadScriptWithSettings(Container parent, final DoomToolsStatusPanel statusPanel, File scriptFile, ExecutionSettings executionSettings)
	{
		final File workingDirectory = executionSettings.getWorkingDirectory();
		final File standardInPath = executionSettings.getStandardInPath();
		final String entryPoint = executionSettings.getEntryPoint();
		final String[] args = executionSettings.getArgs();
		
		utils.createProcessModal(
			parent, 
			language.getText("wadscript.run.message.title"), 
			language.getText("wadscript.run.message.running", entryPoint), 
			language.getText("wadscript.run.message.success"), 
			language.getText("wadscript.run.message.error"), 
			(stream, errstream) -> executeWadScript(statusPanel, scriptFile, workingDirectory, entryPoint, args, stream, errstream, standardInPath)
		).start(tasks);
	}

	private InstancedFuture<Integer> executeWadScript(
		final DoomToolsStatusPanel statusPanel, 
		final File scriptFile, 
		final File workingDirectory, 
		String entryPoint, 
		String[] args, 
		final PrintStream out, 
		final PrintStream err, 
		final File input
	){
		return tasks.spawn(() -> {
			Integer result = null;
			InputStream stdin = null;
			try
			{
				stdin = input != null ? new FileInputStream(input) : IOUtils.getNullInputStream();
				statusPanel.setActivityMessage(language.getText("wadscript.run.message.running", scriptFile.getName()));
				result = callWadScript(scriptFile, workingDirectory, entryPoint, args, out, err, stdin).get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(language.getText("wadscript.run.message.success"));
				}
				else
				{
					LOG.errorf("Error on WadScript invoke (%s) result was %d: %s", entryPoint, result, scriptFile.getAbsolutePath());
					statusPanel.setErrorMessage(language.getText("wadscript.run.message.error.result", result));
				}
			} catch (InterruptedException e) {
				LOG.warnf("Call to WadScript invoke interrupted (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.interrupt"));
			} catch (ExecutionException e) {
				LOG.errorf(e, "Error on WadScript invoke (%s): %s", entryPoint, scriptFile.getAbsolutePath());
				statusPanel.setErrorMessage(language.getText("wadscript.run.message.error"));
			} finally {
				IOUtils.close(stdin);
			}
			return result;
		});
	}
	
	private static InstancedFuture<Integer> callWadScript(final File scriptFile, final File workingDirectory, String entryPoint, String[] args, PrintStream stdout, PrintStream stderr, InputStream stdin)
	{
		ProcessCallable callable = Common.spawnJava(WadScriptMain.class).setWorkingDirectory(workingDirectory);
		callable.arg("--entry").arg(entryPoint)
			.arg(scriptFile.getAbsolutePath())
			.args(args)
			.setOut(stdout)
			.setErr(stderr)
			.setIn(stdin)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDERR."))
			.setInListener((exception) -> LOG.errorf(exception, "Exception occurred on WadScript STDIN."));
		
		LOG.infof("Calling WadScript (%s:%s).", scriptFile, entryPoint);
		return InstancedFuture.instance(callable).spawn();
	}
	
	/**
	 * 
	 * @param parent the parent container for the modal.
	 * @param statusPanel the status panel
	 * @param sourceFile the script file to run.
	 * @param processSettings
	 */
	public void onExecuteDecoHack(Container parent, final DoomToolsStatusPanel statusPanel, File sourceFile, ExportSettings processSettings)
	{
		File sourceOutputFile = processSettings.getSourceOutputFile();
		File outputFile = processSettings.getOutputFile(); 
		boolean budget = processSettings.isOutputBudget();
		
		utils.createProcessModal(
			parent, 
			language.getText("decohack.export.message.title"), 
			language.getText("decohack.export.message.running", sourceFile.getName()), 
			language.getText("decohack.export.message.success"), 
			language.getText("decohack.export.message.error"), 
			(stream, errstream) -> executeDecoHack(statusPanel, sourceFile, sourceOutputFile, outputFile, budget, stream, errstream)
		).start(tasks);
	}
	
	private InstancedFuture<Integer> executeDecoHack(
		final DoomToolsStatusPanel statusPanel, 
		File scriptFile, 
		File outSourceFile, 
		File outTargetFile, 
		boolean budget, 
		PrintStream stdout, 
		PrintStream stderr
	){
		return tasks.spawn(() -> {
			Integer result = null;
			InputStream stdin = null;
			try
			{
				statusPanel.setActivityMessage(language.getText("decohack.export.message.running", scriptFile.getName()));
				result = callDecoHack(scriptFile, outSourceFile, outTargetFile, budget, stdout, stderr).get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(language.getText("decohack.export.message.success"));
				}
				else
				{
					LOG.errorf("Error on DECOHack invoke. Result was %d: %s", result, scriptFile != null ? scriptFile.getAbsolutePath() : "STDIN");
					statusPanel.setErrorMessage(language.getText("decohack.export.message.error.result", result));
				}
			} catch (InterruptedException e) {
				LOG.warnf("Call to DECOHack invoke interrupted: %s", scriptFile != null ? scriptFile.getAbsolutePath() : "STDIN");
				statusPanel.setErrorMessage(language.getText("decohack.export.message.interrupt"));
			} catch (ExecutionException e) {
				LOG.errorf(e, "Error on DECOHack invoke: %s", scriptFile != null ? scriptFile.getAbsolutePath() : "STDIN");
				statusPanel.setErrorMessage(language.getText("decohack.export.message.error"));
			} finally {
				IOUtils.close(stdin);
			}
			return result;
		});
	}
	
	private static InstancedFuture<Integer> callDecoHack(File scriptFile, File outSourceFile, File outTargetFile, boolean budget, PrintStream stdout, PrintStream stderr)
	{
		ProcessCallable callable = Common.spawnJava(DecoHackMain.class).setWorkingDirectory(scriptFile.getParentFile());
		
		callable.arg(scriptFile.getAbsolutePath());
		callable.arg(DecoHackMain.SWITCH_OUTPUT).arg(outTargetFile.getAbsolutePath());
		
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
		return InstancedFuture.instance(callable).spawn();
	}
	
}
