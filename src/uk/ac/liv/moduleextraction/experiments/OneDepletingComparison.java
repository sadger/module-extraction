package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.extractor.OneDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.extractor.AcyclicOneDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class OneDepletingComparison implements Experiment {


	private File originalLocation;
	private File sigLocation;

	private OWLOntology ontology;
	private Set<OWLEntity> refsig;

	private HybridExtractorExperiment starAndHybridExperiment;
	private OneDepletingModuleExtractor oneDepletingExtractor;
	private Set<OWLLogicalAxiom> oneDepletingModule;

	private Stopwatch oneDepletingStopwatch;

	public OneDepletingComparison(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		this.refsig = signature;
		starAndHybridExperiment = 
				new HybridExtractorExperiment(ontology,originalLocation);

		starAndHybridExperiment.performExperiment(signature);


		Set<OWLLogicalAxiom> hybridModule = starAndHybridExperiment.getHybridModule();

		oneDepletingStopwatch = Stopwatch.createStarted();
		oneDepletingExtractor = new OneDepletingModuleExtractor(hybridModule);
		oneDepletingModule = oneDepletingExtractor.extractModule(signature);
		oneDepletingStopwatch.stop();


	}

	public Set<OWLLogicalAxiom> get1DepletingModule(){
		return oneDepletingModule;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
		this.sigLocation = signatureLocation;

		performExperiment(signature);

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		int qbfSmaller = (oneDepletingModule.size() < starAndHybridExperiment.getHybridModule().size()) ? 1 : 0;

		writer.write("StarSize, HybridSize, QBFSize, QBFSmaller, TimeQBF, TimeHybrid, SignatureLocation" + "\n");
		writer.write(starAndHybridExperiment.getStarSize() + "," + starAndHybridExperiment.getIteratedSize() + 
				"," + oneDepletingModule.size() + "," + String.valueOf(qbfSmaller) + "," 
				+ oneDepletingStopwatch.elapsed(TimeUnit.MILLISECONDS) + "," + starAndHybridExperiment.getHybridWatch().elapsed(TimeUnit.MILLISECONDS) + 
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}

	public void printMetrics() throws IOException{
	
		System.out.println("StarSize, HybridSize, QBFSize");
		System.out.println(starAndHybridExperiment.getStarSize() + "," + starAndHybridExperiment.getIteratedSize() + 
				"," + oneDepletingModule.size());
		System.out.println("Time hybrid: " + starAndHybridExperiment.getHybridWatch().toString());
		System.out.println("Time 1-dep: " + oneDepletingStopwatch.toString());
	
		System.out.println("Σ: " + refsig);
		refsig.retainAll(ModuleUtils.getClassAndRoleNamesInSet(starAndHybridExperiment.getHybridModule()));
		System.out.println("Σ ∩ sig(hybrid) = "  + (refsig.isEmpty() ? "∅"  : refsig));

		
//		System.out.println("1-depleting");
//		for(OWLLogicalAxiom axiom : oneDepletingModule){
//			System.out.println(axiom);
//		}
//		
		Set<OWLLogicalAxiom> hybridM = starAndHybridExperiment.getHybridModule();
        System.out.println("Hybrid");
		for(OWLLogicalAxiom axiom : hybridM){
			System.out.println(axiom);
		}
		hybridM.removeAll(oneDepletingModule);
		System.out.println("Hybrid\\1-depleting");
		for(OWLLogicalAxiom axiom : hybridM){
			System.out.println(axiom);
		}
		System.out.println();
		
	
		System.out.println();
		
		System.out.println("============================================================================");
	}

	public static void main(String[] args) throws IOException {

		// axiom-331870650
		///LOCAL/wgatens/Signatures/onedepletingcomparison/AxiomSignatures/04f14c93-17f9-41ed-b2df-958b2be7136d_graphy.owl-QBF

		File ontLoc = 
				new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/04f14c93-17f9-41ed-b2df-958b2be7136d_graphy.owl-QBF");
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());
		
		OneDepletingComparison comp = new OneDepletingComparison(ont, ontLoc);
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/onedepletingcomparison/AxiomSignatures/" + ontLoc.getName()));
		Set<OWLEntity> signature = man.readFile("axiom-331870650");

		comp.performExperiment(signature);
		comp.printMetrics();



	}
}





