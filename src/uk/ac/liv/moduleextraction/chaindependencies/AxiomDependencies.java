package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	private AxiomDefinitorialDepth depth;
	private ArrayList<OWLLogicalAxiom> sortedAxioms;
	
	public AxiomDependencies(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public AxiomDependencies(Set<OWLLogicalAxiom> axioms){
		depth = new AxiomDefinitorialDepth(axioms);
		sortedAxioms = depth.getDefinitorialSortedList();
		calculateDependencies();
	}
	
	private void calculateDependencies(){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			updateDependenciesWith(it.next());
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
		for(OWLEntity e : definition.getSignature()){
			if(!e.isTopEntity() && !e.isBottomEntity())
				axiomDeps.add(e);
		}
	}

	private void updateFromDefinition(OWLClass axiomName, OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){

			for(OWLLogicalAxiom axiom : keySet()){
				OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				//TODO does this check make it any faster? 
				// OR we could simply search up to the index of the axiom (probably better) 
				if(depth.lookup(cls) < depth.lookup(axiomName) && name.equals(cls)){
					DependencySet clsDependencies = get(axiom);
					if(clsDependencies != null){
						axiomDeps.addAll(clsDependencies);
					}
				}
			}

		}
	}
	
	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedAxioms() {
		return sortedAxioms;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/axiomdep.krss");
		
		AxiomDefinitorialDepth d = new AxiomDefinitorialDepth(ont);
		List<OWLLogicalAxiom> dtSorted = d.getDefinitorialSortedList();
		for(OWLLogicalAxiom axiom : dtSorted){
			System.out.println(axiom);
		}
		AxiomDependencies depends = new AxiomDependencies(ont);
		
		System.out.println(depends);
	}
}
