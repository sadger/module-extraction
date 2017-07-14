package uk.ac.liv.moduleextraction.axiomdependencies;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.AxiomStructureInspector;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

import java.util.*;

/***
 * Generates the set of dependT for ontologies 
 * with (possibly) repeated axioms distinishing different
 * dependency sets for each 
 */
public class AxiomDependencies extends HashMap<OWLLogicalAxiom, DependencySet>{

	private AxiomDefinitorialDepth depth;
	private ArrayList<OWLLogicalAxiom> sortedAxioms;
	private AxiomStructureInspector inspector;

	public AxiomDependencies(OWLOntology ontology) {
		this(OWLAPIStreamUtils.asSet(ontology.logicalAxioms()));
	}

	public AxiomDependencies(Set<OWLLogicalAxiom> axioms){
		depth = new AxiomDefinitorialDepth(axioms);
		sortedAxioms = depth.getDefinitorialSortedList();
		inspector = new AxiomStructureInspector(axioms);
		calculateDependencies();
	}

	private void calculateDependencies(){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			OWLLogicalAxiom nextAxiom = it.next();
			/* No dependencies for non-terminological axioms */
			if(ModuleUtils.isInclusionOrEquation(nextAxiom)){
				updateDependenciesWith(nextAxiom);
			}
		}	
	}

	private void updateDependenciesWith(OWLLogicalAxiom axiom){
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

		DependencySet axiomDeps = new DependencySet();

		
		addImmediateDependencies(definition,axiomDeps);
		updateFromDefinition(name, definition, axiomDeps);

		put(axiom, axiomDeps);
	}

	private void addImmediateDependencies(OWLClassExpression definition, DependencySet axiomDeps) {
		definition.signature().forEach(e -> {
			if(!e.isTopEntity() && !e.isBottomEntity())
				axiomDeps.add(e);
		});
	}

	private void updateFromDefinition(OWLClass axiomName, OWLClassExpression definition, DependencySet axiomDeps) {

		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){

			for(OWLLogicalAxiom axiom : getAxiomsWithName(cls)){

				DependencySet clsDependencies = get(axiom);
				if(clsDependencies != null){
					axiomDeps.addAll(clsDependencies);
				}
			}
		}


	}

	private Set<OWLLogicalAxiom> getAxiomsWithName(OWLClass name){
		Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
		axioms.addAll(inspector.getDefinitions(name));
		axioms.addAll(inspector.getPrimitiveDefinitions(name));
		return axioms;
	}

	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedAxioms() {
		return sortedAxioms;
	}

}
