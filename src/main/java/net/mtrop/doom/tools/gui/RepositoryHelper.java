/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doom.tools.gui.RepositoryHelper.StatusEntry.StatusType;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.util.OSUtils;

/**
 * A repositiory helper for repo directories.
 * @author Matthew Tropiano
 */
public interface RepositoryHelper
{
	/**
	 * Repository operation.
	 */
	public enum Operation
	{
		INIT,
		COMMIT,
		STAGE,
		STAGE_ALL,
		UNSTAGE,
		REVERT,
		PULL,
		PUSH,
		PUSH_BRANCH,
		PUSH_NEW_BRANCH,
		BRANCH,
		CHECKOUT_BRANCH,
		MERGE,
	}
	
	/**
	 * Checks if this supports an operation.
	 * @param operation the operation.
	 * @return true if so, false if not.
	 */
	boolean supports(Operation operation);
	
	/**
	 * Performs an operation on this repository. 
	 * @param operation the operation.
	 * @param args the operation arguments.
	 * @return the result of the call.
	 * @throws UnsupportedOperationException if the operation is unsupported (see {@link #supports(Operation)}.
	 * @throws RuntimeException if a major error occurs.
	 */
	int perform(Operation operation, String ... args);
	
	/**
	 * Fetches the current repository status (file status and such).
	 * @return a list of file statuses.
	 */
	List<StatusEntry> fetchStatus(); 
	
	/**
	 * Fetches the repository's current branch status (commits ahead/behind, remote).
	 * @return a list of file statuses.
	 */
	BranchStatus fetchBranchStatus(); 
	
	/**
	 * Fetches the repository's local branches.
	 * @return a list of file statuses.
	 */
	List<String> fetchBranches(); 
	
	/**
	 * A single status entry.
	 */
	static class StatusEntry
	{
		public enum StatusType
		{
			UNKNOWN,
			MISSING,
			MODIFIED,
			ADDED,
			RENAMED,
			COPIED,
			DELETED,
			UPDATED,
			IGNORED,
			TYPE_CHANGED,
			;
			
			public char getChar()
			{
				return 
					this == UNKNOWN ? '?' :
					this == MISSING ? '?' :
					name().charAt(0);
			}
		}
		
		private StatusType type;
		private boolean staged;
		private String path;
		
		protected StatusEntry(StatusType type, boolean staged, String path)
		{
			this.type = type;
			this.staged = staged;
			this.path = path;
		}
		
		public StatusType getType()
		{
			return type;
		}
		
		public boolean isStaged() 
		{
			return staged;
		}
		
		public String getName() 
		{
			return (new File(path)).getName();
		}
		
		public String getParentPath() 
		{
			return (new File(path)).getParent();
		}

		public String getPath() 
		{
			return path;
		}
		
		public String toDisplayString() 
		{
			String parent = getParentPath();
			return type.getChar() + " " + getName() + (parent != null ? " - " + parent : "");
		}

		@Override
		public String toString() 
		{
			return (staged ? "[STAGED] " : "") + type.getChar() + " " + path;
		}

	}

	/**
	 * Branch status.
	 */
	static class BranchStatus
	{
		private String name;
		private String remoteName;
		private String commit;
		private int ahead;
		private int behind;
		
		protected BranchStatus(String name, String remoteName, String commit, int ahead, int behind)
		{
			this.name = name;
			this.remoteName = remoteName;
			this.commit = commit;
			this.ahead = ahead;
			this.behind = behind;
		}
		
		public String getName() 
		{
			return name;
		}
		
		public String getRemoteName()
		{
			return remoteName;
		}
		
		public String getCommit() 
		{
			return commit;
		}
		
		public int getAhead() 
		{
			return ahead;
		}
		
		public int getBehind() 
		{
			return behind;
		}
	}
	
	/**
	 * Helper class for Git repositories.
	 */
	static class Git implements RepositoryHelper
	{
		private File directory;

