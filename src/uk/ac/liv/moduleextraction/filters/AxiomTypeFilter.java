package uk.ac.liv.moduleextraction.filters;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AxiomTypeFilter implements SupportedFilter {

	private final AxiomType<?> axiomType;

	public AxiomTypeFilter(AxiomType<?> type){
		this.axiomType = type;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();

		for(OWLLogicalAxiom axiom : axioms){

			if(axiom.getAxiomType().equals(axiomType)){
				unsupported.add(axiom);
			}
		}

		return unsupported;
	}

}
