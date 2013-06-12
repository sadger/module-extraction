package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class EquivalentToTerminologyExtractor {

	private EquivalentToTerminologyProcessor processor;
	private Set<OWLLogicalAxiom> module;
	private SemanticRuleExtractor extractor;
	OWLOntology equiv;
	
	public EquivalentToTerminologyExtractor(OWLOntology equivalentToTerm) throws NotEquivalentToTerminologyException {
		processor = new EquivalentToTerminologyProcessor(equivalentToTerm);
		//equiv = processor.preProcessOntology();
		extractor = new SemanticRuleExtractor(equivalentToTerm);
		
	}

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		
		//System.out.println("S:" + equiv.getLogicalAxiomCount());
		long startTime = System.currentTimeMillis();
		
		Set<OWLLogicalAxiom> module =  extractor.extractModule(signature);		
		//module = processor.postProcessModule(module);
		
		long endTime = System.currentTimeMillis() - startTime;
		
		System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(endTime));
		return module;
		
	}
	
	
	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}
	
	public static void main(String[] args) throws IOException, NotEquivalentToTerminologyException {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/Bioportal/NOTEL/Terminologies/NatPrO-converted");
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/skizzobreak"));
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ont);

		Set<OWLEntity> sig3 = man.readFile("random50-" + 3);
		Set<OWLEntity> sig4 = man.readFile("random50-" + 4);

		for (int i = 0; i < 50; i++) {
			System.out.println(extractor.extractModule(sig3).size());
			System.out.println(extractor.extractModule(sig4).size());
				
		}

		
		
	}
}
