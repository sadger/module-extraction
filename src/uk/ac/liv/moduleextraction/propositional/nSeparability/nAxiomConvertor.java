package uk.ac.liv.moduleextraction.propositional.nSeparability;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.ArrayList;
import java.util.List;


/**
 * Converts OWLLogicalAxioms to Propositional formulae for valuation under n-element interpretations
 * where n can be specified.
 */
public class nAxiomConvertor implements OWLAxiomVisitorEx<PropositionalFormula> {

    private final int DOMAIN_SIZE;
    private final int[] DOMAIN_ELEMENTS;

    /**
     * Initialise the domain and convertor for converting arbitrary axioms for specified
     * domain size
     * @param domainSize - size of the domain to convert the axioms
     */
    public nAxiomConvertor(int domainSize) {
        this.DOMAIN_SIZE = domainSize;
        this.DOMAIN_ELEMENTS = new int[DOMAIN_SIZE];
        for (int i = 0; i < DOMAIN_SIZE; i++) {
            DOMAIN_ELEMENTS[i] = i+1;
        }
    }

    public static void main(String[] args) {
        nAxiomConvertor convertor1 = new nAxiomConvertor(1);
        nAxiomConvertor convertor2 = new nAxiomConvertor(2);
        nAxiomConvertor convertor3 = new nAxiomConvertor(3);

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/newprop.owl");
        for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
            System.out.println(axiom);
            System.out.println(axiom.getAxiomType());
            System.out.println("[1] " + axiom.accept(convertor1));
            System.out.println("[2] " + axiom.accept(convertor2));
//            System.out.println("[3] " + axiom.accept(convertor3));
        }
    }

    /**
     * C \sqsubseteq D = \bigwedge \limits_{d \in \Delta_n} [C]_{d} \rightarrow [D]_{d}$
     * Convert a concept inclusion under specified domain size
     * @param owlSubClassOfAxiom - axiom to convert under domain
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(owlSubClassOfAxiom);
        OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(owlSubClassOfAxiom);

        PropositionalFormula result = null;

        for (int i = 1; i <= DOMAIN_SIZE; i++){
            nClassExpressionConvertor convertor_n = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS,i);
            Implication convertedAxiom = new Implication(lhs.accept(convertor_n), rhs.accept(convertor_n));
            result = (i == 1) ? convertedAxiom : new Conjunction(result,convertedAxiom);
        }
        return result;
    }

    /**
     * $C \equiv D = v(C \sqsubseteq D) \wedge v(D \sqsubseteq C)$
     * Convert concept equality under specified domain sizes
     * @param owlEquivalentClassesAxiom - Concept equality to convert under domain
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(owlEquivalentClassesAxiom);
        OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(owlEquivalentClassesAxiom);
        PropositionalFormula result = null;
        for (int i = 1; i <= DOMAIN_SIZE; i++){
            nClassExpressionConvertor convertor_n = nConvertorFactory.getClassExpressionConvertor(DOMAIN_ELEMENTS,i);
            Equality convertedEquality = new Equality(lhs.accept(convertor_n), rhs.accept(convertor_n));
            result = (i == 1) ? convertedEquality : new Conjunction(result,convertedEquality);
        }
        return result;
    }

    /**
     * Convert disjoint classes under specified domain
     * Can be represented as a concept inclusion so transformed and converted by concept inclusion
     * code
     * @param owlDisjointClassesAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLDisjointClassesAxiom owlDisjointClassesAxiom) {
        int i = 1;
        PropositionalFormula result = null;
        for(OWLSubClassOfAxiom subAxiom : owlDisjointClassesAxiom.asOWLSubClassOfAxioms()){
            result = (i++ == 1) ? subAxiom.accept(this) : new Conjunction(result,subAxiom.accept(this));
        }
        return result;
    }

    //Can optimise these conversions rather than convert straight to subclass

    /**
     * Convert OWLFunctionalObjectPropertyAxiom under specifed domain
     * Can be represented as a concept inclusion so tranformation delegated to that visitor
     * @param owlFunctionalObjectPropertyAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLFunctionalObjectPropertyAxiom owlFunctionalObjectPropertyAxiom) {
        return owlFunctionalObjectPropertyAxiom.asOWLSubClassOfAxiom().accept(this);
    }

    /**
     * Convert OWLObjectPropertyRangeAxiom under specifed domain
     * Can be represented as a concept inclusion so tranformation delegated to that visitor
     * @param owlObjectPropertyRangeAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom) {
        return owlObjectPropertyRangeAxiom.asOWLSubClassOfAxiom().accept(this);
    }

    /**
     * Convert OWLObjectPropertyDomainAxiom under specifed domain
     * Can be represented as a concept inclusion so tranformation delegated to that visitor
     * @param owlObjectPropertyDomainAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom) {
        return owlObjectPropertyDomainAxiom.asOWLSubClassOfAxiom().accept(this);
    }

    /**
     * Convert OWLInverseFunctionalObjectPropertyAxiom under specified domain
     * Can be represented as a concept inclusion so tranformation delegated to that visitor
     * @param owlInverseFunctionalObjectPropertyAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLInverseFunctionalObjectPropertyAxiom owlInverseFunctionalObjectPropertyAxiom) {
        return owlInverseFunctionalObjectPropertyAxiom.asOWLSubClassOfAxiom().accept(this);
    }

    /**
     * Convert OWLDisjointUnionAxiom under specified domain
     * Can be represented as a concept equation and disjoint classes axioms
     * joined together so delagated to those visited and joined.
     * @param owlDisjointUnionAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLDisjointUnionAxiom owlDisjointUnionAxiom) {
        return new Conjunction(owlDisjointUnionAxiom.getOWLEquivalentClassesAxiom().accept(this), owlDisjointUnionAxiom.getOWLDisjointClassesAxiom().accept(this));
    }

    /**
     * Convert OWLSubObjectPropertyOfAxiom under specified domain size
     * For each pair (d,e) \in \Delta \times \Delta create an implication r(d,e) \rightarrow s(d,e) and join
     * together all such implications with a conjunction
     * @param owlSubObjectPropertyOfAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
        OWLObjectPropertyExpression subProp = owlSubObjectPropertyOfAxiom.getSubProperty();
        OWLObjectPropertyExpression superProp = owlSubObjectPropertyOfAxiom.getSuperProperty();
        PropositionalFormula result = null;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[j]);
                Implication convertedProperty = new Implication(subProp.accept(nRoleConvertor), superProp.accept(nRoleConvertor));
                result = (i == 0 && j == 0) ? convertedProperty : new Conjunction(result,convertedProperty);
            }
        }
        return result;
    }

    /**
     * Convert OWLEquivalentObjectPropertiesAxiom under specified domain size
     * Can be represented as two OWLSubObjectPropertyOfAxiom so delegate to that visitor and join with conjunction
     * @param owlEquivalentObjectPropertiesAxiom - axiom to convert
     * @return converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {
        PropositionalFormula result = null;
        int i = 0;
        for(OWLSubObjectPropertyOfAxiom subAxiom :owlEquivalentObjectPropertiesAxiom.asSubObjectPropertyOfAxioms()){
            result = (i++ == 0) ? subAxiom.accept(this) : new Conjunction(result,subAxiom.accept(this));
        }
        return result;
    }

    /**
     * Convert OWLDisjointObjectPropertiesAxiom under specified domain size
     * May throw UnsupportedAxiomException if the object property has not exactly 2 operands - a malformed axiom
     * For each (d,e) in \Delta \time \Delta create a new implication r_(d,e) \rightarrow \neg s_(d,e) and join all
     * such implications together with a conjunction
     * @param owlDisjointObjectPropertiesAxiom axiom to convert
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLDisjointObjectPropertiesAxiom owlDisjointObjectPropertiesAxiom) {
        ArrayList<OWLObjectPropertyExpression> ops = new ArrayList<OWLObjectPropertyExpression>(owlDisjointObjectPropertiesAxiom.getProperties());
        if(ops.size() != 2){
            try {
                throw new UnsupportedAxiomException(owlDisjointObjectPropertiesAxiom);
            } catch (UnsupportedAxiomException e) {
                e.printStackTrace();
            }
        }

        PropositionalFormula result = null;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[j]);
                Implication convertedAxiom = new Implication(ops.get(0).accept(nRoleConvertor), new Negation(ops.get(1).accept(nRoleConvertor)));
                result = (i == 0 && j == 0) ? convertedAxiom : new Conjunction(result,convertedAxiom);
            }
        }
        return result;
    }

    /**
     * OWLSymmetricObjectPropertyAxiom to evaluate under interpretation
     * For all pairs (d,e) take from the domain such that d =/= e construct an implication
     * such that r_(d,e) implies r_(e,d). That is, a relation implies its inverse.
     * Join together all such pairs with a conjunction.
     * May throw UnsupportedAxiomException is the object property is already an inverse, must be simplified before conversion
     * @param owlSymmetricObjectPropertyAxiom
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLSymmetricObjectPropertyAxiom owlSymmetricObjectPropertyAxiom) {
        OWLObjectPropertyExpression property = owlSymmetricObjectPropertyAxiom.getProperty();
        // The property should never be an inverse or can probably be simplified
        if(property instanceof OWLObjectInverseOf){
            try {
                throw new UnsupportedAxiomException(owlSymmetricObjectPropertyAxiom);
            } catch (UnsupportedAxiomException e) {
                e.printStackTrace();
            }
        }

        //The case of 1 element interpretations
        PropositionalFormula result = new BooleanAtom(true);

        //For other intepretations where the domain elements are different in the relation
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[j]);
                if(i != j){
                    result = new Conjunction(result, new Equality(property.accept(nRoleConvertor), property.getInverseProperty().accept(nRoleConvertor)));
                }
            }
        }

        return result;
    }

    /**
     * To be asymmetric r and its inverse r^- must be disjoint. For each (d,e) over the domain if
     * d == e then r and its inverse may only be disjoint if \neg [r]_(d,e) is satsifiable otherwise
     * construct a equality true when r_(d,e) <=> ~r^-_(d,e). Join these together with a conjunction.
     * May throw UnsupportedAxiomException is the object property is already an inverse, must be simplified before conversion
     * @param owlAsymmetricObjectPropertyAxiom
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLAsymmetricObjectPropertyAxiom owlAsymmetricObjectPropertyAxiom) {
       OWLObjectPropertyExpression property = owlAsymmetricObjectPropertyAxiom.getProperty();
        // The property should never be an inverse or can probably be simplified
        if(property instanceof OWLObjectInverseOf){
            try {
                throw new UnsupportedAxiomException(owlAsymmetricObjectPropertyAxiom);
            } catch (UnsupportedAxiomException e) {
                e.printStackTrace();
            }
        }
        PropositionalFormula result = null;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[j]);
                if(i == 0 && j == 0){
                    result = new Negation(property.accept(nRoleConvertor));
                }
                else if(i == j){
                    result = new Conjunction(result, new Negation(property.accept(nRoleConvertor)));
                }
                else{
                    result = new Conjunction(result,
                            new Equality(property.accept(nRoleConvertor),
                            new Negation(property.getInverseProperty().accept(nRoleConvertor))));
                }
            }
        }
        return result;
    }

    /**
     * OWLInverseObjectPropertiesAxiom interpreted under the specified domain size
     * Equalities constructed for each (d,e) over the domain ensure that
     * r_(d,e) <=> s_(e,d) - s is always the inverse of r. Join all such equalities together
     * using a conjunction.
     * @param owlInverseObjectPropertiesAxiom
     * @return
     */
    @Override
    public PropositionalFormula visit(OWLInverseObjectPropertiesAxiom owlInverseObjectPropertiesAxiom) {
        OWLObjectPropertyExpression property = owlInverseObjectPropertiesAxiom.getFirstProperty();
        OWLObjectPropertyExpression inverseProperty = owlInverseObjectPropertiesAxiom.getSecondProperty().getInverseProperty().getSimplified();
        PropositionalFormula result = null;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[j]);
                Equality convertedProperty = new Equality(property.accept(nRoleConvertor), inverseProperty.accept(nRoleConvertor));
                result = (i == 0 && j == 0) ? convertedProperty : new Conjunction(result,convertedProperty);
            }
        }
        return result;
    }


    /**
     * OWLReflexiveObjectPropertyAxiom interpreted under the specified domain size
     * Every d in the domain, (d,d) must be in r. Formula asserting r_(d,d) joined together
     * with conjunctions
     * @param owlReflexiveObjectPropertyAxiom - axiom to convert
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLReflexiveObjectPropertyAxiom owlReflexiveObjectPropertyAxiom) {
        PropositionalFormula result = null;
        OWLObjectPropertyExpression property = owlReflexiveObjectPropertyAxiom.getProperty();
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[i]);
            result = (i == 0) ? property.accept(nRoleConvertor) : new Conjunction(result, property.accept(nRoleConvertor));
        }
        return result;
    }

    /**
     * OWLIrreflexiveObjectPropertyAxiom interpreted under the specified domain size
     * Every d in the domain, (d,d) must be NOT be r. Formula asserting ¬r_(d,d) joined together
     * with conjunctions
     * @param owlIrreflexiveObjectPropertyAxiom - axiom to convert
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLIrreflexiveObjectPropertyAxiom owlIrreflexiveObjectPropertyAxiom) {
        PropositionalFormula result = null;
        OWLObjectPropertyExpression property = owlIrreflexiveObjectPropertyAxiom.getProperty();
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            nElementRoleConvertor nRoleConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(DOMAIN_ELEMENTS[i], DOMAIN_ELEMENTS[i]);
            Negation convertedProperty = new Negation(property.accept(nRoleConvertor));
            result = (i == 0) ? convertedProperty : new Conjunction(result,convertedProperty);
        }
        return result;
    }

    /**
     * OWLTransitiveObjectPropertyAxiom interpreted under specified domain size
     * Assert for all pairs (d,e) and (e, e') over the domain that
     * (d,e) & (e, e') => (d,e'). By also asserting that d =/= e and e =/= e' computing
     * tautologies is avoided.
     * @param owlTransitiveObjectPropertyAxiom - axiom to convert
     * @return PropositionalFormula converted axiom
     */
    @Override
    public PropositionalFormula visit(OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
        OWLObjectPropertyExpression role = owlTransitiveObjectPropertyAxiom.getProperty();

        //One element domains
        PropositionalFormula result = new BooleanAtom(true);

        //Additionally for larger domains
        int implicationCount = 0;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                int first = DOMAIN_ELEMENTS[i];
                int second = DOMAIN_ELEMENTS[j];
                for(int d : DOMAIN_ELEMENTS){
                    //Avoid computation of tautologies
                    if(d != first && d != second){
                        nElementRoleConvertor firstConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(first,d);
                        nElementRoleConvertor secondConvertor =  nRoleConvertorFactory.getNElementRoleConvertor(d,second);
                        PropositionalFormula conjunct = new Conjunction(role.accept(firstConvertor), role.accept(secondConvertor));
                        PropositionalFormula implication = new Implication(conjunct,role.accept( nRoleConvertorFactory.getNElementRoleConvertor(first,second)));

                        result = (implicationCount++ == 0) ? implication : new Conjunction(result,implication);
                    }
                }
            }
        }

        return result;
    }


    /**
     * OWLSubPropertyChainOfAxiom interpreted under the specified domain
     * For every combination (d,e) over the domain construct an interpretation
     * such that (d,e) \in chain and (d,e) in r. Then by joining every option with
     * disjunctions and the result of that by conjuncts we obtain the resulting formula.
     * @param owlSubPropertyChainOfAxiom - axiom to convert
     * @return PropositionalFormula converted axiom under the specified domain
     */
    @Override
    public PropositionalFormula visit(OWLSubPropertyChainOfAxiom owlSubPropertyChainOfAxiom) {
        List<OWLObjectPropertyExpression> chain = owlSubPropertyChainOfAxiom.getPropertyChain();
        OWLObjectPropertyExpression role = owlSubPropertyChainOfAxiom.getSuperProperty();
        PropositionalFormula result = null;
        for (int i = 0; i < DOMAIN_ELEMENTS.length; i++) {
            for (int j = 0; j < DOMAIN_ELEMENTS.length; j++) {
                int first = DOMAIN_ELEMENTS[i];
                int second = DOMAIN_ELEMENTS[j];
                    SubPropertyChainConvertor chainConvertor = new SubPropertyChainConvertor(chain,DOMAIN_ELEMENTS,first,second);
                    PropositionalFormula convertedRoleChain = chainConvertor.getConvertedChain();
                    PropositionalFormula convertedChain = new Implication(convertedRoleChain,role.accept( nRoleConvertorFactory.getNElementRoleConvertor(first,second)));
                    result = (i == 0 && j == 0) ? convertedChain : new Conjunction(result,convertedChain);
            }
        }
        return result;
    }


    //UNSUPPORTED
    @Override
    public PropositionalFormula visit(OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLEquivalentDataPropertiesAxiom owlEquivalentDataPropertiesAxiom) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLAnnotationPropertyRangeAxiom owlAnnotationPropertyRangeAxiom) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLDisjointDataPropertiesAxiom owlDisjointDataPropertiesAxiom) {
        return null;
    }


    @Override
    public PropositionalFormula visit(OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLFunctionalDataPropertyAxiom owlFunctionalDataPropertyAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLClassAssertionAxiom owlClassAssertionAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLNegativeDataPropertyAssertionAxiom owlNegativeDataPropertyAssertionAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDeclarationAxiom owlDeclarationAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLSubAnnotationPropertyOfAxiom owlSubAnnotationPropertyOfAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLAnnotationPropertyDomainAxiom owlAnnotationPropertyDomainAxiom) {
        return null;
    }
    @Override
    public PropositionalFormula visit(OWLHasKeyAxiom owlHasKeyAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLDatatypeDefinitionAxiom owlDatatypeDefinitionAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(SWRLRule swrlRule) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLNegativeObjectPropertyAssertionAxiom owlNegativeObjectPropertyAssertionAxiom) {
        return null;
    }
    @Override
    public PropositionalFormula visit(OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom) {
        return null;
    }

    @Override
    public PropositionalFormula visit(OWLSameIndividualAxiom owlSameIndividualAxiom) {
        return null;
    }

}


