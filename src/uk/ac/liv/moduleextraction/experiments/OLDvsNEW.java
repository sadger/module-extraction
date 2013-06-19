package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.extractor.OldApproach;
import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class OLDvsNEW implements Experiment {
	
	OWLOntologyManager manager;
	OWLOntology ontology;
	SyntacticFirstModuleExtraction moduleExtractor;
	OldApproach oldModuleExtractor;

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		
		Set<OWLLogicalAxiom> newModule = null;
		moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(), signature);
		try {
			newModule = moduleExtractor.extractModule();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		Set<OWLLogicalAxiom> oldModule = null;
		oldModuleExtractor = new OldApproach(ontology.getLogicalAxioms(), signature);
		try {
			oldModule = oldModuleExtractor.extractModule();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		

//		
		System.out.println("Modules are the same?: " + oldModule.equals(newModule));

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		LinkedHashMap<String, Long> oldMetrics = oldModuleExtractor.getMetrics();
		LinkedHashMap<String, Long> newMetrics = moduleExtractor.getMetrics();
		
		writeResults(experimentLocation, newMetrics, "new");
		writeResults(experimentLocation, oldMetrics, "old");
		

	}
	
	public void writeResults(File experimentLocation, LinkedHashMap<String, Long> metrics, String fileName) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + fileName, false));
		
		Object[] keysetArray = metrics.keySet().toArray();
		/*Write header - the keyset values */
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(keysetArray[i] + ",");
		}
		writer.write(keysetArray[keysetArray.length-1] + "\n");
		
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(metrics.get(keysetArray[i]) + ",");
		}
		writer.write(metrics.get(keysetArray[keysetArray.length-1]) + "\n");
		writer.flush();
		writer.close();
		
	}


}
