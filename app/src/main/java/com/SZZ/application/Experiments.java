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
		//calculateFileSizes();		
		calculateDays();
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
	
//	case 0: System.out.println("Sun"); 
//	case 1: System.out.println("Mon"); 
//	case 2: System.out.println("Tues"); 
//	case 3: System.out.println("Weds"); 
//	case 4: System.out.println("Thu"); 
//	case 5: System.out.println("Fri"); 
//	case 6: System.out.println("Sat"); 
	
	private void calculateDays() {
		//baseline
		double[] days = new double[7];
		for(int i = 0; i < days.length; i++) {
			days[i] = 0;
		}		
		for(Commit c: allCommits) {
			days[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;			
		}
		
		//Row one
		double[] daysRowOne = new double[7];
		for(int i = 0; i < daysRowOne.length; i++ ) {
			daysRowOne[i] = 0;
		}		
		for(Link l : bugFixingCommits) {
			daysRowOne[(int)(Math.floor(l.getCommit().getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resOne = new double[7];
		for(int i = 0; i < 7; i++) {
			resOne[i] = (daysRowOne[i]);
			//resOne[i] = (daysRowOne[i] / days[i]) * 100;
			System.out.print(resOne[i] + " ");
		}
		System.out.println();

		//Row Two
		double[] daysRowTwo = new double[7];
		for(int i = 0; i < daysRowTwo.length; i++ ) {
			daysRowTwo[i] = 0;
		}		
		for(Commit c : fixInducingCommitsList) {
			daysRowTwo[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resTwo = new double[7];
		for(int i = 0; i < 7; i++) {
			resTwo[i] = (daysRowTwo[i]);
			//resTwo[i] = (daysRowTwo[i] / days[i]) * 100;
			System.out.print(resTwo[i] + " ");
		}
		System.out.println();
		
		//Row Three
		double[] daysRowThree = new double[7];
		for(int i = 0; i < daysRowThree.length; i++ ) {
			daysRowThree[i] = 0;
		}	
		for(Commit c : fixInducingCommitsList) {
			if(bugFixingCommitsHash.get(c.getHash()) !=  null) {
				daysRowThree[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;
			}
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resThree = new double[7];
		for(int i = 0; i < 7; i++) {
			resThree[i] = (daysRowThree[i]);
			//resThree[i] = (daysRowThree[i] / days[i]) * 100;
			System.out.print(resThree[i] + " ");
		}
		System.out.println();
		
		//Row Four
		double[] daysRowFour = new double[7];
		for(int i = 0; i < daysRowFour.length; i++ ) {
			daysRowFour[i] = 0;
		}	
		for(Commit c: allCommits) {
			if(bugFixingCommitsHash.get(c.getHash()) == null && fixInducingCommitsHash.get(c.getHash()) == null)
				daysRowFour[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;			
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resFour = new double[7];
		for(int i = 0; i < 7; i++) {
			resFour[i] = (daysRowFour[i]);
			//resFour[i] = (daysRowFour[i] / days[i]) * 100;
			System.out.print(resFour[i] + " ");
		}
		System.out.println();
		
		
		//Row Five
		double[] fixDays = new double[7];
		for(int i = 0; i < days.length; i++) {
			fixDays[i] = 0;
		}	
		for(Link l: bugFixingCommits) {
			fixDays[(int)(Math.floor(l.getCommit().getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		double[] daysRowFive = new double[7];
		for(int i = 0; i < daysRowFive.length; i++ ) {
			daysRowFive[i] = 0;
		}	
		for(Link l: bugFixingCommits) {
			if(fixInducingCommitsHash.get(l.getCommit().getHash()) != null)
				daysRowFive[(int)(Math.floor(l.getCommit().getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resFive = new double[7];
		for(int i = 0; i < 7; i++) {
			resFive[i] = (daysRowFive[i] / fixDays[i]) * 100;
			System.out.print(resFive[i] + " ");
		}
		System.out.println();
		
		
		//Row Six
		double[] nonFixDays = new double[7];
		for(int i = 0; i < nonFixDays.length; i++) {
			nonFixDays[i] = 0;
		}
		for(Commit c: allCommits) {
			if(bugFixingCommitsHash.get(c.getHash()) == null)
				nonFixDays[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		double[] daysRowSix = new double[7];
		for(int i = 0; i < daysRowSix.length; i++ ) {
			daysRowSix[i] = 0;
		}	
		for(Commit c: allCommits) {
			if(bugFixingCommitsHash.get(c.getHash()) == null)
				if(fixInducingCommitsHash.get(c.getHash()) != null)
					daysRowSix[(int)(Math.floor(c.getTimeStamp() / 86400) + 4) % 7] +=1;
		}
		System.out.println("Sun Mon Tue Wed Thu Fri Sat");
		double[] resSix = new double[7];
		for(int i = 0; i < 7; i++) {
			resSix[i] = (daysRowSix[i] / nonFixDays[i]) * 100;
			System.out.print(resSix[i] + " ");
		}
		System.out.println();
		
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




