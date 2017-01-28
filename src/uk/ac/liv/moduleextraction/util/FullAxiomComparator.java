package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Comparator;
import java.util.Map;


public class FullAxiomComparator implements Comparator<OWLLogicalAxiom>{
	private Map<OWLLogicalAxiom, Integer> base;

	public FullAxiomComparator(Map<OWLLogicalAxiom, Integer> base) {
		this.base = base;
	}

	@Override
	public int compare(OWLLogicalAxiom axiom1, OWLLogicalAxiom axiom2) {
		
		if(base.get(axiom1) < base.get(axiom2)) 
			return -1;
		else if	(base.get(axiom1) > base.get(axiom2)) 
			return 1;
		else 
			return 0;
	}

}