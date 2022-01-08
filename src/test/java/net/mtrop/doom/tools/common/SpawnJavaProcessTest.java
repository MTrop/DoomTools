package net.mtrop.doom.tools.common;

import java.io.File;
import java.io.IOException;

import net.mtrop.doom.tools.DoomMakeMain;

public final class SpawnJavaProcessTest 
{
	public static void main(String[] args) throws IOException, InterruptedException 
	{
		Common.spawnJavaProcess(new File("."), DoomMakeMain.class).stdout(System.out).waitFor();
	}
}
