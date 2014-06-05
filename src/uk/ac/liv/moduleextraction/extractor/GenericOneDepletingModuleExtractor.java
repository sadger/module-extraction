package uk.ac.liv.moduleextraction.extractor;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;

public class GenericOneDepletingModuleExtractor implements Extractor {

	Extractor extractor;
	public GenericOneDepletingModuleExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public GenericOneDepletingModuleExtractor(Set<OWLLogicalAxiom> axioms){
		OntologyCycleVerifier verifier = new OntologyCycleVerifier(axioms);
		if(verifier.isCyclic()){
			extractor = new CyclicOneDepletingModuleExtractor(axioms);
		}
		else{
			extractor = new OneDepletingModuleExtractor(axioms);
		}
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractor.extractModule(signature);
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(
			Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		return extractor.extractModule(existingModule, signature);
	}



}
