package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFFileWriter;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ExtractionComparisonFolder {

	Logger logger = LoggerFactory.getLogger(ExtractionComparisonFolder.class);

	private ExtractionComparision compare;
	private SigManager manager;

	public ExtractionComparisonFolder(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException {
		ontology = new AxiomExtractor().extractInclusionsAndEqualities(ontology);
		logger.info("Extracted inclusions and equalities only ({} logical axioms)", ontology.getLogicalAxiomCount());
		this.manager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 
		for(File f : files){
			if(f.isFile()){
				logger.info("Testing signature: {}",f.getName());
				File experimentLocation = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName() + "/" + f.getName());
				compare = new ExtractionComparision(ontology, manager.readFile(f.getName()), experimentLocation);
				compare.compareExtractionApproaches();
			}
		}
		QBFFileWriter.printMetrics();
		SyntacticFirstModuleExtraction.printMetrics();
	}  
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");

		try {
			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/paper-1000random"));
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