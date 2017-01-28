package uk.ac.liv.moduleextraction.cycles;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.HashSet;
import java.util.Set;

public class CycleRemover {

	OntologyCycleVerifier verifier;
	public CycleRemover() {
		
	}
	
	/**
	 * 
	 * @param axioms - axioms to derrive acyclic subset from
	 * @return some acyclic subset, as no unique maximal may exist (emptyset if no acyclic subset exists)
	 */
	public Set<OWLLogicalAxiom> getAcyclicSubset(Set<OWLLogicalAxiom> axioms){
		Set<OWLLogicalAxiom> acyclicSubset = new HashSet<OWLLogicalAxiom>();
		
		for(OWLLogicalAxiom axiom : axioms){
			acyclicSubset.add(axiom);
			verifier = new OntologyCycleVerifier(acyclicSubset);
			if(verifier.isCyclic()){
				acyclicSubset.remove(axiom);
			}
		}
	
		return acyclicSubset;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/cycle-test.krss");
		
		Set<OWLLogicalAxiom> axioms = ont.getLogicalAxioms();
		System.out.println(axioms);
		System.out.println(new OntologyCycleVerifier(axioms).getCycleCausingAxioms());
		System.out.println(new CycleRemover().getAcyclicSubset(axioms));
		
	}
}
