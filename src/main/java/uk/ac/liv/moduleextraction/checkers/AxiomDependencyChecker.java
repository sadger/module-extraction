package uk.ac.liv.moduleextraction.checkers;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;

import java.util.HashSet;
import java.util.Set;

public class AxiomDependencyChecker {
	
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