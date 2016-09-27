package uk.ac.liv.moduleextraction.checkers;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;

import java.util.HashSet;
import java.util.Set;

public class ChainAxiomCollector {

	SyntacticDependencyChecker checker = new SyntacticDependencyChecker();
	public ChainAxiomCollector() {

	}

	public Set<OWLLogicalAxiom> collectAxiomChain(boolean[] terminology, int currentIndex, DefinitorialAxiomStore axiomStore, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM) {
		SyntacticDependencyChecker checker = new SyntacticDependencyChecker();
		Set<OWLLogicalAxiom> chain = new HashSet<OWLLogicalAxiom>();

		for (int i = currentIndex; i >= 0; i--) {
			if(terminology[currentIndex]){
				OWLLogicalAxiom axiom = axiomStore.getAxiom(currentIndex);
				if(checker.hasSyntacticSigDependency(axiom, dependT, sigUnionSigM)){
					
					chain.add(axiom);

					//Update signature
					sigUnionSigM.addAll(axiom.getSignature());
					//Remove from ontology
					terminology[currentIndex] = false;

				}
			}
		}


		return chain;
	}

}
