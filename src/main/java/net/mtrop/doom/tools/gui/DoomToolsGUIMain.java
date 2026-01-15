/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.apps.DImageConvertApp;
import net.mtrop.doom.tools.gui.apps.DImageConvertOffsetterApp;
import net.mtrop.doom.tools.gui.apps.DMXConvertApp;
import net.mtrop.doom.tools.gui.apps.DecoHackCompilerApp;
import net.mtrop.doom.tools.gui.apps.DecoHackEditorApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeNewProjectApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeOpenProjectApp;
import net.mtrop.doom.tools.gui.apps.DoomMakeStudioApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesCompilerApp;
import net.mtrop.doom.tools.gui.apps.WSwAnTablesEditorApp;
import net.mtrop.doom.tools.gui.apps.WTExportApp;
import net.mtrop.doom.tools.gui.apps.WTexListApp;
import net.mtrop.doom.tools.gui.apps.WTexListTExportApp;
import net.mtrop.doom.tools.gui.apps.WTexScanApp;
import net.mtrop.doom.tools.gui.apps.WTexScanTExportApp;
import net.mtrop.doom.tools.gui.apps.WadMergeEditorApp;
import net.mtrop.doom.tools.gui.apps.WadMergeExecutorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptEditorApp;
import net.mtrop.doom.tools.gui.apps.WadScriptExecutorApp;
import net.mtrop.doom.tools.gui.apps.WadTexCompilerApp;
import net.mtrop.doom.tools.gui.apps.WadTexEditorApp;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIPreWarmer;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;
import net.mtrop.doom.tools.gui.swing.DoomToolsApplicationFrame;
import net.mtrop.doom.tools.gui.swing.DoomToolsMainWindow;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.StringUtils;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * Manages the DoomTools GUI window. 
 * @author Matthew Tropiano
 */
public final class DoomToolsGUIMain 
{
	/**
	 * Valid application names. 
	 */
	public interface ApplicationNames
	{
		/** DECOHack. */
		String DECOHACK = "decohack";
		/** DECOHack Compiler. */
		String DECOHACK_COMPILER = "decohack-compiler";
		/** DImageConvert. */
		String DIMGCONVERT = "dimgconv";
		/** DImageConvert Offsetter. */
		String DIMGCONVERT_OFFSETTER = "dimgconv-offsetter";
		/** DMXConvert. */
		String DMXCONVERT = "dmxconv";
		/** DoomMake - New Project. */
		String DOOMMAKE_NEW = "doommake-new";
		/** DoomMake - Open Project. */
		String DOOMMAKE_OPEN = "doommake-open";
		/** DoomMake - Studio Project. */
		String DOOMMAKE_STUDIO = "doommake-studio";
		/** WadMerge. */
		String WADMERGE = "wadmerge";
		/** WadMerge Executor. */
		String WADMERGE_EXECUTOR = "wadmerge-executor";
		/** WadScript. */
		String WADSCRIPT = "wadscript";
		/** WadScript Executor. */
		String WADSCRIPT_EXECUTOR = "wadscript-executor";
		/** WADTex. */
		String WADTEX = "wadtex";
		/** WADTex Compiler. */
		String WADTEX_COMPILER = "wadtex-compiler";
		/** WSwAnTbl. */
		String WSWANTBL = "wswantbl";
		/** WSwAnTbl Compiler. */
		String WSWANTBL_COMPILER = "wswantbl-compiler";
		/** WTexList. */
		String WTEXLIST = "wtexlist";
		/** WTexScan. */
		String WTEXSCAN = "wtexscan";
		/** WTExport. */
		String WTEXPORT = "wtexport";
		/** WTexList/WTExport. */
		String WTEXLIST_WTEXPORT = "wtexlist-wtexport";
		/** WTexScan/WTExport. */
		String WTEXSCAN_WTEXPORT = "wtexscan-wtexport";
	}
	
