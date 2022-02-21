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
/**
 * Representation of a git Commit.
 * @author Thomas Watkins
 */
public class Commit {
	private String hash;
	private long timeStamp;
	private String author;
	private String comment;
	private ArrayList<AffectedFile> files;
	private String bugIDLink;
	
	/**
	 * Creates a commit.
	 * @param hash the commit hash
	 * @param timeStamp the time stamp of the commit
	 * @param author the author of the comment
	 * @param comment the commit message
	 */
	public Commit(String hash, long timeStamp, String author, String comment) {
		this.hash = hash;
		this.timeStamp = timeStamp;
		this.author = author;
		this.comment = comment;		
		this.bugIDLink = "";
	}

	/**	 
	 * Stores the potential bug link id for this commit.
	 * @param bugIDLink the bug link id
	 */
	public void setBugIDLink(String bugIDLink) {
		this.bugIDLink = bugIDLink;
	}
	
	/**
	 * @return the bug link id for this commit
	 */
	public String getBugIDLink() {
		return this.bugIDLink;
	}
	
	/**
	 * @return the hash for this commit
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * @return the time stamp for this commit
	 */
	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	/**
	 * @return the author for this commit
	 */
	public String getAuthor() {
		return this.author;
	}
	
	/**
	 * @return the message for this commit
	 */
	public String getComment() {
		return this.comment;
	}
		
	/**
	 * Assigns the affected files for this commit.
	 * @return the list of affected files
	 */
	public void setFiles(ArrayList<AffectedFile> files) {
		this.files = files;
	}
	
	/**
	 * @return the affected files for this commit
	 */
	public ArrayList<AffectedFile> getFiles() {
		return this.files;
	}
	
	@Override
	public String toString() {		
		return hash + "\n" +
			   timeStamp + "\n" +
			   author + "\n" +
			   comment + "\n" +
			   files + "\n";
	}
}