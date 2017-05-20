package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;


public class TruthIdentitySimplifier{

    public static void main(String[] args) {
        BooleanAtom t = new BooleanAtom(true);
        BooleanAtom f = new BooleanAtom(false);
        NamedAtom a = new NamedAtom("A");
        NamedAtom b = new NamedAtom("B");
        PropositionalFormula form = new Conjunction(new Conjunction(t,a), new Conjunction(t,b));

        TruthIdentitySimplifier simp = new TruthIdentitySimplifier();
        System.out.println(simp.simplifyTruthIdentities(form));

    }


    public PropositionalFormula simplifyTruthIdentities(PropositionalFormula form){
        boolean changeMade = true;
        TruthEliminatorVisitor visitor = new TruthEliminatorVisitor();
        while(changeMade){
            visitor.setChangeMade(false);
            form = form.accept(visitor);
            changeMade = visitor.getChangeMade();
        }
        return form;
    }

    private class TruthEliminatorVisitor implements FormulaVisitorEx<PropositionalFormula>{
        private boolean changeMade = false;

        public boolean getChangeMade(){
            return changeMade;
        }

        public void setChangeMade(boolean changeMade) {
            this.changeMade = changeMade;
        }

        @Override
        public PropositionalFormula visit(NamedAtom a) {
            return a;
        }

        @Override
        public PropositionalFormula visit(BooleanAtom b) {
            return b;
        }

        @Override
        public PropositionalFormula visit(Conjunction conj) {

            PropositionalFormula leftConjunct = conj.getLeftFormula();
            PropositionalFormula rightConjunct = conj.getRightFormula();

            if(leftConjunct instanceof BooleanAtom){
                changeMade = true;
                BooleanAtom bool = (BooleanAtom) leftConjunct;
                if(bool.getValue()){
                    return rightConjunct.accept(this);
                }
                else{
                    return new BooleanAtom(false).accept(this);
                }

            }
            else if(rightConjunct instanceof BooleanAtom){
                changeMade = true;
                BooleanAtom bool = (BooleanAtom) rightConjunct;
                if(bool.getValue()){
                    return leftConjunct.accept(this);
                }
                else{
                    return new BooleanAtom(false).accept(this);
                }

            }
            else{
                return new Conjunction(leftConjunct.accept(this), rightConjunct.accept(this));

            }
        }

        @Override
        public PropositionalFormula visit(Disjunction disj) {
            PropositionalFormula leftDisjunct = disj.getLeftFormula();
            PropositionalFormula rightDisjunct = disj.getRightFormula();

            if(leftDisjunct instanceof BooleanAtom){
                changeMade = true;
                BooleanAtom bool = (BooleanAtom) leftDisjunct;
                if(bool.getValue()){
                    return new BooleanAtom(true).accept(this);
                }
                else{
                    return rightDisjunct.accept(this);
                }
            }
            else if(rightDisjunct instanceof BooleanAtom){
                changeMade = true;
                BooleanAtom bool = (BooleanAtom) rightDisjunct;
                if(bool.getValue()){
                    return new BooleanAtom(true).accept(this);
                }
                else{
                    return leftDisjunct.accept(this);
                }
            }
            else{
                return new Disjunction(leftDisjunct.accept(this),rightDisjunct.accept(this));
            }

        }

        @Override
        public PropositionalFormula visit(Negation neg) {
            return neg;
        }


        /* These shouldn't exist any more as we remove them before converting
           to NNF which we should perform before removing these identities
        */
        @Override
        public PropositionalFormula visit(Equality equality) {
            return null;
        }
        @Override
        public PropositionalFormula visit(Implication implication) {
            return null;
        }


    }





}
