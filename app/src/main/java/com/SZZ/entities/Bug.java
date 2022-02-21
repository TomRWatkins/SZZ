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

import java.util.List;
/**
 * Represents a Jira Issue.
 * @author Thomas Watkins
 */
public class Bug {	
	private String issueKey;			
	private String title;
	private String resolution;
	private String status;
	private String assignee;
	private long createdDate;
	private long resolvedDate;	
	private List<String> attachmentsList;
	
	/**
	 * Creates a bug given its internal values.
	 * @param issueKey the key for an issue
	 * @param title the title of an issue
	 * @param resolution the resolution of the issue
	 * @param status the status of the issue
	 * @param assignee the assignee of the issue
	 * @param createdDate the date of creation
	 * @param resolvedDate the date of resolution
	 * @param attachmentsList the list of attachments for the issue
	 */
	public Bug(String issueKey, String title, String resolution, String status, String assignee, long createdDate, long resolvedDate, List<String> attachmentsList) {		
		this.issueKey = issueKey;
		this.title = title;
		this.resolution = resolution;
		this.status = status;
		this.assignee = assignee;
		this.createdDate = createdDate;
		this.resolvedDate = resolvedDate;
		this.attachmentsList = attachmentsList;		
	}

	/**
	 * @return the issue key
	 */
	public String getIssueKey() {
		return this.issueKey;
	}

	/**
	 * @return the issue title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * @return the issue resolution
	 */
	public String getResolution() {
		return this.resolution;
	}

	/**
	 * @return the issue status
	 */
	public String getStatus() {
		return this.status;
	}
	
	/**
	 * @return the issue assignee
	 */
	public String getAssignee() {
		return this.assignee;
	}

	/**
	 * @return the issue created date
	 */
	public long getCreatedDate() {
		return this.createdDate;
	}

	/**
	 * @return the issue resolved date
	 */
	public long getResolvedDate() {
		return this.resolvedDate;
	}

	/**
	 * @return the issue attachments list
	 */
	public List<String> getAttachmentsList() {
		return this.attachmentsList;
	}
	
	@Override
	public String toString() {
		return "Issue Key: " + this.issueKey +
				" Title: " + this.title +
				" Resolution: " + this.resolution +
				" Status: " + this.status +
				" Assignee: " + this.assignee +
				" Created Date: " + this.createdDate +
				" Resolved Date: " + this.resolvedDate;
	}	
}