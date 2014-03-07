package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.AxiomComparator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

/***
 * Definitorial depth calculator for ontologies with potentially 
 * shared names and repeated axioms. For repeated axioms 
 * repeated(A) = A <> C, A <> D is given the max depth of all 
 * such axioms depend(A <> X) max(repeated)  
 */
public class AxiomDefinitorialDepth {


	private Set<OWLLogicalAxiom> logicalAxioms;
	private HashMap<OWLClass, Integer> definitorialDepth;
	private HashMap<OWLClass, Set<OWLClass>> immediateDependencies;
	
	public AxiomDefinitorialDepth(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public int lookup(OWLClass cls) {
		return definitorialDepth.get(cls);
	}

	public AxiomDefinitorialDepth(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
		this.definitorialDepth = new HashMap<OWLClass, Integer>(axioms.size());
		this.immediateDependencies = new HashMap<OWLClass, Set<OWLClass>>();
		
		populateImmediateDependencies();
		generateDefinitorialDepths();
	}
	
	private void populateImmediateDependencies() {
		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definiton = AxiomSplitter.getDefinitionofAxiom(axiom);
			Set<OWLClass> currentDepedencies = immediateDependencies.get(name);
			if(currentDepedencies == null){
				immediateDependencies.put(name,definiton.getClassesInSignature());
			}
			else{
				currentDepedencies.addAll(definiton.getClassesInSignature());
			}
		}
	}
	
	private void generateDefinitorialDepths(){
		for(OWLClass name : ModuleUtils.getClassesInSet(logicalAxioms)){
			definitorialDepth.put(name, calculateDepth(name));
		}
	}
	
	private int calculateDepth(OWLClass name) {
		if(immediateDependencies.get(name) == null)
			return 0;
		else{
			HashSet<Integer> depths = new HashSet<Integer>();
			for(OWLClass dep : immediateDependencies.get(name)){
				if(definitorialDepth.get(dep) == null)
					depths.add(calculateDepth(dep));
				else
					depths.add(definitorialDepth.get(dep));
			}
			return 1 + Collections.max(depths);
		}	
	}

	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedList(){
		ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
		Collections.sort(sortedAxioms, new AxiomComparator(definitorialDepth));

		return sortedAxioms;
	}
	
	//If already defined just update the value to the MAX
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/axiomdep.krss");
		AxiomDefinitorialDepth d = new AxiomDefinitorialDepth(ont);
		System.out.println(d.immediateDependencies);
		for(OWLLogicalAxiom ax : d.getDefinitorialSortedList()){
			OWLClass cls = (OWLClass) AxiomSplitter.getNameofAxiom(ax);
			System.out.println(d.lookup(cls) + ":" + ax);
		}
	}


}
