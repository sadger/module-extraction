package uk.ac.liv.moduleextraction.util;

import java.util.HashMap;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.Dependency;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

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
		int axiomCount = 0;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			axiomCount++;
//			System.out.println("Checking axiom " + axiomCount + "/" + ontology.getLogicalAxiomCount());
//			System.out.println(axiom);
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
		HashSet<DependencySet> seenDependencies = new HashSet<DependencySet>();

		/* If there is nothing left to check */
		while(!toCheck.isEmpty()){
//			System.out.println(toCheck.hashCode());
			/* If the name we are searching for is in it's own dependencies
			 * or we find some dependencies to check we have already seen we have a cycle */
			if(toCheck.contains(new Dependency(name)) || seenDependencies.contains(toCheck)){
				return true;
			}
			else{
				DependencySet newDependencies = new DependencySet();
				for(Dependency d : toCheck){
					DependencySet depSet = immediateDependencies.get(d.getValue());
					if(depSet != null){
						newDependencies.addAll(depSet);
					}
				}
				seenDependencies.add(toCheck);
				toCheck = newDependencies;
			}

		}
		return false;
		
	}
	
	
	
	
	
	
	
}
