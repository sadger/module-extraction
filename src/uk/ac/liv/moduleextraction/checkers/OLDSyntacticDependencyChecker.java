package uk.ac.liv.moduleextraction.checkers;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class OLDSyntacticDependencyChecker {
	
	Set<OWLLogicalAxiom> axiomsWithDeps;

	public boolean hasSyntacticSigDependency(LinkedHashList<OWLLogicalAxiom> W, ChainDependencies dependsW, Set<OWLEntity> signatureAndSigM){
		
		OWLLogicalAxiom lastAdded = W.getLast();

		OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(lastAdded);
		axiomsWithDeps = new HashSet<OWLLogicalAxiom>();
		
		boolean result = false;

		if(!signatureAndSigM.contains(axiomName))
			return result;
		else{
			HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependsW.get(axiomName).asOWLEntities());
			intersect.retainAll(signatureAndSigM);

			if(!intersect.isEmpty()){
				signatureAndSigM.addAll(lastAdded.getSignature());
				findAxiomChains(W,dependsW,signatureAndSigM);
				result = true;
//				System.out.println("Intersect: " + intersect);
				axiomsWithDeps.add(lastAdded);
				dependsW.clear();
			}
			return result;
		
		}

	}
	
	public Set<OWLLogicalAxiom> getAxiomsWithDependencies(){
		return axiomsWithDeps;
	}
	
	private void findAxiomChains(LinkedHashList<OWLLogicalAxiom> W, ChainDependencies dependsW, Set<OWLEntity> signatureAndSigM){
		ListIterator<OWLLogicalAxiom> endIterator = W.listIterator(W.size()-1);
		while(endIterator.hasPrevious()){
			OWLLogicalAxiom axiom = endIterator.previous();
			OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			
			if(!signatureAndSigM.contains(axiomName)){
				//Do nothing
			}
			else{
				HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependsW.get(axiomName).asOWLEntities());
				intersect.retainAll(signatureAndSigM);
				if(!intersect.isEmpty()){
					axiomsWithDeps.add(axiom);
					signatureAndSigM.addAll(axiom.getSignature());
				}
			}
		}

	}

}