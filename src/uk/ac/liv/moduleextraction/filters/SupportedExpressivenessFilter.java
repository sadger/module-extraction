package uk.ac.liv.moduleextraction.filters;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.util.AtomicLHSAxiomVerifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SupportedExpressivenessFilter implements SupportedFilter {

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Collection<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();
		unsupported.addAll(axioms.stream().filter(axiom -> !verifier.isSupportedAxiom(axiom)).collect(Collectors.toSet()));
		return unsupported;
	}

}
