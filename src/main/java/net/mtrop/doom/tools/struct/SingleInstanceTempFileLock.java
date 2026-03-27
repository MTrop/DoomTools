/*******************************************************************************
 * Copyright (c) 2026 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

/**
 * A file-based, single instance locking mechanism for programs or other functions.
 * @author Matthew Tropiano
 */
public class SingleInstanceTempFileLock implements AutoCloseable
{
	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private File lockFile;
	private FileChannel lockChannel;
	private FileLock fileLock;
	
	/**
	 * Creates a lock based around a temporary file.
	 * An exclusive lock is attempted on the provided file. If it fails, an {@link IOException} is thrown.
	 * If the file does not exist, it is created.
	 * Best used in a try-with-resources block to ensure that the channel gets closed and cleaned up, like so:
	 * <pre>
	 * try (SingleInstanceLock lock = new SingleInstanceLock("processLock"))
	 * {
	 *     // ... critical code goes here ...
	 * }
	 * catch (IOException e)
	 * {
	 *     // ... lock was not acquired ...
	 * }
	 * </pre>
	 * @param fileName the name of the file to attempt a lock on.
	 * @throws IOException if the lock could not be acquired.
	 */
	public SingleInstanceTempFileLock(String fileName) throws IOException
	{
		lockFile = new File(TEMP_DIR.getAbsolutePath() + File.separator + fileName + ".lock");
		lockChannel = FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		if ((fileLock = lockChannel.tryLock()) == null)
			throw new IOException("Lock already acquired for: " + fileName);
	}

	@Override
	public void close() throws IOException
	{
		fileLock.close();
		lockChannel.close();
		lockFile.delete();
	}
	
}
