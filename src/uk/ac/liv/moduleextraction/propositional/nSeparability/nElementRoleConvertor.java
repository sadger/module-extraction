package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.propositional.formula.BooleanAtom;
import uk.ac.liv.moduleextraction.propositional.formula.NamedAtom;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

public class nElementRoleConvertor implements OWLPropertyExpressionVisitorEx<PropositionalFormula> {

    private int first;
    private int second;

    public nElementRoleConvertor(int firstElem, int secondElem){
        this.first = firstElem;
        this.second = secondElem;
    }

    @Override
    public PropositionalFormula visit(OWLObjectProperty owlObjectProperty) {
        if(owlObjectProperty.isOWLTopObjectProperty()){
            return new BooleanAtom(true);
        }
        else if(owlObjectProperty.isOWLBottomObjectProperty()){
            return new BooleanAtom(false);
        }
        else{
            String role_prefix = "r_";
            String suffix = "[d" + first + "," + "d" + second + "]";
            IRI roleIRI = owlObjectProperty.getIRI();
            return new NamedAtom(roleIRI.getNamespace() + role_prefix + roleIRI.getFragment() + suffix);
        }
    }

    @Override
    public PropositionalFormula visit(OWLObjectInverseOf owlObjectInverseOf) {
        //Swap the elements

        nElementRoleConvertor oppositeConvertor = nRoleConvertorFactory.getNElementRoleConvertor(second,first);
        return owlObjectInverseOf.getInverse().getSimplified().accept(oppositeConvertor);
    }


    //Unsupported
    @Override
    public PropositionalFormula visit(OWLDataProperty owlDataProperty) {
        return null;
    }
}
