package uk.ac.liv.moduleextraction.experiments;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.EquivalentToTerminologyExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class AMEXvsSTAR implements Experiment {

	OWLOntologyManager manager;
	OWLOntology ontology;
	EquivalentToTerminologyExtractor semanticExtractor;
	SyntacticLocalityModuleExtractor syntaxModExtractor;
	Set<OWLLogicalAxiom> syntacticModule = null;
	Set<OWLLogicalAxiom> semanticModule = null;
	

	public AMEXvsSTAR(OWLOntology ontology) {
		this.manager = ontology.getOWLOntologyManager();
		this.syntaxModExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
		this.semanticExtractor = new EquivalentToTerminologyExtractor(ontology);
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLAxiom> syntacticOntology = syntaxModExtractor.extract(signature);
		
		syntacticModule = getLogicalAxioms(syntacticOntology);
		semanticModule = semanticExtractor.extractModule(signature);
		

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
//		writeResults(experimentLocation);
//		writeMetricsToFile(experimentLocation, semanticExtractor.getMetrics(), "metrics");
//		writeMetricsToFile(experimentLocation, semanticExtractor.getQBFMetrics(), "qbf-metrics");
	}
	
	public void writeResults(File experimentLocation) throws IOException{		
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		writer.write("Syntactic Size,Semantic Size, ModulesSame, SemSubsetOfStar, StarSubsetSem, InSemButNotStar, InStarButNotSem" + "\n");
		
		writer.write(syntacticModule.size() + "," + semanticModule.size() + "," 
		+ semanticModule.equals(syntacticModule) + "," + syntacticModule.containsAll(semanticModule) + "," + semanticModule.containsAll(syntacticModule) + ",");
		
		Set<OWLLogicalAxiom> semanticDifference = new HashSet<OWLLogicalAxiom>(semanticModule);
		semanticDifference.removeAll(syntacticModule);
		
		Set<OWLLogicalAxiom> syntacticDifference = new HashSet<OWLLogicalAxiom>(syntacticModule);
		syntacticDifference.removeAll(semanticModule);
		
		writer.write(semanticDifference.size() + "," + syntacticDifference.size() + "\n");
		
		writer.flush();
		writer.close();
	}
	

	public void writeMetricsToFile(File experimentLocation, LinkedHashMap<String, Long> metrics, String fileName) throws IOException{
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
	
	private Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom())
				result.add((OWLLogicalAxiom) ax);
		}
		return result;
	}

	@Override
	public void performExperiment(Set<OWLEntity> sig, File f) {
		// TODO Auto-generated method stub
		
	}
	

}