	/**
	 * Supported GUI Themes
	 */
	public enum GUIThemeType
	{
		LIGHT("Light", "com.formdev.flatlaf.FlatLightLaf", false),
		DARK("Dark", "com.formdev.flatlaf.FlatDarkLaf", true),
		INTELLIJ("IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf", false),
		DARCULA("Darcula", "com.formdev.flatlaf.FlatDarculaLaf", true),
		MACOSLIGHT("macOS Light", "com.formdev.flatlaf.themes.FlatMacLightLaf", false),
		MACOSDARK("macOS Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf", true),
		ARC("Arc", "com.formdev.flatlaf.intellijthemes.FlatArcIJTheme", false),
		ARCORANGE("Arc - Orange", "com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme", false),
		ARCDARK("Arc Dark", "com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme", true),
		ARCDARKORANGE("Arc Dark - Orange", "com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme", true),
		CARBON("Carbon", "com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme", true),
		COBALT2("Cobalt 2", "com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme", true),
		CYANLIGHT("Cyan light", "com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme", false),
		DARKFLAT("Dark Flat", "com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme", true),
		DARKPURPLE("Dark purple", "com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme", true),
		DRACULA("Dracula", "com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme", true),
		GRADIENTODARKFUSCHIA("Gradianto Dark Fuchsia", "com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme", true),
		GRADIENTODEEPOCEAN("Gradianto Deep Ocean", "com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme", true),
		GRADIENTOMIDNIGHTBLUE("Gradianto Midnight Blue", "com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme", true),
		GRADIENTONATUREGREEN("Gradianto Nature Green", "com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme", true),
		GRAY("Gray", "com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme", false),
		GRUVBOXDARKHARD("Gruvbox Dark Hard", "com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme", true),
		HIBERBEEDARK("Hiberbee Dark", "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme", true),
		HIGHCONTRAST("High Contrast", "com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme", true),
		LIGHTFLAT("Light Flat", "com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme", false),
		MATERIALDESIGNDARK("Material Design Dark", "com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme", true),
		MONOKAI("Monocai", "com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme", true),
		MONOKAIPRO("Monokai Pro", "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme", true),
		NORD("Nord", "com.formdev.flatlaf.intellijthemes.FlatNordIJTheme", true),
		ONEDARK("One Dark", "com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme", true),
		SOLARIZEDDARK("Solarized Dark", "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme", true),
		SOLARIZEDLIGHT("Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme", false),
		SPACEGRAY("Spacegray", "com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme", true),
		VUESION("Vuesion", "com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme", true),
		XCODEDARK("Xcode-Dark", "com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme", true),
		ARCDARKMATERIAL("Arc Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTArcDarkIJTheme", true),
		ATOMONEDARKMATERIAL("Atom One Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneDarkIJTheme", true),
		ATOMONELIGHTMATERIAL("Atom One Light (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTAtomOneLightIJTheme", false),
		DRACULAMATERIAL("Dracula (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTDraculaIJTheme", true),
		GITHUBMATERIAL("GitHub (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubIJTheme", false),
		GITHUBDARKMATERIAL("GitHub Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTGitHubDarkIJTheme", true),
		LIGHTOWLMATERIAL("Light Owl (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTLightOwlIJTheme", false),
		MATERIALDARKER("Material Darker (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDarkerIJTheme", true),
		MATERIALDEEPOCEAN("Material Deep Ocean (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialDeepOceanIJTheme", true),
		MATERIALLIGHTER("Material Lighter (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme", false),
		MATERIALOCEANIC("Material Oceanic (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialOceanicIJTheme", true),
		MATERIALPALEKNIGHT("Material Palenight (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialPalenightIJTheme", true),
		MONOKAIPROMATERIAL("Monokai Pro (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMonokaiProIJTheme", true),
		MOONLIGHTMATERIAL("Moonlight (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMoonlightIJTheme", true),
		NIGHTOWLMATERIAL("Night Owl (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTNightOwlIJTheme", true),
		SOLARIZEDDARKMATERIAL("Solarized Dark (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedDarkIJTheme", true),
		SOLARIZEDLIGHTMATERIAL("Solarized Light (Material)", "com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedLightIJTheme", false),
		;
		
