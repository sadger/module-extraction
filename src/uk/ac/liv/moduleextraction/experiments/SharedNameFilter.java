package uk.ac.liv.moduleextraction.experiments;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;

public class SharedNameFilter implements SupportedFilter {

	public enum REMOVAL_METHOD{
		REMOVE_INCLUSIONS,
		REMOVE_EQUALITIES,
		RANDOM,
	}
	AxiomStructureInspector inspector;
	REMOVAL_METHOD remove_method;
	public SharedNameFilter(AxiomStructureInspector inspector, REMOVAL_METHOD remove_method) {
		this.inspector = inspector;
		this.remove_method = remove_method;
	}

	@Override
	public boolean isRequired() {
		return inspector.getNamesInIntersection().size() > 0;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		Set<OWLClass> sharedNames = inspector.getNamesInIntersection();

		for(OWLClass cls : sharedNames){
			if(remove_method == REMOVAL_METHOD.REMOVE_INCLUSIONS){
				unsupported.addAll(inspector.getPrimitiveDefinitions(cls));
			}
			else if(remove_method == REMOVAL_METHOD.REMOVE_INCLUSIONS){
				unsupported.addAll(inspector.getDefinitions(cls));
			}else{
				//50% chance for each approach to be selected per name
				if(Math.random() < 0.5){
					unsupported.addAll(inspector.getPrimitiveDefinitions(cls));
				}
				else{
					unsupported.addAll(inspector.getDefinitions(cls));
				}
			}

		}
		return unsupported;
	}

}
