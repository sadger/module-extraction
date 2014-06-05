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
		HashSet<OWLLogicalAxiom> checkingSet = new HashSet<OWLLogicalAxiom>(axioms);
		Set<OWLLogicalAxiom> cycleCausingAxioms = new HashSet<OWLLogicalAxiom>();
		
		for(OWLLogicalAxiom axiom : axioms){
			if(cycleCausing.contains(axiom)){
				cycleCausingAxioms.add(axiom);
			}
		}
		//Remove cyclic causing to extract LHS
		checkingSet.removeAll(cycleCausingAxioms);
		checkingSet = lhsExtractor.getLHSSigAxioms(axioms, sigUnionSigM, dependT);
		//Re-add them again
		checkingSet.addAll(cycleCausingAxioms);
		
		return checkingSet;
	}

}
