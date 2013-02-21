package uk.ac.liv.moduleextraction.testing;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.Dependency;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class AcyclicChecker {
	
	private HashMap<OWLClass, DependencySet> immediateDependencies = new HashMap<OWLClass, DependencySet>();
	private OWLOntology ontology;

	public AcyclicChecker(OWLOntology ontology) {
		this.ontology = ontology;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
			addImmediateDependencies(axiom);
	}
	
	private void addImmediateDependencies(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		DependencySet axiomDeps = new DependencySet();
	
		for(OWLEntity e : definition.getSignature()){
			if(!e.isTopEntity() && !e.isBottomEntity())
				axiomDeps.add(new Dependency(e));
		}
		
		immediateDependencies.put(name, axiomDeps);
	}
	
	
	public boolean isAcyclic(){
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			if(causesCycle(axiom))
				return false;
		}
		return true;
	}

	/* Follow the definition of the axioms and see if you find it's own
	 * name in that definition
	 */
	private boolean causesCycle(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		DependencySet toCheck = immediateDependencies.get(name);
		
		while(!toCheck.isEmpty()){
			if(toCheck.contains(new Dependency(name)))
				return true;
			else{
				DependencySet newDependencies = new DependencySet();
				for(Dependency d : toCheck){
					DependencySet depSet = immediateDependencies.get(d.getValue());
					if(depSet != null){
						newDependencies.mergeWith(depSet);
					}
				}
				toCheck = newDependencies;
			}

		}
		return false;
		
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "moduletest/test1.krss");
		AcyclicChecker check = new AcyclicChecker(ont);
		System.out.println(check.isAcyclic());
		
		
	}
	
	
	
	
}
