package uk.ac.liv.moduleextraction.checkers;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.axiomdependencies.DefinitorialAxiomStore;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ELValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ELAxiomChainCollector {

	private int syntacticChecks = 0;

	public ArrayList<OWLLogicalAxiom> collectELAxiomChain(boolean[] terminology, int currentIndex, 
			DefinitorialAxiomStore axiomStore, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM) {

		ArrayList<OWLLogicalAxiom> chain = new ArrayList<OWLLogicalAxiom>();

		for (int i = currentIndex; i >= 0; i--) {

			OWLLogicalAxiom axiom = axiomStore.getAxiom(i);

			if(terminology[i]){
				if(hasELSyntacticDependency(axiom, dependT, sigUnionSigM)){

					chain.add(axiom);
					//Update signature
					sigUnionSigM.addAll(axiom.getSignature());
					//Remove from ontology
					terminology[i] = false;

				}
			}
		}

		return chain;

	}

	public int getSyntacticChecks() {
		return syntacticChecks;
	}

	public void resetSyntacticChecks(){
		syntacticChecks = 0;
	}

	public ArrayList<OWLLogicalAxiom> collectELAxiomChain(List<OWLLogicalAxiom> allAxioms, int currentIndex,
			boolean[] terminology, DefinitorialAxiomStore axiomStore, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM) {

		ArrayList<OWLLogicalAxiom> chain = new ArrayList<OWLLogicalAxiom>();

		for (int i = currentIndex; i >= 0; i--) {

			OWLLogicalAxiom axiom = allAxioms.get(i);

			if(hasELSyntacticDependency(axiom, dependT, sigUnionSigM)){

				chain.add(axiom);
				//Update signature
				sigUnionSigM.addAll(axiom.getSignature());
				//Remove from ontology
				axiomStore.removeAxiom(terminology, axiom);

			}

		}

		return chain;
	}


	public boolean hasELSyntacticDependency(OWLLogicalAxiom axiom, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM){
		OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		ELValidator validator = new ELValidator();
		syntacticChecks++;
		if(!validator.isELAxiom(axiom) || !sigUnionSigM.contains(axiomName)){
			return false;
		}
		else{
			HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependT.get(axiom));
			intersect.retainAll(sigUnionSigM);

			return !intersect.isEmpty();
		}

	}


}

