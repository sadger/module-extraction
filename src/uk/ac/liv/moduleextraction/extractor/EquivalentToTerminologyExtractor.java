package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class EquivalentToTerminologyExtractor {

	private OWLOntology equivToTerm;
	private EquivalentToTerminologyProcessor processor;
	
	public EquivalentToTerminologyExtractor(OWLOntology equivalentToTerm) throws NotEquivalentToTerminologyException {
		this.equivToTerm = equivalentToTerm;
		processor = new EquivalentToTerminologyProcessor(equivalentToTerm);
	}
	
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) throws IOException, QBFSolverException{
		
		processor.preProcessOntology();
		SyntacticFirstModuleExtraction moduleExtractor = 
				new SyntacticFirstModuleExtraction(equivToTerm.getLogicalAxioms(), signature);
		
		Set<OWLLogicalAxiom> module = moduleExtractor.extractModule();
		
		return processor.postProcessModule(module);

		
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/moduletest/equiv.krss");
		try {
			EquivalentToTerminologyExtractor extract = new EquivalentToTerminologyExtractor(ont);
			SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
			Set<OWLEntity> sig = gen.generateRandomSignature(3);
			System.out.println("Sig: " + sig);
			try {
				System.out.println(extract.extractModule(sig));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (QBFSolverException e) {
				e.printStackTrace();
			}
		} catch (NotEquivalentToTerminologyException e) {
			e.printStackTrace();
		}
		
	}
	
}
