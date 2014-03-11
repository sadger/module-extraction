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
import uk.ac.liv.ontologyutils.axioms.FullAxiomComparator;
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
	private HashMap<OWLLogicalAxiom, Integer> definitorialDepth;
	private HashMap<OWLClass, Set<OWLClass>> immediateDependencies;
	private int max = 0;
	private Set<OWLLogicalAxiom> expressiveAxioms;

	public AxiomDefinitorialDepth(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public int lookup(OWLLogicalAxiom ax) {
		return definitorialDepth.get(ax);
	}

	public AxiomDefinitorialDepth(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
		this.definitorialDepth = new HashMap<OWLLogicalAxiom, Integer>(axioms.size());
		this.immediateDependencies = new HashMap<OWLClass, Set<OWLClass>>();
		this.expressiveAxioms = new HashSet<OWLLogicalAxiom>();
		populateImmediateDependencies();
		generateDefinitorialDepths();
		assignExpressiveAxiomsValue();

	}
	
	public ArrayList<OWLLogicalAxiom> getDefinitorialSortedList(){
		ArrayList<OWLLogicalAxiom> sortedAxioms = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
		Collections.sort(sortedAxioms, new FullAxiomComparator(definitorialDepth));

		return sortedAxioms;
	}

	private void populateImmediateDependencies() {
		for(OWLLogicalAxiom axiom : logicalAxioms){
			if(ModuleUtils.isInclusionOrEquation(axiom)){
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
			else{
				expressiveAxioms.add(axiom);
			}
		}
		
	}

	private void generateDefinitorialDepths(){
		for(OWLLogicalAxiom axiom : logicalAxioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			definitorialDepth.put(axiom, calculateDepth(name));
		}
	}

	private int calculateDepth(OWLClass name) {
		if(immediateDependencies.get(name) == null)
			return 0;
		else{
			HashSet<Integer> depths = new HashSet<Integer>();
			for(OWLClass dep : immediateDependencies.get(name)){
				if(definitorialDepth.get(dep) == null){
					depths.add(calculateDepth(dep));
				}
				else{
					depths.add(definitorialDepth.get(dep));
				}
			}
			int result =  1 + Collections.max(depths);
			max = Math.max(max, result);
			
			return result;
		}	
	}

	/*
	 * Expressive axioms often cannot be realised in terms of definitioral depth
	 * (role inclusions) or can create depth cycles (disjointness axioms). So
	 * we assign any non-inclusion or equation to be MAX+1 depth of any other
	 * axiom in the ontology;
	 */
	private void assignExpressiveAxiomsValue() {
		int expressiveValue = max + 1;
		for(OWLLogicalAxiom axiom : expressiveAxioms){
			definitorialDepth.put(axiom, expressiveValue);
		}
	}



}
