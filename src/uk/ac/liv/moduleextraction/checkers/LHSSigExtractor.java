package uk.ac.liv.moduleextraction.checkers;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;


public class LHSSigExtractor {
	private HashMap<OWLClass, Set<OWLEntity>> dependencies;
	private Set<OWLEntity> signatureDependencies = new HashSet<OWLEntity>();


	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(HashMap<OWLClass, Set<OWLEntity>> dependW, Set<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM){
		this.dependencies = dependW;
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		generateSignatureDependencies(signatureAndSigM);
		for(OWLLogicalAxiom axiom : ontology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(signatureAndSigM.contains(name) || isInSigDependencies(name))
				lhsSigT.add(axiom);
		}
		return lhsSigT;
	}

	private void generateSignatureDependencies(Set<OWLEntity> signature) {
		for(OWLEntity sigConcept : signature){
			Set<OWLEntity> sigDeps = dependencies.get(sigConcept);
			if(sigDeps != null)
				signatureDependencies.addAll(sigDeps);
		}
	}

	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}
}
