package net.mtrop.doom.tools.gui;

/**
 * An application setting object, used to save and restore application settings
 * on the DoomTools workspace.
 * <p> This object is set up to be serialized via JSON. Use JSON-compatible structures!
 * @author Matthew Tropiano
 */
public class DoomToolsApplicationSettings 
{
	/** Application window bounds: Top-left X-coordinate. */
	public int windowBoundsX;
	/** Application window bounds: Top-left Y-coordinate. */
	public int windowBoundsY;
	/** Application window bounds: Width. */
	public int windowBoundsWidth;
	/** Application window bounds: Height. */
	public int windowBoundsHeight;
	/** Application window bounds: Is minimized? */
	public boolean windowMinimized;
}
