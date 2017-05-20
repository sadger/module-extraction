package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

public class UnsupportedAxiomException extends Exception {
    private static final long serialVersionUID = 619549638995516601L;

    OWLLogicalAxiom axiom;

    public UnsupportedAxiomException(OWLLogicalAxiom axiom) {
        this.axiom = axiom;
    }
    @Override
    public String getMessage() {
        return "Cannot convert axiom " + axiom + "of type " + axiom.getAxiomType() + "to propositional";
    }
}
