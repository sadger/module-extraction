package uk.ac.liv.moduleextraction.filters;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OntologyFilters implements SupportedFilter {

	// Queue of filters the output of the i-th passed to i+1-th
	ArrayList<SupportedFilter> filters = new ArrayList<SupportedFilter>();
	
	public OntologyFilters() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean isRequired() {
		return true;
	}

	public void addFilter(SupportedFilter filter){
		filters.add(filter);
	}
	
	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> supported = new HashSet<OWLLogicalAxiom>(axioms);
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		for(SupportedFilter filter : filters){
			if(filter.isRequired()){
				unsupported.addAll(filter.getUnsupportedAxioms(supported));
			}
			supported.removeAll(unsupported);
		}
		
		return unsupported;
	}

}
