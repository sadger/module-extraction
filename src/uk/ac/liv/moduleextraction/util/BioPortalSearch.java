package uk.ac.liv.moduleextraction.util;

	import java.util.ArrayList;
	import java.util.Iterator;

	import org.w3c.dom.*;
	import javax.xml.parsers.DocumentBuilderFactory;
	import javax.xml.parsers.DocumentBuilder;
	import org.xml.sax.SAXException;
	import org.xml.sax.SAXParseException; 


	public class BioPortalSearch {
		/**
		 * @param uri RESTful Search URI for BioPortal web services
		 */

		public static void parseXMLFile (String uri){
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		
				
				Document doc = docBuilder.parse(uri);

				//Normalize text representation
				doc.getDocumentElement().normalize(); 

				doc.getElementsByTagName("success");
				NodeList listOfOntologies = doc.getElementsByTagName("ontologyBean");
				int totalNumberOfOntologies = listOfOntologies.getLength(); 
				

				String TAB = "\t";
				if (totalNumberOfOntologies == 0) {
					System.out.println("No ontology metadata returned");
				}
				else {
					for(int s=0; s<listOfOntologies.getLength(); s++) {
						Node ontologyBeanNode = listOfOntologies.item(s);

						if(ontologyBeanNode.getNodeType() == Node.ELEMENT_NODE){

							Element ontologyBeanElements = (Element)ontologyBeanNode;    
							
				
							ArrayList<String> ontologyElementNames = new ArrayList<String> ();
							ontologyElementNames.add("id");
							ontologyElementNames.add("ontologyId");
							ontologyElementNames.add("abbreviation");
							ontologyElementNames.add("displayLabel");
							//ontologyElementNames.add("description");

							//ontologyElementNames.add("format");
							//ontologyElementNames.add("internalVersionNumber");
							//ontologyElementNames.add("versionNumber");
							//ontologyElementNames.add("contactName");
							//ontologyElementNames.add("contactEmail");
							//ontologyElementNames.add("statusId");
							//3146ontologyElementNames.add("dateCreated");

							//int numberOfOntologyElements = ontologyElementNames.size();

//							String API_KEY = "1aac0488-a34c-46c2-b6c3-4ea066a72db8";
//							
//							NodeList idNode = ontologyBeanElements.getElementsByTagName("id");
//							Element id = (Element)idNode.item(0);	
//							
//							NodeList abNode = ontologyBeanElements.getElementsByTagName("abbreviation");
//							Element abbr = (Element)abNode.item(0);	
//							
//							System.out.printf("wget http://rest.bioontology.org/bioportal/ontologies/download/%s?apikey=%s -O %s", id.getTextContent(), API_KEY, abbr.getTextContent());
							
							
							for (Iterator<String> it = ontologyElementNames.iterator(); it.hasNext();) {
								String nodeElementName = (String) it.next();
								//System.out.print("Array Node Name-"+nodeElementName+TAB); 

								NodeList Node = ontologyBeanElements.getElementsByTagName(nodeElementName);
								Element ontologyElement = (Element)Node.item(0);	
								
								//System.out.println(ontologyElement);
								if (ontologyElement != null) {
									//System.out.println(ontologyElement);
									String ontologyVersionId = ontologyElement.getTextContent();
									System.out.print(ontologyVersionId+TAB);
								}
								else {
									System.out.print("ArrayNodeName-"+nodeElementName+" Not found"+TAB);
								}

							}
							//System.out.println();
						}
						System.out.println();
					}
					System.out.println("Total number of ontologies: "+totalNumberOfOntologies);
				}
			}catch (SAXParseException err) {
				System.out.println ("**PARSING ERROR" + ", line "+err.getLineNumber () + ", uri " + err.getSystemId ());
				System.out.println(" " + err.getMessage ());

			}catch (SAXException e) {
				Exception x = e.getException ();
				((x == null) ? e : x).printStackTrace ();

			}catch (Throwable t) {
				t.printStackTrace ();
			}
		}
	
	
	public static void main(String[] args) {
		
	String getLatestOntologiesUrl = "http://rest.bioontology.org/bioportal/ontologies?apikey=";
	String API_KEY = "1aac0488-a34c-46c2-b6c3-4ea066a72db8";  //Login to BioPortal (http://bioportal.bioontology.org/login) to get your API key 
	
	//Call Search REST URL and Parse results 
	BioPortalSearch.parseXMLFile(getLatestOntologiesUrl+API_KEY);
	
	}
}
