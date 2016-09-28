package uk.ac.liv.moduleextraction.checkers;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.axiomdependencies.DependencySet;
import uk.ac.liv.ontologyutils.axioms.AtomicLHSAxiomVerifier;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ExtendedLHSSigExtractor {

	private Set<OWLEntity> signatureDependencies = new HashSet<OWLEntity>();

	AxiomDependencies dependencies;
    AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();


	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(List<OWLLogicalAxiom> sortedOntology, Set<OWLEntity> sigUnionSigM, AxiomDependencies depends){

		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		this.dependencies = depends;

		generateSignatureDependencies(sortedOntology, sigUnionSigM);
		Set<OWLClass> sharedOrRepeated = getSharedOrRepeatedNames(sortedOntology);

		for(OWLLogicalAxiom axiom : sortedOntology){
			if(!verifier.isSupportedAxiom(axiom)){
				lhsSigT.add(axiom);
			}
			else{
				OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				if(sigUnionSigM.contains(name) || isInSigDependencies(name) || sharedOrRepeated.contains(name)){
					lhsSigT.add(axiom);
				}
			}

		}
		return lhsSigT;
	}


	private void generateSignatureDependencies(List<OWLLogicalAxiom> terminologyAxioms, Set<OWLEntity> signature) {
		AxiomStructureInspector inspector = new AxiomStructureInspector(terminologyAxioms);
		for(OWLEntity sigConcept : signature){

			Set<OWLLogicalAxiom> toCheck = new HashSet<OWLLogicalAxiom>();
			if(sigConcept instanceof OWLClass){
				toCheck.addAll(inspector.getDefinitions((OWLClass) sigConcept));
				toCheck.addAll(inspector.getPrimitiveDefinitions((OWLClass) sigConcept));
			}

			for(OWLLogicalAxiom axiom : toCheck){
				DependencySet sigDeps = dependencies.get(axiom);
				if(sigDeps != null){
					signatureDependencies.addAll(sigDeps);
				}
			}

		}
	}

	private boolean isInSigDependencies(OWLClass name){
		return signatureDependencies.contains(name);
	}

	private Set<OWLClass> getSharedOrRepeatedNames(List<OWLLogicalAxiom> terminologyAxioms){
		AxiomStructureInspector structInspector = new AxiomStructureInspector(terminologyAxioms);
		Set<OWLClass> sharedNames = structInspector.getSharedNames();
		Set<OWLClass> repeatedEquivalances = structInspector.getNamesWithRepeatedEqualities();

		Set<OWLClass> toAddToLhs = new HashSet<OWLClass>();
		toAddToLhs.addAll(sharedNames);
		toAddToLhs.addAll(repeatedEquivalances);


		return toAddToLhs;

	}



}
