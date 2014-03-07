package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
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

	public void printMetrics() throws IOException{
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

		//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/test.krss");

		int role = 0;
		int size = 100;
		int num = 185;

		String[] sigs = {
				"nci-equiv-sig"
		};



 
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/paperexample.krss");
		ModuleUtils.remapIRIs(ont, "X");
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		System.out.println(ont);
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.add(factory.getOWLClass(IRI.create("X#Malignt_U_T_Neoplasm ")));
		sig.add(factory.getOWLClass(IRI.create("X#Renal_Pelvis_and_U")));
		sig.add(factory.getOWLClass(IRI.create("X#Benign_U_T_Neoplasm")));
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont, null);
		System.out.println("Sig size: " + sig.size());
		System.out.println(sig);
		//System.out.println(sig);
		compare.performExperiment(sig);
		compare.printMetrics();

		new AMEXvsSTAR(ont).performExperiment(sig);;
		
		
		
//		System.out.println(SemanticOnlyComparison.total);
//		System.out.println(SemanticOnlyComparison.differences.size());


	}
}
