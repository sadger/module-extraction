/*
package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.CSVWriter;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.ox.cs.prism.PrisM;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

*/
/**
* Created by william on 05/10/15.
*//*

public class DatalogExperiment implements  Experiment {


    private PrisM prismExtractor = null;
    private Set<OWLLogicalAxiom> starModule;
    private Set<OWLLogicalAxiom> prismModule;
    private Set<OWLLogicalAxiom> hybridModule;
    private int starSize;
    private HybridExtractorExperiment hybridExpr = null;
    private Stopwatch prismWatch;
    private File sigLocation;

    public DatalogExperiment(OWLOntology ont, File originalLocation) throws OWLOntologyCreationException {
        hybridExpr = new HybridExtractorExperiment(ont,originalLocation);
        prismExtractor = new PrisM(ont, PrisM.InseparabilityRelation.MODEL_INSEPARABILITY);
    }

    @Override
    public void performExperiment(final Set<OWLEntity> signature) {
        hybridExpr.performExperiment(signature);
        starModule = hybridExpr.getStarModule();
        starSize = starModule.size();
        hybridModule = hybridExpr.getHybridModule();

        prismWatch = Stopwatch.createStarted();
        prismModule = ModuleUtils.getLogicalAxioms(prismExtractor.extract(signature));
        prismWatch.stop();
        prismExtractor.finishDisposal();

    }

    @Override
    public void performExperiment(Set<OWLEntity> sig, File sigLocation) {
        this.sigLocation = sigLocation;
        performExperiment(sig);
    }


    @Override
    public void writeMetrics(File experimentLocation) throws IOException {
        CSVWriter metricWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results.csv");

        metricWriter.addMetric("StarSize",starSize);
        metricWriter.addMetric("PrismSize",prismModule.size());
        metricWriter.addMetric("HybridSize", hybridModule.size());
        metricWriter.addMetric("StarTime", hybridExpr.getStarWatch().elapsed(TimeUnit.MILLISECONDS));
        metricWriter.addMetric("PrismTime",prismWatch.elapsed(TimeUnit.MILLISECONDS));
        metricWriter.addMetric("HybridTime", hybridExpr.getStarWatch().elapsed(TimeUnit.MILLISECONDS));

        if(sigLocation != null){
            metricWriter.addMetric("SignatureLocation",sigLocation.getAbsolutePath());
        }

        metricWriter.writeCSVFile();

    }


    public static void main(String[] args) throws OWLOntologyCreationException, IOException {

        OWLOntology o = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/LiPrO.owl");
        DatalogExperiment expr = new DatalogExperiment(o,new File("/tmp/"));

        int i = 1;
        for(OWLLogicalAxiom ax : o.getLogicalAxioms()){
            if(i++ < 5 ){
                expr.performExperiment(ax.getSignature());
                expr.writeMetrics(new File("/tmp/"));

            }

        }

    }
}
*/
