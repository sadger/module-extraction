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

import uk.ac.liv.moduleextraction.extractor.NewIteratingExtractor;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class NewIteratingExperiment implements Experiment {

	private SyntacticLocalityModuleExtractor starExtractor;
	private NewIteratingExtractor iteratingExtractor;
	private int starSize = 0;
	private int itSize = 0;
	private Set<OWLLogicalAxiom> starModule;
	private Set<OWLLogicalAxiom> itModule;
	private OWLOntology ontology;
	Stopwatch iteratedWatch;
	Stopwatch starWatch;
	private File location;
	private File sigLocation;


	public NewIteratingExperiment(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.location = originalLocation;
		OWLOntologyManager manager = ont.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ont, ModuleType.STAR);
		this.iteratingExtractor = new NewIteratingExtractor(ont);
	}



	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		

		starWatch = new Stopwatch().start();
		//Compute the star module on it's own
		Set<OWLAxiom> starAxioms = starExtractor.extract(signature);
		starWatch.stop();
		
		starModule = ModuleUtils.getLogicalAxioms(starAxioms);

		starSize = starModule.size();
		

		iteratedWatch = new Stopwatch().start();
		//And then the iterated one 
		itModule = iteratingExtractor.extractModule(signature);
		itSize = itModule.size();
//		
		iteratedWatch.stop();

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
	
	
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation){
		this.sigLocation = signatureLocation;
		performExperiment(signature);
	}
	

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		writer.write("StarSize, IteratedSize, Difference, StarExtractions, AmexExtractions, StarTime, IteratedTime, OntLocation, SigLocation" + "\n");
		writer.write(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "," +
				iteratingExtractor.getStarExtractions() + "," + iteratingExtractor.getAmexExtrations() + "," + 
				+ starWatch.elapsed(TimeUnit.MILLISECONDS) + "," + iteratedWatch.elapsed(TimeUnit.MILLISECONDS) + ","
				+ location.getAbsolutePath() + "," + sigLocation.getAbsolutePath() + "\n");
		writer.flush();
		writer.close();

	}


}