		public static final Map<String, GUIThemeType> MAP = EnumUtils.createCaseInsensitiveNameMap(GUIThemeType.class);
		
		private final String className;
		private final String name;
		private final boolean dark;
		
		private GUIThemeType(String name, String className, boolean dark)
		{
			this.className = className;
			this.name = name;
			this.dark = dark;
		}
		
		public boolean isDark()
		{
			return this.dark;
		}
		
		@Override
		public String toString() 
		{
			return (this.dark ? "(Dark) " : "(Light) ") + this.name;
		}
	}
	
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsGUIMain.class); 

	/** Instance socket. */
	private static final int INSTANCE_SOCKET_PORT = 54666;
	/** The instance encapsulator. */
	private static final SingletonProvider<DoomToolsGUIMain> INSTANCE = new SingletonProvider<>(() -> new DoomToolsGUIMain());
	/** Application starter linker. */
	private static final DoomToolsApplicationStarter STARTER = new DoomToolsApplicationStarter()
	{
		@Override
		public void startApplication(DoomToolsApplicationInstance applicationInstance) 
		{
			DoomToolsGUIMain.startApplication(applicationInstance);
		}
	};
	
	/** Instance socket. */
	@SuppressWarnings("unused")
	private static ServerSocket instanceSocket;
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsGUIMain get()
	{
		return INSTANCE.get();
	}

	/**
	 * @return true if already running, false if not.
	 */
	public static boolean isAlreadyRunning()
	{
		try {
			instanceSocket = new ServerSocket(INSTANCE_SOCKET_PORT, 50, InetAddress.getByName(null));
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	/**
	 * Starts an orphaned main GUI Application.
	 * Inherits the working directory and environment.
	 * @return the process created.
	 * @throws IOException if the application could not be created.
	 * @see Common#spawnJava(Class) 
	 */
	public static Process startGUIAppProcess() throws IOException
	{
		return Common.spawnJava(DoomToolsGUIMain.class).exec();
	}
	
	/**
	 * Starts an orphaned GUI Application by name.
	 * Inherits the working directory and environment.
	 * @param appName the application name (see {@link ApplicationNames}).
	 * @param args optional addition arguments (some apps require them).
	 * @return the process created.
	 * @throws IOException if the application could not be created.
	 * @see Common#spawnJava(Class) 
	 */
	public static Process startGUIAppProcess(String appName, String ... args) throws IOException
	{
		return Common.spawnJava(DoomToolsGUIMain.class).arg(appName).args(args).exec();
	}
	
	// Sets the exception handler.
	private static void setExceptionHandler()
	{
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			String threadName = thread.getName();
			LOG.errorf(exception, "Thread [%s] threw an uncaught exception!", threadName);
			
			DoomToolsLanguageManager language = DoomToolsLanguageManager.get();
			
			JScrollPane exceptionPane = scroll(ObjectUtils.apply(textArea(StringUtils.getJREExceptionString(exception), 20, 80), (area) -> {
				area.setEditable(false);
			}));
			
			Toolkit.getDefaultToolkit().beep();
			Boolean choice = modal(language.getText("doomtools.exception.title", threadName),
				containerOf(borderLayout(),
					node(BorderLayout.NORTH, label(language.getText("doomtools.exception.content"))),
					node(BorderLayout.CENTER, exceptionPane)
				),
				choice(language.getText("doomtools.exception.continue"), Boolean.FALSE),
				choice(language.getText("doomtools.exception.shutdown"), Boolean.TRUE)
			).openThenDispose();
			
			if (choice == Boolean.TRUE)
			{
				LOG.info("Forcing JVM shutdown...");
				System.exit(2);
			}
			
		});
	}

	/* ==================================================================== */

	/**
	 * Adds a new application instance to the main desktop.
	 * @param applicationInstance the application instance.
	 */
	private static void startApplication(final DoomToolsApplicationInstance applicationInstance)
	{
		final DoomToolsApplicationFrame frame = new DoomToolsApplicationFrame(applicationInstance, STARTER);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				if (applicationInstance.shouldClose(e.getSource(), false))
				{
					frame.setVisible(false);
					applicationInstance.onClose(e.getSource());
					frame.dispose();
				}
			}
		});
		frame.setVisible(true);
	}
	
	/**
	 * Sets the preferred Look And Feel.
	 */
	public static void setLAF() 
	{
		// Fool OS into properly setting DPI constraints
		System.setProperty("sun.java2d.dpiaware", "false");
		
		if (OSUtils.isOSX())
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		GUIThemeType theme = GUIThemeType.MAP.get(DoomToolsSettingsManager.get().getThemeName());
		SwingUtils.setLAF(theme != null ? theme.className : GUIThemeType.LIGHT.className);
	}
	

	/* ==================================================================== */

	/**
	 * Main method - check for running local instance. If running, do nothing.
	 * @param args command line arguments.
	 */
	public static void main(String[] args) 
	{
		if (GraphicsEnvironment.isHeadless())
		{
			System.err.println("ERROR: Can't initialize GUI. Environment is headless!");
			System.exit(1);
		}
		
		setLAF();
		setExceptionHandler();
		
		// no args - run main application.
		if (args.length == 0)
		{
			if (isAlreadyRunning())
			{
				System.err.println("DoomTools is already running.");
				System.exit(1);
				return;
			}
			DoomToolsGUIPreWarmer.get();
			get().createAndDisplayMainWindow();
		}

		// run standalone application.
		else if (ApplicationNames.DECOHACK.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new DecoHackEditorApp(path != null ? new File(path) : null));
		}
		else if (ApplicationNames.DECOHACK_COMPILER.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new DecoHackCompilerApp(path));
		}
		else if (ApplicationNames.DIMGCONVERT.equals(args[0]))
		{
			startApplication(new DImageConvertApp());
		}
		else if (ApplicationNames.DIMGCONVERT_OFFSETTER.equals(args[0]))
		{
			File projectDirectory;
			String path = ArrayUtils.arrayElement(args, 1);
			String paletteWadPath = ArrayUtils.arrayElement(args, 2);
			
			// No path. Open file.
			if (ObjectUtils.isEmpty(path) || !(projectDirectory = new File(path)).isDirectory())
			{
				DImageConvertOffsetterApp app;
				if ((app = DImageConvertOffsetterApp.openAndCreate(null)) != null)
					startApplication(app);
			}
			else
			{
				startApplication(new DImageConvertOffsetterApp(projectDirectory, paletteWadPath));
			}
		}
		else if (ApplicationNames.DMXCONVERT.equals(args[0]))
		{
			startApplication(new DMXConvertApp());
		}
		else if (ApplicationNames.DOOMMAKE_NEW.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			if (ObjectUtils.isEmpty(path))
				path = null;
			startApplication(new DoomMakeNewProjectApp(path, !ObjectUtils.isEmpty(ArrayUtils.arrayElement(args, 2))));
		}
		else if (ApplicationNames.DOOMMAKE_OPEN.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			
			// No path. Open file.
			if (ObjectUtils.isEmpty(path))
			{
				DoomMakeOpenProjectApp app;
				if ((app = DoomMakeOpenProjectApp.openAndCreate(null)) != null)
					startApplication(app);
			}
			else
			{
				File projectDirectory = new File(args[1]);
				if (DoomMakeOpenProjectApp.isProjectDirectory(projectDirectory))
					startApplication(new DoomMakeOpenProjectApp(projectDirectory));
				else
					SwingUtils.error(DoomToolsLanguageManager.get().getText("doommake.project.open.browse.baddir", projectDirectory.getAbsolutePath()));
			}
		}
		else if (ApplicationNames.DOOMMAKE_STUDIO.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			
			// No path. Open file.
			if (ObjectUtils.isEmpty(path))
			{
				DoomMakeStudioApp app;
				if ((app = DoomMakeStudioApp.openAndCreate(null)) != null)
					startApplication(app);
			}
			else
			{
				File projectDirectory = new File(args[1]);
				if (DoomMakeStudioApp.isProjectDirectory(projectDirectory))
					startApplication(new DoomMakeStudioApp(projectDirectory));
				else
					SwingUtils.error(DoomToolsLanguageManager.get().getText("doommake.project.open.browse.baddir", projectDirectory.getAbsolutePath()));
			}
		}
		else if (ApplicationNames.WADMERGE.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadMergeEditorApp(path != null ? new File(path) : null));
		}
		else if (ApplicationNames.WADMERGE_EXECUTOR.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadMergeExecutorApp(path));
		}
		else if (ApplicationNames.WADSCRIPT.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadScriptEditorApp(path != null ? new File(path) : null));
		}
		else if (ApplicationNames.WADSCRIPT_EXECUTOR.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadScriptExecutorApp(path));
		}
		else if (ApplicationNames.WADTEX.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadTexEditorApp(path != null ? new File(path) : null));
		}
		else if (ApplicationNames.WADTEX_COMPILER.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WadTexCompilerApp(path != null ? path : null));
		}
		else if (ApplicationNames.WSWANTBL.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WSwAnTablesEditorApp(path != null ? new File(path) : null));
		}
		else if (ApplicationNames.WSWANTBL_COMPILER.equals(args[0]))
		{
			String path = ArrayUtils.arrayElement(args, 1);
			startApplication(new WSwAnTablesCompilerApp(path));
		}
		else if (ApplicationNames.WTEXLIST.equals(args[0]))
		{
			startApplication(new WTexListApp());
		}
		else if (ApplicationNames.WTEXSCAN.equals(args[0]))
		{
			startApplication(new WTexScanApp());
		}
		else if (ApplicationNames.WTEXPORT.equals(args[0]))
		{
			startApplication(new WTExportApp());
		}
		else if (ApplicationNames.WTEXLIST_WTEXPORT.equals(args[0]))
		{
			startApplication(new WTexListTExportApp());
		}
		else if (ApplicationNames.WTEXSCAN_WTEXPORT.equals(args[0]))
		{
			startApplication(new WTexScanTExportApp());
		}
		else
		{
			SwingUtils.error("Expected valid application name.");
			System.err.println("ERROR: Expected valid application name.");
			System.exit(-1);
			return;
		}
	}

	/** Settings singleton. */
	private DoomToolsSettingsManager settings;
	/** Language manager. */
	private DoomToolsLanguageManager language;
	/** The main window. */
	private DoomToolsMainWindow window;
	
	private DoomToolsGUIMain()
	{
		this.settings = DoomToolsSettingsManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.window = null;
	}

	/**
	 * Creates and displays the main window.
	 */
	public void createAndDisplayMainWindow()
	{
		LOG.info("Creating main window...");
		window = new DoomToolsMainWindow(this::attemptShutDown);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				attemptShutDown();
			}
		});
		
		Rectangle windowBounds;
		if ((windowBounds = settings.getBounds()) != null)
			window.setBounds(windowBounds);
		
		window.setVisible(true);
		if (settings.getBoundsMaximized())
			window.setExtendedState(window.getExtendedState() | DoomToolsMainWindow.MAXIMIZED_BOTH);
		
		LOG.info("Window created.");
	}

	// Attempts a shutdown, prompting the user first.
	private boolean attemptShutDown()
	{
		LOG.debug("Shutdown attempted.");
		if (SwingUtils.yesTo(window, language.getText("doomtools.quit")))
		{
			shutDown();
			return true;
		}
		return false;
	}

	// Saves and quits.
	private void shutDown()
	{
		LOG.info("Shutting down DoomTools GUI...");
		
		LOG.info("Sending close to all open apps...");
		if (!window.shutDownApps())
		{
			LOG.info("Shutdown aborted. All apps could not be closed!");
			return;
		}
		
		LOG.debug("Disposing main window...");
		settings.setBounds(window);
		window.setVisible(false);
		window.dispose();
		LOG.debug("Main window disposed.");
		
		LOG.info("Exiting JVM...");
		System.exit(0);
	}
	
}
