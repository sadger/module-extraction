package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class TerminologyValidator {


    private final Collection<OWLLogicalAxiom> axioms;
    private boolean isTerminology = true;
    private boolean isTerminologyWithRCIs = true;

    private HashMap<OWLClass, Integer> inclusionNameCount = new HashMap<>();
    private HashMap<OWLClass, Integer> equalityNameCount = new HashMap<>();

    public TerminologyValidator(Collection<OWLLogicalAxiom> axioms){
        this.axioms = axioms;
        processAxioms();
    }

    /**
     * Is the input a terminology
     * @return true if it is
     */
    public boolean isTerminology(){
        return isTerminology;
    }

    /**
     * Is the ontology logically equivalent to a terminology
     * i.e is it a terminology with (optional) RCIs
     * @return true if it is
     */
    public boolean isTerminologyWithRCIs(){
        return isTerminologyWithRCIs;
    }

    private boolean isComplexAxiom(){
        for(OWLLogicalAxiom ax : axioms){
           if(!(ax.getAxiomType() == AxiomType.SUBCLASS_OF) &&
                   !(ax.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)){
                return true;
           }
           OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(ax);
           if(!(lhs.getClassExpressionType() == ClassExpressionType.OWL_CLASS)){
               return true;
           }
        }
        return false;
    }

    private void processAxioms(){
        //Can't be a terminology with complex axioms
        if(isComplexAxiom()){
            isTerminology = false;
            isTerminologyWithRCIs = false;
        }
        else{
            inspectAxiomLHS();

            Predicate<OWLClass> repeatedEquality = e -> equalityNameCount.get(e) > 1;
            Predicate<OWLClass> repeatedInclusion = e -> inclusionNameCount.get(e) > 1;

            //No shared names or repeated equalities
            if(containsSharedNames() || equalityNameCount.keySet().stream().anyMatch(repeatedEquality)){
                isTerminology = false;
                isTerminologyWithRCIs = false;
            }
            //Has some repeated inclusions
            else if(inclusionNameCount.keySet().stream().anyMatch(repeatedInclusion)){
                isTerminology = false;
                isTerminologyWithRCIs = true;
            }
            else{
                //Is a terminology hence is a terminology with RCIs
                isTerminology = true;
                isTerminologyWithRCIs = true;
            }

        }

        System.out.println(inclusionNameCount);
        System.out.println(equalityNameCount);


    }

    private void inspectAxiomLHS(){
        for(OWLLogicalAxiom ax : axioms){
            OWLClass lhs = (OWLClass) AxiomSplitter.getNameofAxiom(ax);

            if(ax instanceof OWLSubClassOfAxiom){

                Integer i = inclusionNameCount.get(lhs);
                if(i == null){
                    inclusionNameCount.put(lhs, 1);
                }
                else{
                    inclusionNameCount.put(lhs, ++i);

                }
            }
            else if(ax instanceof OWLEquivalentClassesAxiom){
                Integer i = equalityNameCount.get(lhs);
                if(i == null){
                    equalityNameCount.put(lhs,1);
                }
                else{
                    equalityNameCount.put(lhs, ++i);
                }
            }

        }
    }

    private boolean containsSharedNames(){
        Set<OWLClass> incClasses = new HashSet<>(inclusionNameCount.keySet());
        Set<OWLClass> equivClasses = new HashSet<>(equalityNameCount.keySet());

        incClasses.retainAll(equivClasses);
        return !incClasses.isEmpty();
    }


}
