package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;


import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ExtractionComparisonFolder {

	private ExtractionComparision compare;
	private SigManager manager;

	public ExtractionComparisonFolder(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException {
		this.manager = new SigManager(signaturesLocation);
		for(File f : signaturesLocation.listFiles()){
			if(f.isFile()){
				File experimentLocation = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName() + "/" + f.getName());
				if(experimentLocation.exists())
					compare = new ExtractionComparision(experimentLocation);
				else
					compare = new ExtractionComparision(ontology, manager.readFile(f.getName()), experimentLocation);

				compare.compareExtractionApproaches();
			}
		}
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
		try {
			new ExtractionComparisonFolder(ont, new File("/LOCAL/wgatens/Signatures/Intermediate_Fibrocytic_Neoplasm"));
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
