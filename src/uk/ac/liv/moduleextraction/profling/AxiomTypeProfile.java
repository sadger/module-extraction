package uk.ac.liv.moduleextraction.profling;

import java.util.HashMap;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class AxiomTypeProfile {
	
	private HashMap<AxiomType<?>, Integer> typeMap = new HashMap<AxiomType<?>, Integer>();

	public AxiomTypeProfile(OWLOntology ontology) {
		
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			AxiomType<?> axiomType = axiom.getAxiomType();
			Integer count = typeMap.get(axiomType);
			
			if(count == null)
				typeMap.put(axiomType, 1);
			else
				typeMap.put(axiomType, ++count);
		}
	}
	
	public void printMetrics(){
		System.out.println("== Axiom Types ==");
		for(AxiomType<?> type : typeMap.keySet()){
			System.out.println(type.getName() + ":" + typeMap.get(type));
		}
	}

	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + 
				"Bioportal/NOTEL/Terminologies/Acyclic/Big/test.krss");
		
		System.out.println(ont);
		AxiomTypeProfile types = new AxiomTypeProfile(ont);
		System.out.println("Axioms: " + ont.getLogicalAxiomCount());
		types.printMetrics();
		ExpressionTypeProfiler typeyy = new ExpressionTypeProfiler();
		typeyy.profileOntology(ont);
		

	}
}
