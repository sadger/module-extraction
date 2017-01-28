package uk.ac.liv.moduleextraction.filters;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.util.AxiomStructureInspector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SharedNameFilter implements SupportedFilter {


	AxiomStructureInspector inspector;
	public SharedNameFilter(AxiomStructureInspector inspector) {
		this.inspector = inspector;
	}

	@Override
	public boolean isRequired() {
		return inspector.getSharedNames().size() > 0;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		Set<OWLClass> sharedNames = inspector.getSharedNames();

		for(OWLClass cls : sharedNames) {
			unsupported.addAll(inspector.getDefinitions(cls));
		}

		return unsupported;
	}

}


