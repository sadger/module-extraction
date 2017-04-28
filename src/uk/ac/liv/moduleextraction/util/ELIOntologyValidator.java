package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Collection;

/**
 * Created by william on 28/04/17.
 */
public class ELIOntologyValidator {


    public boolean isELIAxiom(OWLLogicalAxiom ax){
        AxiomType<?> type = ax.getAxiomType();
        if(type == AxiomType.SUBCLASS_OF || type == AxiomType.EQUIVALENT_CLASSES){
            ELIExpressionValidator eliExpressionValidator = new ELIExpressionValidator();
            OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(ax);
            OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(ax);

            return lhs.accept(eliExpressionValidator) && rhs.accept(eliExpressionValidator);
        }
        else{
            //Anything else can't be an EL Axiom
            return false;
        }
    }


    public boolean isELIOntology(Collection<OWLLogicalAxiom> ont){
        return ont.stream().allMatch(e -> isELIAxiom(e));
    }



}


