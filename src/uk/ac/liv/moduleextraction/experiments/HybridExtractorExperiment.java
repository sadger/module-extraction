package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
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

public class HybridExtractorExperiment implements Experiment {

	private SyntacticLocalityModuleExtractor starExtractor;
	private HybridModuleExtractor iteratingExtractor;
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
		OWLOntologyManager manager = ont.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ont, ModuleType.STAR);
		this.iteratingExtractor = new HybridModuleExtractor(ont, CycleRemovalMethod.NAIVE);
	}



	@Override
	public void performExperiment(Set<OWLEntity> signature) {


		starWatch = new Stopwatch().start();
		//Compute the star module on it's own
		Set<OWLAxiom> starAxioms = starExtractor.extract(signature);
		starWatch.stop();

		starModule = ModuleUtils.getLogicalAxioms(starAxioms);

		starSize = starModule.size();


		hybridWatch = new Stopwatch().start();
		//And then the iterated one 
		itModule = iteratingExtractor.extractModule(signature);
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
				+ starWatch.elapsed(TimeUnit.MILLISECONDS) + "," + hybridWatch.elapsed(TimeUnit.MILLISECONDS) + ","
				+ location.getAbsolutePath() + "," + sigLocation.getAbsolutePath() + "\n");
		writer.flush();
		writer.close();

	}
	
	public void printMetrics(){
		System.out.print("StarSize, IteratedSize, Difference, StarExtractions, AmexExtractions, StarTime, HybridTime" + "\n");
		System.out.print(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "," +
				iteratingExtractor.getStarExtractions() + "," + iteratingExtractor.getAmexExtrations() 
				+ "," + 	starWatch.toString() + "," + hybridWatch.toString() + "\n");

	}

	public static void main(String[] args) throws IOException {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/cyclicdiff2.krss");
		System.out.println(ont);
		ModuleUtils.remapIRIs(ont, "X");
	
		OWLDataFactory f = ont.getOWLOntologyManager().getOWLDataFactory();
		
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		OWLClass a = f.getOWLClass(IRI.create("X#A"));
		OWLClass b = f.getOWLClass(IRI.create("X#B"));
		OWLClass c = f.getOWLClass(IRI.create("X#C"));
		sig.add(c);
		sig.add(b);
		
		System.out.println("Sig: " + sig);
		
		OneDepletingComparison expr = new OneDepletingComparison(ont, null);
		expr.performExperiment(sig);
		expr.printMetrics();
		
	}


}
