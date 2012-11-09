package checkers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import axioms.AxiomExtractor;
import axioms.AxiomSplitter;

import util.ModulePaths;
import util.ModuleUtils;
import util.NewDefinitorialDepth;


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
	
	public HashMap<OWLClass, Set<OWLClass>> getDependencies() {
		return dependencies;
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
		NewDefinitorialDepth definitorialDepth = new NewDefinitorialDepth(logicalAxioms);
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
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/nci-03.10j.owl");
		System.out.println("Ontology Loaded");
		DefinitorialDependencies def = new DefinitorialDependencies(ont.getLogicalAxioms());
//		for(OWLLogicalAxiom ax : ont.getLogicalAxioms())
//			System.out.println(ax);
		for(OWLClass cls : ont.getClassesInSignature()){
			System.out.println(cls + ":" + def.getDependenciesFor(cls));
		}
	}
	
}
