package uk.ac.liv.moduleextraction.reloading;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.signature.SigManager;

public class DumpExtractionToDisk implements Runnable {

	Logger logger = LoggerFactory.getLogger(DumpExtractionToDisk.class);
	
	OWLOntologyManager ontologyManager;
	
	Set<OWLLogicalAxiom> module;
	Set<OWLEntity> signature;
	Date timeStarted;
	File directory;
	
	private static final String SIGNATURE_FILE = "sig";
	private static final String MOD_FILE = "module.owl";

	public DumpExtractionToDisk(File location, Set<OWLLogicalAxiom> mod, Set<OWLEntity> signature) {
		this.module = mod;
		this.signature = signature;
		this.directory = location;
		
		this.ontologyManager = OWLManager.createOWLOntologyManager();
	}

	@Override
	public void run() {
		if(!directory.exists())
			directory.mkdir();
		
		writeSignature();
		writeSetToOntology(module, MOD_FILE);
		
		logger.trace("Dumped to: {} at {}",directory.getAbsolutePath(),new Date());
		
	}
	
	private void writeSetToOntology(Set<OWLLogicalAxiom> ontology, String file){
		OWLXMLOntologyFormat owlFormat = new OWLXMLOntologyFormat();
		OWLOntology ontologyToWrite = null;
		try {
			ontologyToWrite = ontologyManager.createOntology(new HashSet<OWLAxiom>(ontology));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		File saveLocation = new File(directory.getAbsolutePath()+ "/" + file);
		
		try {
			ontologyManager.saveOntology(ontologyToWrite,owlFormat,IRI.create(saveLocation));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		logger.trace("Written {} ({})",file, ontology.size());
	}
	
	private void writeSignature(){
		SigManager man = new SigManager(directory);
		try {
			man.writeFile(signature, SIGNATURE_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}