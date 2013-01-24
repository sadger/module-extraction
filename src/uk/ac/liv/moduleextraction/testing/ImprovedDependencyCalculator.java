package uk.ac.liv.moduleextraction.testing;

import java.util.ArrayList;
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

import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class ImprovedDependencyCalculator {

	private HashMap<OWLClass, Integer> definitorialMap;
	private OWLDataFactory factory = OWLManager.getOWLDataFactory();
	private HashMap<OWLClass, Set<OWLEntity>> dependencies;
	private ArrayList<OWLLogicalAxiom> allAxiomsSorted;

	public ImprovedDependencyCalculator(OWLOntology ont) {
		this(ont.getLogicalAxioms());
	}

	public ImprovedDependencyCalculator(Set<OWLLogicalAxiom> axioms) {
		this.definitorialMap = new DefinitorialDepth(axioms).getDefinitorialMap();
		this.allAxiomsSorted = new ArrayList<OWLLogicalAxiom>(axioms);
		Collections.sort(allAxiomsSorted, new AxiomComparator(definitorialMap));
	}

	public HashMap<OWLClass, Set<OWLEntity>> getDependenciesFor(Set<OWLLogicalAxiom> subsetOfAxioms){
		dependencies = new HashMap<OWLClass, Set<OWLEntity>>();
		intialiseMappings(subsetOfAxioms);
		populateDependencies(subsetOfAxioms);
		
		return dependencies;
	}

	private void intialiseMappings(Set<OWLLogicalAxiom> axioms){
		for(OWLClass cls : ModuleUtils.getClassesInSet(axioms))
			dependencies.put(cls, new HashSet<OWLEntity>());
	}
	
	private void populateDependencies(Set<OWLLogicalAxiom> axiomstoSort){
		ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>();
		for(OWLLogicalAxiom axiom : allAxiomsSorted){
			if(axiomstoSort.contains(axiom)){
				sortedAxioms.add(axiom);
			}
			if(sortedAxioms.size() == axiomstoSort.size())
				break;
		}
	
		for(OWLLogicalAxiom axiom : sortedAxioms)
			addFromTop(axiom, dependencies);
		
	}
	
	private void addFromTop(OWLLogicalAxiom axiom, HashMap<OWLClass, Set<OWLEntity>> dependencies){
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		dependencies.put(name, definition.getSignature());

		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){
			Set<OWLEntity> clsDependencies = dependencies.get(cls);
			clsDependencies.remove(factory.getOWLThing());
			clsDependencies.remove(factory.getOWLNothing());
			if(clsDependencies != null)
				dependencies.get(name).addAll(clsDependencies);
		}
	}


}
