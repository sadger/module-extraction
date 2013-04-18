package uk.ac.liv.moduleextraction.util;

import java.util.Comparator;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;


public class AxiomComparator implements Comparator<OWLLogicalAxiom>{
	private Map<OWLClass, Integer> base;

	public AxiomComparator(Map<OWLClass, Integer> base) {
		this.base = base;
	}
	@Override
	public int compare(OWLLogicalAxiom axiom1, OWLLogicalAxiom axiom2) {
		OWLClass name1 = (OWLClass) AxiomSplitter.getNameofAxiom(axiom1);
		OWLClass name2 = (OWLClass) AxiomSplitter.getNameofAxiom(axiom2);
		
		if(base.get(name1) < base.get(name2)) 
			return -1;
		else if	(base.get(name1) > base.get(name2)) 
			return 1;
		else 
			return 0;
	}

}