/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackrook.json.annotation.JSONMapType;

import net.mtrop.doom.tools.common.Common;

/**
 * A workspace state abstraction.
 * Serialized on save to JSON.
 * @author Matthew Tropiano
 */
public final class DoomToolsWorkspace
{
	/**
	 * Creates a new application instance.
	 * @param entry the application entry.
	 * @return a new application instance.
	 * @throws ClassCastException if the entry does not refer to an application class.
	 */
	public static DoomToolsApplicationInstance createApplication(Entry entry)
	{
		DoomToolsApplicationInstance instance = (DoomToolsApplicationInstance)Common.create(entry.appClass);
		instance.setApplicationState(entry.state);
		return instance;
	}
	
	/** Entries. */
	private List<Entry> entries;
	/** Main Window width. */
	private int windowWidth;
	/** Main Window height. */
	private int windowHeight;
	
	public DoomToolsWorkspace()
	{
		this.entries = new LinkedList<>();
		this.windowWidth = 0;
		this.windowHeight = 0;
	}
	
	/**
	 * Creates a new entry.
	 * @return the new entry.
	 */
	public Entry createEntry()
	{
		Entry out;
		entries.add(out = new Entry());
		return out;
	}
	
	public void setEntries(Entry[] entries)
	{
		this.entries = Arrays.asList(entries); 
	}
	
	public List<Entry> getEntries()
	{
		return entries;
	}
	
	public void setWindowWidth(int windowWidth) 
	{
		this.windowWidth = windowWidth;
	}
	
	public int getWindowWidth() 
	{
		return windowWidth;
	}
	
	public void setWindowHeight(int windowHeight) 
	{
		this.windowHeight = windowHeight;
	}
	
	public int getWindowHeight() 
	{
		return windowHeight;
	}
	
	/**
	 * A single workspace entry.
	 */
	public static class Entry
	{
		/** Application type. */
		private Class<?> appClass;
		
		/** Application window bounds: Top-left X-coordinate. */
		private Integer windowBoundsX;
		/** Application window bounds: Top-left Y-coordinate. */
		private Integer windowBoundsY;
		/** Application window bounds: Width. */
		private Integer windowBoundsWidth;
		/** Application window bounds: Height. */
		private Integer windowBoundsHeight;
		/** Application window bounds: Is minimized? */
		private Boolean windowMinimized;
		/** State data. */
		private Map<String, String> state;

		public Entry()
		{
			this.appClass = null;
			this.windowBoundsX = null;
			this.windowBoundsY = null;
			this.windowBoundsWidth = null;
			this.windowBoundsHeight = null;
			this.windowMinimized = null;
			this.state = null;
		}
		
		/**
		 * @return the class name of the application.
		 */
		public String getAppClassName() 
		{
			return appClass.getName();
		}
		
		/**
		 * @param appClassName the application class name, fully qualified.
		 * @throws ClassCastException if bad class.
		 */
		public void setAppClassName(String appClassName) 
		{
			try {
				this.appClass = (Class<?>)Class.forName(appClassName);
			} catch (ClassNotFoundException e) {
				throw new ClassCastException("Invalid application class: " + appClassName);
			}
		}
		
		public Integer getWindowBoundsX() 
		{
			return windowBoundsX;
		}

		public void setWindowBoundsX(Integer windowBoundsX) 
		{
			this.windowBoundsX = windowBoundsX;
		}

		public Integer getWindowBoundsY()
		{
			return windowBoundsY;
		}

		public void setWindowBoundsY(Integer windowBoundsY) 
		{
			this.windowBoundsY = windowBoundsY;
		}

		public Integer getWindowBoundsWidth()
		{
			return windowBoundsWidth;
		}

		public void setWindowBoundsWidth(Integer windowBoundsWidth)
		{
			this.windowBoundsWidth = windowBoundsWidth;
		}

		public Integer getWindowBoundsHeight() 
		{
			return windowBoundsHeight;
		}

		public void setWindowBoundsHeight(Integer windowBoundsHeight)
		{
			this.windowBoundsHeight = windowBoundsHeight;
		}

		public Boolean getWindowMinimized() 
		{
			return windowMinimized;
		}

		public void setWindowMinimized(Boolean windowMinimized)
		{
			this.windowMinimized = windowMinimized;
		}

		public Map<String, String> getState() 
		{
			return state;
		}

		@JSONMapType(keyType = String.class, valueType = String.class)
		public void setState(Map<String, String> state)
		{
			this.state = state;
		}
		
	}
	
}
