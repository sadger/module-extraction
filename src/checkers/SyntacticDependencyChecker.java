package checkers;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public class SyntacticDependencyChecker {

	public SyntacticDependencyChecker() {
		//Do Nuffin'
	}

	public boolean hasSyntacticSigDependency(HashMap<OWLClass, Set<OWLClass>> dependW, Set<OWLClass> signature){
		boolean result = false;	
	
		for(OWLClass cls : signature){
			Set<OWLClass> classDeps = dependW.get(cls);
			if(!(classDeps == null)){
				classDeps.retainAll(signature);
				result = result || !classDeps.isEmpty();
			}

		}
		return result;
	}


}	


