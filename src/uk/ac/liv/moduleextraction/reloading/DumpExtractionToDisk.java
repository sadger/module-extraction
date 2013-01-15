package uk.ac.liv.moduleextraction.reloading;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.html.parser.Entity;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.util.ModulePaths;

public class DumpExtractionToDisk implements Runnable {

	OWLOntologyManager ontologyManager;
	
	private Set<OWLLogicalAxiom> terminology;
	Set<OWLLogicalAxiom> module;
	Set<OWLEntity> signature;
	String name;
	Date timeStarted;
	File directory;
	
	private static final String SIGNATURE_FILE = "/sig";
	private static final String TERM_FILE = "/terminology.owl";
	private static final String MOD_FILE = "/module.owl";

	public DumpExtractionToDisk(String folderName, Set<OWLLogicalAxiom> term,Set<OWLLogicalAxiom> mod, Set<OWLEntity> signature) {
		this.terminology = term;
		this.module = mod;
		this.signature = signature;
		this.name = folderName;
		this.timeStarted = new Date();
		
		this.ontologyManager = OWLManager.createOWLOntologyManager();
	}

	@Override
	public void run() {
		System.out.println("Terminology Size: " + terminology.size());
		System.out.println("Module Size: " + module.size());
		System.out.println("Signature Size: " + signature.size());

		directory = new File(ModulePaths.getOntologyLocation() + "/Results/" + name);

		if(!directory.exists())
			directory.mkdir();
		
		writeSignature();

		writeSetToOntology(terminology, TERM_FILE);
		writeSetToOntology(module, MOD_FILE);
		
		System.out.println("Dumped to: " + directory.getAbsolutePath() + " at " + new Date());
		
	}
	
	
	private void writeSetToOntology(Set<OWLLogicalAxiom> ontology, String file){
		OWLXMLOntologyFormat owlFormat = new OWLXMLOntologyFormat();
		OWLOntology ontologyToWrite = null;
		try {
			ontologyToWrite = ontologyManager.createOntology(new HashSet<OWLAxiom>(ontology));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		File saveLocation = new File(directory.getAbsolutePath()+file);
		
		try {
			ontologyManager.saveOntology(ontologyToWrite,owlFormat,IRI.create(saveLocation));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		System.out.println("Written " + file);
	}
	
	private void writeSignature(){
		File signatureFile = new File(directory.getAbsolutePath()+ SIGNATURE_FILE);
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
		System.out.println("Written signature");
	}

}
