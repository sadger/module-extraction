package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.propositional.formula.*;

import java.util.ArrayList;
import java.util.TreeSet;

/** Convert arbitary OWLClassExpressions to Propositional formulae under n-element
 * interpretations
 *
Given a domain size $n$ and element $d \in \Delta_{n} = \{d_1, \dots, d_n \} $
for a possibly complex OWLClassExpression $C$ to decide if $d \in C^\Imc$ if $v([C]_{d}) =
1$ as defined below:

 Useful for cardinality restrictions:
 Let $\mathsf{Rsets}(m)$ be the set of all sets size $m$ taken from elements $e \in
 \Delta_n$
 **/


public class nClassExpressionConvertor implements OWLClassExpressionVisitorEx<PropositionalFormula> {

    private final int[] DOMAIN_ELEMENTS;
    private final int CHOSEN_ELEMENT;
    private final nElementNamedIndividualConvertor individualConvertor;


    /**
     * Construct a convertor for a particular domain element to visit OWLClassExpression(s) and
     * return a Propostional formula which represents that expression under the chosen element.
     * @param domainElements - the total number of elements in the domain
     * @param chosenDomainElement - the element chosen from the domainElements to
     *                              convert OWLClassExpression under */
    public nClassExpressionConvertor(int[] domainElements, int chosenDomainElement) {
        this.DOMAIN_ELEMENTS = domainElements;
        this.CHOSEN_ELEMENT = chosenDomainElement;
        individualConvertor = new nElementNamedIndividualConvertor(chosenDomainElement);
    }

    /**
     * $[\top]_{d} = true, $
     * $[\bot]_{d} = false $
     * $[A]_{d} = \Ad$ - the named class under the chosen element
     * @param owlClass - OWLClass to convert
     * @return n-element representation of the OWLClass
     */
    @Override
    public PropositionalFormula visit(OWLClass owlClass) {
        if(owlClass.isOWLThing()){
            return new BooleanAtom(true);
        }
        else if(owlClass.isOWLNothing()){
            return new BooleanAtom(false);
        }
        else{
            String name = owlClass.getIRI().toString();
            return new NamedAtom(name + "_d" + CHOSEN_ELEMENT);
        }
    }

    /**
     * $[C_1 \sqcap C_2]_{d} = [C_1]_{d} \wedge [C_2]_{d}$ - For the chosen element to belong to the
     * conjunction under the interpretation, it must belong to both conjuncts.
     * The implementation covers when there are more than 2 conjuncts, a simple extension of the given
     * conversion.
     * @param owlObjectIntersectionOf - conjunction to convert to convert under chosen element
     * @return Propositonal formula which is true if the chosen element belongs to the conjunction
     */
    @Override
    public PropositionalFormula visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
        //Cans we improve on the performance by not using a treeset for this?
        TreeSet<OWLClassExpression> conjuncts =
                new TreeSet<OWLClassExpression>(owlObjectIntersectionOf.asConjunctSet());

        OWLClassExpression firstConjunct = conjuncts.pollFirst();
        //If there is only one conjunct (caused by owlapi simplification)
        if(conjuncts.size() == 0){
            return firstConjunct.accept(this);
        }
        OWLClassExpression secondConjunct = conjuncts.pollFirst();


        PropositionalFormula result = new Conjunction(firstConjunct.accept(this), secondConjunct.accept(this));

		/* Handle more than two conjuncts */
        if(!conjuncts.isEmpty()){
            for(OWLClassExpression cls : conjuncts){
                result = new Conjunction(result, cls.accept(this));
            }
        }