		/**
		 * Creates a Git client interface. 
		 * @param directory the directory to use as the repository directory.
		 * @throws UnsupportedOperationException if Git cannot be found on PATH.
		 * @throws IllegalArgumentException if the provided directory is not a Git repository.
		 * @see Git#isGit(File)
		 * @see Git#checkGit()
		 */
		public Git(File directory)
		{
			checkGit();
			if (!isGit(directory))
				throw new IllegalArgumentException("Not a Git repository directory.");
			this.directory = directory;
		}
		
		/**
		 * Checks if Git is initialized for a directory.
		 * @param directory the directory root to check.
		 * @return true if so, false if not. 
		 */
		public static boolean isGit(File directory)
		{
			File[] files = directory.listFiles((file) -> file.getName().equals(".git"));
			return files != null && files.length == 1;
		}

		/**
		 * Checks if Git is present on this system.
		 */
		public static void checkGit()
		{
			if (!OSUtils.onPath("git"))
				throw new UnsupportedOperationException("Could not find Git on PATH.");
		}

		@Override
		public boolean supports(Operation operation)
		{
			switch (operation)
			{
				case INIT:
				case STAGE:
				case STAGE_ALL:
				case UNSTAGE:
				case REVERT:
				case COMMIT:
				case PULL:
				case PUSH:
				case BRANCH:
				case PUSH_NEW_BRANCH:
				case CHECKOUT_BRANCH:
				case MERGE:
					return true;
				default:
					return false;
			}
		}

		@Override
		public int perform(Operation operation, String... args) 
		{
			switch (operation)
			{
				case INIT:
					return performInit();
				case STAGE:
					return performStage(args);
				case STAGE_ALL:
					return performStageAll();
				case UNSTAGE:
					return performUnStage(args);
				case REVERT:
					return performRevert(args);
				case COMMIT:
					return performCommit(args);
				case PULL:
					return performPull();
				case PUSH:
					return performPush();
				case PUSH_NEW_BRANCH:
					return performPushNewBranch(args);
				case BRANCH:
					return performBranch(args);
				case CHECKOUT_BRANCH:
					return performCheckout(args);
				case MERGE:
					return performMerge(args);
				default:
					// Fall through.
			}
			
			throw new UnsupportedOperationException("Not supported: " + operation.name());
		}

		@Override
		public List<StatusEntry> fetchStatus()
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();

			doCall(start(out, err).args("status", "--porcelain=v1"), "STATUS");
			
			List<StatusEntry> outList = new LinkedList<>();
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.trim().length() == 0)
						continue;
					
					String statusChunk = line.substring(0, 2);
					String path = line.substring(3);
					if (path.charAt(0) == '"')
						path = path.substring(1, path.length() - 1);

