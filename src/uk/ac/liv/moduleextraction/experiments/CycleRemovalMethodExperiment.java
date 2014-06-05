package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Stopwatch;

import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor;
import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor.CycleRemovalMethod;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class CycleRemovalMethodExperiment implements Experiment {

	private OWLOntology ontology;
	private HybridModuleExtractor naiveCycleRemovalExtractor;
	private HybridModuleExtractor improvedCycleRemovalExtractor;
	private Set<OWLLogicalAxiom> naiveModule;
	private Set<OWLLogicalAxiom> improvedModule;
	private SyntacticLocalityModuleExtractor starExtractor;
	private Set<OWLLogicalAxiom> starModule;
	private int starSize;
	private Stopwatch starWatch;
	private Stopwatch naiveWatch;
	private Stopwatch improvedWatch;
	private File sigLocation;
	private File location;
	
	public CycleRemovalMethodExperiment(OWLOntology ontology, File ontLocation) {
		this.ontology = ontology;
		this.location = ontLocation;
		this.naiveCycleRemovalExtractor = new HybridModuleExtractor(ontology, CycleRemovalMethod.NAIVE);
		this.improvedCycleRemovalExtractor = new HybridModuleExtractor(ontology, CycleRemovalMethod.IMPROVED);
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		
		starWatch = new Stopwatch();
		starWatch.start();
		Set<OWLAxiom> starAxioms = starExtractor.extract(signature);
		starWatch.stop();
		starModule = ModuleUtils.getLogicalAxioms(starAxioms);
		starSize = starModule.size();
		
		naiveWatch = new Stopwatch();
		naiveWatch.start();
		naiveModule = naiveCycleRemovalExtractor.extractModule(signature);
		naiveWatch.stop();
		
		improvedWatch = new Stopwatch();
		improvedWatch.start();
		improvedModule = improvedCycleRemovalExtractor.extractModule(signature);
		improvedWatch.stop();

	}



	@Override
	public void performExperiment(Set<OWLEntity> sig, File sigLocation) {
		this.sigLocation = sigLocation;
		performExperiment(sig);
	}
	
	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		int naiveSize = naiveModule.size();
		int improvedSize = improvedModule.size();
		writer.write("StarSize, HybridNaiveSize, HybridImprovedSize, "
				+ "SmallerImprovedStar, SmallerImprovedNaive, "
				+ "StarTime, NaiveTime, ImprovedTime, SigLocation, OntLocation" + "\n");
		writer.write(starSize + "," + naiveSize + "," + improvedSize 
				+ "," + ((starSize == improvedSize) ? "0" : "1") + "," 
				+ ((improvedSize == naiveSize) ? "0" : "1") + "," 
				+ starWatch.elapsed(TimeUnit.MILLISECONDS) + "," +  naiveWatch.elapsed(TimeUnit.MILLISECONDS) + "," 
				+ improvedWatch.elapsed(TimeUnit.MILLISECONDS) + 
				location.getAbsolutePath() + ","  + sigLocation.getAbsolutePath() + "\n");
		
		writer.close();
	}
	
	public void printMetrics(){
		int naiveSize = naiveModule.size();
		int improvedSize = improvedModule.size();
		System.out.print("StarSize, HybridNaiveSize, HybridImprovedSize, SmallerImprovedStar, SmallerImprovedNaive, StarTime, NaiveTime, ImprovedTime" + "\n");
		System.out.print(starSize + "," + naiveSize + "," + improvedSize 
				+ "," + ((starSize == improvedSize) ? "0" : "1") + "," 
				+ ((improvedSize == naiveSize) ? "0" : "1") + "," 
				+ starWatch.elapsed(TimeUnit.MILLISECONDS) + "," +  naiveWatch.elapsed(TimeUnit.MILLISECONDS) + "," 
				+ improvedWatch.elapsed(TimeUnit.MILLISECONDS) + "\n");
	}
	

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Profile/Thesaurus_14.05d.owl-core");
		System.out.println("Loaded");
		
		CycleRemovalMethodExperiment iterating = new CycleRemovalMethodExperiment(ont,null);
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		
		for (int i = 0; i < 100; i++) {
			iterating.performExperiment(gen.generateRandomSignature(1000));
			iterating.printMetrics();
		}

	}
}
