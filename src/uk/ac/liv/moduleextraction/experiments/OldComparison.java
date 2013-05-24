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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.extractor.OldApproach;
import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class OldComparison {
	Logger logger = LoggerFactory.getLogger(OldComparison.class);
	
	OWLOntologyManager manager;
	OWLOntology ontology;
	SyntacticFirstModuleExtraction moduleExtractor;
	OldApproach oldModuleExtractor;
	private Set<OWLEntity> signature;
	
	private DumpExtractionToDisk dump;

	private File experimentLocation;
	
	public OldComparison(OWLOntology ontology, Set<OWLEntity> sig, File experimentLocation) {
		AxiomExtractor extractor = new AxiomExtractor();
		this.experimentLocation = experimentLocation;
		this.signature = sig;
		this.ontology = extractor.extractInclusionsAndEqualities(ontology);
	}
	
	public void compareExtractionApproaches() throws IOException, QBFSolverException{
		File experimentResultFile = new File(experimentLocation + "/" + "new");
		if(experimentResultFile.exists()){
			logger.info("Already complete" + "\n");
			return;
		}
		
		oldModuleExtractor = new OldApproach(ontology.getLogicalAxioms(), signature);
		Set<OWLLogicalAxiom> oldModule = oldModuleExtractor.extractModule();
		
		moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(), signature);
		Set<OWLLogicalAxiom> newModule = moduleExtractor.extractModule();
		
		
		this.dump = new DumpExtractionToDisk(experimentLocation, newModule, signature);
		System.out.println(oldModuleExtractor.getMetrics());
		System.out.println(moduleExtractor.getMetrics());
		
		if(!experimentLocation.exists()){
			experimentLocation.mkdirs();
		}
		writeResults();
			
	}
	
	public void writeResults() throws IOException {
		LinkedHashMap<String, Long> oldMetrics = oldModuleExtractor.getMetrics();
		LinkedHashMap<String, Long> newMetrics = moduleExtractor.getMetrics();
		
		writeMetrics(newMetrics, "new");
		writeMetrics(oldMetrics, "old");
		
		
		/* Dump the results before finishing */
		new Thread(dump).start();
	}
	
	public void writeMetrics(LinkedHashMap<String, Long> metrics, String fileName) throws IOException{
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
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities("/LOCAL/wgatens/Ontologies/Bioportal/NOTEL/Terminologies/Acyclic/Big/LiPrO-converted");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		Set<OWLEntity> sig = gen.generateRandomSignature(1);
		
		try {
			new OldComparison(ont, sig, new File(ModulePaths.getResultLocation() + "/pewrrrr")).compareExtractionApproaches();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
	}
}
