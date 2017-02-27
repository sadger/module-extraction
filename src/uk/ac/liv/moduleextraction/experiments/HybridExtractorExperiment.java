package uk.ac.liv.moduleextraction.experiments;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.STARAMEXHybridExtractor;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HybridExtractorExperiment implements Experiment {

	private SyntacticLocalityModuleExtractor starExtractor;
	private STARAMEXHybridExtractor hybridExtractor;
	private int starSize = 0;
	private int itSize = 0;
	private Set<OWLLogicalAxiom> starModule;
	private Set<OWLLogicalAxiom> itModule;
	private OWLOntology ontology;
	Stopwatch hybridWatch;
	Stopwatch starWatch;
	private File location;
	private File sigLocation;


	public HybridExtractorExperiment(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.location = originalLocation;
		this.starExtractor = new SyntacticLocalityModuleExtractor(ont.getOWLOntologyManager(), ont, ModuleType.STAR);
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		starWatch = Stopwatch.createStarted();
		//Compute the star module on it's own
		Set<OWLAxiom> starAxioms = starExtractor.extract(signature);
		starWatch.stop();

		starModule = ModuleUtils.getLogicalAxioms(starAxioms);

		starSize = starModule.size();

		//Begin with the STAR module as it's the basis of the hybrid process anyway
		hybridExtractor= new STARAMEXHybridExtractor(starModule);

		hybridWatch = Stopwatch.createStarted();
		//And then the iterated one 
		itModule = hybridExtractor.extractModule(signature);
		itSize = itModule.size();
		//		
		hybridWatch.stop();
	}

	public int getIteratedSize(){
		return itSize;
	}

	public int getStarSize(){
		return starSize;
	}

	public Set<OWLLogicalAxiom> getHybridModule(){
		return itModule;
	}

	public Set<OWLLogicalAxiom> getStarModule(){
		return starModule;
	}
	
	public Stopwatch getHybridWatch() {
		return hybridWatch;
	}
	
	public Stopwatch getStarWatch() {
		return starWatch;
	}

	public int getAMEXExtractions(){ return hybridExtractor.getAmexExtractions(); }
	public int getSTARExtractions(){ return hybridExtractor.getStarExtractions(); }


	public void performExperiment(Set<OWLEntity> signature, File signatureLocation){
		this.sigLocation = signatureLocation;
		performExperiment(signature);
	}


	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		writer.write("StarSize, IteratedSize, Difference, StarExtractions, AmexExtractions, StarTime, IteratedTime, OntLocation, SigLocation" + "\n");
		writer.write(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "," +
				hybridExtractor.getStarExtractions() + "," + hybridExtractor.getAmexExtractions() + "," +
				+ starWatch.elapsed(TimeUnit.MILLISECONDS) + "," + hybridWatch.elapsed(TimeUnit.MILLISECONDS) + ","
				+ location.getAbsolutePath() + "," + sigLocation.getAbsolutePath() + "\n");
		writer.flush();
		writer.close();

	}
	
	public void printMetrics(){
		System.out.print("StarSize, IteratedSize, Difference, StarExtractions, AmexExtractions, StarTime, HybridTime" + "\n");
		System.out.print(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "," +
				hybridExtractor.getStarExtractions() + "," + hybridExtractor.getAmexExtractions()
				+ "," + 	starWatch.toString() + "," + hybridWatch.toString() + "\n");

	}



}
