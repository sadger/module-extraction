package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

import java.util.HashSet;


public class nEntityConvertor implements OWLEntityVisitorEx<HashSet<PropositionalFormula>>{

    private final int DOMAIN_SIZE;
    private int[] domainElements;

    public nEntityConvertor(int domainSize){
        this.DOMAIN_SIZE = domainSize;
        this.domainElements = new int[DOMAIN_SIZE];
        for (int i = 0; i < DOMAIN_SIZE; i++) {
            domainElements[i] = i+1;
        }
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLClass owlClass) {
        HashSet<PropositionalFormula> underAllInterpretations = new HashSet<PropositionalFormula>();
        for(int d : domainElements){
            nClassExpressionConvertor convertor = nConvertorFactory.getClassExpressionConvertor(domainElements, d);
            underAllInterpretations.add(owlClass.accept(convertor));
        }
        return underAllInterpretations;
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLObjectProperty owlObjectProperty) {
        HashSet<PropositionalFormula> underAllInterpretations = new HashSet<PropositionalFormula>();
        for(int d1 : domainElements){
            for(int d2 : domainElements){
                nElementRoleConvertor convertor = nRoleConvertorFactory.getNElementRoleConvertor(d1,d2);
                underAllInterpretations.add(owlObjectProperty.accept(convertor));
            }
        }
        return underAllInterpretations;
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLDataProperty owlDataProperty) {
        return null;
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLNamedIndividual owlNamedIndividual) {
        HashSet<PropositionalFormula> underAllInterpretations = new HashSet<PropositionalFormula>();

        for(int d : domainElements){
            nElementNamedIndividualConvertor convertor = new nElementNamedIndividualConvertor(d);
            underAllInterpretations.add(owlNamedIndividual.accept(convertor));
        }
        return underAllInterpretations;
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLAnnotationProperty prop) {
        return null;
    }

    @Override
    public HashSet<PropositionalFormula> visit(OWLDatatype datatype) {
        return null;
    }

}
