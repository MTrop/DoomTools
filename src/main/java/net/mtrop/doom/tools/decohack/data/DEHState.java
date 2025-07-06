/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.RangeUtils;

/**
 * A single state.
 * @author Matthew Tropiano
 */
public class DEHState extends DEHObject<DEHState>
{
	private int spriteIndex;
	private int frameIndex; // 28 max
	private boolean bright;
	private int nextStateIndex;
	private int duration;
	private int misc1;
	private int misc2;

	// MBF21
	private int[] args;
	private int mbf21Flags;
	
	// ID24
	private String tranmap;
	
	/**
	 * Creates a new state.
	 */
	public DEHState()
	{
		set(
			0,
			0, 
			false,
			0, 
			-1,
			0,
			0,
			new int[0],
			0x00
		);
		setTranmap(null);
	}
	
	public static DEHState create(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration)
	{
		return create(spriteIndex, frameIndex, bright, nextStateIndex, duration, 0, 0, new int[0], 0);
	}

	public static DEHState create(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration, int mbfFlags)
	{
		return create(spriteIndex, frameIndex, bright, nextStateIndex, duration, 0, 0, new int[0], mbfFlags);
	}

	public static DEHState create(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration, int misc1, int misc2, int[] args, int mbfFlags)
	{
		return (new DEHState()).set(
			spriteIndex,
			frameIndex, 
			bright,
			nextStateIndex, 
			duration,
			misc1,
			misc2,
			args,
			mbfFlags
		).setTranmap(null);
	}

	@Override
	public DEHState copyFrom(DEHState source) 
	{
		if (source == this)
			return this;
		
		setSpriteIndex(source.spriteIndex);
		setFrameIndex(source.frameIndex);
		setBright(source.bright);
		setNextStateIndex(source.nextStateIndex);
		setDuration(source.duration);
		setMisc1(source.misc1);
		setMisc2(source.misc2);
		
		// MBF21
		setArgs(source.args);
		setMBF21Flags(source.mbf21Flags);

		// ID24
		setTranmap(source.tranmap);
		
		return this;
	}
	
	public DEHState set(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration)
	{
		return set(spriteIndex, frameIndex, bright, nextStateIndex, duration, 0, 0, new int[0], 0);
	}
	
	public DEHState set(int spriteIndex, int frameIndex, boolean bright, int nextStateIndex, int duration, int misc1, int misc2, int[] args, int mbfFlags)
	{
		setSpriteIndex(spriteIndex);
		setFrameIndex(frameIndex);
		setBright(bright);
		setNextStateIndex(nextStateIndex);
		setDuration(duration);
		setMisc1(misc1);
		setMisc2(misc2);
		
		// MBF21
		setArgs(args);
		setMBF21Flags(mbfFlags);
		
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
		this.duration = duration;
		return this;
	}
	
	public int getMisc1() 
	{
		return misc1;
	}
	
	public DEHState setMisc1(int misc1) 
	{
		this.misc1 = misc1;
		return this;
	}
	
	public int getMisc2()
	{
		return misc2;
	}
	
	public DEHState setMisc2(int misc2)
	{
		this.misc2 = misc2;
		return this;
	}
	
	public int[] getArgs()
	{
		return args;
	}
	
	public DEHState setArgs(int[] args)
	{
		this.args = args;
		return this;
	}

	public DEHState setArgs(List<Integer> arglist)
	{
		// gotta do this manually, 'cause unboxing, yuck :P
		this.args = new int[arglist.size()];
		int i = 0;
		for (Integer arg : arglist)
		{
			this.args[i] = arg;
			i++;
		}
		return this;
	}
	
	public int getMBF21Flags() 
	{
		return mbf21Flags;
	}
	
	public DEHState setMBF21Flags(int flags) 
	{
		this.mbf21Flags = flags;
		return this;
	}
	
	public String getTranmap() 
	{
		return tranmap;
	}
	
	public DEHState setTranmap(String tranmap) 
	{
		this.tranmap = tranmap;
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
			&& misc1 == obj.misc1
			&& misc2 == obj.misc2
			// MBF21
			&& Arrays.equals(args, obj.args)
			&& mbf21Flags == obj.mbf21Flags
			// ID24
			&& ObjectUtils.areEqual(tranmap, obj.tranmap)
		;
	}	
		
	@Override
	public void writeObject(Writer writer, DEHState frame, DEHFeatureLevel level) throws IOException
	{
		if (spriteIndex != frame.spriteIndex)
			writer.append("Sprite number = ").append(String.valueOf(spriteIndex)).append("\r\n");
		if (frameIndex != frame.frameIndex || bright != frame.bright)
			writer.append("Sprite subnumber = ").append(String.valueOf(frameIndex | (bright ? 0x08000 : 0x00000))).append("\r\n");
		if (nextStateIndex != frame.nextStateIndex)
			writer.append("Next frame = ").append(String.valueOf(nextStateIndex)).append("\r\n");
		if (duration != frame.duration)
			writer.append("Duration = ").append(String.valueOf(duration)).append("\r\n");
		if (misc1 != frame.misc1)
			writer.append("Unknown 1 = ").append(String.valueOf(misc1)).append("\r\n");
		if (misc2 != frame.misc2)
			writer.append("Unknown 2 = ").append(String.valueOf(misc2)).append("\r\n");

		if (level.supports(DEHFeatureLevel.MBF21))
		{
			for (int i = 0; i < args.length; i++)
				if (i >= frame.args.length || args[i] != frame.args[i])
					writer.append("Args").append(String.valueOf(i+1)).append(" = ").append(String.valueOf(args[i])).append("\r\n");
			if (mbf21Flags != frame.mbf21Flags)
				writer.append("MBF21 Bits = ").append(String.valueOf(mbf21Flags)).append("\r\n");
		}

		if (level.supports(DEHFeatureLevel.ID24))
		{
			if (!ObjectUtils.areEqual(tranmap, frame.tranmap))
				writer.append("Tranmap = ").append(tranmap).append("\r\n");
		}
		
		writeCustomProperties(writer);
		writer.flush();
	}

}
