package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.*;


public class ALCQIExpressionVerifier implements OWLClassExpressionVisitorEx<Boolean> {




    @Override
    public Boolean visit(OWLClass arg0) {
        return true;
    }

    @Override
    public Boolean visit(OWLObjectIntersectionOf arg0) {
        if(arg0.operands().count() == 0) {
            return false;
        }
        else{
            return arg0.operands().allMatch(expr -> expr.accept(this));
        }
    }

    @Override
    public Boolean visit(OWLObjectUnionOf arg0) {
        if(arg0.operands().count() == 0) {
            return false;
        }
        else{
            return arg0.operands().allMatch(expr -> expr.accept(this));
        }
    }

    @Override
    public Boolean visit(OWLObjectComplementOf arg0) {
        return arg0.getComplementNNF().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectSomeValuesFrom arg0) {
        return arg0.getFiller().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectAllValuesFrom arg0) {
        return arg0.getFiller().accept(this);
    }


    @Override
    public Boolean visit(OWLObjectMinCardinality arg0) {
        return arg0.getFiller().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectExactCardinality arg0) {
        return arg0.getFiller().accept(this);
    }

    @Override
    public Boolean visit(OWLObjectMaxCardinality arg0) {
        return arg0.getFiller().accept(this);
    }

    /* Unsupported currently */
    @Override
    public Boolean visit(OWLObjectHasValue arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLObjectHasSelf arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLObjectOneOf arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataSomeValuesFrom arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataAllValuesFrom arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataHasValue arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataMinCardinality arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataExactCardinality arg0) {
        return false;
    }

    @Override
    public Boolean visit(OWLDataMaxCardinality arg0) {
        return false;
    }


}
