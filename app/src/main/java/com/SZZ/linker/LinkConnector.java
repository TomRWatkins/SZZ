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
package com.SZZ.linker;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.SZZ.entities.Bug;
import com.SZZ.entities.Commit;
import com.SZZ.entities.Link;
/**
 * Formulates links between a given list of issues and commits and returns a list of bug fixing commits.
 * We rule out bug fixing commits that are outliers in terms of the number of files affected. We also
 * use reguglar expressions to find all commits that are first potentially bug fixing by finding their
 * assosiated bug issue number. This is then used to formulate a link.
 * @author Thomas Watkins
 */
public class LinkConnector {
	private HashMap<String, Bug> issues;         
	private ArrayList<Commit> commits;   		  
	private ArrayList<Link> bugFixingLinks;       
	private ArrayList<Commit> bugFixingCommits;   
	private String projectKey;		
	
	/**
	 * Creates a link connector for a given list of issues and commits.
	 * @param issues the list of issues
	 * @param commits the list of commits
	 * @param projectKey the project key for this project
	 */
	public LinkConnector(HashMap<String, Bug> issues, ArrayList<Commit> commits, String projectKey) {
		this.issues = issues;
		this.commits = commits;
		this.projectKey = projectKey;
		this.bugFixingLinks = new ArrayList<>();
		this.bugFixingCommits = new ArrayList<>();
	}
	
	/**
	 * Calculates the bug fixing commits creating links between issues and commits. Ensures to 
	 * generate an outlier boundary to ignore commits that affect too many files. Also doesn't 
	 * consider commits that do not have a Jira bug key within it's commit message.
	 * @return the list of bug fixing commits
	 */
	public ArrayList<Link> getBugFixingCommits() {
		System.out.println("Getting Bug Fixing Commits");
		//Calculate median number files affected per commit
		ArrayList<Integer> filesAffected = new ArrayList<>();
		for(int i = 0; i < this.commits.size(); i++) 
			filesAffected.add(this.commits.get(i).getFiles().size());		
		
		//Grab median value and times by 5 to generate outlier boundary
		Collections.sort(filesAffected);		
		int outlier = filesAffected.get(filesAffected.size()/2) * 5;
		
		//Filter commits that don't have issue numbers within commit message AND filter commits than have more than 5 times median number of files affected
		for(Commit commit: this.commits) {
			if(potentiallyBugFixing(commit) && !(commit.getFiles().size() > outlier)) 
				this.bugFixingCommits.add(commit);		
		}	
		
		//Create links between commits and issues
		for(Commit commit: this.bugFixingCommits) {
			if(this.issues.get(commit.getBugIDLink()) != null) 
				this.bugFixingLinks.add(new Link(this.issues.get(commit.getBugIDLink()), commit));			
		}
		
		//Only consider links that satisfy sem > 1 ∨ (sem = 1 ∧ syn > 0)
		ArrayList<Link> tempLinks = new ArrayList<>();
		for(Link link: this.bugFixingLinks) {
			if((link.getSemanticConfidence() > 1) || (link.getSemanticConfidence() == 1 && link.getSyntacticConfidence() > 0))
				tempLinks.add(link);
		}		
		this.bugFixingLinks = tempLinks;	
		
		//Check how many unique bugs have been linked to commits
		HashMap<String, Bug> uniques = new HashMap<>();
		for(Link l: this.bugFixingLinks) 
			uniques.put(l.getBug().getIssueKey(), l.getBug());		
		System.out.println("Unique Bugs Matched: " + uniques.size());
		
		printToFile();
		return this.bugFixingLinks;
	}
	
	/**
	 * A function to print the bug fixing commits to file.
	 */
	private void printToFile() {
		try {
			File f = new File(this.projectKey + "-BugFixCommits" + ".csv");
			PrintWriter writer = new PrintWriter(f);
			writer.println("CommitHash;CommitTime;CommitComment;IssueKey;IssueOpenD;IssueResolvedD;IssueTitle;");
			for(Link l: this.bugFixingLinks) {				
				String printString = l.getCommit().getHash() + ";"
				 + l.getCommit().getTimeStamp() + ";"
				 + l.getCommit().getComment().replace(";", "") + ";"
				 + l.getBug().getIssueKey() + ";"
				 + l.getBug().getCreatedDate() + ";"
				 + l.getBug().getResolvedDate() + ";"
				 + l.getBug().getTitle().replace(";", "") + ";";				
				writer.println(printString);
			}	
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	/**
	 * A helper function to determine if a commit can be considered potentially bug fixing.
	 * @param commit the commit to be analysed
	 * @return true if commit message contains jira bug
	 */
	private boolean potentiallyBugFixing(Commit commit) {
		//If commit comment contains projectname-numberid E.g APACHE-1234
		String regex = projectKey.toLowerCase()+"[ ]*-[ ]*[0-9]+";
     	Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(commit.getComment().toLowerCase());
	    boolean found = matcher.find();
	    
	    //Store bug id in commit for link retrieval
	    if(found) 
	    	commit.setBugIDLink(matcher.group());
	    
	    return found;
	   	
	}	
}
