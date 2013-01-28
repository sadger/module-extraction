package uk.ac.liv.moduleextraction.checkers;



import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;


public class LHSSigExtractor {
	
	private ChainDependencies dependencies = new ChainDependencies();
	private Set<OWLEntity> signatureDependencies = new HashSet<OWLEntity>();
	

	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(List<OWLLogicalAxiom> sortedOntology, Set<OWLEntity> signatureAndSigM){
		
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		
		dependencies.updateDependenciesWith(sortedOntology);
		generateSignatureDependencies(signatureAndSigM);
		

		for(OWLLogicalAxiom axiom : sortedOntology){
//			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
//			if(signatureAndSigM.contains(name) || isInSigDependencies(name))
				lhsSigT.add(axiom);
		}
		System.out.println("Size: " + lhsSigT.size());
		return lhsSigT;
	}

	private void generateSignatureDependencies(Set<OWLEntity> signature) {
		for(OWLEntity sigConcept : signature){
			DependencySet sigDeps = dependencies.get(sigConcept);
			if(sigDeps != null)
				signatureDependencies.addAll(sigDeps.asOWLEntities());
		}
	}

	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}
}
