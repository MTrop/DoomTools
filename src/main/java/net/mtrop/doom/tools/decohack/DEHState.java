package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

import net.mtrop.doom.util.RangeUtils;

/**
 * A single state.
 * @author Matthew Tropiano
 */
public class DEHState implements DEHObject<DEHState>
{
	private int spriteIndex;
	private int frameIndex; // 28 max
	private boolean bright;
	private int nextStateIndex;
	private int duration;
	private int parameter0;
	private int parameter1;
	
	/**
	 * Creates a new frame.
	 */
	public DEHState()
	{
		setSpriteIndex(0);
		setFrameIndex(0);
		setBright(false);
		setNextStateIndex(0);
		setDuration(0);
		setParameter0(0);
		setParameter1(0);
	}
	
	public static DEHState create(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration)
	{
		return create(spriteIndex, frameIndex, bright, nextStateIndex, duration, 0, 0);
	}

	public static DEHState create(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration, int parameter0, int parameter1)
	{
		DEHState out = new DEHState();
		out.setSpriteIndex(spriteIndex);
		out.setFrameIndex(frameIndex);
		out.setBright(bright);
		out.setNextStateIndex(nextStateIndex);
		out.setDuration(duration);
		out.setParameter0(parameter0);
		out.setParameter1(parameter1);
		return out;
	}

	@Override
	public DEHState copyFrom(DEHState source) 
	{
		setSpriteIndex(source.spriteIndex);
		setFrameIndex(source.frameIndex);
		setBright(source.bright);
		setNextStateIndex(source.nextStateIndex);
		setDuration(source.duration);
		setParameter0(source.parameter0);
		setParameter1(source.parameter1);
		return this;
	}
	
	public int getSpriteIndex()
	{
		return spriteIndex;
	}
	
	public DEHState setSpriteIndex(int spriteIndex)
	{
		RangeUtils.checkRange("Sprite index", 0, Integer.MAX_VALUE, spriteIndex);
		this.spriteIndex = spriteIndex;
		return this;
	}
	
	public int getFrameIndex()
	{
		return frameIndex;
	}
	
	public DEHState setFrameIndex(int frameIndex)
	{
		RangeUtils.checkRange("Sprite frame index", 0, 28, frameIndex);
		this.frameIndex = frameIndex;
		return this;
	}
	
	public boolean isBright()
	{
		return bright;
	}
	
	public DEHState setBright(boolean bright) 
	{
		this.bright = bright;
		return this;
	}
	
	public int getNextStateIndex()
	{
		return nextStateIndex;
	}
	
	public DEHState setNextStateIndex(int nextStateIndex)
	{
		RangeUtils.checkRange("Next state index", 0, Integer.MAX_VALUE, nextStateIndex);
		this.nextStateIndex = nextStateIndex;
		return this;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public DEHState setDuration(int duration) 
	{
		RangeUtils.checkRange("Duration", 0, 9999, duration);
		this.duration = duration;
		return this;
	}
	
	public int getParameter0() 
	{
		return parameter0;
	}
	
	public DEHState setParameter0(int parameter0) 
	{
		this.parameter0 = parameter0;
		return this;
	}
	
	public int getParameter1()
	{
		return parameter1;
	}
	
	public DEHState setParameter1(int parameter1)
	{
		this.parameter1 = parameter1;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof DEHState)
			return equals((DEHState)obj);
		return super.equals(obj);
	}
	
	public boolean equals(DEHState obj) 
	{
		return spriteIndex == obj.spriteIndex
			&& frameIndex == obj.frameIndex
			&& bright == obj.bright
			&& nextStateIndex == obj.nextStateIndex
			&& duration == obj.duration
			&& parameter0 == obj.parameter0
			&& parameter1 == obj.parameter1
		;
	}	
		
	@Override
	public void writeObject(Writer writer, DEHState frame) throws IOException
	{
		if (spriteIndex != frame.spriteIndex)
			writer.append("Sprite number = ").append(String.valueOf(spriteIndex)).append('\n');
		if (frameIndex != frame.frameIndex)
			writer.append("Sprite subnumber = ").append(String.valueOf(frameIndex)).append('\n');
		if (nextStateIndex != frame.nextStateIndex)
			writer.append("Next frame = ").append(String.valueOf(frameIndex)).append('\n');
		if (duration != frame.duration || bright != frame.bright)
			writer.append("Duration = ").append(String.valueOf(duration | (bright ? 0x8000 : 0x0000))).append('\n');
		if (parameter0 != frame.parameter0)
			writer.append("Unknown 1 = ").append(String.valueOf(parameter0)).append('\n');
		if (parameter1 != frame.parameter1)
			writer.append("Unknown 2 = ").append(String.valueOf(parameter1)).append('\n');
		writer.flush();
	}

}
