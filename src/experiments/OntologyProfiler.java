package experiments;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Set;

import loader.OntologyLoader;


import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import util.ModulePaths;

import axioms.AxiomExtractor;

import checkers.DefinitorialDependencies;

public class OntologyProfiler {

	HashMap<OWLClass, Integer> depSizes =  new HashMap<OWLClass, Integer>();
	OWLOntology ontology;
	DefinitorialDependencies dependencies;
	AxiomExtractor extractor = new AxiomExtractor();
	NumberFormat nf = NumberFormat.getInstance();

	public OntologyProfiler(OWLOntology ont) {
		this.ontology = ont;
		
		this.dependencies = new DefinitorialDependencies
				(extractor.extractInclusionsAndEqualities(ontology.getLogicalAxioms()));
	}
	
	public void showProfile(){
		System.out.println("=== Ontology ===");
		printOntologyInformation();
		System.out.println();
		
		System.out.println("=== Axioms ===");
		printAxiomInformation();
		System.out.println();
		
		System.out.println("== Dependencies ==");
		printDependenciesInformation();
		System.out.println();
	}

	private void printOntologyInformation() {
		System.out.println("Name: " + ontology.getOntologyID().toString());
		System.out.println("Axioms: " + nf.format(ontology.getAxiomCount()));
		System.out.println("Logical Axioms " + nf.format(ontology.getLogicalAxiomCount()));
		System.out.println("Classes in Signature: " + nf.format(ontology.getClassesInSignature().size()));
	}

	private void printDependenciesInformation() {

		Set<OWLClass> classesInSig = ontology.getClassesInSignature();
		HashMap<Integer, Integer> sizeInformation= new HashMap<Integer, Integer>();
		Integer[] sizesToCheck = {100,200,300,400,500,550};
		

		int max = 0;
		int total = 0;
		int empty = 0;

		for(OWLClass cls : classesInSig){
			int depSize = dependencies.getDependenciesFor(cls).size();
			total += depSize;

			if(depSize > max)
				max = depSize;
			
			if(depSize == 0)
				empty++;
			else{
				for(Integer size : sizesToCheck){
					if(depSize >= size){
						if(sizeInformation.get(size) == null)
							sizeInformation.put(size, 1);
						else
							sizeInformation.put(size, sizeInformation.get(size)+1);
					}
				}
			}
		}

		System.out.println("Max dep size: " + nf.format(max));
		System.out.println("Average dep size: " + nf.format(total/classesInSig.size()));
		System.out.println("No dependencies (undefined concepts): " + nf.format(empty));
		System.out.println("Size of dependencies:");
		for(Integer size : sizesToCheck)
			System.out.println("\t >= " + size + ": " + nf.format(sizeInformation.get(size)));

	}
	
	private void printAxiomInformation() {
		int definedConcepts, primitiveConcepts,domainOrRange, other, disjoint;
		HashMap<String,Integer> otherTypes = new HashMap<String, Integer>();
		definedConcepts = primitiveConcepts = domainOrRange = other = disjoint = 0;
		
		for(OWLAxiom axiom : ontology.getAxioms()){
			AxiomType<?> type = axiom.getAxiomType();
			
			if(type == AxiomType.SUBCLASS_OF)
				primitiveConcepts++;
			else if(type == AxiomType.EQUIVALENT_CLASSES)
				definedConcepts++;
			else if(type == AxiomType.OBJECT_PROPERTY_DOMAIN || type == AxiomType.OBJECT_PROPERTY_RANGE)
				domainOrRange++;
			else if(type == AxiomType.DISJOINT_CLASSES)
				disjoint++;
			else{
				other++;
				String typeValue = type.toString();
				if(otherTypes.get(typeValue) == null)
					otherTypes.put(typeValue, 1);
				else{
					otherTypes.put(typeValue, otherTypes.get(typeValue)+1);
				}
			}
			
		}
		
		System.out.println("Defined concept definitions: " + nf.format(definedConcepts));
		System.out.println("Primitive concept definitions: " + nf.format(primitiveConcepts));
		System.out.println("Domain OR Range restriction: " + nf.format(domainOrRange));
		System.out.println("Disjoint Assertions: " + nf.format(disjoint));
		
		if(!otherTypes.isEmpty()){
			System.out.println("Other: " + nf.format(other));
			for(String typeName : otherTypes.keySet()){
				System.out.println("\t" + typeName + ": " + nf.format(otherTypes.get(typeName)));
			}
		}
	}
	

	public static void main(String[] args) {
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"NCI/nci-08.09d-terminology.owl");
		OntologyProfiler profiler = new OntologyProfiler(ontology);
		profiler.showProfile();
	}

}
