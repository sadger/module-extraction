package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

/***
 * Generates the set of dependT for ontologies 
 * with (possibly) repeated axioms distinishing different
 * dependency sets for each 
 */
public class AxiomDependencies extends HashMap<OWLLogicalAxiom, DependencySet>{

	public AxiomDependencies() {
		
	}
	
	public void updateDependenciesWith(List<OWLLogicalAxiom> sortedAxioms){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			updateDependenciesWith(it.next());
		}	
	}
	
	public void updateDependenciesWith(OWLLogicalAxiom[] axiomArray){
		for(OWLLogicalAxiom ax : axiomArray){
			updateDependenciesWith(ax);
		}
	}
	
	public void updateDependenciesWith(OWLLogicalAxiom axiom){

		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

		DependencySet axiomDeps = new DependencySet();
		
		addImmediateDependencies(definition,axiomDeps);
		updateFromDefinition(definition, axiomDeps);

		put(axiom, axiomDeps);
	}
	
	private void addImmediateDependencies(OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLEntity e : definition.getSignature()){
			if(!e.isTopEntity() && !e.isBottomEntity())
				axiomDeps.add(e);
		}
	}

	private void updateFromDefinition(OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){

			for(OWLLogicalAxiom axiom : keySet()){
				OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				if(name.equals(cls)){
					DependencySet clsDependencies = get(axiom);
					if(clsDependencies != null){
						axiomDeps.addAll(clsDependencies);
					}
				}
			}

		}
	}
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/axiomdep.krss");
		
		AxiomDefinitorialDepth d = new AxiomDefinitorialDepth(ont);
		List<OWLLogicalAxiom> dtSorted = d.getDefinitorialSortedList();
		for(OWLLogicalAxiom axiom : dtSorted){
			System.out.println(axiom);
		}
		AxiomDependencies depends = new AxiomDependencies();
		depends.updateDependenciesWith(dtSorted);
		
		System.out.println(depends);
	}
}
