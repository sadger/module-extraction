package uk.ac.liv.moduleextraction.signature;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;



public class DependencyHierarchy {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private HashMap<OWLClass, DependencySet> immediateDependencies = new HashMap<OWLClass, DependencySet>();
	private HashMap<OWLClass, HashMap<Integer, DependencySet>> dependencyHierarchy = 
			new HashMap<OWLClass, HashMap<Integer,DependencySet>>();

	public DependencyHierarchy(Set<OWLLogicalAxiom> logicalAxioms) {
		this.logicalAxioms = logicalAxioms;
		populateImmediateDependencies();
		generateHierarchy();
	}

	public DependencySet getDependencyForDepth(OWLClass cls, int depth){
		HashMap<Integer, DependencySet> hierarchy = dependencyHierarchy.get(cls);
		return hierarchy.get(depth); 
	}

	public int getMaxHierarchyDepth(OWLClass cls){
		HashMap<Integer, DependencySet> hierarchy = dependencyHierarchy.get(cls);
		if(hierarchy == null ||hierarchy.isEmpty())
			return 0;
		else
			return Collections.max(hierarchy.keySet());
	}
	
	private void populateImmediateDependencies() {
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms)){
			immediateDependencies.put(cls, new DependencySet());
		}
		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definiton = AxiomSplitter.getDefinitionofAxiom(axiom);
			immediateDependencies.put(name,ModuleUtils.convertToDependencySet(definiton.getSignature()));
		}
	}

	private void generateHierarchy(){
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms)){
			generateHierarchyForClass(cls);
		}
			
	}

	private void generateHierarchyForClass(OWLClass cls){
		HashMap<Integer, DependencySet> hierarchy = new HashMap<Integer, DependencySet>();
		DependencySet currentDependencies = immediateDependencies.get(cls);

		int depDepth = 1;

		while(!currentDependencies.isEmpty()){
			DependencySet newDeps = new DependencySet();

			for(OWLEntity dep : currentDependencies.asOWLEntities()){
				if(dep.isOWLClass())
					newDeps.addAll(immediateDependencies.get((OWLClass)dep));
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
			HashMap<Integer, DependencySet> hierarchy = dependencyHierarchy.get(cls);
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
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
		//OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"interp/diff.krss");
//		System.out.println(ontology);
		DependencyHierarchy h = new DependencyHierarchy(ontology.getLogicalAxioms());
//		System.out.println(h);
	}

}
