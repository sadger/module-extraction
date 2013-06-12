package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.terminology.TerminologyChecker;


public class EquivalentToTerminologyExtractor {

	private EquivalentToTerminologyProcessor processor;
	private Set<OWLLogicalAxiom> module;
	private SemanticRuleExtractor extractor;
	
	public EquivalentToTerminologyExtractor(OWLOntology equivalentToTerm) throws NotEquivalentToTerminologyException, OWLOntologyCreationException {
		processor = new EquivalentToTerminologyProcessor(equivalentToTerm);
		OWLOntology newOnt = processor.getConvertedOntology();
		extractor = new SemanticRuleExtractor(newOnt);
	}

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) throws IOException, QBFSolverException {
		
		//System.out.println("S:" + equiv.getLogicalAxiomCount());
		long startTime = System.currentTimeMillis();
		
		Set<OWLLogicalAxiom> module =  extractor.extractModule(signature);
		module = processor.postProcessModule(module);
		
		long endTime = System.currentTimeMillis() - startTime;
		
		System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(endTime));
		return module;
		
	}
	
	
	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}
	
	public static void main(String[] args) throws IOException, NotEquivalentToTerminologyException, OWLOntologyCreationException, QBFSolverException {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/NCI/nci-08.09d.owl");
		
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ont);
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/skizzobreak"));
		Set<OWLEntity> sig3 = man.readFile("random50-" + 3); 
		Set<OWLEntity> sig4 = man.readFile("random50-" + 4);

		for (int i = 0; i < 1000; i++) {
			System.out.println(extractor.extractModule(gen.generateRandomSignature(1000)).size());
						
		}
		
		

		

		
		
	}
}