					for (int i = 0; i < 2; i++)
					{
						char status = statusChunk.charAt(i);
						if (status == ' ' || (i == 0 && status == '?') || status == '!')
							continue;
						boolean staged = i == 0;
						
						StatusType type;
						switch (status)
						{
							default:
							case '?':
								type = StatusType.UNKNOWN;
								break;
							case 'M':
								type = StatusType.MODIFIED;
								break;
							case 'A':
								type = StatusType.ADDED;
								break;
							case 'T':
								type = StatusType.TYPE_CHANGED;
								break;
							case 'U':
								type = StatusType.UPDATED;
								break;
							case 'R':
								type = StatusType.RENAMED;
								break;
							case 'C':
								type = StatusType.COPIED;
								break;
							case 'D':
								type = StatusType.DELETED;
								break;
							case '!':
								type = StatusType.IGNORED;
								break;
						}
						outList.add(new StatusEntry(type, staged, path));
					}
				}
			} 
			catch (IOException e)
			{
				// Should not be thrown.
			}
			
			return outList;
		}

		@Override
		public BranchStatus fetchBranchStatus()
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();

			doCall(start(out, err).args("status", "--branch", "--porcelain=v2"), "STATUS-BRANCH");
			
			String name = null;
			String remoteName = null;
			String commit = null;
			int ahead = 0;
			int behind = 0;
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.startsWith("# branch.oid "))
						commit = line.substring("# branch.oid ".length());
					else if (line.startsWith("# branch.head "))
						name = line.substring("# branch.head ".length());
					else if (line.startsWith("# branch.upstream "))
						remoteName = line.substring("# branch.upstream ".length());
					else if (line.startsWith("# branch.ab "))
					{
						String[] ab = line.substring("# branch.ab ".length()).split("\\s+");
						ahead = Integer.parseInt(ab[0]);
						behind = Math.abs(Integer.parseInt(ab[1]));
					}
				}
			}
			catch (IOException e)
			{
				// Should not be thrown.
			}
			
			return new BranchStatus(name, remoteName, commit, ahead, behind);
		}

		@Override
		public List<String> fetchBranches()
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();
			
			doCall(start(out, err).args("branch", "--color=never"), "BRANCH");

			List<String> outList = new LinkedList<>();
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					outList.add(line.substring(2));
				}
			} 
			catch (IOException e) 
			{
				// Should not be thrown.
			}
			
			return outList;
		}

		/**
		 * Initializes a repository.
		 * @return the resulting error code. 0 is no error.
		 */
		public int init()
		{
			return perform(Operation.INIT);
		}
		
		/**
		 * Stages a set of file paths.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int stage(String ... paths)
		{
			return perform(Operation.STAGE, paths);
		}
		
		/**
		 * Stages all unstaged file paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int stageAll()
		{
			return perform(Operation.STAGE_ALL);
		}
		
		/**
		 * Unstages a set of file paths.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int unstage(String ... paths)
		{
			return perform(Operation.UNSTAGE, paths);
		}
		
		/**
		 * Reverts a set of file changes.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int revert(String ... paths)
		{
			return perform(Operation.REVERT, paths);
		}
		
		/**
		 * Pulls commits from the upstream for the current branch.
		 * @return the resulting error code. 0 is no error.
		 */
		public int pull()
		{
			return perform(Operation.PULL);
		}
		
		/**
		 * Pushes pending commits to the upstream for the current branch.
		 * @return the resulting error code. 0 is no error.
		 */
		public int push()
		{
			return perform(Operation.PUSH);
		}
		
		/**
		 * Pushes a branch.
		 * @param branch the branch to push.
		 * @return the resulting error code. 0 is no error.
		 */
		public int pushBranch(String branch)
		{
			return perform(Operation.PUSH_BRANCH, branch);
		}

		/**
		 * Pushes a new branch to a remote and sets the upstream for the current branch.
		 * @param remoteName the name of the remote.
		 * @param branchName the remote name of the branch. 
		 * @return the resulting error code. 0 is no error.
		 */
		public int pushNewBranch(String remoteName, String branchName)
		{
			return perform(Operation.PUSH_NEW_BRANCH, remoteName, branchName);
		}
		
		/**
		 * Merges another branch into the current branch.
		 * @param branchName the name of the branch to merge into this one. 
		 * @return the resulting error code. 0 is no error.
		 */
		public int merge(String branchName)
		{
			return perform(Operation.MERGE, branchName);
		}
		
		/**
		 * Commits staged changes to the current branch.
		 * @param paragraphs each commit paragraph.
		 * @return the resulting error code. 0 is no error.
		 */
		public int commit(String ... paragraphs)
		{
			return perform(Operation.COMMIT, paragraphs);
		}
		
		/**
		 * Checks out a branch.
		 * @param branch the branch to check out.
		 * @return the resulting error code. 0 is no error.
		 */
		public int checkout(String branch)
		{
			return perform(Operation.CHECKOUT_BRANCH, branch);
		}
		
		/**
		 * Creates a new branch.
		 * @param newBranch the new branch to create.
		 * @return the resulting error code. 0 is no error.
		 */
		public int branch(String newBranch)
		{
			return perform(Operation.BRANCH, newBranch);
		}
		
		private ProcessCallable start()
		{
			return start(null, null);
		}
		
		private ProcessCallable start(StringWriter out, StringWriter err)
		{
			return ProcessCallable.shell("git").setWorkingDirectory(directory).setOut(out).setErr(err);
		}
		
		private int performInit()
		{
			return doCall(start().arg("init"), "INIT");
		}

		private int performStage(String ... args)
		{
			return doCall(start().arg("add").args(args), "STAGE");
		}

		private int performStageAll()
		{
			return doCall(start().arg("add").arg("-A"), "STAGE-ALL");
		}

		private int performUnStage(String ... args)
		{
			return doCall(start().arg("restore").arg("--staged").args(args), "UNSTAGE");
		}

		private int performRevert(String ... args)
		{
			ProcessCallable callable = start().arg("restore");
			for (int i = 0; i < args.length; i++)
				callable.arg(args[i]);
			
			return doCall(callable, "REVERT");
		}

		private int performCommit(String ... args)
		{
			ProcessCallable callable = start().arg("commit");
			for (int i = 0; i < args.length; i++)
				callable.arg("-m").arg(args[i]);
			
			return doCall(callable, "COMMIT");
		}

		private int performPull()
		{
			return doCall(start().arg("pull"), "PULL");
		}

		private int performPush()
		{
			return doCall(start().arg("push").setIn((File)null), "PUSH");
		}

		private int performPushNewBranch(String ... args)
		{
			return doCall(start().arg("push").arg("--set-upstream").args(args).setIn((File)null), "PUSH-UPSTREAM");
		}

		private int performBranch(String ... args)
		{
			return doCall(start().arg("branch").args(args), "BRANCH");
		}

		private int performMerge(String ... args)
		{
			return doCall(start().arg("merge").args(args), "MERGE");
		}

		private int performCheckout(String ... args)
		{
			return doCall(start().arg("checkout").args(args), "CHECKOUT");
		}


		private static int doCall(ProcessCallable callable, String type)
		{
			try {
				return callable.call();
			} catch (Exception e) {
				throw new RuntimeException("Exception on "+ type + "!", e);
			}
		}
		
		private static BufferedReader readFrom(StringWriter writer)
		{
			return new BufferedReader(new StringReader(writer.toString()));
		}
		
	}

	/**
	 * Helper class for Mercurial repositories.
	 */
	static class Mercurial implements RepositoryHelper
	{
		private File directory;

		/**
		 * Creates a Mercurial client interface. 
		 * @param directory the directory to use as the repository directory.
		 * @throws UnsupportedOperationException if Mercurial cannot be found on PATH.
		 * @throws IllegalArgumentException if the provided directory is not a Git repository.
		 * @see Mercurial#isMercurial(File)
		 * @see Mercurial#checkMercurial()
		 */
		public Mercurial(File directory)
		{
			checkMercurial();
			if (!isMercurial(directory))
				throw new IllegalArgumentException("Not a Mercurial repository directory.");
			this.directory = directory;
		}
		
		/**
		 * Checks if Mercurial is initialized for a directory.
		 * @param directory the directory root to check.
		 * @return true if so, false if not. 
		 */
		public static boolean isMercurial(File directory)
		{
			File[] files = directory.listFiles((file) -> file.getName().equals(".hg"));
			return files != null && files.length == 1;
		}

		/**
		 * Checks if Mercurial is present on this system.
		 */
		public static void checkMercurial()
		{
			if (!OSUtils.onPath("hg"))
				throw new UnsupportedOperationException("Could not find Mercurial (hg) on PATH.");
		}

		@Override
		public boolean supports(Operation operation)
		{
			switch (operation)
			{
				case INIT:
				case STAGE:
				case STAGE_ALL:
				case UNSTAGE:
				case REVERT:
				case COMMIT:
				case PULL:
				case PUSH:
				case PUSH_NEW_BRANCH:
				case BRANCH:
				case CHECKOUT_BRANCH:
				case MERGE:
					return true;
				default:
					return false;
			}
		}

		@Override
		public int perform(Operation operation, String... args) 
		{
			switch (operation)
			{
				case INIT:
					return performInit();
				case STAGE:
					return performStage(args);
				case STAGE_ALL:
					return performStageAll();
				case UNSTAGE:
					return performUnStage(args);
				case REVERT:
					return performRevert(args);
				case COMMIT:
					return performCommit(args);
				case PULL:
					return performPull();
				case PUSH:
					return performPush();
				case PUSH_NEW_BRANCH:
					return performPushNewBranch();
				case BRANCH:
					return performBranch(args);
				case CHECKOUT_BRANCH:
					return performCheckout(args);
				case MERGE:
					return performMerge(args);
				default:
					// Fall through.
			}
			
			throw new UnsupportedOperationException("Not supported: " + operation.name());
		}

		@Override
		public List<StatusEntry> fetchStatus() 
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();

			doCall(start(out, err).args("status", "--color", "never"), "STATUS");
			
			List<StatusEntry> outList = new LinkedList<>();
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.trim().length() == 0)
						continue;

					char status = line.charAt(0);
					String path = line.substring(2);

					if (status == ' ')
						continue;
					
					StatusType type;
					switch (status)
					{
						default:
						case '?':
							type = StatusType.UNKNOWN;
							break;
						case '!':
							type = StatusType.MISSING;
							break;
						case 'M':
							type = StatusType.MODIFIED;
							break;
						case 'A':
							type = StatusType.ADDED;
							break;
						case 'R':
							type = StatusType.RENAMED;
							break;
						case 'I':
							type = StatusType.IGNORED;
							break;
					}
					outList.add(new StatusEntry(type, type != StatusType.UNKNOWN, path));
				}
			}
			catch (IOException e)
			{
				// Should not be thrown.
			}
			
			return outList;
		}

		@Override
		public BranchStatus fetchBranchStatus()
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();

			String name = null;
			String remoteName = null;
			String commit = null;
			int ahead = 0;
			int behind = 0;
			
			doCall(start(out, err).args("summary", "--remote", "--color", "never"), "BRANCH-STATUS");
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.trim().length() == 0)
						continue;
					
					if (line.startsWith("branch:"))
						name = line.substring("branch:".length()).trim();
					else if (line.startsWith("parent:"))
						commit = line.substring("parent:".length()).trim();
					else if (line.startsWith("remote:"))
					{
						String incoming = null;
						String outgoing = null;
						String value = line.substring("remote:".length()).trim();
						if (value.indexOf(",") >= 0)
						{
							String[] statuses = value.split("\\,\\s+");
							for (int i = 0; i < 2; i++)
							{
								if (statuses[i].endsWith("incoming"))
									incoming = statuses[i];
								else if (statuses[i].endsWith("outgoing"))
									outgoing = statuses[i];
							}
						}
						else if (value.endsWith("incoming"))
							incoming = value;
						else if (value.endsWith("outgoing"))
							outgoing = value;
						
						if (incoming != null)
							behind = Integer.parseInt(incoming.substring(0, incoming.indexOf(" ")));
						if (outgoing != null)
							ahead = Integer.parseInt(outgoing.substring(0, outgoing.indexOf(" ")));
					}
				}
			}
			catch (IOException e)
			{
				// Should not be thrown.
			}

			out = new StringWriter();
			err = new StringWriter();
			doCall(start(out, err).args("paths", "--color", "never"), "BRANCH-STATUS-PATHS");
			
			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.trim().length() == 0)
						continue;

					remoteName = line.split("\\s+=\\s+")[0];
					break;
				}
			}
			catch (IOException e)
			{
				// Should not be thrown.
			}

			return new BranchStatus(name, remoteName, commit, ahead, behind);
		}

		@Override
		public List<String> fetchBranches() 
		{
			StringWriter out = new StringWriter();
			StringWriter err = new StringWriter();

			doCall(start(out, err).args("branches", "--color", "never"), "BRANCHES");
			
			List<String> outList = new LinkedList<>();

			try (BufferedReader br = readFrom(out))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					if (line.trim().length() == 0)
						continue;

					outList.add(line.split("\\s+")[0]);
				}
			}
			catch (IOException e)
			{
				// Should not be thrown.
			}
			
			return outList;
		}

		/**
		 * Initializes a repository.
		 * @return the resulting error code. 0 is no error.
		 */
		public int init()
		{
			return perform(Operation.INIT);
		}

		/**
		 * Stages a set of file paths.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int stage(String ... paths)
		{
			return perform(Operation.STAGE, paths);
		}

		/**
		 * Stages all unstaged file paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int stageAll()
		{
			return perform(Operation.STAGE_ALL);
		}

		/**
		 * Unstages a set of file paths.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int unstage(String ... paths)
		{
			return perform(Operation.UNSTAGE, paths);
		}

		/**
		 * Reverts a set of file changes.
		 * @param paths the list of paths.
		 * @return the resulting error code. 0 is no error.
		 */
		public int revert(String ... paths)
		{
			return perform(Operation.REVERT, paths);
		}

		/**
		 * Pulls commits from the upstream for the current branch.
		 * @return the resulting error code. 0 is no error.
		 */
		public int pull()
		{
			return perform(Operation.PULL);
		}

		/**
		 * Pushes pending commits to the upstream for the current branch.
		 * @return the resulting error code. 0 is no error.
		 */
		public int push()
		{
			return perform(Operation.PUSH);
		}

		/**
		 * Pushes a branch.
		 * @param branch the branch to push.
		 * @return the resulting error code. 0 is no error.
		 */
		public int pushBranch(String branch)
		{
			return perform(Operation.PUSH_BRANCH, branch);
		}

		/**
		 * Pushes a new branch.
		 * @param branch the branch to push.
		 * @return the resulting error code. 0 is no error.
		 */
		public int pushNewBranch(String branch)
		{
			return perform(Operation.PUSH_NEW_BRANCH, branch);
		}

		/**
		 * Merges another branch into the current branch.
		 * @param branchName the name of the branch to merge into this one. 
		 * @return the resulting error code. 0 is no error.
		 */
		public int merge(String branchName)
		{
			return perform(Operation.MERGE, branchName);
		}

		/**
		 * Commits staged changes to the current branch.
		 * @param paragraphs each commit paragraph.
		 * @return the resulting error code. 0 is no error.
		 */
		public int commit(String ... paragraphs)
		{
			return perform(Operation.COMMIT, paragraphs);
		}

		/**
		 * Checks out a branch.
		 * @param branch the branch to check out.
		 * @return the resulting error code. 0 is no error.
		 */
		public int checkout(String branch)
		{
			return perform(Operation.CHECKOUT_BRANCH, branch);
		}

		/**
		 * Creates a new branch.
		 * @param newBranch the new branch to create.
		 * @return the resulting error code. 0 is no error.
		 */
		public int branch(String newBranch)
		{
			return perform(Operation.BRANCH, newBranch);
		}

		private ProcessCallable start()
		{
			return start(null, null);
		}
		
		private ProcessCallable start(StringWriter out, StringWriter err)
		{
			return ProcessCallable.shell("hg").setWorkingDirectory(directory).setOut(out).setErr(err);
		}
		
		private int performInit()
		{
			return doCall(start().arg("init"), "INIT");
		}

		private int performStage(String ... args)
		{
			return doCall(start().arg("add").args(args), "STAGE");
		}

		private int performStageAll(String ... args)
		{
			return doCall(start().arg("add"), "STAGE-ALL");
		}

		private int performUnStage(String ... args)
		{
			return doCall(start().arg("forget").args(args), "UNSTAGE");
		}

		private int performRevert(String ... args)
		{
			ProcessCallable callable = start().arg("revert");
			for (int i = 0; i < args.length; i++)
				callable.arg(args[i]);
			
			return doCall(callable, "REVERT");
		}

		private int performCommit(String ... args)
		{
			return doCall(start().arg("commit").arg("-m").args(args), "COMMIT");
		}

		private int performPull()
		{
			return doCall(start().arg("pull"), "PULL");
		}

		private int performPush()
		{
			return doCall(start().arg("push"), "PUSH");
		}

		private int performPushNewBranch()
		{
			return doCall(start().arg("push").arg("--new-branch"), "PUSH");
		}

		private int performBranch(String ... args)
		{
			return doCall(start().arg("branch").args(args), "BRANCH");
		}

		private int performMerge(String ... args)
		{
			return doCall(start().arg("merge").args(args), "MERGE");
		}

		private int performCheckout(String ... args)
		{
			return doCall(start().arg("checkout").args(args), "CHECKOUT");
		}

		private static int doCall(ProcessCallable callable, String type)
		{
			try {
				return callable.call();
			} catch (Exception e) {
				throw new RuntimeException("Exception on "+ type + "!", e);
			}
		}
		
		private static BufferedReader readFrom(StringWriter writer)
		{
			return new BufferedReader(new StringReader(writer.toString()));
		}
		
	}
}
