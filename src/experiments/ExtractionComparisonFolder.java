package experiments;

import java.io.File;
import java.io.IOException;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import qbf.QBFSolverException;

import signature.SigManager;
import util.ModulePaths;

public class ExtractionComparisonFolder {

	private ExtractionComparision compare;
	private SigManager manager;

	public ExtractionComparisonFolder(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException {
		this.manager = new SigManager(signaturesLocation);
		for(File f : signaturesLocation.listFiles()){
			if(f.isFile()){
				File experimentLocation = new File(ModulePaths.getOntologyLocation() + "/Results/" + f.getName());
				if(experimentLocation.exists())
					compare = new ExtractionComparision(experimentLocation.getAbsolutePath());

				else
					compare = new ExtractionComparision(ontology, manager.readFile(f.getAbsolutePath()), f.getName());

				compare.compareExtractionApproaches();
			}
		}
	}

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/expr/nci-08.09d-terminology.owl");
		try {
			new ExtractionComparisonFolder(ont, new File("/home/william/PhD/Ontologies/nci-08.09d-avgdeps"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}


	}

}
