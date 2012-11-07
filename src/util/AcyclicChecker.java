package util;

import java.util.Set;



import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import checkers.CyclicDependencies;

public class AcyclicChecker {

	public AcyclicChecker() {
	}
	
	public boolean isAcyclic (OWLOntology ontology){
		return isAcylic(ontology.getLogicalAxioms());
	}

	public boolean isAcylic(Set<OWLLogicalAxiom> logicalAxioms) {
		boolean result = true;
		CyclicDependencies dependencies = new CyclicDependencies(logicalAxioms);
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms)){
			if(dependencies.getDependenciesFor(cls).contains(cls)){
				result = false;
				break;
			}
		}
		return result;
	}
	
}
