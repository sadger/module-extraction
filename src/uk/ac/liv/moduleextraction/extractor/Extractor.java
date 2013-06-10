package uk.ac.liv.moduleextraction.extractor;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public interface Extractor {

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature);
	
}
