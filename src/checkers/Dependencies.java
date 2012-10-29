package checkers;




import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ontologyutils.AxiomExtractor;
import ontologyutils.AxiomSplitter;
import ontologyutils.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import util.ModuleUtils;

public class Dependencies {

	private HashMap<OWLClass, HashSet<OWLClass>> dependencies = new HashMap<OWLClass, HashSet<OWLClass>>();
	private Set<OWLLogicalAxiom> ontology;
	AxiomExtractor extractor = new AxiomExtractor();

	public Dependencies(Set<OWLLogicalAxiom> ont) {
		/* We can only generate dependencies for inclusions or equalities */
		this.ontology = extractor.extractInclusionsAndEqualities(ont);
		initialiseMappings();
		populateFromOwnDefinitions();
		buildMappings();
	}
	

	public HashSet<OWLClass> getDependenciesFor(OWLClass name){
		return dependencies.get(name);
	}
	
	public boolean isEmpty(){
		return dependencies.isEmpty();
	}
	
	private void initialiseMappings(){
		for(OWLClass c : ModuleUtils.getClassesInSet(ontology)){
			dependencies.put(c, new HashSet<OWLClass>());
		}
	}
	
	private void populateFromOwnDefinitions(){
		for(OWLLogicalAxiom axiom : ontology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

			//Add class names in immediate definition to that classes dependencies
			dependencies.get(name).addAll(definition.getClassesInSignature());
		}
	}
	

	private void buildMappings() {
		for(OWLClass cls : ModuleUtils.getClassesInSet(ontology)){
			addImmediateDepsTo(cls, dependencies.get(cls));
		}
	}

	private void addImmediateDepsTo(OWLClass target, HashSet<OWLClass> classesWithDeps) {
		HashSet<OWLClass> toAdd = new HashSet<OWLClass>();
		for(OWLClass dep : classesWithDeps){
			toAdd.addAll(dependencies.get(dep));
		}
		
		dependencies.get(target).addAll(toAdd);
		
		//TODO report this error only once rather than for each concept name
		if(toAdd.contains(target)){
			System.err.println("Error - Input ontology is not acyclic");
		}
		else if(!toAdd.isEmpty()){
			addImmediateDepsTo(target, toAdd);
		}
		
	}


	public void clearMappings(){
		dependencies.clear();
	}

	@Override
	public String toString() {
		return dependencies.toString();
	}

	public static void main(String[] args) {
		OWLOntology o = OntologyLoader.loadOntology();
		Set<OWLLogicalAxiom> ontology = o.getLogicalAxioms();
		System.out.println(o);
		Dependencies deps = new Dependencies(ontology);
		System.out.println(deps);
	}

}
