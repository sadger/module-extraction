package checkers;



import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import axioms.AxiomSplitter;

public class LHSSigExtractor {
	private DefinitorialDependencies deps;
	private Set<OWLClass> signatureDependencies = new HashSet<OWLClass>();

	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature){
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		
		deps = new DefinitorialDependencies(ontology);
		generateSignatureDependencies(signature);
		
		for(OWLLogicalAxiom axiom : ontology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(signature.contains(name) || isInSigDependencies(name));
				lhsSigT.add(axiom);
		}
		return lhsSigT;
	}
	
	private void generateSignatureDependencies(Set<OWLClass> signature) {
		for(OWLClass sigConcept : signature){
			Set<OWLClass> sigDeps = deps.getDependenciesFor(sigConcept);
			if(sigDeps != null)
				signatureDependencies.addAll(sigDeps);
		}
	}
	
	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}
}
