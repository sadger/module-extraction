package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicSeparabilityAxiomLocator extends SeparabilityAxiomLocator {

	public CyclicSeparabilityAxiomLocator(OWLLogicalAxiom[] subsetAsArray, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT) {
		super(subsetAsArray, sigUnionSigM, dependT);
	}
	
	@Override
	public HashSet<OWLLogicalAxiom> getCheckingSet(List<OWLLogicalAxiom> axioms, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT) {
		//For expressive logics LHS is harder to compute so just use everything for now
		HashSet<OWLLogicalAxiom> checkingSet = new HashSet<OWLLogicalAxiom>(axioms);		
		return checkingSet;
	}

}
