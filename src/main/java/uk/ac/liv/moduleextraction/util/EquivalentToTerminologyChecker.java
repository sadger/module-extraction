package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;


public class EquivalentToTerminologyChecker {

	public EquivalentToTerminologyChecker() {
	
	}
	
	/* Take intersection of primitive and defined concepts.
	 * If the intersection is empty then no name is both a primitive concept
	 * definition and defined concept.
	 */
	public boolean isEquivalentToTerminology(Set<OWLLogicalAxiom> ontology){
		
		AxiomStructureInspector inspector = new AxiomStructureInspector(ontology);
		
		return (inspector.getSharedNames().isEmpty()) && (inspector.countNamesWithRepeatedEqualities() == 0);
	}
	
}
