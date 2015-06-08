package uk.ac.liv.moduleextraction.experiments;


import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.ontologyutils.util.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TwoDepletingExperiment implements  Experiment {

    private final OWLOntology ontology;
    private final File originalLocation;
    private File sigLocation;
    private Set<OWLEntity> refSig;
    private boolean twoDepletingExtracted = false;
    private Set<OWLLogicalAxiom> twoDepletingModule;
    private NDepletingModuleExtractor twoDepletingExtractor;
    private Set<OWLLogicalAxiom> oneDepletingModule;
    private NDepletingModuleExtractor oneDepletingExtractor;
    private Set<OWLLogicalAxiom> hybridModule;
    private HybridExtractorExperiment hybridExperiment;
    private Set<OWLLogicalAxiom> exactlyTwoModule;

    public TwoDepletingExperiment(OWLOntology ont, File originalLocation) {
        this.ontology = ont;
        this.originalLocation = originalLocation;
    }

    @Override
    public void performExperiment(Set<OWLEntity> signature) {
        this.refSig = signature;
        hybridExperiment = new HybridExtractorExperiment(ontology,originalLocation);
        hybridExperiment.performExperiment(signature);

        hybridModule = hybridExperiment.getHybridModule();

        oneDepletingExtractor = new NDepletingModuleExtractor(1, hybridModule);
        oneDepletingModule = oneDepletingExtractor.extractModule(signature);

        if(!(oneDepletingModule.size() == hybridModule.size())) {
            twoDepletingExtracted = true;
            twoDepletingExtractor = new NDepletingModuleExtractor(2, hybridModule);

            //Extract EXACTLY 2 module then join with 1-depleting module
            twoDepletingModule = twoDepletingExtractor.extractModule(signature);
            exactlyTwoModule = new HashSet<>(twoDepletingModule);
            twoDepletingModule.addAll(oneDepletingModule);
        }

//        System.out.println("H: " + hybridModule.size());
//        System.out.println("1: " + oneDepletingModule.size());
//        System.out.println("2-dep? :" + twoDepletingExtracted);
//        if(twoDepletingExtracted){
//            System.out.println("E2E: " + (exactly2Size == twoDepletingModule.size()));
//            System.out.println("E2: " + exactly2Size);
//            System.out.println("2: " + twoDepletingModule.size());
//        }
//        System.out.println();

    }



    @Override
    public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
        this.sigLocation = signatureLocation;
        performExperiment(signature);
    }
    @Override
    public void writeMetrics(File experimentLocation) throws IOException {

        CSVWriter metricWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results.csv");
        metricWriter.addMetric("STARSize", hybridExperiment.getStarModule().size());
        metricWriter.addMetric("HybridSize", hybridModule.size());
        metricWriter.addMetric("OneDepSize", oneDepletingModule.size());
        metricWriter.addMetric("TwoDepExtracted", String.valueOf(twoDepletingExtracted).toUpperCase());
        metricWriter.addMetric("ExactlyTwoSize", (twoDepletingExtracted) ? exactlyTwoModule.size() : "NA");
        metricWriter.addMetric("TwoDepSize", (twoDepletingExtracted) ? twoDepletingModule.size() : "NA");
        metricWriter.addMetric("EqualOneTwo", (twoDepletingExtracted) ? String.valueOf(oneDepletingModule.equals(twoDepletingModule)).toUpperCase() : "NA");
        metricWriter.addMetric("EqualTwoExactlyTwo", (twoDepletingExtracted) ? String.valueOf(twoDepletingModule.equals(exactlyTwoModule)).toUpperCase() : "NA");
        metricWriter.writeCSVFile();

        ExtractionMetric oneMetric = oneDepletingExtractor.getMetrics();
        ExtractionMetric twoMetric = (twoDepletingExtracted) ? twoDepletingExtractor.getMetrics() : null;

        CSVWriter qbfWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "qbf-results.csv");
        qbfWriter.addMetric("OneQBFChecks", oneMetric.getQbfChecks());
        qbfWriter.addMetric("OneSyntacticChecks", oneMetric.getSyntacticChecks());
        qbfWriter.addMetric("OneSeparabilityAxioms", oneMetric.getSeparabilityAxiomCount());
        qbfWriter.addMetric("OneTime", oneMetric.getTimeTaken());
        qbfWriter.addMetric("TwoQBFChecks", (twoDepletingExtracted) ? twoMetric.getQbfChecks() : "NA");
        qbfWriter.addMetric("TwoSyntacticChecks", (twoDepletingExtracted) ? twoMetric.getSyntacticChecks() : "NA");
        qbfWriter.addMetric("TwoSeparabilityAxioms", (twoDepletingExtracted) ? twoMetric.getSeparabilityAxiomCount() : "NA");
        qbfWriter.addMetric("TwoTime", (twoDepletingExtracted) ? twoMetric.getTimeTaken() : "NA");
        qbfWriter.writeCSVFile();




    }

}
