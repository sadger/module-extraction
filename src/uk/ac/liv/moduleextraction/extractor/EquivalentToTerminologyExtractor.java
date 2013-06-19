package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class EquivalentToTerminologyExtractor implements Extractor {

	private EquivalentToTerminologyProcessor processor;
	private Set<OWLLogicalAxiom> module;
	private SemanticRuleExtractor extractor;
	
	private long timeTaken = 0;
	
	public EquivalentToTerminologyExtractor(OWLOntology equivalentToTerm) {
		try {
			processor = new EquivalentToTerminologyProcessor(equivalentToTerm);
			OWLOntology newOnt = processor.getConvertedOntology();
			extractor = new SemanticRuleExtractor(newOnt);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (NotEquivalentToTerminologyException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {		
		timeTaken = 0;
		
		long startTime = System.currentTimeMillis();
		module =  extractor.extractModule(existingModule,signature);
		module = processor.postProcessModule(module);
		timeTaken = System.currentTimeMillis() - startTime;
		return module;
	}

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}

	
	public LinkedHashMap<String, Long> getMetrics() {
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		LinkedHashMap<String, Long> extractorMetrics = extractor.getMetrics();
		
		metrics.put("Module size", (long) module.size());
		metrics.put("Time taken", timeTaken);
		metrics.put("Syntactic Checks", extractorMetrics.get("Syntactic Checks"));
		metrics.put("QBF Checks", extractorMetrics.get("QBF Checks"));
		
		return metrics;
	}

	public LinkedHashMap<String, Long> getQBFMetrics() {
		return extractor.getQBFMetrics();
	}
	

	public static void main(String[] args) throws IOException, NotEquivalentToTerminologyException, OWLOntologyCreationException, QBFSolverException {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/Thesaurus_08.09d.OWL");
	
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/chaintest"));
		
		
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ont);

	
		for (int i = 1; i <= 100; i++) {
			System.out.println(extractor.extractModule(man.readFile("random1000-"+ i)).size());
		}
		
	}




}
