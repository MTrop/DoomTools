package net.mtrop.doom.tools.gui;

import java.io.File;

import net.mtrop.doom.tools.gui.RepositoryHelper.Git;
import net.mtrop.doom.tools.gui.RepositoryHelper.Operation;

public final class RepositoryHelperTest 
{

	public static void main(String[] args)
	{
		new Git(new File(".")).perform(Operation.UNSTAGE, "src/main/java/net/mtrop/doom/tools/struct/ReplacerReader.java",
				"src/main/java/net/mtrop/doom/tools/gui/RepositoryHelper.java",
				"src/test/java/net/mtrop/doom/tools/gui/RepositoryHelperTest.java");
	}

}
