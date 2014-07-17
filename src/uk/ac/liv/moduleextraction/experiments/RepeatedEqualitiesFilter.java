package uk.ac.liv.moduleextraction.experiments;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;

public class RepeatedEqualitiesFilter implements SupportedFilter{
	
	private AxiomStructureInspector inspector;
	public RepeatedEqualitiesFilter(AxiomStructureInspector inspector) {
		this.inspector = inspector;
	}

	@Override
	public boolean isRequired() {
		return inspector.countNamesWithRepeatedEqualities() > 0;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		Set<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		for(OWLClass repeated : inspector.getNamesWithRepeatedEqualities()){
			Set<OWLLogicalAxiom> repeatedEqualities = new HashSet<OWLLogicalAxiom>(inspector.getDefinitions(repeated));
			OWLLogicalAxiom toKeep = repeatedEqualities.toArray(new OWLLogicalAxiom[repeatedEqualities.size()])[0];
			// Keep an axiom and the rest are unsupported (no longer any repeated equality after their removal) 
			repeatedEqualities.remove(toKeep);
			unsupported.addAll(repeatedEqualities);
		}
		return unsupported;
	}

}
