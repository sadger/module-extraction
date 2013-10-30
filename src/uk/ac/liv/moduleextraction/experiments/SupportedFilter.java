package uk.ac.liv.moduleextraction.experiments;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public interface SupportedFilter {
	
	//Is this filter required for the input axioms
	public abstract boolean isRequired();
	
	public abstract Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms);
	
	
}