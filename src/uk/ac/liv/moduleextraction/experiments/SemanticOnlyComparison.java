package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.liv.moduleextraction.extractor.SemanticOnlyExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
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
		System.out.println("Signature:" + refsig);
		
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
		
		System.out.println("sig (QBF) ∪ Σ:" + qbfSig);
		
		starIterExperiment.getHybridModule().removeAll(semanticModule);
		System.out.println("\nDifference - Hybrid\\QBF:");
		
		
		for(OWLLogicalAxiom ax : starIterExperiment.getHybridModule()){
			System.out.println(ax);
		}
	}


	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

		//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/test.krss");

		
		String[] sigs = {
				"axiom-22317056",
				"axiom-219578961",
				"axiom586956648",
				"axiom-2028838311",
				"axiom-1691633053",
				"axiom-863463910",
				"axiom-999696411",
				"axiom809370644",
				"axiom2018693310",
				"axiom2050242329",
				"axiom-767216504",
				"axiom489919893",
				"axiom146963947",
				"axiom-750030262",
				"axiom645257775",
				"axiom2091676724",
				"axiom-598959749",
				"axiom-1768131750",
				"axiom1611423724",
				"axiom1460393202",
				"axiom-514559521",
				"axiom521495000",
				"axiom550922629",
				"axiom-428529591",
				"axiom1212029434",
				"axiom-2109246935",
				"axiom-1917961708",
				"axiom824547094",
				"axiom492327439",
				"axiom1665252572",
				"axiom-1177251495",
				"axiom-1919201476",
				"axiom-2015829024",
				"axiom106489286",
				"axiom-56928301",
				"axiom123599926",
				"axiom-1101242434",
				"axiom-175826706",
				"axiom1436697157",
				"axiom887706073",
				"axiom1261965881",
				"axiom796018360",
				"axiom213254978",
				"axiom-434920181",
				"axiom1477910181",
				"axiom-1414308677",
				"axiom-49167526",
				"axiom-965900582",
				"axiom2092583203",
				"axiom-1263045159",
				"axiom-424114053",
				"axiom-1734365458",
				"axiom-1999717001",
				"axiom-395401200",
				"axiom475670751",
				"axiom874704769",
				"axiom-1957219923",
				"axiom1105130425",
				"axiom824077032",
				"axiom-73138414",
				"axiom34568665",
				"axiom2054696964",
				"axiom-2138069844",
				"axiom1822063940",
				"axiom-1129046111",
				"axiom-1894824807",
				"axiom167105582",
				"axiom2019631662",
				"axiom-1695658739",
				"axiom928646753",
				"axiom1646306746",
				"axiom-752286020",
				"axiom-1096520504",
				"axiom53918133",
				"axiom-1152182482",
				"axiom-1736951745",
				"axiom1159142179",
				"axiom-144639175",
				"axiom43707109"

		};

		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/Genomic-CDS-core");
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/semantic-only/AxiomSignatures/Genomic-CDS-core"));
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont, null);
		
		for(String s : sigs){
			Set<OWLEntity> sig = man.readFile(s);
			//System.out.println(sig);
			compare.performExperiment(sig);
			compare.printMetrics();

		}

	

	

	}

}
