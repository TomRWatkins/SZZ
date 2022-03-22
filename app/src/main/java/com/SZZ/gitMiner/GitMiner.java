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

import java.io.File;

import java.io.PrintWriter;
import java.util.ArrayList;
import org.eclipse.jgit.revwalk.RevCommit;

import com.SZZ.entities.BICThread;
import com.SZZ.entities.Commit;
import com.SZZ.entities.FileThread;
import com.SZZ.entities.Link;
import com.SZZ.entities.Suspect;
/**
 * Clones and mines a github repository. Extracts and returns the commits from an obtained git log.
 * @author Thomas Watkins
 */
public class GitMiner {	
	
	private GitUtil gitUtil;
	
	/**
	 * Creates a GitMiner with specified github repository url.
	 * @param githubURL the github repository url
	 */
	public GitMiner(String githubURL) {		
		this.gitUtil = new GitUtil(githubURL);
	}
	
	/**
	 * Clones and mines a github repository, returns an arraylist of commits. 
	 * Ignores the root commit. 
	 * 
	 * @return array list of commits	 
	 */
	public ArrayList<Commit> mineGit() {		
		Iterable<RevCommit> logs = null;
		logs = this.gitUtil.getGitLogs();
				
		return generateCommits(logs);		
	}

	/**
	 * Generates an array list of commits from git logs.
	 * @param logs git logs for git repository
	 * @return	array list of commits
	 */
	private ArrayList<Commit> generateCommits(Iterable<RevCommit> logs) {
		System.out.println("Getting Git Commits");
		
		ArrayList<Commit> commits = new ArrayList<>();
		//Get root commit (Cant be bug fixing)
		String rootHash = this.gitUtil.getRootCommit();	
		
		//Extract commit data from log entry
		for (RevCommit rev : logs) {
			String hash = rev.getId().getName();			
			long timeStamp = rev.getCommitTime();
			String author = rev.getAuthorIdent().getName();
	        String comment = rev.getShortMessage();	        
	        
	        //Ignore root commit
	        if(!hash.equals(rootHash))	        	
	        	commits.add(new Commit(hash, timeStamp, author, comment));
	    }
										
		return acquireAffectedFiles(commits);
	}
	
	/**
	 * Function to acquire list of affected files for all commits. Split across cores for
	 * efficiency.
	 * 	
	 * @param commits array list of commits
	 * @return array list of commits
	 */
	private ArrayList<Commit> acquireAffectedFiles(ArrayList<Commit> commits) {
		int CORES = Runtime.getRuntime().availableProcessors();
		int[] splits = new int[CORES];
		int rem = commits.size() % CORES;
		int split = (commits.size() - rem) / CORES; 
		
		for(int i = 0; i < CORES; i++) 
			splits[i] = split;
		splits[CORES-1] += rem;
		
		FileThread[] threads = new FileThread[CORES];
		for(int i = 0; i < CORES; i++) 
			threads[i] = new FileThread(splits[i], split, i, this.gitUtil, commits);
				
		try {
			for(FileThread t: threads) 
				t.start();			
			for(FileThread t: threads)		
				t.join();
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		
		return commits;		
	}

	/**
	 * Calculates the bug introducing commits for a given list of bug fixing commits.
	 * @param bugFixingCommits the bug fixing commits to be calculated
	 * @return arraylist of commits containing bug introducing commits
	 */
	public ArrayList<Link> calculateBugIntroducingCommits(ArrayList<Link> bugFixingCommits) {
		System.out.println("Calculating Bug Introducing Commits");
		ArrayList<Link> bugIntroducingCommits = bugFixingCommits;
		
		int CORES = Runtime.getRuntime().availableProcessors();
		int[] splits = new int[CORES];
		int rem = bugIntroducingCommits.size() % CORES;
		int split = (bugIntroducingCommits.size() - rem) / CORES; 
		
		for(int i = 0; i < CORES; i++) 
			splits[i] = split;
		splits[CORES-1] += rem;
		
		BICThread[] threads = new BICThread[CORES];
		for(int i = 0; i < CORES; i++) 
			threads[i] = new BICThread(splits[i], split, i, this.gitUtil, bugIntroducingCommits);
				
		try {
			for(BICThread t: threads) 
				t.start();			
			for(BICThread t: threads)		
				t.join();
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		
		printToFile(bugIntroducingCommits);
		return bugIntroducingCommits;
	}
	
	/**
	 * A function to print the bug introducing commits to file.
	 */
	private void printToFile(ArrayList<Link> bugIntroducingCommits) {
		String key = bugIntroducingCommits.get(0).getBug().getIssueKey();		
		String projectName = key.substring(0, key.indexOf("-")).toUpperCase();		
		try {
			File f = new File(projectName + "-BugIntroducingCommits" + ".csv");
			PrintWriter writer = new PrintWriter(f);
			writer.println("BFCHash;BFCTime;File;BICHash;BICTime;IssueKey;");
			for(Link l: bugIntroducingCommits) {		
				for (Suspect suspect : l.getBugIntroducingCommits().values()) { 
					String printString = l.getCommit().getHash() + ";"							
							+ l.getCommit().getTimeStamp() + ";"
							+ suspect.getFileName() + ";"
							+ suspect.getHash() + ";"
							+ suspect.getDate() + ";"
							+ l.getBug().getIssueKey() + ";";			
							writer.println(printString);
				}				
			}	
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}

