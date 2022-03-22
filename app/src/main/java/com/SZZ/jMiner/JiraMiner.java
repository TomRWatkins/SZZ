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
package com.SZZ.jMiner;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import com.SZZ.entities.Bug;
/**
 * Mines a given Jira bug database extracting issues and writing them to a file. 
 * @author Thomas Watkins
 */
public class JiraMiner {
	
	private String jiraIssuesUrl;
	private String jiraKey;
	private String jiraXMLAPI = "/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml";	
	private String jiraQuery = "?jqlQuery=project=%20{0}%20AND%20resolution%20=%20Fixed%20AND%20status%20in%20(Resolved,%20Closed)%20AND%20issuetype%20=%20bug%20ORDER%20BY%20created%20DESC";
	private String totalQuery = "&tempMax=1";
	private String mainQuery = "&tempMax=1000&pager/start={0}";
	private URL url;
	private URLConnection con;
	private Document document;	
	private HashMap<String, Bug> issues;
	
	/**
	 * Creates a JiraMiner with specified Jira Issues url and Key for the project.
	 * @param jiraIssuesUrl the jira issues url
	 * @param jiraKey the key for the jira project
	 */
	public JiraMiner(String jiraIssuesUrl, String jiraKey) {	
		String[] tokens = jiraIssuesUrl.split("/jira/projects/"); 		
		this.jiraIssuesUrl = tokens[0] + this.jiraXMLAPI;
		this.jiraKey = jiraKey;
		this.jiraQuery = this.jiraQuery.replace("{0}",jiraKey);		
		this.issues = new HashMap<String, Bug>();
	}
	
	/**
	 * Extracts issues from the Jira project. Stores them in files and in a hashmap.
	 * @return HashMap containing the extracted issues
	 */
	public HashMap<String, Bug> mineIssues() {
		System.out.println("Mining Jira Issues for " + this.jiraKey);
		
		//Calculate total pages needed (1000 response cap per page)	
		double totalIssues = calculateTotalIssues();
		int totalPages = (int) Math.ceil((totalIssues/1000));
		
		System.out.println("Total Pages: " + totalPages);
		
		//Create file to store issues CSV and mine issues
		for(int i = 0; i < totalPages; i++) {
			String fileName = this.jiraKey + "-" + i + ".csv";
			File file = new File(fileName);
			try {			
				String num = Integer.toString(i * 1000);
				String tempQuery = this.mainQuery.replace("{0}", num);
				this.url = new URL(this.jiraIssuesUrl + this.jiraQuery + tempQuery);
				
				this.con = url.openConnection();
				this.document = docFromStream(this.con.getInputStream());
				PrintWriter writer = null;
				
				writer = new PrintWriter(file);
				String header = "IssueKey;Title;Resolution;Status;Assignee;CreatedDate;ResolvedDate;Attachments;";
				writer.println(header);
				writeIssuesToFile(this.document, writer);
				writer.close();
							
			} catch (Exception e) {
				e.printStackTrace();
			}	
			System.out.println("Page " + (i+1) + "/" + totalPages + " completed.");
		}	
		return this.issues;
	}
	
	/**
	 * Helper function to write issues to file. Also updates the HashMap returned by mineIssues adding
	 * issues as they're read.
	 * @param doc the parsed XML document
	 * @param pw the print writer to write to file
	 */
	private void writeIssuesToFile(Document doc, PrintWriter writer) {		
		SimpleDateFormat patternFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
		NodeList nodes = doc.getElementsByTagName("item");
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			NodeList children = node.getChildNodes();			
			String issueKey = "";			
			String title = "";
			String resolution = "";
			String status = "";
			String assignee = "";
			long createdDateT = 0;
			long resolvedDateT = 0;		
			ArrayList<String> attachmentsList = new ArrayList<>();			
			
			for (int j = 0; j < children.getLength(); j++) {
				switch (children.item(j).getNodeName()) {
					case "title": title = children.item(j).getTextContent().replace(";", ""); break;
					case "resolution": resolution = children.item(j).getTextContent();        break;
					case "key":	issueKey = children.item(j).getTextContent();    			  break;
					case "created":	
						try {
							createdDateT = patternFormat.parse(children.item(j).getTextContent()).getTime();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						break;
					case "resolved":						
						try {
							resolvedDateT = patternFormat.parse(children.item(j).getTextContent()).getTime();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "status": status = children.item(j).getTextContent();	    break;
					case "assignee": assignee = children.item(j).getTextContent(); 	break;					
					case "attachments":
						NodeList attachmentNodes = children.item(j).getChildNodes();						
						for (int l = 0; l < attachmentNodes.getLength(); l++) {
							Node attachmentNode = attachmentNodes.item(l);
							NamedNodeMap attchmentName = attachmentNode.getAttributes();
							if (attchmentName != null) {						
								String name = attchmentName.getNamedItem("name").getNodeValue();
								attachmentsList.add(name);
							}
						}						
						break;
				}					
			}
			//Create new issue to return
			this.issues.put(issueKey.toLowerCase(), new Bug(issueKey.toLowerCase(),title,resolution,status,assignee,
					createdDateT,resolvedDateT,attachmentsList));
			
			//Write this issue to file
			String printString = issueKey + ";" + title + ";" + resolution + ";" + status + ";"
					+ assignee + ";" + createdDateT + ";" + resolvedDateT + ";" + attachmentsList.toString() + ";";			
			writer.println(printString);					
		}
		
	}
	
	/**
	 * Helper function to parse XML 
	 * @param stream the input stream from the API call
	 * @return doc the parsed XML document
	 */
	private Document docFromStream(InputStream stream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();			
			doc = builder.parse(stream);	
		} catch (Exception e) {			
			e.printStackTrace();
		} 
		return doc;		
	}
	
	/**
	 * A helper function to calculate the total issues for a project.
	 * @return the total number of issues
	 */
	private double calculateTotalIssues() {
		try {			
			//Append query and retrieve response
			this.url = new URL(this.jiraIssuesUrl + this.jiraQuery + this.totalQuery);
			//System.out.println("CalculateTotalIssues URL :" + url.toString());
			this.con = this.url.openConnection();
			
			//Obtain total attribute from XML response
			this.document = docFromStream(this.con.getInputStream());
			NodeList issueNodes = this.document.getElementsByTagName("issue");
			Node issue = issueNodes.item(0);
			Element issueElement = (Element) issue;			
			System.out.println("Total Issues: " + issueElement.getAttribute("total"));
			return Double.parseDouble(issueElement.getAttribute("total"));				
		} catch (Exception e) {			
			e.printStackTrace();
		}			
		return 0;
	}	
}
