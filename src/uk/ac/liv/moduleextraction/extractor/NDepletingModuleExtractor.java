package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.qbf.NElementSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
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
	private long qbfChecks = 0;
	private final ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;
	private List<OWLLogicalAxiom> allAxioms;
	private Set<OWLLogicalAxiom> cycleCausing = new HashSet<OWLLogicalAxiom>();
	private Set<OWLLogicalAxiom> expressive;
	private ArrayList<OWLLogicalAxiom> acyclicAxioms;

	private final int DOMAIN_SIZE;

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

		return insepAxiom;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {

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

		return module;
	}

	void applyRules(boolean[] terminology) throws IOException, QBFSolverException, ExecutionException {
		moveELChainsToModule(acyclicAxioms, terminology, axiomStore);	

		if(inseparableChecker.isSeparableFromEmptySet(axiomStore.getSubsetAsList(terminology), sigUnionSigM)){
			OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
			module.add(axiom);
			removeAxiom(terminology, axiom);
			sigUnionSigM.addAll(axiom.getSignature());
			qbfChecks += inseparableChecker.getTestCount();
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

	public static void main(String[] args) {

		//NDepletingModuleExtractor one = new NDepletingModuleExtractor(2,ont.getLogicalAxioms());

		File dir = new File("/home/william/PhD/Ontologies/OWL-Corpus-All/qbf-only");
		int i = 1;
		for(File ontFile : dir.listFiles()){
			if(ontFile.isFile()){
						OWLOntology ontz = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());

						HybridModuleExtractor extractor2 = new HybridModuleExtractor(ontz, HybridModuleExtractor.CycleRemovalMethod.NAIVE);
						System.out.println("Size:" + ontz.getLogicalAxiomCount());
						SignatureGenerator gen = new SignatureGenerator(ontz.getLogicalAxioms());
						Stopwatch watchy = new Stopwatch().start();
						for (int j = 0; j < 10; j++) {
							Set<OWLEntity> sig = gen.generateRandomSignature(10);
							Set<OWLLogicalAxiom> mod = extractor2.extractModule(sig);
							NDepletingModuleExtractor extract = new NDepletingModuleExtractor(1,mod);
							System.out.println(extract.extractModule(sig).size());
						}
						watchy.stop();
						System.out.println("TIME:" + +watchy.elapsed(TimeUnit.MILLISECONDS));

			}
		}


	}



}
