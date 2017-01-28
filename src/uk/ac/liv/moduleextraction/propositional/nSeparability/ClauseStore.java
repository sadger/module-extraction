package uk.ac.liv.moduleextraction.propositional.nSeparability;

import uk.ac.liv.moduleextraction.propositional.cnf.TseitinStackTransformation;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;
import uk.ac.liv.moduleextraction.propositional.satclauses.NumberMap;

import java.util.Arrays;
import java.util.HashSet;


public class ClauseStore {

    private HashSet<int[]> clauses;
    private HashSet<Integer> freshVariables;
    private boolean hasConstantValue = false;
    private Boolean constantValue = null;
    private int clauseCount;


    public ClauseStore(PropositionalFormula formula, NumberMap numberMap) {
        TseitinStackTransformation transformation = new TseitinStackTransformation(formula,numberMap);
        this.clauses = transformation.getCnfClauses();
        this.freshVariables = transformation.getFreshVariables();
        this.hasConstantValue = transformation.hasConstantValue();
        this.constantValue = transformation.getConstantValue();
        this.clauseCount = transformation.getClauseCount();
    }

    public boolean hasConstantValue() {
        return hasConstantValue;
    }

    public boolean getConstantValue() {
        return constantValue;
    }

    public HashSet<int[]> getClauses() {
        return clauses;
    }



    public HashSet<Integer> getFreshVariables() {
        return freshVariables;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(clauseCount + 5);
        if (hasConstantValue) {
            builder.append("[Constant value: " + constantValue + "]");
        } else {
            builder.append("[Clauses: " + clauseCount + ", ");
            builder.append("Fresh Variables: " + freshVariables + ", ");
            builder.append("Clauses: {");
            for (int[] clause : clauses) {
                builder.append(Arrays.toString(clause));
            }
            builder.append("}]");
        }

        return builder.toString();
    }



}
