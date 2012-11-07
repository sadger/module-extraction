package util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ontologyutils.AxiomSplitter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;



public class DefinitorialDepth {

	HashMap<OWLClass, Integer> definitorial = new HashMap<OWLClass, Integer>();
	HashMap<OWLClass, Set<OWLClass>> dependencies = new HashMap<OWLClass, Set<OWLClass>>();
	Set<OWLLogicalAxiom> logicalAxioms;
	ArrayList<OWLLogicalAxiom> sortedAxiomsByDefinitorialDepth;

	public DefinitorialDepth(OWLOntology ont) {
		new DefinitorialDepth(ont.getLogicalAxioms());
	}

	public DefinitorialDepth(Set<OWLLogicalAxiom> axioms){
		this.logicalAxioms = axioms;
		populateImmediateDependencies();
		calculateDefinitorialDepth(collectUndefinedClasses());
	}

	private void populateImmediateDependencies() {
		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definiton = AxiomSplitter.getDefinitionofAxiom(axiom);
			dependencies.put(name,definiton.getClassesInSignature());
		}
	}
	
	private HashSet<OWLClass> collectUndefinedClasses() {
		HashSet<OWLClass> undefinedClasses = new HashSet<OWLClass>();
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms)){
			if(!dependencies.containsKey(cls)){
				undefinedClasses.add(cls);
			}
			definitorial.put(cls, 0);
		}
		return undefinedClasses;
	}

	private void calculateDefinitorialDepth(HashSet<OWLClass> classes){
		HashSet<OWLClass> newClasses = new HashSet<OWLClass>();
		if(!classes.isEmpty()){
			for(OWLClass cls : classes){
				for(OWLClass definedConcept : dependencies.keySet()){
					if(dependencies.get(definedConcept).contains(cls)){
						newClasses.add(definedConcept);
						definitorial.put(definedConcept, definitorial.get(cls)+1);
					}
				}
			}
			calculateDefinitorialDepth(newClasses);
		}
	}

	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedList(){
		/* Don't resort if they have already been sorted */
		if(sortedAxiomsByDefinitorialDepth == null){
			ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
			Collections.sort(sortedAxioms, new AxiomComparator(definitorial));
			sortedAxiomsByDefinitorialDepth = sortedAxioms;
		}
		return sortedAxiomsByDefinitorialDepth;
	}


	public Integer lookup(OWLClass cls){
		return definitorial.get(cls);
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

}