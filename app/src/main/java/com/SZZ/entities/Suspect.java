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
/**
 * Represents a suspected bug introducing commit.
 * @author Thomas Watkins
 */
public class Suspect {
	private String hash;
	private String author;
	private long date;
	private String fileName;
	
	/**
	 * Creates a Suspect given the commit data.
	 * @param hash the suspects commit hash
	 * @param author the author of the commit
	 * @param date the date of the commit
	 * @param fileName the file affected by the commit
	 */
	public Suspect(String hash, String author, long date, String fileName) {
		this.hash = hash;
		this.author = author;
		this.date = date;
		this.fileName = fileName;
	}
	
	/**
	 * @return the file affected by the commi
	 */
	public String getFileName() {
		return this.fileName;
	}
	
	/**
	 * @return the suspects commit hash
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * @return the author of the commit
	 */
	public String getAuthor() {
		return this.author;
	}
	
	/**
	 * @return the date of the commit
	 */
	public long getDate() {
		return this.date;
	}
}
