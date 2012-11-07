package experiments;

import java.util.Set;

import ontologyutils.OntologyLoader;
import ontologyutils.TerminologyChecker;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import checkers.DefinitorialDependencies;
import checkers.Dependencies;
import checkers.CyclicDependencies;

import util.ModuleUtils;

public class DependencyVersionValidation {

	private Set<OWLLogicalAxiom> ontology;
	private static final int TESTS = 2000;
	private static final int ONT_SIZE = 1000;

	public DependencyVersionValidation(Set<OWLLogicalAxiom> ont) {
		this.ontology = ont;
	}

	public void checkDependencies(){
		TerminologyChecker term = new TerminologyChecker();
		for(int i=0; i<TESTS; i++){
			Set<OWLLogicalAxiom> randomOntology = ModuleUtils.generateRandomAxioms(ontology, ONT_SIZE);
			System.out.println("Test " + i);
			if(term.isTerminology(randomOntology)){
				Dependencies d1 = new Dependencies(randomOntology);
				DefinitorialDependencies d2 = new DefinitorialDependencies(randomOntology);

				boolean result = d1.getDependencies().equals(d2.getDependencies());

				if(!result){
					System.out.println("Done");
					for(OWLClass cls : ModuleUtils.getClassesInSet(randomOntology)){
						if(!d1.getDependencies().get(cls).equals(d2.getDependencies().get(cls))){
							System.out.println(cls);
							System.out.println(d1.getDependencies().get(cls));
							System.out.println(d2.getDependencies().get(cls));
						}
					}

				}
			}





		}

	}

	public static void main(String[] args) {
		OWLOntology nci = OntologyLoader.loadOntology("/home/william/PhD/Ontologies/NCI/nci-03.10j.owl");
		for(OWLLogicalAxiom axiom: nci.getLogicalAxioms())
			System.out.println(axiom);
		
		TerminologyChecker term = new TerminologyChecker();
		System.out.println(term.isTerminology(nci));
		System.out.println(term.getCheckStatus());
	}
}
