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
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.SZZ.gitMiner.GitUtil;
/**
 * A representation of a link between a Commit and a Bug. Syntactic and Semantic confidence is calculated
 * upon creation of the link.
 * @author Thomas Watkins
 */
public class Link {
	private Bug bug;
	private Commit commit;
	private int syntacticConfidence;
	private int semanticConfidence;	
	private HashMap<String, Suspect> bugIntroducingCommits;
	
	/**
	 * Creates a link between a Bug and a Commit.
	 * @param bug the bug to be linked
	 * @param commit the commit to be linked
	 */
	public Link(Bug bug, Commit commit) {
		this.bug = bug;
		this.commit = commit;		
		this.syntacticConfidence = calculateSyntacticConfidence();
		this.semanticConfidence = calculateSemanticConfidence();
		this.bugIntroducingCommits = new HashMap<>();
	}
	
	/** 
	 * @return HashMap containing bug introducing commits
	 */
	public HashMap<String, Suspect> getBugIntroducingCommits() {
		return this.bugIntroducingCommits;
	}

	/**
	 * Calculates the list of suspected bug introducing commits for this commit. Ensures to filter suspects by:
	 * 1. Ensuring the suspect wasn't committed after the bug report.
	 * 2. Ensuring the time between the fix commit and the bug introducing commit is less than 2 years.
	 * 3. Ensuring the bug introducing commit isn't obtained through a blank line or comment modification.
	 * 4. Ensuring it was a .java file that was changed.
	 * @param gitUtil the github utility 
	 */
	public void calculateBugIntroducingSuspects(GitUtil gitUtil) {
		for(AffectedFile file: this.commit.getFiles()) {				
			if(file.getPath().contains(".java")) {
				//Get diff between this commit and previous commit
				String diff = gitUtil.getDiff(this.commit.getHash(), file.getPath());
				if(diff.equals(""))	break;
				
				String[] diffArr = diff.split("\n");
				ArrayList<String> diffLines = new ArrayList<>();
				Collections.addAll(diffLines, diffArr);
				
				//Get lines removed in this commit from diff
				ArrayList<Integer> linesRemoved = gitUtil.getLinesRemoved(diffLines);
				ArrayList<BlameLine> blamedLines = new ArrayList<>();			
								
				//Get blame lines for this commit
				if(linesRemoved.size() > 0)					
					blamedLines = gitUtil.blame(this.commit.getHash(), file.getPath(), linesRemoved);
				else
					break;
				
				//Filter each line then potentially add a suspect
				for(BlameLine line: blamedLines) {					
					if(this.bugIntroducingCommits.get(line.getHash()) != null) break;			
					if(line.getDate() > this.bug.getCreatedDate()) break;
					if(this.getCommit().getTimeStamp() - line.getDate() > 63000000) break;
					String regex = "^\\/\\/.*|^\\*.*|^\\/\\*.*|^\\s*\\/\\/.*|^\\s*\\*.*|^\\s*\\/\\*.*";					
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(line.getContent());
				    if(matcher.find()) break;
				    if(line.getContent().trim().isEmpty()) break;				
					
					//Add a new suspect and add to bug fixing commit list of BIC
					this.bugIntroducingCommits.put(line.getHash(), new Suspect(line.getHash(),line.getAuthor(),line.getDate(),file.getPath()));
				}				
			}
		}
	}
	
	/**
	 * @return the bug in this link
	 */
	public Bug getBug() {
		return this.bug;		
	}
	
	/**
	 * @return the commit in this link
	 */
	public Commit getCommit() {
		return this.commit;
	}
	
	/**
	 * Calculates the syntactic confidence of a link (tansaction, bug).
	 * Not analysing just bug numbers as the APACHE projects are consistent and well documented.
	 * Therefore they always use PROJECT-NUMBER for commits relating to an issue.
	 * 
	 * @return the syntactic confidence
	 */
	private int calculateSyntacticConfidence() {		
		int confidence = 0;		
		
		if(this.bug.getIssueKey() != null) 
			confidence++;
	
		String keywordsRegex = "defects?|patch|bugs?|fix(e[ds])?";		
		Pattern keywords = Pattern.compile(keywordsRegex, Pattern.CASE_INSENSITIVE);		
		Matcher k = keywords.matcher(this.commit.getComment().toLowerCase());
				
		if (k.find())
			confidence++;	
		
		return confidence;
	}
	
	/**
	 * Calculates the semantic confidence of a link (transaction, bug). Increments if 
	 * the bug has been fixed atleast once, if the author of the commit is assigned to 
	 * the bug, if the description of the bug report is contained within the log message 
	 * or the commit message, or if one of more or the files affected by the commit have 
	 * been attached to the bug.  
	 * 
	 * @return the semanctic confidene
	 */
	private int calculateSemanticConfidence() {
		int confidence = 0;
		
		if(this.bug.getResolution().toLowerCase().equals("fixed")) confidence++;		
		if(this.bug.getAssignee().toLowerCase().equals(this.commit.getAuthor().toLowerCase())) confidence++;		
		if(bugDescContainedInCommit(this.bug.getTitle(),this.commit.getComment(),this.bug.getTitle().length(),this.commit.getComment().length())) confidence++;		
		if(this.checkFilesAttached()) confidence++;			
		
		return confidence;
	}
	
	/**
	 * Calcualte if description of bug report is contained in the log message of the commit. 
	 * Utilises longest common substring algorithm. 
	 * @param s1 the bug description
	 * @param s2 the commit message
	 * @param l1 the bug description length
	 * @param l2 the commit message length
	 * @return true if longest common substring > 15, false otherwise
	 */
	private boolean bugDescContainedInCommit(String s1, String s2, int l1, int l2) {		
		int dp[][] = new int[2][l2 + 1];
		int res = 0;
		
		//Calculate length of longest common sub string
		for (int i = 1; i <= l1; i++) {
			for (int j = 1; j <= l2; j++) {
				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					dp[i % 2][j] = dp[(i - 1) % 2][j - 1] + 1;
					if (dp[i % 2][j] > res)
						res = dp[i % 2][j];
				} else
					dp[i % 2][j] = 0;
			}
		}
		return res > 15;
	}	
	
	/**
	 * Returns true if atleast one file affected by a commit is attached to a bug.
	 * @return true if atleast one file affected by a commit is attached to a bug, false otherwise
	 */
	private boolean checkFilesAttached() {
		for(int i = 0; i < this.bug.getAttachmentsList().size(); i++) {
			for(int j = 0; j < this.commit.getFiles().size(); j++) 
				if(this.bug.getAttachmentsList().get(i).equals(this.commit.getFiles().get(j).getPath())) 
					return true;			
		}
		return false;
	}
	
	/**
	 * @return the syntactic confidence
	 */
	public int getSyntacticConfidence() {
		return this.syntacticConfidence;
	}
	
	/**
	 * @return the semantic confidence
	 */
	public int getSemanticConfidence() {
		return this.semanticConfidence;
	}
}