package uk.ac.liv.moduleextraction.propositional.cnf;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.nSeparability.nAxiomConvertor;
import uk.ac.liv.moduleextraction.propositional.satclauses.NumberMap;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class TseitinStackTransformation {

    //Allows duplicates because of int[] equals - if space is an issue consider changing
    HashSet<int[]> cnfClauses = new HashSet<int[]>();

    HashSet<Integer> freshVariables = new HashSet<Integer>();

    private NumberMap numberMap;
    private PropositionalFormula formula;
    private boolean hasConstantValue = false;
    private Boolean constantValue = null;
    private int clauseCount = 0;

    private Deque<PropositionalStackElement> stack;

    private static ImplicationEliminator implicationEliminator = new ImplicationEliminator();
    private static NegationNormalForm nnfConvertor = new NegationNormalForm();
    private static TruthIdentitySimplifier truthIdentitySimplifier = new TruthIdentitySimplifier();


    private int maxStack = 0;

    public TseitinStackTransformation(PropositionalFormula inputFormula, NumberMap numberMap){
        this.formula = inputFormula;
        //Ensure in NNF
        formula = formula.accept(implicationEliminator).accept(nnfConvertor);
        //Remove all TRUE/FALSE subformulae by simplification
        formula = truthIdentitySimplifier.simplifyTruthIdentities(formula);
        this.numberMap = numberMap;
        this.stack = new ArrayDeque<>();
        formula.accept(new TseitinTransformationApplicator());
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

    private class TseitinTransformationApplicator implements FormulaVisitorVoid{

        @Override
        public void visit(NamedAtom atom){
            numberMap.updateNumberMap(atom);
            int namedValue = numberMap.get(atom);
            cnfClauses.add(new int[]{namedValue});
            clauseCount += 1;
        }

        @Override
        public void visit(Negation negation){
            //In NNF only NamedAtoms under complement
            NamedAtom positiveAtom = (NamedAtom) negation.getComplement();
            numberMap.updateNumberMap(positiveAtom);
            int value = -numberMap.get(positiveAtom);
            cnfClauses.add(new int[]{value});
            clauseCount += 1;
        }

        @Override
        public void visit(BooleanAtom bool){
            hasConstantValue = true;
            constantValue = bool.getValue();
        }

        @Override
        public void visit(Conjunction conj){
            applyToComplexFormula(conj);
        }

        @Override
        public void visit(Disjunction disj){
            applyToComplexFormula(disj);
        }

        @Override
        public void visit(Equality equality){
            applyToComplexFormula(equality);
        }

        @Override
        public void visit(Implication implication){
            applyToComplexFormula(implication);
        }

        private void applyToComplexFormula(PropositionalFormula p){
            PropositionalFormula topLevelName = pushToStack(formula);
            //New clause with top level variable to maintain equisatisfiability
            cnfClauses.add(new int[]{numberMap.get(topLevelName)});
            clauseCount += 1;
            processStack();
        }
    }



    public void processStack(){
        while(!stack.isEmpty()){
            maxStack = Math.max(maxStack,stack.size());
            PropositionalStackElement poppedElem = stack.pop();
            NamedAtom  subFormulaName = poppedElem.getSubFormulaName();
            PropositionalFormula  poppedFormula = poppedElem.getValue();

            TseitinStackProcessor p = new TseitinStackProcessor();
            poppedFormula.accept(p, subFormulaName);

        }
    }


    private PropositionalFormula pushToStack(PropositionalFormula f){
        //In NNF every negation is a literal so contains no subformulas, same for named atoms
        if(f instanceof NamedAtom || f instanceof Negation){
            return f;
        }
        NamedAtom freshName = new NamedAtom(NameGenerator.getFreshName());
        numberMap.updateNumberMap(freshName);
        int freshMapping = getMappingValue(freshName);
        freshVariables.add(freshMapping);
        stack.push(new PropositionalStackElement(f,freshName));
        return freshName;
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
        if(form instanceof Negation){
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


    private class TseitinStackProcessor implements  FormulaVisitor<NamedAtom>{

        @Override
        public void visit(NamedAtom a, NamedAtom subFormulaName){
            try{
                throw new IllegalTransformationException(a);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
        }
        @Override
        public void visit(BooleanAtom b, NamedAtom subFormulaName){
            try{
                throw new IllegalTransformationException(b);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
        }
        @Override
        public void visit(Conjunction conj, NamedAtom subFormulaName){
            PropositionalFormula left;
            PropositionalFormula right;
            left = pushToStack(conj.getLeftFormula());
            right = pushToStack(conj.getRightFormula());

            int leftVal = getMappingValue(left);
            int rightVal = getMappingValue(right);

            int[] secondClause = new int[2];
            secondClause[0] = leftVal;
            secondClause[1] = -(numberMap.get(subFormulaName));

            int[] thirdClause = new int[2];
            thirdClause[0] = rightVal;
            thirdClause[1] = -(numberMap.get(subFormulaName));

            cnfClauses.add(secondClause);
            cnfClauses.add(thirdClause);

            clauseCount += 2;

        }
        @Override
        public void visit(Disjunction disj, NamedAtom subFormulaName){
            PropositionalFormula left = pushToStack(disj.getLeftFormula());
            PropositionalFormula right = pushToStack(disj.getRightFormula());

            int leftVal = getMappingValue(left);
            int rightVal = getMappingValue(right);

            int[] firstClause = new int[3];
            firstClause[0] = leftVal;
            firstClause[1] = rightVal;
            firstClause[2] = -(numberMap.get(subFormulaName)) ;

            cnfClauses.add(firstClause);
            clauseCount += 1;
        }

        @Override
        public void visit(Negation negation, NamedAtom subFormulaName){
            try{
                throw new IllegalTransformationException(negation);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
        }

        @Override
        public void visit(Equality equality, NamedAtom subFormulaName){
            try{
                throw new IllegalTransformationException(equality);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
        }
        
        @Override
        public void visit(Implication implication, NamedAtom subFormulaName){
            try{
                throw new IllegalTransformationException(implication);

            } catch (IllegalTransformationException ite) {
                ite.printStackTrace();
            }
        }
    }


    private class PropositionalStackElement{

        private PropositionalFormula value;
        private NamedAtom subFormulaName;

        public PropositionalStackElement(PropositionalFormula value, NamedAtom subFormulaName) {
            this.value = value;
            this.subFormulaName = subFormulaName;
        }

        public PropositionalFormula getValue() {
            return value;
        }

        public NamedAtom getSubFormulaName() {
            return subFormulaName;
        }
    }



    public static void main (String[] args){
        ImplicationEliminator implicationEliminator = new ImplicationEliminator();
        NegationNormalForm nnfConvertor = new NegationNormalForm();
        TruthIdentitySimplifier truthIdentitySimplifier = new TruthIdentitySimplifier();
        int count = 0;
        int max = 0;



        for(File f : new File(ModulePaths.getOntologyLocation() + "/NCI/Profile").listFiles()){
            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
            System.out.println(ont.getLogicalAxiomCount());

            Set<PropositionalFormula> converted = new HashSet<>();
            for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){

                PropositionalFormula formula = ax.accept(new nAxiomConvertor(1));
                formula = formula.accept(implicationEliminator);
                formula = formula.accept(nnfConvertor);
                formula = truthIdentitySimplifier.simplifyTruthIdentities(formula);
                converted.add(formula);
            }


            Stopwatch stacktimer = Stopwatch.createStarted();

            NumberMap m = new NumberMap();
            for(PropositionalFormula formula : converted){
                TseitinStackTransformation tr = new TseitinStackTransformation(formula,m);
                max = Math.max(max,tr.maxStack);

            }

            stacktimer.stop();

            Stopwatch old = Stopwatch.createStarted();
            NumberMap m1 = new NumberMap();
            for(PropositionalFormula formula : converted){
                TseitinTransformation tr2 = new TseitinTransformation(formula, m1);
                tr2.applyTransformation();

            }
            old.stop();


            System.out.println(old);
            System.out.println(stacktimer);
            if (old.elapsed(TimeUnit.MICROSECONDS) > stacktimer.elapsed(TimeUnit.MICROSECONDS)){
                count++;
            }
        }

        System.out.println("C: " + count);
        System.out.println("M: " + max);









    }


}
