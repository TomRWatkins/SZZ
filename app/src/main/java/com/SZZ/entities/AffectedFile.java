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
 * Represents an affected file for a Commit.
 * @author Thomas Watkins
 */
public class AffectedFile {
	private String status;
	private String path;
	
	/**
	 * Creates an affected file.
	 * @param status the status of the file (Add,Modify,Delete)
	 * @param path the filename of the file
	 */
	public AffectedFile(String status, String path) {
		this.status = status;
		this.path = path;
	}
	
	/**
	 * @return the status of the file
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the filename of the file
	 */
	public String getPath() {
		return path;
	}	
	
	@Override
	public String toString() {
		return status + " " + path;
	}
}