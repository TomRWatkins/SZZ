package com.SZZ.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.SZZ.entities.Commit;
import com.SZZ.entities.FileThread;
import com.SZZ.entities.Link;
import com.SZZ.entities.Suspect;
import com.SZZ.gitMiner.GitUtil;

public class Experiments {
	private ArrayList<Commit> allCommits;
	private ArrayList<Link> bugFixingCommits;
	private ArrayList<Link> fixInducingCommitHolders;		
	private HashMap<String, Commit> allCommitsHash;
	private HashMap<String, Commit> bugFixingCommitsHash;
	private HashMap<String, Commit> fixInducingCommitsHash;
	private GitUtil gitUtil;	
	private ArrayList<Integer> values;
	private ArrayList<Commit> fixInducingCommitsList;
	private int outlier = 150;
	
	
	public Experiments(ArrayList<Commit> commits, ArrayList<Link> bugFixingCommits,	ArrayList<Link> fixInducingCommitHolders) {
		this.allCommits = commits;
		this.bugFixingCommits = bugFixingCommits;
		this.fixInducingCommitHolders = fixInducingCommitHolders;
		allCommitsHash = new HashMap<>();
		bugFixingCommitsHash = new HashMap<>();
		fixInducingCommitsHash = new HashMap<>();
		this.gitUtil = new GitUtil("");
		values = new ArrayList<>();
		fixInducingCommitsList = new ArrayList<>();
		createHashMaps();
		calculateFileSizes();		
	}
	
	private void createHashMaps() {
		for(Commit c: allCommits) {
			this.allCommitsHash.put(c.getHash(), c);
		}
		
		for(Link l: bugFixingCommits) {
			this.bugFixingCommitsHash.put(l.getCommit().getHash(), l.getCommit());
		}		
		
		for(Link l: fixInducingCommitHolders) {
			for (Map.Entry<String, Suspect> entry : l.getBugIntroducingCommits().entrySet()) {
				this.fixInducingCommitsList.add(new Commit(entry.getValue().getHash(),entry.getValue().getDate(),entry.getValue().getAuthor(), ""));		  
			}
		}
		this.fixInducingCommitsList = acquireAffectedFiles(this.fixInducingCommitsList);
		
		for(Commit c: this.fixInducingCommitsList) {
			this.fixInducingCommitsHash.put(c.getHash(), c);
		}
		this.fixInducingCommitsList.clear();
		
		for (Map.Entry<String, Commit> entry : this.fixInducingCommitsHash.entrySet()) {
			this.fixInducingCommitsList.add(entry.getValue());		  
		}
	}
	
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

	private void calculateFileSizes() {	
		//1
		for(Link l: bugFixingCommits) {
			if(fixInducingCommitsHash.get(l.getCommit().getHash()) != null && l.getCommit().getFiles().size() < outlier) {
				values.add(l.getCommit().getFiles().size());
			}
		}
		double meanVal = mean();
		double standardVal = standardDeviation(meanVal);
		System.out.println("1: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		//2
		for(Link l: bugFixingCommits) { 
			if(fixInducingCommitsHash.get(l.getCommit().getHash()) == null && l.getCommit().getFiles().size() < outlier) {
				values.add(l.getCommit().getFiles().size());
			}
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("2: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		//3		
		for(Link l: bugFixingCommits) { 
			if(l.getCommit().getFiles().size() < outlier)
				values.add(l.getCommit().getFiles().size());
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("3: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		//4		
		for(Commit c: fixInducingCommitsList) {
			if(bugFixingCommitsHash.get(c.getHash()) == null && c.getFiles().size() < outlier) {
				values.add(c.getFiles().size());
			}
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("4: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		
		//5
		for(Commit c: allCommits) {
			if(bugFixingCommitsHash.get(c.getHash()) == null && fixInducingCommitsHash.get(c.getHash()) == null && c.getFiles().size() < outlier ) {
				values.add(c.getFiles().size());
			}
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("5: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		
		//6
		for(Commit c: allCommits) {
			if(bugFixingCommitsHash.get(c.getHash()) == null && c.getFiles().size() < outlier) {
				values.add(c.getFiles().size());
			}
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("6: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		
		//7		
		for(Commit c: fixInducingCommitsList) {	
			if(c.getFiles().size() < outlier)
				values.add(c.getFiles().size());
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("7: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		
		//8
		for(Commit c: allCommits) {
			if(fixInducingCommitsHash.get(c.getHash()) == null && c.getFiles().size() < outlier) {
				values.add(c.getFiles().size());
			}
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("8: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		//9
		for(Commit c: allCommits) {
			if(c.getFiles().size() < outlier)
				values.add(c.getFiles().size());
		}
		meanVal = mean();
		standardVal = standardDeviation(meanVal);
		System.out.println("9: " + meanVal + "   " + standardVal + "   " + values.size());		
		values.clear();
		
		
	}
	
	private double mean() {
		double tot = 0;
		for(int i: values) {
			tot+=i;
		}
		return tot/values.size();
	}
	
	private double standardDeviation(double meanVal) {
		double tot = 0;
		for(int i: values) {
			tot += Math.pow(i - meanVal, 2);
		}
		tot = tot/(values.size());
		
		return Math.sqrt(tot);
	}
	
}




