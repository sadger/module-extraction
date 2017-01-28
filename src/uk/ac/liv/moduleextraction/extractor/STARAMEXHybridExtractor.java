package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.util.AxiomStructureInspector;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class STARAMEXHybridExtractor extends AbstractHybridExtractor {

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

    public static void main(String[] args) {
        OWLOntology food = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/distribution/food.owl");
        ModuleUtils.remapIRIs(food, "X");

        food.getLogicalAxioms().forEach(System.out::println);
        OWLDataFactory f = food.getOWLOntologyManager().getOWLDataFactory();

        OWLObjectProperty hasFood = f.getOWLObjectProperty(IRI.create("X#hasFood"));

        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(hasFood));

        System.out.println(food.getSignature().contains(hasFood));

        STARAMEXHybridExtractor starAmex = new STARAMEXHybridExtractor(food.getLogicalAxioms());
        Set<OWLLogicalAxiom> module = starAmex.extractModule(sig);

        STARExtractor extractor = new STARExtractor(food.getLogicalAxioms());
        System.out.println(extractor.extractModule(sig));

        System.out.println(module);

    }

}
