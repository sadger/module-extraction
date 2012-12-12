package checkers;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import axioms.AxiomSplitter;

public class LHSSigExtractor {
	private HashMap<OWLClass, Set<OWLClass>> dependencies;
	private Set<OWLClass> signatureDependencies = new HashSet<OWLClass>();


	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(HashMap<OWLClass, Set<OWLClass>> dependW, Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature){
		this.dependencies = dependW;
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		generateSignatureDependencies(signature);
		for(OWLLogicalAxiom axiom : ontology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(signature.contains(name) || isInSigDependencies(name))
				lhsSigT.add(axiom);
		}
		return lhsSigT;
	}

	private void generateSignatureDependencies(Set<OWLClass> signature) {
		for(OWLClass sigConcept : signature){
			Set<OWLClass> sigDeps = dependencies.get(sigConcept);
			if(sigDeps != null)
				signatureDependencies.addAll(sigDeps);
		}
	}

	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}
}
