package uk.ac.liv.moduleextraction;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;

import java.util.Comparator;

/**
 * Fixes the order of axioms in a list based on lexographic class name
 * i.e A ≡ B1 ⊓ B2 comes before B1 ≡ B2 and so on.
 * Used mainly for tests to fix the order of the axioms in a list so we can compare them by index to the
 * expected and actual output
 *
 */
public class AxiomNameComparator implements Comparator<OWLLogicalAxiom>{
    @Override
    public int compare(OWLLogicalAxiom ax1, OWLLogicalAxiom ax2) {
        OWLClassExpression exp1 = AxiomSplitter.getNameofAxiom(ax1);
        OWLClassExpression exp2 = AxiomSplitter.getNameofAxiom(ax2);
        OWLClassExpression exp3 = AxiomSplitter.getDefinitionofAxiom(ax1);
        OWLClassExpression exp4 = AxiomSplitter.getDefinitionofAxiom(ax2);

        return exp1.toString().compareTo(exp2.toString());
    }
}
