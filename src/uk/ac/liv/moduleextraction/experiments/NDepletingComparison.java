package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.ontologyutils.util.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NDepletingComparison implements Experiment {


	private File originalLocation;
	private File sigLocation;

	private OWLOntology ontology;
	private Set<OWLEntity> refsig;

	private HybridExtractorExperiment starAndHybridExperiment;
	private NDepletingModuleExtractor nDepletingModuleExtractor;
	private Set<OWLLogicalAxiom> nDepletingModule;

	private Stopwatch nDepletingStopwatch;

	private final int DOMAIN_SIZE;

	public NDepletingComparison(int domain_size, OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
		this.DOMAIN_SIZE = domain_size;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		this.refsig = signature;
		starAndHybridExperiment = 
				new HybridExtractorExperiment(ontology,originalLocation);

		starAndHybridExperiment.performExperiment(signature);

		Set<OWLLogicalAxiom> hybridModule = starAndHybridExperiment.getHybridModule();

		nDepletingStopwatch = new Stopwatch().start();

		NDepletingModuleExtractor nDepletingModuleExtractor = new NDepletingModuleExtractor(DOMAIN_SIZE,hybridModule);
		this.nDepletingModuleExtractor = nDepletingModuleExtractor;
		nDepletingModule = nDepletingModuleExtractor.extractModule(signature);

		nDepletingStopwatch.stop();

	}

	public Set<OWLLogicalAxiom> getNDepletingModule(){
		return nDepletingModule;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
		this.sigLocation = signatureLocation;
		performExperiment(signature);
	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		int ndepletingSmaller = (nDepletingModule.size() < starAndHybridExperiment.getHybridModule().size()) ? 1 : 0;

		CSVWriter csvWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results.csv");
		csvWriter.addMetric("DomainSize", DOMAIN_SIZE);
		csvWriter.addMetric("StarSize", starAndHybridExperiment.getStarSize());
		csvWriter.addMetric("HybridSize", starAndHybridExperiment.getIteratedSize());
		csvWriter.addMetric("NDepletingSize", nDepletingModule.size());
		csvWriter.addMetric("NDepletingSmaller", ndepletingSmaller);
		csvWriter.addMetric("TimeSTAR", starAndHybridExperiment.getStarWatch().elapsed(TimeUnit.MILLISECONDS));
		csvWriter.addMetric("TimeHybrid", starAndHybridExperiment.getHybridWatch().elapsed(TimeUnit.MILLISECONDS));
		csvWriter.addMetric("TimeNDepleting", nDepletingStopwatch.elapsed(TimeUnit.MILLISECONDS));
		csvWriter.addMetric("SignatureLocation", sigLocation.getAbsolutePath());

		csvWriter.writeCSVFile();


		CSVWriter additionalMetrics = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "extra-metrics.csv");
		additionalMetrics.addMetric("HybridSTARExtractions",1);
		additionalMetrics.addMetric("HybridAMEXExtractions",1);
		additionalMetrics.addMetric("HybridQBFChecks",1);
		additionalMetrics.addMetric("NDepletingQBFChecks", 1);

		csvWriter.writeCSVFile();
	}


}





