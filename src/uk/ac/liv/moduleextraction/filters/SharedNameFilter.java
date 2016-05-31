package uk.ac.liv.moduleextraction.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.filters.SupportedFilter;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;

public class SharedNameFilter implements SupportedFilter {

	public enum RemovalMethod{
		REMOVE_INCLUSIONS,
		REMOVE_EQUALITIES,
		RANDOM,
	}
	AxiomStructureInspector inspector;
	RemovalMethod remove_method;
	public SharedNameFilter(AxiomStructureInspector inspector, RemovalMethod remove_method) {
		this.inspector = inspector;
		this.remove_method = remove_method;
	}

	@Override
	public boolean isRequired() {
		return inspector.getSharedNames().size() > 0;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		Set<OWLClass> sharedNames = inspector.getSharedNames();

		for(OWLClass cls : sharedNames){
			
			if(remove_method == RemovalMethod.REMOVE_INCLUSIONS){
		
				unsupported.addAll(inspector.getPrimitiveDefinitions(cls));
			}
			else if(remove_method == RemovalMethod.REMOVE_EQUALITIES){
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
