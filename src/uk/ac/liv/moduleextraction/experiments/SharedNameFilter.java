package uk.ac.liv.moduleextraction.experiments;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.profling.AxiomTypeProfile;
import uk.ac.liv.moduleextraction.profling.ExpressionTypeProfiler;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.expressions.ExpressionTypeCounter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

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
		return inspector.getNamesInIntersection().size() > 0;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		Set<OWLClass> sharedNames = inspector.getNamesInIntersection();

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
