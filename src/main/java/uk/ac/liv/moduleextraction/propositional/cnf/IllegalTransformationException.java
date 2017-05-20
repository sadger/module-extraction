package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;


public class IllegalTransformationException extends Exception {
    PropositionalFormula form;
    IllegalTransformationException(PropositionalFormula form){
        this.form = form;
    }

    @Override
    public String getMessage(){
        return "Cannot transform formula " + form + " it contains illegal constructs";
    }
}