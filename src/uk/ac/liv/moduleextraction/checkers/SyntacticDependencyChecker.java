package uk.ac.liv.moduleextraction.checkers;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class SyntacticDependencyChecker {
	
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

}	


