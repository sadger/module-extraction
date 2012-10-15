package checkers;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import temp.ontologyloader.OntologyLoader;


public class AxiomCheck {

	public static void main(String[] args) {
		
		ALCAxiomChecker ALCchecker = new ALCAxiomChecker();
		ELChecker ELchecker = new ELChecker();
	//OWLOntology ont = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/NCI/nci-09.03d.owl");
		OWLOntology ont = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/NCI/nci-10.02d.owl");

		
		System.out.println("NCI/nci-10.02d.owl");
		System.out.println("Axioms: " + ont.getLogicalAxiomCount() );
		int alcAxioms = 0;
		int otherCount = 0;
		for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){ 
			//System.out.println(axiomCount);
			if(!ELchecker.isELAxiom(axiom) && ALCchecker.isALCAxiom(axiom)){
				//System.out.println(axiom);
				alcAxioms++;
			}
			else if(!ELchecker.isELAxiom(axiom)){
				otherCount++;
			}
		}
		System.out.println();
		System.out.println("ALCI count " + alcAxioms);
		System.out.println("Other count " + otherCount);

	}

}
