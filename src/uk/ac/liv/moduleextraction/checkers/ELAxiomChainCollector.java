package uk.ac.liv.moduleextraction.checkers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class ELAxiomChainCollector {


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

