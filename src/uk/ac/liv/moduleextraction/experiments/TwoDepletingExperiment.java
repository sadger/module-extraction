package uk.ac.liv.moduleextraction.experiments;


import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.CSVWriter;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TwoDepletingExperiment implements  Experiment {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
        this.hybridExperiment = new HybridExtractorExperiment(ontology,originalLocation);
    }

    @Override
    public void performExperiment(Set<OWLEntity> signature) {

        oneDepletingModule = new HashSet<>();
        twoDepletingModule = new HashSet<>();
        exactlyTwoModule = new HashSet<>();
        twoDepletingExtracted = false;

        final ScheduledFuture<?> dumpHandler =
                scheduler.scheduleAtFixedRate(dumpExtraction,10,60,TimeUnit.MINUTES);

        this.refSig = signature;

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

        dumpHandler.cancel(true);

/*        System.out.println("H: " + hybridModule.size());
        System.out.println("1: " + oneDepletingModule.size());
        System.out.println("2-dep? :" + twoDepletingExtracted);
        if(twoDepletingExtracted){
            System.out.println("E2E: " + (exactlyTwoModule.size() == twoDepletingModule.size()));
            System.out.println("E2: " + exactlyTwoModule.size());
            System.out.println("2: " + twoDepletingModule.size());
        }
        System.out.println();*/

    }


    private Runnable dumpExtraction = new Runnable() {
        @Override
        public void run() {
            System.out.println("Hybrid: " + hybridModule.size());
            System.out.println("One: " + oneDepletingExtractor.getModule().size());
            System.out.println("OneElapsedTime: " + ((oneDepletingExtractor != null) ? oneDepletingExtractor.getStopwatch() : "Not Started"));
            System.out.println("Two: " + ((twoDepletingExtractor != null) ? twoDepletingExtractor.getModule().size() : "Not Started"));
            System.out.println("TwoElapsedTime: " + ((twoDepletingExtractor != null) ? twoDepletingExtractor.getStopwatch() : "Not Started"));
            System.out.println();
        }
    };

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

    public static void main(String[] args) throws IOException {
        File ontFile = new File(ModulePaths.getOntologyLocation() + "star-nci.owl");
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());

        TwoDepletingExperiment dep = new TwoDepletingExperiment(ont,ontFile);


        SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/two-depleting/" + "Thesaurus_15.04d.owl"));
        Set<OWLEntity> sig = man.readFile("axiom-1044382871");

        dep.performExperiment(sig);
        dep.writeMetrics(new File("/tmp"));

    }

}
