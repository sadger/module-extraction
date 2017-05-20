package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;

/**
 * Created by william on 28/04/17.
 */
public class ALCQIOntologyVerifier {

    public boolean isALCQIAxiom(OWLLogicalAxiom ax){
        AxiomType<?> type = ax.getAxiomType();
        if(type == AxiomType.SUBCLASS_OF || type == AxiomType.EQUIVALENT_CLASSES){
            ALCQIExpressionVerifier alcqiExpressionVerifier = new ALCQIExpressionVerifier();
            OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(ax);
            OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(ax);

            return lhs.accept(alcqiExpressionVerifier) && rhs.accept(alcqiExpressionVerifier);

        }
        else{
            //Anything else can't be an ALCQI axiom
            return false;
        }
    }


    public boolean isALCQIOntology(Collection<OWLLogicalAxiom> ont){
        return ont.stream().allMatch(e -> isALCQIAxiom(e));
    }
}
