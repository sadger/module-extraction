package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.CSVWriter;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by wgatens on 21/07/15.
 */
public class NDepletingExperiment implements  Experiment{

    private final HybridExtractorExperiment hybridExperiment;
    private final File originalLocation;
    private final OWLOntology ontology;
    private final int DOMAIN_SIZE;
    private Set<OWLLogicalAxiom> hybridModule;

    private ArrayList<NDepletingMetric> metrics;

    private static final Map<Integer,String>  numberAsString = ImmutableMap.of(1, "One", 2, "Two", 3, "Three", 4, "Four", 5, "Five");
    private File signatureLocation;

    private class NDepletingMetric{
        private Set<OWLLogicalAxiom> exactlynModule;
        private Set<OWLLogicalAxiom> nModule;
        private long timeTaken;

        public NDepletingMetric(Set<OWLLogicalAxiom> exactlynModule, Set<OWLLogicalAxiom> nModule, long timeTaken) {
            this.exactlynModule = exactlynModule;
            this.nModule = nModule;
            this.timeTaken = timeTaken;
        }

        public Set<OWLLogicalAxiom> getExactlyNModule() {
            return exactlynModule;
        }

        public Set<OWLLogicalAxiom> getNModule() {
            return nModule;
        }

        public long getTimeTaken() {
            return timeTaken;
        }
    }


    public NDepletingExperiment(int n, OWLOntology ont, File originalLocation){
        this.DOMAIN_SIZE = n;
        this.ontology = ont;
        this.originalLocation = originalLocation;
        this.hybridExperiment = new HybridExtractorExperiment(ont,originalLocation);
    }


    @Override
    public void performExperiment(Set<OWLEntity> signature) {
        performExperiment(signature,new File("/tmp/"));
    }

    @Override
    public void performExperiment(Set<OWLEntity> signature, File sigLocation) {
        this.signatureLocation = sigLocation;

        //RESET ALL MODULES ETC.
        this.metrics = new ArrayList<>();

        System.out.print("Extracting hybrid: ");
        hybridExperiment.performExperiment(signature);
        hybridModule = hybridExperiment.getHybridModule();
        System.out.println(hybridModule.size());

        int hybridSize = hybridModule.size();

        Set<OWLLogicalAxiom> nModule;

        int nModuleExtracted = 0;
        do{

            nModuleExtracted++;

            Stopwatch nModStopWatch = Stopwatch.createStarted();

            System.out.print("Extracting " + nModuleExtracted + "-depleting: ");
            NDepletingModuleExtractor nExtractor = new NDepletingModuleExtractor(nModuleExtracted,hybridModule);
            nModule = nExtractor.extractModule(signature);
            System.out.println(nModule.size());

            Set<OWLLogicalAxiom> exactlyNModule = new HashSet<>(nModule);

            //Create "at-least" DOMAIN_SIZE-depleting - the case where DOMAIN_SIZE = 1 is the at-least already
            if(nModuleExtracted > 1){
                nModule.addAll(metrics.get(nModuleExtracted - 2).getNModule());
            }

            nModStopWatch.stop();

            NDepletingMetric metric = new NDepletingMetric(exactlyNModule,nModule,nModStopWatch.elapsed(TimeUnit.MILLISECONDS));
            metrics.add(nModuleExtracted - 1, metric);

        } while(hybridSize != nModule.size() && nModuleExtracted < DOMAIN_SIZE);

    }

    @Override
    public void writeMetrics(File experimentLocation) throws IOException {

        CSVWriter csvWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "hybrid-metrics.csv");
        csvWriter.addMetric("StarSize", hybridExperiment.getStarSize());
        csvWriter.addMetric("HybridSize", hybridExperiment.getIteratedSize());
        csvWriter.addMetric("TimeSTAR",  hybridExperiment.getStarWatch().elapsed(TimeUnit.MILLISECONDS));
        csvWriter.addMetric("TimeHybrid",  hybridExperiment.getHybridWatch().elapsed(TimeUnit.MILLISECONDS));
        csvWriter.addMetric("HybridSTARExtractions", hybridExperiment.getSTARExtractions());
        csvWriter.addMetric("HybridAMEXExtractions", hybridExperiment.getAMEXExtractions());
        if(signatureLocation != null){
            csvWriter.addMetric("SignatureLocation", signatureLocation.getAbsolutePath());
        }
        csvWriter.writeCSVFile();


        BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "iteration-metrics.csv", false));
        writer.write("Type, Size, Time, QBFChecks, SyntacticChecks, SeparabilityAxioms" + "\n");
        for(ExtractionMetric metric : hybridExperiment.getIterationMetrics()){
            ArrayList<Object> metricList = new ArrayList<Object>();
            metricList.add(metric.getType());
            metricList.add(metric.getModuleSize());
            metricList.add(metric.getTimeTaken());
            metricList.add(metric.getQbfChecks());
            metricList.add(metric.getSyntacticChecks());
            metricList.add(metric.getSeparabilityAxiomCount());
            writer.write(Joiner.on(',').join(metricList));
            writer.write("\n");
        }
        writer.close();

        CSVWriter metricWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "n-depleting-metrics.csv");
        metricWriter.addMetric("STARSize", hybridExperiment.getStarModule().size());
        metricWriter.addMetric("HybridSize", hybridModule.size());
        //To ensure equal number of fields in the CSV file print a column even if the module wasn't extracted
        for (int i = 1; i <= DOMAIN_SIZE; i++) {
            String mod_value = (i <= metrics.size()) ? String.valueOf(metrics.get(i-1).getNModule().size()) : "NA";
            metricWriter.addMetric(numberAsString.get(i) + "Size", mod_value);
        }
        for (int i = 1; i <= DOMAIN_SIZE; i++) {
            String time_value = (i <= metrics.size()) ? String.valueOf(metrics.get(i-1).getTimeTaken()) : "NA";
            metricWriter.addMetric(numberAsString.get(i) + "Time", time_value);
        }
        if(signatureLocation != null){
            metricWriter.addMetric("SignatureLocation", signatureLocation.getAbsolutePath());
        }
        metricWriter.writeCSVFile();

        CSVWriter exactlyWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "exactly-n-depleting-metrics.csv");
        //To ensure equal number of fields in the CSV file print a column even if the module wasn't extracted
        for (int i = 1; i <= DOMAIN_SIZE; i++) {
            String exactly_value = (i <= metrics.size()) ? String.valueOf(metrics.get(i-1).getExactlyNModule().size()) : "NA";
            exactlyWriter.addMetric("Exactly" + numberAsString.get(i) + "Size", exactly_value);

        }
        exactlyWriter.writeCSVFile();
    }

    public static void main(String[] args) throws IOException {

        File ontLocation = new File(ModulePaths.getOntologyLocation() + "OWL-Corpus-All/qbf-only/"
                + "51120da9-3598-4b41-93ed-1afbc0503afa_estIND.owl-QBF");

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLocation.getAbsolutePath());

        for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
            Set<OWLEntity> sig = axiom.getSignature();
            NDepletingExperiment expr = new NDepletingExperiment(2,ont,ontLocation);
            expr.performExperiment(sig);
            expr.writeMetrics(new File("/tmp/xxx"));
        }


    }



}
