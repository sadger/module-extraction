package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.filters.ALCQIwithAtomicLHSFilter;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.util.AxiomStructureInspector;

import java.util.Set;


public class STARAMEXHybridExtractor extends AbstractHybridExtractor {

    private int starExtractions;
    private int amexExtractions;
    private boolean useImprovedCycleRemoval = true;

    public STARAMEXHybridExtractor(Set<OWLLogicalAxiom> ont) {
        super(ont);
    }

    public void setUseImprovedCycleRemoval(boolean v){
        useImprovedCycleRemoval = v;
    }

    @Override
    Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature) {
        starExtractions++;
        STARExtractor starExtractor = new STARExtractor(module);
        return starExtractor.extractModule(signature);
    }

    @Override
    Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature) {
        amexExtractions++;

        //Remove any axioms which cause ontology to not be an acyclic ALCQI terminology with RCIs
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
        //Locate all axioms that are not ALCQI or do not have an atomic LHS..
        filters.addFilter(new ALCQIwithAtomicLHSFilter());
        //.. and those axioms with shared names ..
        filters.addFilter(new SharedNameFilter(inspector));
        //.. finally any repeated equalities
        filters.addFilter(new RepeatedEqualitiesFilter(inspector));
        return filters.getUnsupportedAxioms(axioms);
    }

    private Set<OWLLogicalAxiom> getCycleCausingAxioms(Set<OWLLogicalAxiom> axioms){
        OntologyCycleVerifier cycleVerifier = new OntologyCycleVerifier(axioms);
        return cycleVerifier.getCycleCausingAxioms(useImprovedCycleRemoval);
    }

    public int getAmexExtractions() {
        return amexExtractions;
    }

    public int getStarExtractions() {
        return starExtractions;
    }

}
