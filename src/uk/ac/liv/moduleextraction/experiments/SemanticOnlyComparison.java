package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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

	public SemanticOnlyComparison(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		this.refsig = signature;
		starIterExperiment = 
				new NewIteratingExperiment(ontology,originalLocation);


		starIterExperiment.performExperiment(signature);
		Set<OWLLogicalAxiom> hybridModule = starIterExperiment.getHybridModule();

		semanticExtractor = new SemanticOnlyExtractor(hybridModule);
		semanticModule = semanticExtractor.extractModule(signature);



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

		writer.write("StarSize, HybridSize, QBFSize, QBFSmaller, QBFChecks, SignatureLocation" + "\n");
		writer.write(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size() + "," + String.valueOf(qbfSmaller) + "," + semanticExtractor.getQBFCount() + 
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}

	public void printMetrics(){
		System.out.println("============================================================================");
		System.out.println("StarSize, HybridSize, QBFSize");
		System.out.println(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size());

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

		Set<OWLEntity> qbfSig = ModuleUtils.getClassAndRoleNamesInSet(semanticModule);
		qbfSig.addAll(refsig);


		Set<OWLLogicalAxiom> difference = new HashSet<OWLLogicalAxiom>(starIterExperiment.getHybridModule());
		difference.removeAll(semanticModule);
		differences.addAll(difference);
		total += difference.size();
		
		qbfSig.retainAll(ModuleUtils.getClassAndRoleNamesInSet(difference));
		
		System.out.println("(sig (QBF) ∪ Σ) ∩ sig(Hybrid\\QBF):" + qbfSig);

		
		System.out.println("\nDifference - Hybrid\\QBF:");

		AxiomDependencies dependencies = new AxiomDependencies(starIterExperiment.getHybridModule());

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

		//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/test.krss");


		String[] sigs = {
//				"random1000-100",
//				"random1000-78",
//				"random1000-147",
//				"random1000-113",
//				"random1000-53",
//				"random1000-197",
//				"random1000-119",
//				"random1000-42",
//				"random1000-133",
//				"random1000-173",
//				"random1000-137",
//				"random1000-95",
//				"random1000-120",
//				"random1000-39",
//				"random1000-40",
//				"random1000-159",
//				"random1000-36",
//				"random1000-176",
//				"random1000-96",
				"random1000-29",
//				"random1000-65",
//				"random1000-79",
//				"random1000-27",
//				"random1000-49",
//				"random1000-81",
//				"random1000-28",
//				"random1000-112",
//				"random1000-17",
//				"random1000-171",
//				"random1000-80",
//				"random1000-126",
//				"random1000-34",
//				"random1000-92",
//				"random1000-136",
//				"random1000-151",
//				"random1000-185",
//				"random1000-71",
//				"random1000-101",
//				"random1000-62",
//				"random1000-127",
//				"random1000-117",
//				"random1000-157",
//				"random1000-189",
//				"random1000-158",
//				"random1000-99",
//				"random1000-110",
//				"random1000-50",
//				"random1000-64",
//				"random1000-94",
//				"random1000-193",
//				"random1000-56",
//				"random1000-12",
//				"random1000-129",
//				"random1000-73",
//				"random1000-98",
//				"random1000-45",
//				"random1000-14",
//				"random1000-146",
//				"random1000-35",
//				"random1000-83",
//				"random1000-76",
//				"random1000-145",
//				"random1000-154",
//				"random1000-138",
//				"random1000-84",
//				"random1000-47",
//				"random1000-194",
//				"random1000-115"
		};
		


		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/meaningful/Thesaurus_11.04d.OWL-coreM");
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/semantic-only-meaningful/RandomSignatures/Thesaurus_11.04d.OWL-core/size-1000"));
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont, null);

		for(String s : sigs){
			System.out.println(s);
			Set<OWLEntity> sig = man.readFile(s);
			//System.out.println(sig);
			compare.performExperiment(sig);
			compare.printMetrics();

		}
		
		System.out.println(SemanticOnlyComparison.total);
		System.out.println(SemanticOnlyComparison.differences.size());


	}
}
