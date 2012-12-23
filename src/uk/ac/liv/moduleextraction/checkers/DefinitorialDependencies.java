package uk.ac.liv.moduleextraction.checkers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class DefinitorialDependencies {
	
	private AxiomExtractor extractor; 
	private HashMap<OWLClass, Set<OWLClass>> dependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private Set<OWLLogicalAxiom> logicalAxioms;
	
	public DefinitorialDependencies(Set<OWLLogicalAxiom> axioms) {
		this.extractor = new AxiomExtractor();
		/* You can only generate dependencies for inclusions and equalities */
		this.logicalAxioms = extractor.extractInclusionsAndEqualities(axioms);
		initialseMappings();
		calculateDependencies();
	}
	
	public HashMap<OWLClass, Set<OWLClass>> getDependencyMap() {
		return dependencies;
	}
	
	public HashMap<Integer, Set<OWLClass>> getDependenciesByNumber(){
		HashMap<Integer, Set<OWLClass>> 
		numberMap = new HashMap<Integer, Set<OWLClass>>();

		for(OWLClass cls : dependencies.keySet()){
			int depSize = getDependenciesFor(cls).size();

			if(numberMap.get(depSize) == null)
				numberMap.put(depSize, new HashSet<OWLClass>());

			numberMap.get(depSize).add(cls);
		}
		return numberMap;
	}
	
	public void clearMappings(){
		dependencies.clear();
	}
	
	public Set<OWLClass> getDependenciesFor(OWLClass cls) {
		return dependencies.get(cls);
	}
	
	private void initialseMappings() {
		for(OWLClass cls : ModuleUtils.getClassesInSet(logicalAxioms))
			dependencies.put(cls, new HashSet<OWLClass>());
	}
	
	private void calculateDependencies(){
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(logicalAxioms);
		for(OWLLogicalAxiom axiom : definitorialDepth.getDefinitorialSortedList())
			addFromTop(axiom);
	}
	
	private void addFromTop(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		dependencies.put(name, definition.getClassesInSignature());
		
		for(OWLClass cls : definition.getClassesInSignature()){
			Set<OWLClass> clsDependencies = dependencies.get(cls);
			if(clsDependencies != null)
				dependencies.get(name).addAll(clsDependencies);
		}
	}
	
	
	
	
}
