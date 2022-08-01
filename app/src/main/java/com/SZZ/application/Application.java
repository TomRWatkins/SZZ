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
package com.SZZ.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import com.SZZ.entities.Bug;
import com.SZZ.entities.Commit;
import com.SZZ.entities.Link;
import com.SZZ.entities.Suspect;
import com.SZZ.gitMiner.GitMiner;
import com.SZZ.jMiner.JiraMiner;
import com.SZZ.linker.LinkConnector;
/**
 * A SZZ implementation that mines a jira bug database, acquires issues, mines a github repository,
 * acquires a list of commits, and conducts the SZZ algorithm to derive the bug fixing and bug inducing
 * commits for a given github project.
 * @author Thomas Watkins
 */
public class Application {
	
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("log4j.properties");
		System.out.println("Running AG-SZZ");
		System.out.println("Github Repo URL: " + args[0]);
		System.out.println("Jira Issues URL: " + args[1]);
		System.out.println("Jira Project: " + args[2]);
	     
		String githubURL = args[0];
		String jiraIssuesURL = args[1];
		String jiraKey = args[2];		
		
		//Get Jira Issues
		JiraMiner jMiner = new JiraMiner(jiraIssuesURL, jiraKey);	
		HashMap<String, Bug> issues = jMiner.mineIssues();
		System.out.println("Total Issues List: " + issues.size());
		
		//Get Git Commits
		GitMiner gMiner = new GitMiner(githubURL);		
		ArrayList<Commit> commits = gMiner.mineGit();
		System.out.println("Total Commits List: " + commits.size());
		
		
		//Get Bug Fixing Commits
		LinkConnector connector = new LinkConnector(issues, commits, jiraKey);
		ArrayList<Link> bugFixingCommits = connector.getBugFixingCommits();
		System.out.println("Total Bug Fixing Commits: " + bugFixingCommits.size());
		
		
		//Get Bug Introducing Commits from bug fixing commits
		ArrayList<Link> bugIntroducingCommits = gMiner.calculateBugIntroducingCommits(bugFixingCommits);
		
		
		//Extract bug-introducing commits and remove duplicates
		HashMap<String, Integer> fixInducingCommits = new HashMap<>();		
		for(Link link: bugIntroducingCommits) {	
			for (Map.Entry<String, Suspect> entry : link.getBugIntroducingCommits().entrySet()) {
				fixInducingCommits.put(entry.getValue().getHash(),1);		  
			}
		}
		System.out.println("Total Fix Inducing Commits: " + fixInducingCommits.size());
		
	}
}