        return result;
    }

    /**
     * $[C_1 \sqcup C_2]_{d} = [C_1]_{d} \vee [C_2]_{d}$ - For the chosen element to belong to the
     * conjunction under the interpretation, it must belong to either disjunct.
     * The implementation covers when there are more than 2 disjuncts, a simple extension of the given
     * conversion.
     * @param unions - disjunction to convert to convert under chosen element
     * @return Propositonal formula which is true if the chosen element belongs to the disjunction
     */
    @Override
    public PropositionalFormula visit(OWLObjectUnionOf unions) {
        //Can we improve on the performance by not using a treeset for this?
        TreeSet<OWLClassExpression> disjuncts = new TreeSet<OWLClassExpression>(unions.asDisjunctSet());
        OWLClassExpression firstDisjunct = disjuncts.pollFirst();

        //If there is only one disjunct
        if(disjuncts.size() == 0){
            return firstDisjunct.accept(this);
        }

        OWLClassExpression secondDisjunct = disjuncts.pollFirst();

		/* Handle more than two disjuncts */
        PropositionalFormula result = new Disjunction(firstDisjunct.accept(this), secondDisjunct.accept(this));
        if(!disjuncts.isEmpty()){
            for(OWLClassExpression cls : disjuncts){
                result = new Disjunction(result, cls.accept(this));
            }
        }
        return result;

    }


    /**
     * $[\exists r. C]_{d} = \bigvee \limits_{e \in \Delta_{n}} [r]_{\Rde} \wedge [C]_{e} $
     * Existential restriction - for the chosen element to belong to the restriction there must exist
     * a relation $(d,x) \in r^\Imc$ with $x \in C^\Imc$, $x$ may be the chosen element itself.
     *
     * Go through all pairs of domain elements (d,x) for each create a conjunction which is true if both
     * $(d,x) \in r and x \in C$ by making a disjunction of all such conjunctions the chosen element d belongs
     * to the restriction if one or more of these conjunctions are satisfied.
     *
     * @param owlObjectSomeValuesFrom - disjunction to convert under the chosen element
     * @return result - Propositonal formula which is true when the chosen element belongs to the restriction
     */
    @Override
    public PropositionalFormula visit(OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {

        OWLObjectPropertyExpression role = owlObjectSomeValuesFrom.getProperty();
        OWLClassExpression filler = owlObjectSomeValuesFrom.getFiller();
        nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,CHOSEN_ELEMENT);

        PropositionalFormula result = new Conjunction(role.accept(roleConvertor), filler.accept(this));

        for (int i = 1; i <= DOMAIN_ELEMENTS.length; i++){
            if(i != CHOSEN_ELEMENT){
                nClassExpressionConvertor n_convertor = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS,i);
                nElementRoleConvertor n_role_convertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,i);
                result = new Disjunction(result, new Conjunction(role.accept(n_role_convertor), filler.accept(n_convertor)));
            }

        }

        return result;
    }


    /**
     * $[\forall r. C]_{d} = \bigwedge \limits_{e \in \Delta_{n}} [r]_{\Rde} \rightarrow [C]_{e} $
     * Universal restriction - for the chosen element to belong to the restriction for every
     * $(d,x) \in r^\Imc$ we must have $x \in C^\Imc$ or there is no relation from (d,x) in r^\Imc.
     *
     * For every pair (d,x) of domain elements construct an implication which is true when
     * (d,x) \in r^\Imc implies x \in C^\Imc, by having a conjunction of all such implications the chosen element
     * d belongs to the restriction if every one of these implication is satisfied.
     * @param owlObjectAllValuesFrom - universal restriction to convert to PropositionalFormula  under the chosen element
     * @return result - PropositionalFormula which is true when the chosen element belongs to the restriction
     */
    @Override
    public PropositionalFormula visit(OWLObjectAllValuesFrom owlObjectAllValuesFrom) {
        OWLObjectPropertyExpression role = owlObjectAllValuesFrom.getProperty();
        OWLClassExpression filler = owlObjectAllValuesFrom.getFiller();
        nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,CHOSEN_ELEMENT);

        PropositionalFormula result = new Implication(role.accept(roleConvertor), filler.accept(this));

        for (int i = 1; i <= DOMAIN_ELEMENTS.length; i++){
            if(i != CHOSEN_ELEMENT){
                nClassExpressionConvertor n_convertor = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS,i);
                nElementRoleConvertor n_role_convertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,i);
                result = new Conjunction(result, new Implication(role.accept(n_role_convertor), filler.accept(n_convertor)));
            }

        }

        return result;
    }

    /**
     * $[\neg C]_{d} = \neg([C]_{d})$
     * Negation/Complement - the complement contains the chosen element if the positive occurances does not
     * @param owlObjectComplementOf - complement to convert under the chosen element
     * @return PropositionalFormula true when the complement contains the chosen element
     */
    @Override
    public PropositionalFormula visit(OWLObjectComplementOf owlObjectComplementOf) {
        return new Negation(owlObjectComplementOf.getComplementNNF().accept(this));
    }


    /**
     * $ [(\geq m\ r. C)]_{d} = \left\{
         \begin{array}{l l}
          m = 0 & \quad  \top\\
          n \geq m > 0 & \quad \bigvee \limits_{s \in \Rsets(m)} \bigwedge \limits_{e \in s} [r]_{\Rde} \wedge [C]_{e}\\
          m  > n & \quad  \bot\\
     \end{array} \right.$

     * Min (at-least) cardinality
     *
     * The case where the cardinality is 0 returns a boolean TRUE as cardinality must at least be 0
     * (non-negative)
     *
     * If the cardinality is larger than the number of domain elements returns boolean FALSE - impossible
     * to satsify the restriction with fewer domain elements than the cardinality.
     *
     * Otherwise if domain_elements >= cardinality > 0 ...
     *
     * @param owlObjectMinCardinality
     * @return
     */
    @Override
    public PropositionalFormula visit(OWLObjectMinCardinality owlObjectMinCardinality) {
        int cardinality = owlObjectMinCardinality.getCardinality();
        OWLObjectPropertyExpression role = owlObjectMinCardinality.getProperty();
        OWLClassExpression filler = owlObjectMinCardinality.getFiller();

        PropositionalFormula result;
        if(cardinality == 0){
            result = new BooleanAtom(true);
        }
        else if(DOMAIN_ELEMENTS.length >= cardinality){
            ArrayList<int[]> rSet = new RSets(DOMAIN_ELEMENTS,cardinality).getRsets();
            ArrayList<PropositionalFormula> disjuncts = new ArrayList<PropositionalFormula>();
            PropositionalFormula conjunct = null;
            for(int[] r : rSet){
                for (int i = 0; i < r.length; i++) {
                    int rSetElement = r[i];
                    PropositionalFormula rConjunct = getMinCardinalityConjunction(role, filler, rSetElement);
                    conjunct = (i == 0) ? rConjunct : new Conjunction(conjunct,rConjunct);
                }
                disjuncts.add(conjunct);
            }
            // Make a disjunction if there are more than 1 conjuncts
            result = disjuncts.get(0);
            for (int k = 1; k < disjuncts.size(); k++) {
                result = new Disjunction(result, disjuncts.get(k));
            }
        }
        // cardinality > DOMAIN_ELEMENTS
        else{
            result = new BooleanAtom(false);
        }

        return result;
    }

    /**
     * Creates a conjunction of a converted role and filler for use in min cardinality
     * Each role interpreted under r_(chosen,element) where d is the chosen element and the filler C_element.
     * @param role
     * @param filler
     * @param element
     * @return Conjunction
     */
    private PropositionalFormula getMinCardinalityConjunction(OWLObjectPropertyExpression role, OWLClassExpression filler, int element) {
        nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,element);
        nClassExpressionConvertor fillerConverter = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS, element);
        return new Conjunction(role.accept(roleConvertor), filler.accept(fillerConverter));
    }



    @Override
    public PropositionalFormula visit(OWLObjectMaxCardinality owlObjectMaxCardinality) {
        int cardinality = owlObjectMaxCardinality.getCardinality();
        OWLObjectPropertyExpression role = owlObjectMaxCardinality.getProperty();
        OWLClassExpression filler = owlObjectMaxCardinality.getFiller();

        PropositionalFormula result = null;

        if(cardinality == 0){
            for(int e : DOMAIN_ELEMENTS){
                nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,e);
                nClassExpressionConvertor fillerConvertor = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS,e);
                PropositionalFormula disjunction =
                        new Disjunction(new Negation(role.accept(roleConvertor)), new Negation(filler.accept(fillerConvertor)));
                result = (e == DOMAIN_ELEMENTS[0]) ? disjunction : new Conjunction(result,disjunction);
            }
        }
        else if(DOMAIN_ELEMENTS.length > cardinality){
            ArrayList<int[]> rSet = new RSets(DOMAIN_ELEMENTS,cardinality+1).getRsets();
            ArrayList<PropositionalFormula> conjuncts = new ArrayList<PropositionalFormula>();
            PropositionalFormula disjunct = null;
            for(int[] r : rSet){
                for (int i = 0; i < r.length; i++) {
                    int rSetElement = r[i];
                    PropositionalFormula rDisjunct = getMaxCardinalityDisjunction(role, filler, rSetElement);
                    disjunct = (i == 0) ? rDisjunct : new Disjunction(disjunct,rDisjunct);
                }
                conjuncts.add(disjunct);
            }
            // Make a conjunction if there are more than 1 disjuncts
            result = conjuncts.get(0);
            for (int k = 1; k < conjuncts.size(); k++) {
                result = new Conjunction(result, conjuncts.get(k));
            }
        }
        // cardinality >= DOMAIN_ELEMENTS
        else{
            result = new BooleanAtom(true);
        }

        return result;

    }

    private PropositionalFormula getMaxCardinalityDisjunction(OWLObjectPropertyExpression role, OWLClassExpression filler, int element) {
        nElementRoleConvertor roleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(CHOSEN_ELEMENT,element);
        nClassExpressionConvertor fillerConverter = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS, element);
        return new Disjunction(new Negation(role.accept(roleConvertor)), new Negation(filler.accept(fillerConverter)));
    }

    @Override
    public PropositionalFormula visit(OWLObjectExactCardinality owlObjectExactCardinality) {
        int cardinality = owlObjectExactCardinality.getCardinality();
        OWLClassExpression filler = owlObjectExactCardinality.getFiller();
        OWLObjectPropertyExpression role = owlObjectExactCardinality.getProperty();
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        OWLObjectMinCardinality min = factory.getOWLObjectMinCardinality(cardinality,role,filler);
        OWLObjectMaxCardinality max = factory.getOWLObjectMaxCardinality(cardinality,role, filler);

        return new Conjunction(min.accept(this), max.accept(this));
    }

    //Nominals - ONE
    @Override
    public PropositionalFormula visit(OWLObjectHasValue owlObjectHasValue) {
        return owlObjectHasValue.getValue().accept(individualConvertor);
    }

    //Nominals - MANY
    @Override
    public PropositionalFormula visit(OWLObjectOneOf owlObjectOneOf) {

        TreeSet<OWLNamedIndividual> indivs = new TreeSet<>(owlObjectOneOf.getIndividualsInSignature());

        NamedAtom first = (NamedAtom) indivs.pollFirst().accept(individualConvertor);

        //Only one nominal in set (first is removed already)
        if(indivs.size() == 0){
            return first;
        }

        NamedAtom second = (NamedAtom) indivs.pollFirst().accept(individualConvertor);

        PropositionalFormula result = new Disjunction(first,second);

        for(OWLNamedIndividual i : indivs) {
            result = new Disjunction(result, i.accept(individualConvertor));
        }

        return result;
    }


    //UNSUPPORTED
    @Override
    public PropositionalFormula visit(OWLDataAllValuesFrom owlDataAllValuesFrom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDataHasValue owlDataHasValue) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDataMinCardinality owlDataMinCardinality) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDataExactCardinality owlDataExactCardinality) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDataMaxCardinality owlDataMaxCardinality) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLObjectHasSelf owlObjectHasSelf) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
        return null;
    }




}

