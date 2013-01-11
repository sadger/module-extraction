package uk.ac.liv.moduleextraction.signature;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.checkers.DefinitorialDependencies;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;



public class DependencyHierarchy {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private HashMap<OWLClass, Set<OWLClass>> immediateDependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private HashMap<OWLClass, HashMap<Integer, Set<OWLClass>>> dependencyHierarchy = 
			new HashMap<OWLClass, HashMap<Integer,Set<OWLClass>>>();

	public DependencyHierarchy(Set<OWLLogicalAxiom> logicalAxioms) {
		this.logicalAxioms = logicalAxioms;
		populateImmediateDependencies();
		generateHierarchy();
	}

	public Set<OWLClass> getDependencyForDepth(OWLClass cls, int depth){
		HashMap<Integer, Set<OWLClass>> hierarchy = dependencyHierarchy.get(cls);
		return hierarchy.get(depth); 
	}

	public int getMaxHierarchyDepth(OWLClass cls){
		HashMap<Integer, Set<OWLClass>> hierarchy = dependencyHierarchy.get(cls);
		if(hierarchy.isEmpty())
			return 0;
		else
			return Collections.max(hierarchy.keySet());
	}
	
	private void populateImmediateDependencies() {
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms))
			immediateDependencies.put(cls, new HashSet<OWLClass>());

		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definiton = AxiomSplitter.getDefinitionofAxiom(axiom);
			immediateDependencies.put(name,ModuleUtils.getNamedClassesInSignature(definiton));
		}
	}

	private void generateHierarchy(){
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms))
			generateHierarchyForClass(cls);
	}

	private void generateHierarchyForClass(OWLClass cls){
		HashMap<Integer, Set<OWLClass>> hierarchy = new HashMap<Integer, Set<OWLClass>>();
		Set<OWLClass> currentDependencies = immediateDependencies.get(cls);

		int depDepth = 1;

		while(!currentDependencies.isEmpty()){
			Set<OWLClass> newDeps = new HashSet<OWLClass>();

			for(OWLClass dep : currentDependencies){
				newDeps.addAll(immediateDependencies.get(dep));
			}
			hierarchy.put(depDepth, currentDependencies);

			currentDependencies = newDeps;
			depDepth++;
		}

		dependencyHierarchy.put(cls, hierarchy);
	}

	@Override
	public String toString() {
		String toPrint = "";
		HashSet<OWLClass> emptyHierarchy = new HashSet<OWLClass>();
		for(OWLClass cls : dependencyHierarchy.keySet()){
			HashMap<Integer, Set<OWLClass>> hierarchy = dependencyHierarchy.get(cls);
			if(!hierarchy.isEmpty()){
				toPrint += cls + "\n";
				for(Integer i : hierarchy.keySet())
					toPrint += "  " + i + ":" + hierarchy.get(i) + "\n";
			}
			else
				emptyHierarchy.add(cls);
		}

		toPrint += "\nEmpty: " + emptyHierarchy;
		return toPrint;
	}

	public static void main(String[] args) {
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"interp/diff2.krss");
		DependencyHierarchy h = new DependencyHierarchy(ontology.getLogicalAxioms());
		System.out.println(h);


	}

}
