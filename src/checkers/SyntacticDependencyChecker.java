package checkers;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public class SyntacticDependencyChecker {

	public SyntacticDependencyChecker() {
		//Do Nuffin'
	}

	public boolean hasSyntacticSigDependency(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature){
		boolean result = false;
		DefinitorialDependencies dependencies = new DefinitorialDependencies(ontology);
		//Dependencies deps = new Dependencies(ontology);
		
		for(OWLClass cls : signature){
			Set<OWLClass> classDeps = dependencies.getDependenciesFor(cls);
			if(!(classDeps == null)){
				classDeps.retainAll(signature);
				result = result || !classDeps.isEmpty();
			}

		}
		//System.out.println("Syntactic Dep: (Σ∪sig(M)=" + signature + "): " + result);
		dependencies.clearMappings();
		return result;
	}


}	


