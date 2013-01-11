package uk.ac.liv.moduleextraction.checkers;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

public class SyntacticDependencyChecker {

	public SyntacticDependencyChecker() {
		//Do Nuffin'
	}

	public boolean hasSyntacticSigDependency(HashMap<OWLClass, Set<OWLEntity>> dependW, Set<OWLEntity> signatureAndSigM){
		boolean result = false;	
			
		for(OWLEntity cls : signatureAndSigM){
			Set<OWLEntity> classDeps = dependW.get(cls);
			if(!(classDeps == null)){
				classDeps.retainAll(signatureAndSigM);
				//System.out.print((classDeps.isEmpty()) ? "" : "Syntactic dep on: " + classDeps + "\n");
				result = result || !classDeps.isEmpty();
			}

		}
		return result;
	}


}	


