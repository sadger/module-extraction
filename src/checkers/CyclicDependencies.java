package checkers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import axioms.AxiomSplitter;

import util.ModuleUtils;


/*
 * Generates the set of dependencies including cyclic terminologies but
 * is MUCH slower than the definitorial approach "DefinitorialDependencies.java"
 */
public class CyclicDependencies {
	private HashMap<OWLClass, Set<OWLClass>> dependencies;

	private Set<OWLLogicalAxiom> ontology;

	public CyclicDependencies(Set<OWLLogicalAxiom> ont) {
		this.ontology = ont;
		this.dependencies = new HashMap<OWLClass, Set<OWLClass>>();
		initialseMappings();
		for(OWLLogicalAxiom axiom : ontology){
			addAxiomToDependencies(axiom);
		}
	}


	public void addAxiomToDependencies(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		Set<OWLClass> toAdd = AxiomSplitter.getDefinitionofAxiom(axiom).getClassesInSignature();
		addFromTop(name, toAdd);
		addFromBottom(name, toAdd);
		
		/* Finally add the immediate dependencies */
		dependencies.get(name).addAll(toAdd);
	}

	public void clearMappings(){
		dependencies.clear();
	}
	
	public HashMap<OWLClass, Set<OWLClass>> getDependencies() {
		return dependencies;
	}
	
	public Set<OWLClass> getDependenciesFor(OWLClass name){
		return dependencies.get(name);
	}
	
	public boolean isEmpty(){
		return dependencies.isEmpty();
	}
	
	private void initialseMappings() {
		for(OWLClass cls : ModuleUtils.getClassesInSet(ontology)){
			dependencies.put(cls, new HashSet<OWLClass>());
		}
	}

	/*
	 * Give a class cls adds the dependencies of it's immediate
	 * dependencies such that if we have deps(A) = {B,C},
	 * deps(B) = {C} and add a new axiom X -> A we get
	 * deps(X) = {A,B,C}
	 */
	private void addFromTop(OWLClass name, Set<OWLClass> toAdd) {
		for(OWLClass cls : toAdd){
			Set<OWLClass> clsDependencies = dependencies.get(cls);
			if(clsDependencies != null){
				dependencies.get(name).addAll(clsDependencies);
			}
		}
	}

	/*
	 * Propagate changes to dependencies from bottom adding changes
	 * e.g if D -> E is added and B -> D and A -> B is in the ontology
	 * E is added to both deps(B) AND deps(A). 
	 */
	private void addFromBottom(OWLClass name, Set<OWLClass> toAdd) {
		Set<OWLClass> changed = name.getClassesInSignature();
		Set<OWLClass> toRemove = new HashSet<OWLClass>();
		Set<OWLClass> addToChanged = new HashSet<OWLClass>();
		while(!changed.isEmpty()){
			for(OWLClass c : changed){
				for(OWLClass a : ModuleUtils.getClassesInSet(ontology)){
					Set<OWLClass> dependenciesOfA = dependencies.get(a);
					if(dependenciesOfA != null && dependenciesOfA.contains(c)){
						addToChanged.add(a);
						dependenciesOfA.addAll(toAdd);
					}
					toRemove.add(c);
				}
			}
			changed.addAll(addToChanged);
			changed.removeAll(toRemove);
		}
	}

	@Override
	public String toString() {
		return dependencies.toString();
	}

	public static void main(String[] args) {
		
		OWLOntology ont = OntologyLoader.loadOntology("/home/william/PhD/Ontologies/interp/diff.krss");
		Set<OWLLogicalAxiom> ontology = ont.getLogicalAxioms();
		for(OWLLogicalAxiom axiom : ontology)
			System.out.println(axiom);

//		NewDependencies deps = new NewDependencies(ontology);
//		System.out.println(deps);
	}
}
