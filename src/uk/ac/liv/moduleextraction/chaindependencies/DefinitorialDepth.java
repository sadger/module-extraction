package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.AxiomComparator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;




public class DefinitorialDepth {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private HashMap<OWLClass, Set<OWLClass>> dependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private HashMap<OWLClass, Integer> definitorialDepth = new HashMap<OWLClass, Integer>();
	private ArrayList<OWLLogicalAxiom> sortedAxiomsByDefinitorialDepth;

	/**
	 * Calculates Definitorial Depths for ACYCLIC terminologies
	 * do NOT run if not an acyclic terminology
	 * @param ontology
	 */
	public DefinitorialDepth(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	/**
	 * Calculates Definitorial Depths for ACYCLIC terminologies
	 * do NOT run if set of axioms are not an acyclic terminology
	 * @param axioms
	 */
	public DefinitorialDepth(Set<OWLLogicalAxiom> axioms){
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
	
	public HashMap<Integer, Set<OWLClass>> getDefinitorialDepthByNumber(){
		HashMap<Integer, Set<OWLClass>> 
		numberMap = new HashMap<Integer, Set<OWLClass>>();

		for(OWLClass cls : definitorialDepth.keySet()){
			int depSize = lookup(cls);

			if(numberMap.get(depSize) == null)
				numberMap.put(depSize, new HashSet<OWLClass>());

			numberMap.get(depSize).add(cls);
		}
		return numberMap;
	}
	
	
	public HashMap<OWLClass, Integer> getDefinitorialMap(){
		return definitorialDepth;
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
	


	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "interp/diff2.krss");
		DefinitorialDepth d = new DefinitorialDepth(ont);
		for(OWLLogicalAxiom ax : d.getDefinitorialSortedList()){
			OWLClass cls = (OWLClass) AxiomSplitter.getNameofAxiom(ax);
			System.out.println(d.lookup(cls) + ":" + ax);
		}
	
	}
}
