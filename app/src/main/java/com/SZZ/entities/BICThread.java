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
 * Permits a multithreaded solution for obtaining bug introducing commits and a given set of bug fixing commits. 
 * @author Thomas Watkins
 */
public class BICThread extends Thread {
	private int iterate;
	private int split;
	private int index;
	private GitUtil gitUtil;	
	private ArrayList<Link> bugIntroducingCommits;
	
	/**
	 * Create a BIC Thread. A given BIC thread will process "iterate" number of commits.
	 * @param iterate the number of commits to be processed by this thread
	 * @param split the split of total commits used to calculate start and finish
	 * @param index the index of this thread
	 * @param gitUtil the git utility
	 * @param bugIntroducingCommits the array list of commits 
	 */
	public BICThread(int iterate, int split, int index, GitUtil gitUtil, ArrayList<Link> bugIntroducingCommits) {
		this.iterate = iterate;
		this.split = split;
		this.index = index;
		this.gitUtil = gitUtil;
		this.bugIntroducingCommits = bugIntroducingCommits;
	}
	
	/**
	 * Run method utilised by thread. 
	 */
	public void run() {
		for(int i = index * split; i < (index * split) + iterate; i++) 
			this.bugIntroducingCommits.get(i).calculateBugIntroducingSuspects(gitUtil);		
	}
}
