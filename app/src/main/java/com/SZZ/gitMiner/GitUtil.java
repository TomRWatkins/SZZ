//
// =============================================================
// Thomas Watkins
// University of Lancaster UK 2022
// Project undertaken for my Undergraduate Dissertation Thesis
// 
// Supervised by Dr David Bowes
// 
// MIT License  
// =============================================================
//
package com.SZZ.gitMiner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.SZZ.entities.BlameLine;
/**
 * A utility class enabling git functionality utilising JGit and ProcessBuilder. 
 * @author Thomas Watkins
 */
public class GitUtil {
	private String githubURL;
	private String path;

	/**
	 * Creates a git utility for a given github repository.
	 * @param githubURL the URL for the github repository
	 */
	public GitUtil(String githubURL) {
		this.githubURL = githubURL;
		this.path = System.getProperty("user.dir") + "/Repo";
	}

	/**
	 * Obtains the git logs for a github repository, also clones the repository.
	 * @return the git logs for a github repository.
	 */
	public Iterable<RevCommit> getGitLogs()	{		
		try {
			// Delete repository if exists
			Path repoPath = Paths.get(this.path);
			deleteRepository(this.path);

			// Clone repository
			System.out.println("Cloning github repository");
			File f = new File(this.path);
			Git git = Git.cloneRepository().setURI(this.githubURL).setDirectory(f).call();			

			// Get git logs
			git = Git.open(repoPath.toFile());
			return git.log().call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * A helper funciton to delete a repository if it already exists locally.
	 * @param directoryFilePath the repository path
	 */
	private void deleteRepository(String directoryFilePath) {
		Path directory = Paths.get(directoryFilePath);
		try {
			if (Files.exists(directory)) {
				System.out.println("Existing git repository found, deleting repository...");
				Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

					public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
							throws IOException {
						Files.delete(path);
						return FileVisitResult.CONTINUE;
					}

					public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
						Files.delete(directory);
						return FileVisitResult.CONTINUE;
					}
				});
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * @return The root commit for a given repository
	 */
	public String getRootCommit() {
		try {
			String[] command = { "git", "rev-list", "HEAD", "--reverse" };
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			File f = new File(this.path);
			processBuilder.directory(f);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			// Return first line (Root)
			while ((line = reader.readLine()) != null)
				return line;

			int ec = process.waitFor();
			if (ec != 0)
				System.out.println("Error " + ec);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Obtains the files affected by a commit.
	 * @param hash the commit hash
	 * @return the files affected
	 */
	public String getFiles(String hash) {
		String[] command = { "git", "diff-tree", "--no-commit-id", "--name-status", "-r", hash };
		return executeCmd(command);
	}
	
	/**
	 * Obtains the diff between a given commit and the commit previous for a given file.
	 * @param hash the commit hash
	 * @param filename the file
	 * @return the diff between a given commit and the previous commit
	 */
	public String getDiff(String hash, String filename) {
		String[] command = { "git", "rev-parse", hash + "^" };
		String previousHash = executeCmd(command).replaceAll("\n", "");

		String[] diffCommand = { "git", "diff", "-U0", previousHash, hash, "--", filename };

		return executeCmd(diffCommand);
	}

	/**
	 * Conducts a git blame on the previous commit of the commit given (The last commit without the fix).
	 * Returns an arraylist of Blamed Lines. 
	 * @param hash the commit hash
	 * @param file the file to be blamed
	 * @param linesRemoved the lines to be assessed
	 * @return an arraylist of blamed lines
	 */
	public ArrayList<BlameLine> blame(String hash, String file, ArrayList<Integer> linesRemoved) {
		String[] command = { "git", "rev-parse", hash + "^" };
		String previousHash = executeCmd(command).replaceAll("\n", "");
		ArrayList<BlameLine> blamedLines = new ArrayList<>();
		
		try {
			Git git = Git.open(Paths.get(this.path).toFile());		
			ObjectId gitCommitToBeBlamed = git.getRepository().resolve(previousHash);		
			BlameResult result = git.blame().setFilePath(file).setStartCommit(gitCommitToBeBlamed)
						.setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();

			RawText rawText = result.getResultContents();
			for (int line: linesRemoved) {
				PersonIdent sourceAuthor = result.getSourceAuthor(line-1);
				RevCommit sourceCommit = result.getSourceCommit(line-1);
				if (sourceCommit != null) {
					blamedLines.add(new BlameLine(sourceCommit.getName(), sourceCommit.getCommitTime(), line,
							rawText.getString(line-1), sourceAuthor.getName()));
				}
			}		
		} catch(Exception e) {
			e.printStackTrace();
		}		
		return blamedLines;
	}
	
	/**
	 * Calculates the starting line number of a hunk within a diff. 
	 * @param line the line of the diff
	 * @return the starting line number
	 */
	private int getStartingHunk(String line) {
		int lineNumber;
		int leftIndex = line.indexOf('-');
		leftIndex++;
		char[] arr = line.toCharArray();
		int rightIndex = leftIndex + 1;

		while (arr[rightIndex] != ',' && arr[rightIndex] != ' ') {
			rightIndex++;
		}
		String lineNumberParse = line.substring(leftIndex, rightIndex);
		lineNumber = Integer.parseInt(lineNumberParse);
		return lineNumber;
	}

	/**
	 * Calculates the deleted lines within a diff.
	 * @param diff the arraylist of diff lines
	 * @return an arraylist of lines deleted
	 */
	public ArrayList<Integer> getLinesRemoved(ArrayList<String> diff) {
		ArrayList<String> diffLines = diff;
		int lineNumber = 1;
		ArrayList<Integer> linesRemoved = new ArrayList<>();

		for (int i = 4; i < diff.size(); i++) {
			String line = diffLines.get(i);
			switch (line.charAt(0)) {
			case '@':
				lineNumber = getStartingHunk(line);
				break;
			case '-':
				linesRemoved.add(lineNumber);
				lineNumber++;
				break;
			default:
				break;
			}

		}
		return linesRemoved;
	}

	/**
	 * A helper function that executes commands using a process builder.
	 * @param command the command to be executed
	 * @return the output of execution
	 */
	private String executeCmd(String[] command) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			File f = new File(this.path);
			processBuilder.directory(f);
			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				System.out.println(exitCode);
				return "";
			}

			return output.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}
}
