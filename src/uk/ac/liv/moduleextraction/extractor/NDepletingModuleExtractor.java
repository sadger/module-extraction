package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;
import uk.ac.liv.moduleextraction.filters.AxiomTypeFilter;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.profling.AxiomTypeProfile;
import uk.ac.liv.moduleextraction.qbf.NElementSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

	//Mapping between axioms and clauses for QBF - each extractor holds its own instance for caching
	private nAxiomToClauseStore clauseStore;

	private NDepletingModuleExtractor(int domain_size, OWLOntology ontology) {
		this(domain_size,ontology.getLogicalAxioms());
	}

	public NDepletingModuleExtractor(int domain_size, Set<OWLLogicalAxiom> ontology){
		this.DOMAIN_SIZE = domain_size;
		this.axiomStore = new DefinitorialAxiomStore(ontology);
		this.chainCollector = new ELAxiomChainCollector();
		this.clauseStore = new nAxiomToClauseStore(DOMAIN_SIZE);
		this.inseparableChecker = new NElementInseparableChecker(clauseStore);
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException, ExecutionException {

		NElementSeparabilityAxiomLocator locator =
				new NElementSeparabilityAxiomLocator(clauseStore,axiomStore.getSubsetAsArray(terminology),sigUnionSigM);

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


		int test = 1;


		File ontDir = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/");



		for(File f : ontDir.listFiles()){

 		    OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
			Set<OWLLogicalAxiom> ontaxioms = ont.getLogicalAxioms();

            //AxiomTypeFilter typeFilter = new AxiomTypeFilter(AxiomType.INVERSE_OBJECT_PROPERTIES);
//			ontaxioms.removeAll(typeFilter.getUnsupportedAxioms(ontaxioms));

			System.out.println("Ontsize: " + ontaxioms.size());


			System.out.println(f.getName() + ": " + test++);

            if(ontaxioms.size() < 400){
                Set<OWLLogicalAxiom> randomSample = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(),5);

                Stopwatch samplewatch = new Stopwatch().start();
                for(OWLLogicalAxiom axiom : randomSample){


                    Set<OWLEntity> sig = axiom.getSignature();
                    HybridModuleExtractor hybrid = new HybridModuleExtractor(ontaxioms);
                    Set<OWLLogicalAxiom> hybridMod = hybrid.extractModule(sig);
                    System.out.println(hybridMod.size());


                    NDepletingModuleExtractor extractor = new NDepletingModuleExtractor(2,hybridMod);
                    Set<OWLLogicalAxiom> nDep = extractor.extractModule(sig);
                    System.out.println(nDep.size());


                    System.out.println(nDep.size() == hybridMod.size());


                }
                samplewatch.stop();
                System.out.println(samplewatch);
            }



			ont.getOWLOntologyManager().removeOntology(ont);
			ont = null;

		}



              /*
        Interesting
        55e5e251-5c11-4b64-8860-066e0c8e2a77_bility.owl-QBF
        0122fdf6-2230-4961-9e3a-94ca44ca1a2f_Qimage.owl-QBF
        3ac2a2b1-a86e-453b-830d-6814b286da46_owl%2Fcoma-QBF
        30ec40e7-a0b8-42fd-b814-05de85f89116_PNO-UPN.owl-QBF
        5012d3f2-9d81-4f56-8456-4da8174aed82_1122.owl-QBF

         */





//		for (int j = 1; j <= 10; j++) {
//
//			String signame = "random10-" + j;
//			Set<OWLEntity> sig = man.readFile(signame);
//
//			AxiomTypeFilter typeFilter = new AxiomTypeFilter(AxiomType.INVERSE_OBJECT_PROPERTIES);
//			Set<OWLLogicalAxiom> ontaxioms = ontz.getLogicalAxioms();
//			ontaxioms.removeAll(typeFilter.getUnsupportedAxioms(ontaxioms));
//			System.out.println("Ontsize: " + ontaxioms.size());
//
//			HybridModuleExtractor hybrid = new HybridModuleExtractor(ontaxioms);
//			Set<OWLLogicalAxiom> hybridMod = hybrid.extractModule(sig);
//			System.out.println(hybridMod.size());
//

//
//		}

	}
}

//
//	}\




