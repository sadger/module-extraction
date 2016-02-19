package uk.ac.liv.moduleextraction.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.filters.SupportedFilter;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;

public class SupportedExpressivenessFilter implements SupportedFilter {

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		SupportedAxiomVerifier verifier = new SupportedAxiomVerifier();
		for(OWLLogicalAxiom axiom : axioms){
			
			if(!verifier.isSupportedAxiom(axiom)){
				unsupported.add(axiom);
			}
		}
		
		return unsupported;
	}

}
