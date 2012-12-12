package testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import axioms.AxiomSplitter;

import util.AxiomComparator;
import util.DefinitorialDepth;
import util.ModulePaths;
import util.ModuleUtils;

public class DependencyCalculator {

	HashMap<OWLClass, Integer> definitorialMap;


	public DependencyCalculator(OWLOntology ont) {
		this(ont.getLogicalAxioms());
	}

	public DependencyCalculator(Set<OWLLogicalAxiom> axioms) {
		this.definitorialMap = new DefinitorialDepth(axioms).getDefinitorialMap();
	}

	public HashMap<OWLClass, Set<OWLClass>> getDependenciesFor(Set<OWLLogicalAxiom> subsetOfAxioms){
		HashMap<OWLClass, Set<OWLClass>> dependencies = new HashMap<OWLClass, Set<OWLClass>>();
		ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(subsetOfAxioms);
		Collections.sort(sortedAxioms, new AxiomComparator(definitorialMap));

		for(OWLClass cls : ModuleUtils.getClassesInSet(subsetOfAxioms))
			dependencies.put(cls, new HashSet<OWLClass>());

		for(OWLLogicalAxiom axiom : sortedAxioms){
			addFromTop(axiom, dependencies);
		}

		return dependencies;
	}

	private void addFromTop(OWLLogicalAxiom axiom,HashMap<OWLClass, Set<OWLClass>> dependencies) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		dependencies.put(name, definition.getClassesInSignature());

		for(OWLClass cls : definition.getClassesInSignature()){
			Set<OWLClass> clsDependencies = dependencies.get(cls);
			if(clsDependencies != null)
				dependencies.get(name).addAll(clsDependencies);
		}
	}


	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");

		DependencyCalculator deps = new DependencyCalculator(ont);

		for(int i=0; i<=4; i++){
			Set<OWLLogicalAxiom> randomOnt = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(),10);
			for(OWLLogicalAxiom ax : randomOnt)
				System.out.println(ax);
			deps.getDependenciesFor(randomOnt);
		}

	}

}
