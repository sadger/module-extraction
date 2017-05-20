package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ExtendedLHSSigExtractor;
import uk.ac.liv.moduleextraction.propositional.nSeparability.nAxiomToClauseStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * To be used ONLY on acyclic terminologies with optional repeated inclusions
 * LHS can only be calculated for them
 */
public class OneElementSeparabilityAxiomLocator extends NElementSeparabilityAxiomLocator{

    AxiomDependencies dependencies;
    Set<OWLEntity> sigUnionSigM;

    public OneElementSeparabilityAxiomLocator(nAxiomToClauseStore clauseStoreMapping, OWLLogicalAxiom[] subsetAsArray, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependW) {
        super(clauseStoreMapping, subsetAsArray, sigUnionSigM);
        this.dependencies = dependW;
        this.sigUnionSigM = sigUnionSigM;
    }

    @Override
    public Collection<OWLLogicalAxiom> getCheckingSet(OWLLogicalAxiom[] input){
        ExtendedLHSSigExtractor lhsExtractor = new ExtendedLHSSigExtractor();
        return lhsExtractor.getLHSSigAxioms(Arrays.asList(input),sigUnionSigM,dependencies);
    }
}
