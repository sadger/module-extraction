package uk.ac.liv.moduleextraction.checkers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class ChainAxiomCollector {

	NewSyntacticDependencyChecker checker = new NewSyntacticDependencyChecker();
	public ChainAxiomCollector() {
		
	}
	
	public Set<OWLLogicalAxiom> collectAxiomChain(ListIterator<OWLLogicalAxiom> positionInTerminology, ChainDependencies dependencies, Set<OWLEntity> signatureAndSigM){
		HashSet<OWLLogicalAxiom> axiomChain = new HashSet<OWLLogicalAxiom>();
		while(positionInTerminology.hasPrevious()){
			OWLLogicalAxiom axiom = positionInTerminology.previous();

			if(checker.hasSyntacticSigDependency(axiom, dependencies, signatureAndSigM)){
				axiomChain.add(axiom);
				signatureAndSigM.addAll(axiom.getSignature());
			}
		}
		return axiomChain;
	}
	
}
