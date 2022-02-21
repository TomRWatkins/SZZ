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
 * Represents a line returned by blaming a commit.
 * @author Thomas Watkins
 */
public class BlameLine {
	private String hash;
	private long date;
	private int lineNumber;
	private String content;
	private String author;
	
	/**
	 * Creates a blame line.
	 * @param hash the hash the line belongs to
	 * @param date the date of the commit
	 * @param lineNumber the line number
	 * @param content the content of the line
	 * @param author the author of the line
	 */
	public BlameLine(String hash, long date, int lineNumber, String content, String author) {
		this.hash = hash;
		this.date = date;
		this.lineNumber = lineNumber;
		this.content = content;
		this.author = author;
	}
	
	/**
	 * @return the lines hash
	 */
	public String getHash() {
		return this.hash;
	}
	
	/**
	 * @return the lines commit date
	 */
	public long getDate() {
		return this.date;
	}
	
	/**
	 * @return the line number
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	/**
	 * @return the line content
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * @return the author of the line
	 */
	public String getAuthor() {
		return this.author;
	}

	@Override
	public String toString() {
		return this.hash + " " + this.author + " " + this.date + " " + this.lineNumber + " " + this.content;
	}
	
}
