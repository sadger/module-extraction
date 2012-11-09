package util;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLOntology;

import terminology.TerminologyChecker;


public class AcyclicTerminologyChecker {
	
	private OWLOntology ontology;

	public AcyclicTerminologyChecker(OWLOntology ont) {
		this.ontology = ont;
	}
	
	public boolean isAcyclicTerminology(){
		AcyclicChecker acyclic =  new AcyclicChecker();
		TerminologyChecker terminology = new TerminologyChecker();
		return acyclic.isAcyclic(ontology) && terminology.isTerminology(ontology);
	}
	
	public static void main(String[] args) {
		OWLOntology nci1 = OntologyLoader.loadOntology("/home/william/PhD/Ontologies/interp/diff.krss");
		AcyclicTerminologyChecker checker = new AcyclicTerminologyChecker(nci1);
		System.out.println(checker.isAcyclicTerminology());
	}
}
