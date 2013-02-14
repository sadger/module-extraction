package uk.ac.liv.moduleextraction.util;


import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.terminology.TerminologyChecker;


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
		OWLOntology nci1 = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.10e-alc-no-disjoint.owl");
		System.out.println(ModuleUtils.getClassAndRoleNamesInSet(nci1.getLogicalAxioms()).size());
	}
}
