package checkers;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public class SyntacticDependencyChecker {

	public SyntacticDependencyChecker() {
		//Do Nuffin'
	}

	public boolean hasSyntacticSigDependency(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature){
		//Ensure the names in the signature are in the same namespace
		//remapOntologyIRI(ontology,signature);
		boolean result = false;
		Dependencies deps = new Dependencies(ontology);
		
		for(OWLClass cls : signature){
			HashSet<OWLClass> classDeps = deps.getDependenciesFor(cls);
			if(!(classDeps == null)){
				classDeps.retainAll(signature);
				result = result || !classDeps.isEmpty();
			}

		}
		//System.out.println(deps);
		//System.out.println("Syntactic Dep: (Σ∪sig(M)=" + signature + "): " + result);
		deps.clearMappings();
		return result;
	}


}	


