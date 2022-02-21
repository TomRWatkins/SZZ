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
package com.SZZ.entities;

import java.util.ArrayList;

import com.SZZ.gitMiner.GitUtil;
/**
 * Acquires all affected files for a split of a given list of commits.
 * @author Thomas Watkins
 */
public class FileThread extends Thread{
	private int iterate;
	private int split;
	private int index;
	private GitUtil gitUtil;	
	private ArrayList<Commit> commits;
	
	/**
	 * Create a File Thread. A given file thread will process "iterate" number of commits.
	 * @param iterate the number of commits to be processed by this thread
	 * @param split the split of total commits used to calculate start and finish
	 * @param index the index of this thread
	 * @param gitUtil the git utility
	 * @param commits the array list of commits
	 */
	public FileThread(int iterate, int split, int index, GitUtil gitUtil, ArrayList<Commit> commits) {
		this.iterate = iterate;
		this.split = split;
		this.index = index;
		this.gitUtil = gitUtil;
		this.commits = commits;
	}	
	
	/**
	 * Run method utilised by thread. 
	 */
	public void run() {
		for(int i = index * split; i < (index * split) + iterate; i++) 
			this.commits.get(i).setFiles(getFiles(this.commits.get(i).getHash()));		
	}
	
	/**
	 * Helper function to acquire affected files for a given commit.
	 * @param hash the hash of a commit
	 * @return the list of affected files
	 */
	private ArrayList<AffectedFile> getFiles(String hash) {
		//Get files affected by commit
		String files = this.gitUtil.getFiles(hash);		
		ArrayList<AffectedFile> affectedFiles = new ArrayList<>();
				
		//Split affected file by status and path
		String[] array = files.split("\n");			
		for(String str: array) {
			String[] split = str.split("	");
			//If commit has affected files add them
			if(split.length > 1)
				affectedFiles.add(new AffectedFile(split[0], split[1]));			
		}
		
		return affectedFiles;
	}
	
}