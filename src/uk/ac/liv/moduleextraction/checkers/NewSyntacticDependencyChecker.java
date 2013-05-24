package uk.ac.liv.moduleextraction.checkers;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class NewSyntacticDependencyChecker {
	
	public boolean hasSyntacticSigDependency(OWLLogicalAxiom chosenAxiom, ChainDependencies dependsW, Set<OWLEntity> signatureAndSigM){
		
		OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(chosenAxiom);
		
		boolean result = false;


		if(!signatureAndSigM.contains(axiomName)){
			return result;
		}
		else{
			HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependsW.get(axiomName).asOWLEntities());
			intersect.retainAll(signatureAndSigM);
			
			if(!intersect.isEmpty()){
				result = true;
			}
			return result;
		
		}

	}

}