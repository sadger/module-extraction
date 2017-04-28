package uk.ac.liv.moduleextraction.util;


import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;

public class SHIQOntologyVerifier {


    /*
     * Only the inclusions and equivalences can contain nominals so
     * check they are ALCQI which is most expressive they can be without nominals
     */
    public boolean isSHIQAxiom(OWLLogicalAxiom ax){
        AxiomType<?> type = ax.getAxiomType();
        if(type == AxiomType.SUBCLASS_OF || type == AxiomType.EQUIVALENT_CLASSES){
            ALCQIExpressionVerifier alcqiExpressionVerifier = new ALCQIExpressionVerifier();
            OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(ax);
            OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(ax);

            return lhs.accept(alcqiExpressionVerifier) && rhs.accept(alcqiExpressionVerifier);

        }
        else{
            //Anything else must be a SHIQ axiom
            return true;
        }
    }


    public boolean isSHIQOntology(Collection<OWLLogicalAxiom> ont) {
        return ont.stream().allMatch(e -> isSHIQAxiom(e));
    }

}
