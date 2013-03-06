package uk.ac.liv.moduleextraction.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
public class OntologyRenamer {

	public OntologyRenamer(File location) {
		if(location.isFile())
			System.out.println(getNewFileName(getXMLBaseString(location)));
		else if(location.isDirectory()){
			for(File f : location.listFiles()){
				if(f.isFile()){
					String newName = getNewFileName(getXMLBaseString(f));
					File newLocation = new File(f.getParent() + "/" + newName);
					f.renameTo(newLocation);
				}
			}
		}

	}

	private String getXMLBaseString(File f){
		String pattern = ".*xml:base.*";
		String xmlBaseString = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));

			String line = "";
			while ((line = reader.readLine()) != null) {
				if(line.matches(pattern)){
					xmlBaseString = line;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return xmlBaseString;
	}

	private String getNewFileName(String xmlBaseString){
		int lastindex = xmlBaseString.lastIndexOf('/');
		return xmlBaseString.substring(lastindex+1,xmlBaseString.length()-1);
	}

	public static void main(String[] args)
	{

		OntologyRenamer r = new OntologyRenamer(new File("/LOCAL/wgatens/Ontologies/All/"));


	}

}
