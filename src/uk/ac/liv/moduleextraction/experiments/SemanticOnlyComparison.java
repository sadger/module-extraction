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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.base.Stopwatch;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.extractor.SemanticOnlyExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticOnlyComparison implements Experiment {


	private File originalLocation;
	private OWLOntology ontology;
	private NewIteratingExperiment starIterExperiment;
	private File sigLocation;
	private SemanticOnlyExtractor semanticExtractor;
	private Set<OWLLogicalAxiom> semanticModule;
	private Set<OWLEntity> refsig;
	static Set<OWLLogicalAxiom> differences = new HashSet<OWLLogicalAxiom>();
	static int total = 0;
	private Stopwatch semanticStopwatch;
	private Stopwatch hybridStopwatch;

	public SemanticOnlyComparison(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		this.refsig = signature;
		starIterExperiment = 
				new NewIteratingExperiment(ontology,originalLocation);

		hybridStopwatch = new Stopwatch().start();
		starIterExperiment.performExperiment(signature);
		hybridStopwatch.stop();
		
		Set<OWLLogicalAxiom> hybridModule = starIterExperiment.getHybridModule();
		

		semanticStopwatch = new Stopwatch().start();
		semanticExtractor = new SemanticOnlyExtractor(hybridModule);
		semanticModule = semanticExtractor.extractModule(signature);
		semanticStopwatch.stop();


	}

	@Override
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
		this.sigLocation = signatureLocation;

		performExperiment(signature);

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		int qbfSmaller = (semanticModule.size() < starIterExperiment.getHybridModule().size()) ? 1 : 0;

		writer.write("StarSize, HybridSize, QBFSize, QBFSmaller, QBFChecks, TimeHybrid, TimeQBF, SignatureLocation" + "\n");
		writer.write(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size() + "," + String.valueOf(qbfSmaller) + "," 
				+ semanticStopwatch.elapsed(TimeUnit.MILLISECONDS) + "," + hybridStopwatch.elapsed(TimeUnit.MILLISECONDS) + 
				"," + semanticExtractor.getQBFCount() + 
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}

	public void printMetrics() throws IOException{
		System.out.println("============================================================================");
		System.out.println("StarSize, HybridSize, QBFSize");
		System.out.println(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size());


		System.out.println("Hybrid time:" + hybridStopwatch.elapsed(TimeUnit.MILLISECONDS));
		System.out.println("Semantic time:" + semanticStopwatch.elapsed(TimeUnit.MILLISECONDS));
		System.out.println();
		//System.out.println("Signature:" + refsig);

		//		System.out.println("Hybrid:");
		//		for(OWLLogicalAxiom ax : starIterExperiment.getHybridModule()){
		//			System.out.println(ax);
		//		}
		//		System.out.println();
		//		System.out.println("QBF:");
		//		for(OWLLogicalAxiom ax : semanticModule){
		//			System.out.println(ax);
		//		}

		
		System.out.println("QBF checks: " + semanticExtractor.getQBFCount());
		Set<OWLEntity> qbfSig = ModuleUtils.getClassAndRoleNamesInSet(semanticModule);
		qbfSig.addAll(refsig);


		Set<OWLLogicalAxiom> difference = new HashSet<OWLLogicalAxiom>(starIterExperiment.getHybridModule());
		difference.removeAll(semanticModule);
		differences.addAll(difference);
		total += difference.size();
		
		qbfSig.retainAll(ModuleUtils.getClassAndRoleNamesInSet(difference));
		
		System.out.println("(sig (QBF) ∪ Σ) ∩ sig(Hybrid\\QBF):" + qbfSig);

		
		System.out.println("\nDifference - Hybrid\\QBF:");

		System.out.println("Diff size: " + difference.size());

		
		
		for(OWLLogicalAxiom ax : difference){
			Set<OWLEntity> sigCopy = new HashSet<OWLEntity>(qbfSig);
			OWLClass cls = (OWLClass) AxiomSplitter.getNameofAxiom(ax);
			//System.out.println();
			//System.out.println(dependencies.get(ax));
		//	sigCopy.retainAll(dependencies.get(ax));
		//	System.out.println(qbfSig.contains(cls) + "/"  + !sigCopy.isEmpty());
			System.out.println(ax);
		}
	
	}


	public static void main(String[] args) throws IOException, OWLOntologyCreationException {
		
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/qbf-only/" + "Thesaurus_08.09d.OWL-QBF");
		SignatureGenerator gen = new SignatureGenerator(ontology.getLogicalAxioms());
		
		for(OWLLogicalAxiom ax : ontology.getLogicalAxioms()){
			if(!ModuleUtils.isInclusionOrEquation(ax)){
				System.out.println(ax);
			}
		}
		
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ontology, null);
		for (int i = 0; i < 10; i++) {
			compare.performExperiment(gen.generateRandomSignature(1000));
			compare.printMetrics();
		}

		
	}
	
		
	
		
//		System.out.println(SemanticOnlyComparison.total);
//		System.out.println(SemanticOnlyComparison.differences.size());


	
}
