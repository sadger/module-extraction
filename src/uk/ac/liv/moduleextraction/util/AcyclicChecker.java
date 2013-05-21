package uk.ac.liv.moduleextraction.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class AcyclicChecker {
	
	private HashMap<OWLClass, Set<OWLClass>> classDependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private OWLOntology ontology;

	private HashSet<OWLClass> dependsOnCycle = new HashSet<OWLClass>();
	private HashSet<OWLClass> causesCycle = new HashSet<OWLClass>();
	
	
	public AcyclicChecker(OWLOntology ontology) {
		this.ontology = ontology;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			addImmediateDependencies(axiom);
		}		
		
	}
	
	public void printMetrics(){
		System.out.println();
		boolean acyclic = isAcyclic();
		System.out.println("Is acyclic " + acyclic);
		if(!acyclic){
			System.out.println("Concepts in sig: " + ontology.getClassesInSignature().size());
			int lhsSize = classDependencies.keySet().size();
			System.out.println("LHS size: " + lhsSize);
			System.out.println("Cycle causing: " + causesCycle.size() + " (" + Math.round(((double) causesCycle.size()/lhsSize)*100) + "%)");
			dependsOnCycle.removeAll(causesCycle);
			System.out.println("Depends on cycle: " + dependsOnCycle.size() + " (" + Math.round(((double) dependsOnCycle.size()/lhsSize)*100) + "%)");
			int doesNotDepend = lhsSize - causesCycle.size() - dependsOnCycle.size();
			System.out.println("Does not depend on cycle: " + doesNotDepend + " (" + Math.round(((double) doesNotDepend/lhsSize)*100) + "%)");

			
			System.out.println(causesCycle);
		}

	}
	
	private void addImmediateDependencies(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		Set<OWLClass> axiomDeps = createAxiomDependencySet(definition);
		
		populateImmediateDependency(name, axiomDeps);
		
		
	}
	
	private Set<OWLClass> createAxiomDependencySet(OWLClassExpression definition) {
		HashSet<OWLClass> axiomDeps = new HashSet<OWLClass>();
		for(OWLClass cls : definition.getClassesInSignature()){
			if(!cls.isTopEntity() && !cls.isBottomEntity()){
				axiomDeps.add(cls);
			}
		}
		return axiomDeps;
	}
	
	private void populateImmediateDependency(OWLClass name, Set<OWLClass> axiomDeps) {
		Set<OWLClass> nameDependencies = classDependencies.get(name);
		if(nameDependencies == null){
			classDependencies.put(name, axiomDeps);
		}
		else{
			// Respect names with multiple definitions
			nameDependencies.addAll(axiomDeps);
			classDependencies.put(name, nameDependencies);
		}
	}


	public boolean isAcyclic(){
		int axiomCount = 0;
		boolean result = true ;

		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			axiomCount++;
			System.out.println("Checking axiom " + axiomCount + "/" + ontology.getLogicalAxiomCount());
			System.out.println(axiom);
			
			boolean noCycle = doesNotContainCycle(axiom);
			System.out.println("Cycley?: " + !noCycle);
			result = result && noCycle;
			
		}

		
		return result;
	}
	
	public boolean doesNotContainCycle(OWLLogicalAxiom axiom){
		
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		
		boolean containsNoCycle = 
				noCycleExistsInAxiom(new HashSet<OWLClass>(Collections.singleton(name)), classDependencies.get(name));
		
		if(!containsNoCycle){
			dependsOnCycle.add(name);
		}
		
		return containsNoCycle;
	}
	
	
	public boolean noCycleExistsInAxiom(Set<OWLClass> names, Set<OWLClass> toCheck){
		
		//System.out.println(names + "|" + toCheck);
		if(toCheck.isEmpty()){
			return true;
		}
		else{
			for(OWLClass cls : names){
				if(toCheck.contains(cls)){
					return false;
				}
			}
			
			boolean result = true;
			
			for(OWLClass check : toCheck){
				HashSet<OWLClass> newNames = new HashSet<OWLClass>();
				newNames.addAll(names);
				newNames.add(check);
				
				HashSet<OWLClass> newCheck = new HashSet<OWLClass>();
				
				Set<OWLClass> checkDepends = classDependencies.get(check);
				
				if(checkDepends != null){
					newCheck.addAll(checkDepends);
				}
				
				boolean p = noCycleExistsInAxiom(newNames, newCheck);
				result = result && p;
			}
			
			return result;
		}
	}

	


	
	public static void main(String[] args) {
	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/moduletest/acyclic.krss");
	//	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
	//	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/Bioportal/NatPrO");
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/smallcycley");
	
	//System.out.println(ont);
		System.out.println("Logical axioms: " + ont.getLogicalAxiomCount());
	AcyclicChecker checker = new AcyclicChecker(ont);


	System.out.println("Is acyclic: " + checker.isAcyclic());
//	checker.printMetrics();



		
	} 
	
	
	
	
	
}
