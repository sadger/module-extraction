package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import sun.security.pkcs11.Secmod;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;
import uk.ac.liv.moduleextraction.experiments.NDepletingComparison;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.qbf.IncrementalSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.NElementSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NDepletingModuleExtractor implements Extractor {

	private final DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private final NElementInseparableChecker inseparableChecker;

	private final ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;
	private List<OWLLogicalAxiom> allAxioms;
	private Set<OWLLogicalAxiom> cycleCausing = new HashSet<OWLLogicalAxiom>();
	private Set<OWLLogicalAxiom> expressive;
	private ArrayList<OWLLogicalAxiom> acyclicAxioms;

	private final int DOMAIN_SIZE;
	private Stopwatch nDepWatch;

	private long qbfChecks = 0;
	private long separabilityAxioms = 0;
	private long syntacticChecks = 0;

	private NDepletingModuleExtractor(int domain_size, OWLOntology ontology) {
		this(domain_size,ontology.getLogicalAxioms());
	}

	public NDepletingModuleExtractor(int domain_size, Set<OWLLogicalAxiom> ontology){
		this.DOMAIN_SIZE = domain_size;
		this.axiomStore = new DefinitorialAxiomStore(ontology);
		this.inseparableChecker = new NElementInseparableChecker(domain_size);
		this.chainCollector = new ELAxiomChainCollector();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException, ExecutionException {

		NElementSeparabilityAxiomLocator locator =
				new NElementSeparabilityAxiomLocator(DOMAIN_SIZE,axiomStore.getSubsetAsArray(terminology),sigUnionSigM);

		OWLLogicalAxiom insepAxiom = locator.getSeparabilityCausingAxiom();
		qbfChecks += locator.getCheckCount();
		separabilityAxioms++;

		return insepAxiom;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        resetMetrics();

		nDepWatch = new Stopwatch().start();

		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		allAxioms = axiomStore.getSubsetAsList(terminology);

		SupportedExpressivenessFilter filter = new SupportedExpressivenessFilter();
		expressive = filter.getUnsupportedAxioms(allAxioms);
		allAxioms.removeAll(expressive);

		//Can only determine if there is a cycle after removing any expressive axioms
		OntologyCycleVerifier verifier = new OntologyCycleVerifier(allAxioms);
		if(verifier.isCyclic()){
			cycleCausing = verifier.getCycleCausingAxioms();
			/* All axioms is now the acyclic subset of the ontology 
			after removing unsupported or cycle causing axioms */
			allAxioms.removeAll(cycleCausing);
		}

		dependT = new AxiomDependencies(new HashSet<OWLLogicalAxiom>(allAxioms));
		acyclicAxioms = dependT.getDefinitorialSortedAxioms();


		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);

		try {
			//Apply the rules to everything
			applyRules(terminology);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		nDepWatch.stop();


		return module;
	}

	private void resetMetrics() {
		qbfChecks = 0;
		separabilityAxioms = 0;
		syntacticChecks = 0;
	}

	private void applyRules(boolean[] terminology) throws IOException, QBFSolverException, ExecutionException {
		moveELChainsToModule(acyclicAxioms, terminology, axiomStore);
		qbfChecks++;
		if(inseparableChecker.isSeparableFromEmptySet(axiomStore.getSubsetAsList(terminology), sigUnionSigM)){
			OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
			module.add(axiom);
			removeAxiom(terminology, axiom);
			sigUnionSigM.addAll(axiom.getSignature());
			applyRules(terminology);
		}
	}

	private void moveELChainsToModule(List<OWLLogicalAxiom> acyclicAxioms, boolean[] terminology, DefinitorialAxiomStore axiomStore){
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < acyclicAxioms.size(); i++) {
				OWLLogicalAxiom chosenAxiom = acyclicAxioms.get(i);
				if(chainCollector.hasELSyntacticDependency(chosenAxiom, dependT, sigUnionSigM)){
					change = true;
					ArrayList<OWLLogicalAxiom> chain = 
							chainCollector.collectELAxiomChain(acyclicAxioms, i, terminology, axiomStore, dependT, sigUnionSigM);

					module.addAll(chain);
					acyclicAxioms.removeAll(chain);
				}
			}

		}
		syntacticChecks += chainCollector.getSyntacticChecks();
		chainCollector.resetSyntacticChecks();
	}


	public long getQBFCount(){
		return qbfChecks;
	}

	void removeAxiom(boolean[] terminology, OWLLogicalAxiom axiom){
		axiomStore.removeAxiom(terminology, axiom);
		allAxioms.remove(axiom);
		cycleCausing.remove(axiom);
		acyclicAxioms.remove(axiom);
		expressive.remove(axiom);
	}

	public ExtractionMetric getMetrics(){
		ExtractionMetric.MetricBuilder builder = new ExtractionMetric.MetricBuilder(ExtractionMetric.ExtractionType.N_DEPLETING);
		builder.moduleSize(module.size());
		builder.timeTaken(nDepWatch.elapsed(TimeUnit.MILLISECONDS));
		builder.qbfChecks(qbfChecks);
		builder.separabilityCausingAxioms(separabilityAxioms);
		builder.syntacticChecks(syntacticChecks);
		return builder.createMetric();
	}

	public static void main(String[] args) throws IOException {

		//NDepletingModuleExtractor one = new NDepletingModuleExtractor(2,ont.getLogicalAxioms());

		File dir = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only");
//		for(File ontFile : dir.listFiles()){
//			if(ontFile.isFile()){
//				System.out.println2(ontFile.getName());
		File ontFile = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/0a3f75bb-693b-4adb-b277-dc7fe493d3f4_DUL.owl-QBF");
		OWLOntology ontz = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());

		for(OWLLogicalAxiom ax : ontz.getLogicalAxioms()){
			System.out.println(ax);
		}
		HybridModuleExtractor extractor2 = new HybridModuleExtractor(ontz, HybridModuleExtractor.CycleRemovalMethod.NAIVE);
		System.out.println("Size:" + ontz.getLogicalAxiomCount());
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "qbfspeed/" + ontFile.getName()));
		SignatureGenerator gen = new SignatureGenerator(ontz.getLogicalAxioms());
		Stopwatch watchy = new Stopwatch().start();
		for (int j = 1; j <= 10; j++) {
			String signame = "random10-" + j;
			Set<OWLEntity> sig = man.readFile(signame);
//			Set<OWLLogicalAxiom> mod = extractor2.extractModule(sig);
//			System.out.println("Mod size: " + mod.size());
//			NDepletingModuleExtractor extract = new NDepletingModuleExtractor(1,mod);
//			System.out.println(extract.extractModule(sig).size());
			NDepletingComparison compare = new NDepletingComparison(1,ontz,new File("/tmp/"));
			compare.performExperiment(sig);
			File sigLocation = new File(ModulePaths.getResultLocation() + "qbfspeed/" + ontFile.getName() + "/" + signame);
			sigLocation.mkdirs();
			compare.writeMetrics(sigLocation);
		}
		watchy.stop();
		System.out.println("TIME:" + watchy);

	}
}

//
//	}\




