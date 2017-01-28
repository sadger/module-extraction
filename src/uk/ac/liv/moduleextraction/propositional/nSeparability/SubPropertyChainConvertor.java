package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import uk.ac.liv.moduleextraction.propositional.formula.Conjunction;
import uk.ac.liv.moduleextraction.propositional.formula.Disjunction;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubPropertyChainConvertor {

    private List<OWLObjectPropertyExpression> chain;
    private int firstElem;
    private int lastElem;
    private  Set<PropositionalFormula> disjuncts;
    private  int[] domainElements;

    /**
     * Converts the chain part of OWLSubPropertyChainOfAxiom(s)
     * @param chain - chain to converted
     * @param domainElements the fixed number of elements in the specified domain
     * @param firstElem the element the chain should start with i.e d when (d,e) \in chain
     * @param lastElem the last element the chain ends with i.e e when (d,e) \in chain
     */
    public SubPropertyChainConvertor(List<OWLObjectPropertyExpression> chain, int[] domainElements, int firstElem, int lastElem) {
        this.chain = chain;
        this.firstElem = firstElem;
        this.lastElem = lastElem;
        this.disjuncts = new HashSet<PropositionalFormula>();
        this.domainElements = domainElements;
    }

    /**
     * Process every combination of interpretations for each chain.
     * Collect the conjuncts and join them together with disjunctions
     * @return PropositionalFormula representing every way of interpretating the chain for the given domain.
     */
    public PropositionalFormula getConvertedChain(){
        //Compute the conjuncts and collect the disjuncts
        processChain(new PropositionalFormula[chain.size()], 0, firstElem);

        //Join them together
        int i = 0;
        PropositionalFormula result = null;
        for(PropositionalFormula disjunct : disjuncts){
            result = (i++ == 0) ? disjunct : new Disjunction(result,disjunct);
        }
        return result;
    }

    /**
     * Recursively generate all thains which start with the start element and end with the
     * end element
     * @param result - array of PropositionalFormula(s) to hold the converted chain
     * @param startIndex - which index of chain are we considering
     * @param startElement - what domain element does the interpretation start with i.e d if we consider r_(d,x) where x is
     *                       variable
     */
    private void processChain(PropositionalFormula[] result, int startIndex, int startElement) {
        //If the next index is the last in the chain use the last element
        if(chain.size() == startIndex + 1){
            nElementRoleConvertor roleConvertor = nRoleConvertorFactory.getNElementRoleConvertor(startElement,lastElem);
            result[startIndex] = chain.get(startIndex).accept(roleConvertor);
            collectDisjuncts(result);
        }
        //Otherwise consider all other combinations of ways of getting from the start element
        else{
            for(int second : domainElements){
                nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(startElement,second);
                result[startIndex] = chain.get(startIndex).accept(roleConvertor);
                processChain(result, startIndex + 1, second);
            }
        }
    }

    /**
     * Create a conjunct of each combination of interpreations for a particular chain
     * then add this new conjunct to the set of disjuncts
     * @param result Array of PropositionalFormula conjuncts to join
     */
    private void collectDisjuncts(PropositionalFormula[] result){
        PropositionalFormula conjuncts = null;
        for (int i = 0; i < result.length; i++) {
            conjuncts = (i == 0) ? result[0] : new Conjunction(conjuncts,result[i]);
        }
        disjuncts.add(conjuncts);
    }

}
