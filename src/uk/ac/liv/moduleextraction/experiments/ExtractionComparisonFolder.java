package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFFileWriter;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ExtractionComparisonFolder {

	private ExtractionComparision compare;
	private SigManager manager;

	public ExtractionComparisonFolder(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException {
		this.manager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 
		for(File f : files){
			if(f.isFile()){
				System.out.println("Testing sig: " + f.getName());
				File experimentLocation = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName() + "/" + f.getName());
				compare = new ExtractionComparision(ontology, manager.readFile(f.getName()), experimentLocation);
				compare.compareExtractionApproaches();
				
			}
		}
		QBFFileWriter.printMetrics();
		SyntacticFirstModuleExtraction.printMetrics();
	}  
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/nci-08.09d-terminology.owl");
		try {
			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/chainrandom500"));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
	}
}