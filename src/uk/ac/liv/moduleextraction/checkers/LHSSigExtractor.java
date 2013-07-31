package uk.ac.liv.moduleextraction.checkers;



import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.moduleextraction.extractor.SemanticRuleExtractor.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;


public class LHSSigExtractor {
	
	private Set<OWLEntity> signatureDependencies = new HashSet<OWLEntity>();
	
	ChainDependencies dependencies;
	
	
	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(boolean[] terminology,
			DefinitorialAxiomStore axiomStore, Set<OWLEntity> sigUnionSigM, ChainDependencies dependT) {

		this.dependencies = dependT;
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		generateSignatureDependencies(sigUnionSigM);
		
		for (int i = 0; i < terminology.length; i++) {
			if(terminology[i]){
				OWLLogicalAxiom axiom = axiomStore.getAxiom(i);
				OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				if(sigUnionSigM.contains(name) || isInSigDependencies(name)){
					lhsSigT.add(axiom);
				}
			}
		}
		
		
		return lhsSigT;
	}

	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(List<OWLLogicalAxiom> sortedOntology, 
			Set<OWLEntity> signatureAndSigM, ChainDependencies depends){
		
		this.dependencies = depends;
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		generateSignatureDependencies(signatureAndSigM);
		
		
		for(OWLLogicalAxiom axiom : sortedOntology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(signatureAndSigM.contains(name) || isInSigDependencies(name))
				lhsSigT.add(axiom);
		}
		return lhsSigT;
	}

	private void generateSignatureDependencies(Set<OWLEntity> signature) {
		for(OWLEntity sigConcept : signature){
			DependencySet sigDeps = dependencies.get(sigConcept);
			if(sigDeps != null)
				signatureDependencies.addAll(sigDeps);
		}
	}

	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}


}
