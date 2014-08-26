package uk.ac.liv.moduleextraction.qbf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;

public class CyclicSeparabilityAxiomLocator extends SeparabilityAxiomLocator {

	private Set<OWLLogicalAxiom> cycleCausing;

	public CyclicSeparabilityAxiomLocator(OWLLogicalAxiom[] subsetAsArray, Set<OWLLogicalAxiom> cycleCausing, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT) {
		super(subsetAsArray, sigUnionSigM, dependT);
		this.cycleCausing = cycleCausing;
	}
	
	@Override
	public HashSet<OWLLogicalAxiom> getCheckingSet(List<OWLLogicalAxiom> axioms, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT) {
		//For expressive logics LHS is harder to compute so just use everything for now
		HashSet<OWLLogicalAxiom> checkingSet = new HashSet<OWLLogicalAxiom>(axioms);		
		return checkingSet;
	}

}
