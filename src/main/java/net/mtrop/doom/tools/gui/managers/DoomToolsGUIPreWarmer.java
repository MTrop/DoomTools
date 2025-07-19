/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomTools GUI pre-warming singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsGUIPreWarmer 
{
	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsGUIPreWarmer.class); 
	/** The instance encapsulator. */
	private static final SingletonProvider<DoomToolsGUIPreWarmer> INSTANCE = new SingletonProvider<>(() -> new DoomToolsGUIPreWarmer());

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsGUIPreWarmer get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private DoomToolsGUIPreWarmer()
	{
		preWarmCompletionProviders();
		preWarmCommonImages();
		preWarmCommonIcons();
		preWarmCommonComponents();
	}

	/**
	 * Pre-loads all of the completion providers into memory in a separate thread.
	 * <p>
	 * The completion providers are not instantiated until they are needed for a
	 * particular style, but depending on how complex they are, this will cause a very noticeable
	 * hitch on first use. Calling this function will start pre-loading them in a separate thread
	 * so that they are ready to be used instantly.
	 */
	private void preWarmCompletionProviders()
	{
		DoomToolsTaskManager tasks = DoomToolsTaskManager.get();
		LOG.info("Pre-warming completion providers...");
		tasks.spawn(() -> {
			DoomToolsEditorProvider editorProvider = DoomToolsEditorProvider.get();
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_DECOHACK);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_DEFSWANI);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_DOOMMAKE);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_ROOKSCRIPT);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_WADMERGE);
			editorProvider.getProviderByStyle(DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT);
			LOG.info("Completion providers pre-warm finished.");
		});
	}
	
	/**
	 * Pre-loads common icons.
	 */
	private void preWarmCommonIcons() 
	{
		DoomToolsTaskManager tasks = DoomToolsTaskManager.get();
		LOG.info("Pre-warming common icons...");
		tasks.spawn(() -> {
			DoomToolsIconManager iconManager = DoomToolsIconManager.get();
			iconManager.getImage("activity.gif");
			LOG.info("Icon pre-warm finished.");
		});
	}

	/**
	 * Pre-loads common non-animated images.
	 */
	private void preWarmCommonImages() 
	{
		DoomToolsTaskManager tasks = DoomToolsTaskManager.get();
		LOG.info("Pre-warming common images...");
		tasks.spawn(() -> {
			DoomToolsImageManager imageManager = DoomToolsImageManager.get();
			imageManager.getImage("doomtools-logo-16.png"); 
			imageManager.getImage("doomtools-logo-32.png"); 
			imageManager.getImage("doomtools-logo-48.png"); 
			imageManager.getImage("doomtools-logo-64.png"); 
			imageManager.getImage("doomtools-logo-96.png"); 
			imageManager.getImage("doomtools-logo-128.png"); 
			imageManager.getImage("script.png");
			imageManager.getImage("script-unsaved.png");
			imageManager.getImage("close-icon.png");
			imageManager.getImage("success.png");
			imageManager.getImage("error.png");
			LOG.info("Image pre-warm finished.");
		});
	}

	/**
	 * Pre-loads common components.
	 */
	private void preWarmCommonComponents()
	{
		DoomToolsTaskManager tasks = DoomToolsTaskManager.get();
		LOG.info("Pre-warming common components...");
		tasks.spawn(() -> {
			DoomToolsEditorProvider editorProvider = DoomToolsEditorProvider.get();
			editorProvider.initCustomLanguages();
			new EditorMultiFilePanel();
			new RSyntaxTextArea();
			LOG.info("Component pre-warm finished.");
		});
	}

}
