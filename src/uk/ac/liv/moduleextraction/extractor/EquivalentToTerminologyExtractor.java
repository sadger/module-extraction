package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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


public class EquivalentToTerminologyExtractor implements Extractor {

	private EquivalentToTerminologyProcessor processor;
	private Set<OWLLogicalAxiom> module;
	private SemanticRuleExtractor extractor;
	
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
		Set<OWLLogicalAxiom> module =  extractor.extractModule(existingModule,signature);
		module = processor.postProcessModule(module);
		return module;
	}

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}

	
	public Set<OWLLogicalAxiom> getModule() {
		
		return module;
		
		
	}
	
	public static void main(String[] args) throws IOException, NotEquivalentToTerminologyException, OWLOntologyCreationException, QBFSolverException {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/Bioportal/NatPrO");
		OWLOntology ont2 = OntologyLoader.loadOntologyInclusionsAndEqualities("/LOCAL/wgatens/Ontologies/moduletest/repeated.krss");
		SignatureGenerator gen = new SignatureGenerator(ont2.getLogicalAxioms());
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ont);
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/skizzobreak"));
		Set<OWLEntity> sig3 = man.readFile("random50-" + 3); 
		Set<OWLEntity> sig4 = man.readFile("random50-" + 4);

		Set<OWLLogicalAxiom> newy = gen.randomAxioms(5).getLogicalAxioms();
		
		System.out.println(extractor.extractModule(newy,sig4));
		
		

		

		
		
	}


}
