package net.mtrop.doom.tools.gui;

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
		instance.setState(entry.state);
		return instance;
	}
	
	/**
	 * A single workspace entry.
	 */
	public static class Entry
	{
		/** Application type. */
		private Class<?> appClass;
		/** Application state type. */
		private Class<?> appStateClass;
		
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
		
		/**
		 * @return the class name of the application.
		 */
		public String getAppStateClassName() 
		{
			return appStateClass.getName();
		}
		
		/**
		 * @param appStateClassName the application state class name, fully qualified.
		 * @throws ClassCastException if bad class.
		 */
		public void setAppStateClassName(String appStateClassName)
		{
			try {
				this.appStateClass = (Class<?>)Class.forName(appStateClassName);
			} catch (ClassNotFoundException e) {
				throw new ClassCastException("Invalid application class: " + appStateClassName);
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
