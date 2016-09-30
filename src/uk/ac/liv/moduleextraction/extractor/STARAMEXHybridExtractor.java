package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;

import java.util.Set;

/**
 * Created by william on 30/09/16.
 */
public class STARAMEXHybridExtractor extends GenericHybridExtractor{

    public STARAMEXHybridExtractor(Set<OWLLogicalAxiom> ont) {
        super(ont);
    }

    @Override
    Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature) {
        STARExtractor starExtractor = new STARExtractor(module);
        return starExtractor.extractModule(signature);
    }

    @Override
    Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature) {
        Set<OWLLogicalAxiom> unsupported = getUnsupportedAxioms(module);
        module.removeAll(unsupported);
        Set<OWLLogicalAxiom> cycleCausing = getCycleCausingAxioms(module);
        module.removeAll(cycleCausing);
        unsupported.addAll(cycleCausing);

        return new AMEX(module).extractModule(unsupported,signature);
    }

    private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms){
        OntologyFilters filters = new OntologyFilters();
        AxiomStructureInspector inspector = new AxiomStructureInspector(axioms);
        filters.addFilter(new SupportedExpressivenessFilter());
        filters.addFilter(new SharedNameFilter(inspector));
        filters.addFilter(new RepeatedEqualitiesFilter(inspector));
        return filters.getUnsupportedAxioms(axioms);
    }

    private Set<OWLLogicalAxiom> getCycleCausingAxioms(Set<OWLLogicalAxiom> axioms){
        OntologyCycleVerifier cycleVerifier = new OntologyCycleVerifier(axioms);
        return cycleVerifier.getCycleCausingAxioms();
    }


}
