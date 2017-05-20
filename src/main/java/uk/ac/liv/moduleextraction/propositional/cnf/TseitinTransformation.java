package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.satclauses.NumberMap;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;

import java.util.Arrays;
import java.util.HashSet;


public class TseitinTransformation {

    //Allows duplicates because of int[] equals - if space is an issue consider changing
    HashSet<int[]> cnfClauses = new HashSet<int[]>();

    HashSet<Integer> freshVariables = new HashSet<Integer>();

    private NumberMap numberMap;
    private PropositionalFormula formula;
    private boolean hasConstantValue = false;
    private Boolean constantValue = null;
    private int clauseCount = 0;

    public TseitinTransformation(PropositionalFormula inputFormular, NumberMap numberMap){
        this.formula = inputFormular;
        this.numberMap = numberMap;
    }

    public int getClauseCount() {
        return clauseCount;
    }

    public boolean hasConstantValue() {
        return hasConstantValue;
    }

    public Boolean getConstantValue() {
        return constantValue;
    }

    public HashSet<int[]> getCnfClauses() {
        return cnfClauses;
    }

    public HashSet<Integer> getFreshVariables() {
        return freshVariables;
    }

    public void applyTransformation(){
        if(formula instanceof NamedAtom){
            NamedAtom atom = (NamedAtom) formula;
            numberMap.updateNumberMap(atom);
            int namedValue = numberMap.get(atom);
            cnfClauses.add(new int[]{namedValue});
            clauseCount += 1;
        }
        else if(formula instanceof  Negation){
            Negation neg = (Negation) formula;
            //In NNF only NamedAtoms under complement
            NamedAtom positiveAtom = (NamedAtom) neg.getComplement();
            numberMap.updateNumberMap(positiveAtom);
            int value = -numberMap.get(positiveAtom);
            cnfClauses.add(new int[]{value});
            clauseCount += 1;
        }
        else if(formula instanceof BooleanAtom){
            BooleanAtom bool = (BooleanAtom) formula;
            hasConstantValue = true;
            constantValue = bool.getValue();
        }
        else{
            PropositionalFormula topLevelName = formula.accept(new TseitinVisitor());
            //New clause with top level variable to maintain equisatisfiability
            cnfClauses.add(new int[]{numberMap.get(topLevelName)});
            clauseCount += 1;
        }
    }


    private class TseitinVisitor implements FormulaVisitorEx<PropositionalFormula>{
        @Override
        public PropositionalFormula visit(NamedAtom a) {
            return a;
        }

        @Override
        public PropositionalFormula visit(BooleanAtom b) {
            try{
                throw new IllegalTransformationException(formula);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
            return null;
        }

        /**
         * Get the value of a NamedAtom or negation of NamedAtom under the number mapping
         * As formula is in NNF when this is used every negation is a negation of a NamedAtom
         * Also updates the number map for the variable.
         * @param form NamedAtom or Negation to obtain value for.
         * @return Value of formula in number map, positive if NamedAtom, negative if Negation of NamedAtom
         */
        private int getMappingValue(PropositionalFormula form){
            int val;
            if(isNegation(form)){
                Negation rightNegation = (Negation) form;
                NamedAtom rightPositive = (NamedAtom) rightNegation.getComplement();
                numberMap.updateNumberMap(rightPositive);
                val = -numberMap.get(rightPositive);
            }
            else{
                NamedAtom rightAtom = (NamedAtom) form;
                numberMap.updateNumberMap(rightAtom);
                val = numberMap.get(rightAtom);
            }
            return val;
        }

        @Override
        public PropositionalFormula visit(Conjunction conj) {
            NamedAtom freshName = new NamedAtom(NameGenerator.getFreshName());
            numberMap.updateNumberMap(freshName);
            int freshValue = numberMap.get(freshName);
            freshVariables.add(freshValue);

            PropositionalFormula leftForm = conj.getLeftFormula().accept(this);
            PropositionalFormula rightForm =  conj.getRightFormula().accept(this);

            int leftVal = getMappingValue(leftForm);
            int rightVal = getMappingValue(rightForm);


            int[] secondClause = new int[2];
            secondClause[0] = leftVal;
            secondClause[1] = -(numberMap.get(freshName));

            int[] thirdClause = new int[2];
            thirdClause[0] = rightVal;
            thirdClause[1] = -(numberMap.get(freshName));

            cnfClauses.add(secondClause);
            cnfClauses.add(thirdClause);

            clauseCount += 2;
            return freshName;
        }

        @Override
        public PropositionalFormula visit(Disjunction disj) {
            NamedAtom freshName = new NamedAtom(NameGenerator.getFreshName());
            numberMap.updateNumberMap(freshName);
            int freshValue = numberMap.get(freshName);
            freshVariables.add(freshValue);
            PropositionalFormula leftForm = disj.getLeftFormula().accept(this);
            PropositionalFormula rightForm =  disj.getRightFormula().accept(this);

            int leftVal = getMappingValue(leftForm);
            int rightVal = getMappingValue(rightForm);


            int[] firstClause = new int[3];
            firstClause[0] = leftVal;
            firstClause[1] = rightVal;
            firstClause[2] = -(numberMap.get(freshName)) ;

            cnfClauses.add(firstClause);
            clauseCount += 1;
            return freshName;
        }

        @Override
        public PropositionalFormula visit(Negation negation) {
            return negation;
        }

        @Override
        public PropositionalFormula visit(Implication implication) {
            try{
                throw new IllegalTransformationException(formula);
            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
            return null;
        }

        @Override
        public PropositionalFormula visit(Equality equality) {
            try{
                throw new IllegalTransformationException(formula);
            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
            return null;
        }



        private boolean isNegation(PropositionalFormula form){
            return form instanceof Negation;
        }
    }




    private static class NameGenerator{
        private static int index = 1;
        public static String getFreshName(){
            return "x_" + index++;
        }
    }

    public static void main (String[] args){
        NamedAtom p = new NamedAtom("P");
        NamedAtom q = new NamedAtom("Q");
        PropositionalFormula form = new Conjunction(new Negation(p),new Negation(q));
        TseitinTransformation t = new TseitinTransformation(form,new NumberMap());
        t.applyTransformation();
        for(int[] clause : t.cnfClauses){
            System.out.println(Arrays.toString(clause));
        }
    }


}
