package uk.ac.liv.moduleextraction.signature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigManager {
	Logger logger = LoggerFactory.getLogger(SigManager.class);
	private File directory;

	public SigManager(File directory) {
		if(!directory.exists()){
			directory.mkdir();
		}
		this.directory = directory;
	}

	public void writeFile(Set<OWLEntity> signature, String name) throws IOException{
		File signatureFile = new File(directory.getAbsolutePath()+ "/" + name);
		if (signatureFile.exists())
			signatureFile.delete();
		
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(signatureFile,false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		HashSet<OWLClass> classes = new HashSet<OWLClass>();
		HashSet<OWLObjectProperty> roles = new HashSet<OWLObjectProperty>();

		for(OWLEntity ent: signature){
			if(ent.isOWLClass())
				classes.add((OWLClass) ent);
			else if(ent.isOWLObjectProperty())
				roles.add((OWLObjectProperty) ent);
		}
		try{
			writer.write("[Classes]\n");
			for(OWLClass cls : classes)
				writer.write(cls.getIRI().toString() + "\n");
			writer.write("[Roles]\n");
			for(OWLObjectProperty prop : roles)
				writer.write(prop.getIRI().toString() + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				fileWriter.flush();
				writer.flush();
				fileWriter.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("Written signature \"{}\"",name);
	}
	

	public Set<OWLEntity> readFile(String name) throws IOException{
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		File signatureFile = new File(directory.getAbsolutePath() + "/" + name);
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
	
		
		if(signatureFile.exists()){
			BufferedReader br = new BufferedReader(new FileReader(signatureFile));
			String line;
			boolean readingRoles = false;
			
			while((line = br.readLine()) != null) {
				String trimmedLine = line.trim();
				
				if(trimmedLine.equals("[Roles]"))
					readingRoles = true;
					
				if(!trimmedLine.equals("[Classes]") && !trimmedLine.equals("[Roles]"))
					if(!readingRoles)
						signature.add(factory.getOWLClass(IRI.create(trimmedLine)));
					else{
						signature.add(factory.getOWLObjectProperty(IRI.create(trimmedLine)));
					}
			}
			try{
				br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			throw new IOException("No signature file " + "\"" + name + "\"" + " found");
		}
		

		return signature;
	}
	

	
}
