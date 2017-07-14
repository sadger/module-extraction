package uk.ac.liv.moduleextraction.extractor;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.filters.ELIwithAtomicLHSFilter;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.util.AxiomStructureInspector;

import java.util.Set;


public class STARMEXHybridExtractor extends AbstractHybridExtractor {

    private int starExtractions;
    private int mexExtractions;
    private Set<OWLLogicalAxiom> starModule;

    Logger logger = LoggerFactory.getLogger(STARMEXHybridExtractor.class);

    public STARMEXHybridExtractor(Set<OWLLogicalAxiom> ont) {
        super(ont);
    }

    public STARMEXHybridExtractor(OWLOntology ont) {
        this(OWLAPIStreamUtils.asSet(ont.logicalAxioms()));
    }


    @Override
    Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature) {
        STARExtractor starExtractor = new STARExtractor(module);
        ++starExtractions;
        Set<OWLLogicalAxiom> module = starExtractor.extractModule(signature);
        //The STAR module is the same as the first extraction in the hybrid module
        if (starExtractions == 1) {
            this.starModule = ImmutableSet.copyOf(module);
        }
        return module;
    }

    @Override
    Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature) {
        mexExtractions++;

        //Remove any axioms which cause ontology to not be an acyclic ELI terminology with RCIs
        Set<OWLLogicalAxiom> unsupported = getUnsupportedAxioms(module);
        module.removeAll(unsupported);
        Set<OWLLogicalAxiom> cycleCausing = getCycleCausingAxioms(module);
        module.removeAll(cycleCausing);
        unsupported.addAll(cycleCausing);

        MEX mex = null;
        try{
          mex = new MEX(module);
        }
        catch (ExtractorException e){
            e.printStackTrace();
        }

        return mex.extractModule(unsupported, signature);
    }

    @Override
    void resetMetrics() {
        starExtractions = 0;
        mexExtractions = 0;
    }

    private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms){
        OntologyFilters filters = new OntologyFilters();
        AxiomStructureInspector inspector = new AxiomStructureInspector(axioms);
        //Locate all axioms that are not ELI or do not have an atomic LHS..
        filters.addFilter(new ELIwithAtomicLHSFilter());
        //.. and those axioms with shared names ..
        filters.addFilter(new SharedNameFilter(inspector));
        //.. finally any repeated equalities
        filters.addFilter(new RepeatedEqualitiesFilter(inspector));
        Set<OWLLogicalAxiom> unsupportedAxioms = filters.getUnsupportedAxioms(axioms);
        logger.trace("Unsupported EL axioms: {}", unsupportedAxioms);
        return unsupportedAxioms;
    }

    private Set<OWLLogicalAxiom> getCycleCausingAxioms(Set<OWLLogicalAxiom> axioms){
        OntologyCycleVerifier cycleVerifier = new OntologyCycleVerifier(axioms);
        Set<OWLLogicalAxiom> cycleCausingAxioms = cycleVerifier.getCycleCausingAxioms();
        logger.trace("Cycle causing axioms: {}", cycleCausingAxioms);
        return cycleCausingAxioms;
    }

    public Set<OWLLogicalAxiom> getStarModule() {
        return starModule;
    }



    public int getMexExtractions() {
        return mexExtractions;
    }

    public int getStarExtractions() {
        return starExtractions;
    }

}
