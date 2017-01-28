package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.util.CSVWriter;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExactlyNDepletingComparison implements Experiment {


	private File originalLocation;
	private File sigLocation;

	private OWLOntology ontology;
	private Set<OWLEntity> refsig;

	private HybridExtractorExperiment starAndHybridExperiment;
	private NDepletingModuleExtractor nDepletingModuleExtractor;
	private Set<OWLLogicalAxiom> nDepletingModule;


	private Stopwatch nDepletingStopwatch;

	private final int DOMAIN_SIZE;

	public ExactlyNDepletingComparison(int domain_size, OWLOntology ont, File originalLocation) {
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

		nDepletingStopwatch = Stopwatch.createStarted();

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

	public int getDomainSize() {
		return DOMAIN_SIZE;
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
		csvWriter.addMetric("HybridSTARExtractions",starAndHybridExperiment.getSTARExtractions());
		csvWriter.addMetric("HybridAMEXExtractions",starAndHybridExperiment.getAMEXExtractions());
		if(sigLocation != null){
			csvWriter.addMetric("SignatureLocation", sigLocation.getAbsolutePath());
		}

		csvWriter.writeCSVFile();

		ExtractionMetric nDepMetrics = nDepletingModuleExtractor.getMetrics();

		csvWriter = new CSVWriter(experimentLocation.getAbsoluteFile() + "/" + "n-depleting-metrics.csv");
		csvWriter.addMetric("DomainSize", DOMAIN_SIZE);
		csvWriter.addMetric("Size", nDepMetrics.getModuleSize());
		csvWriter.addMetric("Time", nDepMetrics.getTimeTaken());
		csvWriter.addMetric("QBFChecks", nDepMetrics.getQbfChecks());
		csvWriter.addMetric("SyntacticChecks", nDepMetrics.getSyntacticChecks());
		csvWriter.addMetric("SeparabilityAxioms", nDepMetrics.getSeparabilityAxiomCount());

		csvWriter.writeCSVFile();



	}

    public void printMetrics() throws IOException {
        System.out.println("S/H/D " + starAndHybridExperiment.getStarSize() + "," + starAndHybridExperiment.getIteratedSize() + "," + nDepletingModule.size());

        System.out.println("Entities:" + ModuleUtils.getClassAndRoleNamesInSet(starAndHybridExperiment.getHybridModule()));

        System.out.println("Σ: " + refsig);


        for(OWLLogicalAxiom ax : starAndHybridExperiment.getHybridModule()){
            System.out.println(ax);
        }
        System.out.println("=============");
        for(OWLLogicalAxiom ax : nDepletingModule){
            System.out.println(ax);
        }

        System.out.println("Difference:");
        Set<OWLLogicalAxiom> hybrid = starAndHybridExperiment.getHybridModule();
       //ModuleUtils.writeOntology(hybrid,ModulePaths.getOntologyLocation() + "/dep-explore.owl");
//        SigManager man = new SigManager(new File(ModulePaths.getOntologyLocation()));
//        man.writeFile(refsig,"explore");

        hybrid.removeAll(nDepletingModule);
        System.out.println("=============");
        for(OWLLogicalAxiom ax : hybrid){
            System.out.println(ax);
        }


    }


}





