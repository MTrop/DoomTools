package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

/**
 * A single frame.
 * @author Matthew Tropiano
 */
public class DEHFrame implements DEHObject<DEHFrame>
{
	private int spriteIndex;
	private int frameIndex; // 28 max
	private boolean fullbright;
	private int nextFrame;
	private int duration;
	private ActionPointer action;
	private int parameter0;
	private int parameter1;
	

	@Override
	public void writeObject(Writer writer, DEHFrame frame) throws IOException
	{
		// TODO Auto-generated method stub
	}

}
