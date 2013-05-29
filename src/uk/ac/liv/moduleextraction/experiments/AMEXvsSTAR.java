package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class AMEXvsSTAR implements Experiment {

	OWLOntologyManager manager;
	OWLOntology ontology;
	SyntacticFirstModuleExtraction moduleExtractor;
	SyntacticLocalityModuleExtractor syntaxModExtractor;
	Set<OWLLogicalAxiom> syntacticModule = null;
	Set<OWLLogicalAxiom> semanticModule = null;
	
	@Override
	public void performExperiment(OWLOntology ontology, Set<OWLEntity> signature) {
		manager = OWLManager.createOWLOntologyManager();
		syntaxModExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
		
		Set<OWLAxiom> syntacticOntology = syntaxModExtractor.extract(signature);
		
		syntacticModule = getLogicalAxioms(syntacticOntology);
 
		moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(),signature);
		
		try {
			semanticModule = moduleExtractor.extractModule();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		writeResults(experimentLocation);
		writeExtractorMetrics(experimentLocation);
		writeQBFMetrics(experimentLocation);
	}
	
	public void writeResults(File experimentLocation) throws IOException{		
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		writer.write("Syntactic Size,Semantic Size, ModulesSame, SemSubsetOfStar, StarSubsetSem" + "\n");
		writer.write(syntacticModule.size() + "," + semanticModule.size() + "," 
		+ semanticModule.equals(syntacticModule) + "," + syntacticModule.containsAll(semanticModule) + "," + semanticModule.containsAll(syntacticModule) + "\n");
		writer.flush();
		writer.close();
	}
	
	private void writeExtractorMetrics(File experimentLocation) throws IOException{
		LinkedHashMap<String, Long> metrics = moduleExtractor.getMetrics();
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "metrics", false));
		
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
	
	private void writeQBFMetrics(File experimentLocation) throws IOException{
		LinkedHashMap<String, Long> metrics = moduleExtractor.getQBFMetrics();
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "qbf-metrics", false));
			
		Object[] keysetArray = metrics.keySet().toArray();
		
		/*Write header - the keyset values */
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(keysetArray[i] + ",");
		}
		writer.write(keysetArray[keysetArray.length-1] + "\n");
		
		/*Write content*/
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(metrics.get(keysetArray[i]) + ",");
		}
		writer.write(metrics.get(keysetArray[keysetArray.length-1]) + "\n");
		writer.flush();
		writer.close();
	}
	
	
	private Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom())
				result.add((OWLLogicalAxiom) ax);
		}
		return result;
	}
	

}
