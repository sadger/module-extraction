package uk.ac.liv.moduleextraction.checkers;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class SyntacticDependencyChecker {
	
	public boolean hasSyntacticSigDependency(OWLLogicalAxiom chosenAxiom, AxiomDependencies dependT, Set<OWLEntity> signatureAndSigM){
		
		OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(chosenAxiom);
		
		boolean result = false;


		if(!signatureAndSigM.contains(axiomName)){
			return result;
		}
		else{
			HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependT.get(chosenAxiom));
			intersect.retainAll(signatureAndSigM);
			
			if(!intersect.isEmpty()){
				result = true;
			}
			return result;
		
		}

	}

}