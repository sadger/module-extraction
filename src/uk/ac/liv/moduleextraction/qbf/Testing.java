package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by william on 27/11/14.
 */
public class Testing {
    Set<OWLLogicalAxiom> mod = new HashSet<OWLLogicalAxiom>();
    public void doThings(Set<OWLLogicalAxiom> axioms, Set<OWLEntity> sig) throws IOException, QBFSolverException, ExecutionException {
        NElementInseparableChecker insep = new NElementInseparableChecker(1);
        System.out.println(axioms);
        if(insep.isSeparableFromEmptySet(axioms,sig)){
            IncrementalSeparabilityAxiomLocator loc = new IncrementalSeparabilityAxiomLocator(
                    2,
                    axioms.toArray(new OWLLogicalAxiom[axioms.size()]),
                    sig);
            OWLLogicalAxiom ax = loc.findSeparabilityCausingAxiom();
            System.out.println(ax);
            mod.add(ax);
            axioms.remove(ax);
            sig.addAll(ax.getSignature());
            doThings(axioms,sig);
        }
        System.out.println(mod);
    }
}
