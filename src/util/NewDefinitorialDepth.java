package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ontologyutils.AxiomSplitter;
import ontologyutils.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


public class NewDefinitorialDepth {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private HashMap<OWLClass, Set<OWLClass>> dependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private HashMap<OWLClass, Integer> definitorialDepth = new HashMap<OWLClass, Integer>();
	private ArrayList<OWLLogicalAxiom> sortedAxiomsByDefinitorialDepth;

	/**
	 * Calculates Definitorial Depths for ACYCLIC terminologies
	 * do NOT run if not an acyclic terminology
	 * @param ontology
	 */
	public NewDefinitorialDepth(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	/**
	 * Calculates Definitorial Depths for ACYCLIC terminologies
	 * do NOT run if set of axioms are not an acyclic terminology
	 * @param axioms
	 */
	public NewDefinitorialDepth(Set<OWLLogicalAxiom> axioms){
		this.logicalAxioms = axioms;
		populateImmediateDependencies();
		generateDefinitorialDepths();
	}
	
	public Integer lookup(OWLClass cls){
		return definitorialDepth.get(cls);
	}
	
	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedList(){
		/* Don't resort if they have already been sorted */
		if(sortedAxiomsByDefinitorialDepth == null){
			ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
			Collections.sort(sortedAxioms, new AxiomComparator(definitorialDepth));
			sortedAxiomsByDefinitorialDepth = sortedAxioms;
		}
		return sortedAxiomsByDefinitorialDepth;
	}
	

	private void populateImmediateDependencies() {
		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definiton = AxiomSplitter.getDefinitionofAxiom(axiom);
			dependencies.put(name,definiton.getClassesInSignature());
		}
	}

	private void generateDefinitorialDepths(){
		for(OWLClass name : ModuleUtils.getClassesInSet(logicalAxioms))
			definitorialDepth.put(name, calculateDepth(name));
	}

	private int calculateDepth(OWLClass name) {
		if(dependencies.get(name) == null)
			return 0;
		else{
			HashSet<Integer> depths = new HashSet<Integer>();
			for(OWLClass dep : dependencies.get(name)){
				if(definitorialDepth.get(dep) == null)
					depths.add(calculateDepth(dep));
				else
					depths.add(definitorialDepth.get(dep));
			}
			return 1 + Collections.max(depths);
		}	
	}
	

	private static class AxiomComparator implements Comparator<OWLLogicalAxiom>{
		private Map<OWLClass, Integer> base;

		public AxiomComparator(Map<OWLClass, Integer> base) {
			this.base = base;
		}
		@Override
		public int compare(OWLLogicalAxiom axiom1, OWLLogicalAxiom axiom2) {
			OWLClass name1 = (OWLClass) AxiomSplitter.getNameofAxiom(axiom1);
			OWLClass name2 = (OWLClass) AxiomSplitter.getNameofAxiom(axiom2);
			if(base.get(name1) < base.get(name2)) 
				return -1;
			else if(name1 == name2) 
				return 0;
			else 
				return 1;
		}

	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "interp/diff.krss");
		System.out.println("Ontology Loaded");
		NewDefinitorialDepth d = new NewDefinitorialDepth(ont);
		for(OWLLogicalAxiom ax : d.getDefinitorialSortedList()){
			System.out.println(d.lookup((OWLClass) AxiomSplitter.getNameofAxiom(ax)) + ":" + ax);
		}
	}
}
