package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import uk.ac.liv.moduleextraction.propositional.formula.NamedAtom;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

public class nElementNamedIndividualConvertor implements OWLIndividualVisitorEx<PropositionalFormula>{


    private final int domainElement;

    public nElementNamedIndividualConvertor(int domainElement){
       this.domainElement = domainElement;
    }

    @Override public PropositionalFormula visit(OWLNamedIndividual owlIndividual){
        return new NamedAtom("{" + owlIndividual.toString() + "}_d" + domainElement);
    }

    //Unsupported
    @Override public PropositionalFormula visit(OWLAnonymousIndividual individual){return null;}

}
