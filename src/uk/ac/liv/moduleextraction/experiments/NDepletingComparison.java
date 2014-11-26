package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		int qbfSmaller = (nDepletingModule.size() < starAndHybridExperiment.getHybridModule().size()) ? 1 : 0;

		writer.write("StarSize, HybridSize, DomainSize, NDepletingSize, NDepletingSmaller, TimeNDepleting, TimeHybrid, SignatureLocation" + "\n");
		writer.write(starAndHybridExperiment.getStarSize() + "," + starAndHybridExperiment.getIteratedSize() + 
				"," + String.valueOf(DOMAIN_SIZE) + "," + nDepletingModule.size() + "," + String.valueOf(qbfSmaller) + ","
				+ nDepletingStopwatch.elapsed(TimeUnit.MILLISECONDS) + "," + starAndHybridExperiment.getHybridWatch().elapsed(TimeUnit.MILLISECONDS) +
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}


}





