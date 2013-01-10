package uk.ac.liv.moduleextraction.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class DependencyCalculator {

	HashMap<OWLClass, Integer> definitorialMap;


	public DependencyCalculator(OWLOntology ont) {
		this(ont.getLogicalAxioms());
	}

	public DependencyCalculator(Set<OWLLogicalAxiom> axioms) {
		this.definitorialMap = new DefinitorialDepth(axioms).getDefinitorialMap();
	}

	public HashMap<OWLClass, Set<OWLEntity>> getDependenciesFor(Set<OWLLogicalAxiom> subsetOfAxioms){
		
		HashMap<OWLClass, Set<OWLEntity>> dependencies = new HashMap<OWLClass, Set<OWLEntity>>();
		ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(subsetOfAxioms);
		Collections.sort(sortedAxioms, new AxiomComparator(definitorialMap));
		

		for(OWLClass cls : ModuleUtils.getClassesInSet(subsetOfAxioms))
			dependencies.put(cls, new HashSet<OWLEntity>());

		for(OWLLogicalAxiom axiom : sortedAxioms)
			addFromTop(axiom, dependencies);
		

		return dependencies;
	}

	private void addFromTop(OWLLogicalAxiom axiom,HashMap<OWLClass, Set<OWLEntity>> dependencies) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		dependencies.put(name, definition.getSignature());

		for(OWLClass cls : definition.getClassesInSignature()){
			Set<OWLEntity> clsDependencies = dependencies.get(cls);
			if(clsDependencies != null)
				dependencies.get(name).addAll(clsDependencies);
		}
	}


	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "interp/diff.krss");

		DependencyCalculator deps = new DependencyCalculator(ont);
		HashMap<OWLClass, Set<OWLEntity>> dependencies = deps.getDependenciesFor(ont.getLogicalAxioms());
		
		for(OWLClass cls : ont.getClassesInSignature()){
			System.out.println(cls + ":" + dependencies.get(cls));
		}

		

	}

}
