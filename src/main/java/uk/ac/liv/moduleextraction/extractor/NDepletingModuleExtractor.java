package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.axiomdependencies.DefinitorialAxiomStore;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.filters.ALCQIwithAtomicLHSFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.propositional.nSeparability.nAxiomToClauseStore;
import uk.ac.liv.moduleextraction.qbf.NElementSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.SHIQOntologyVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NDepletingModuleExtractor implements Extractor {

	private DefinitorialAxiomStore axiomStore = null;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private NElementInseparableChecker inseparableChecker = null;

	private ELAxiomChainCollector chainCollector = null;
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

	private NDepletingModuleExtractor(int domain_size, OWLOntology ontology) throws ExtractorException {
		this(domain_size, OWLAPIStreamUtils.asSet(ontology.logicalAxioms()));
	}

	public NDepletingModuleExtractor(int domain_size, Set<OWLLogicalAxiom> ontology) throws ExtractorException {
		this.DOMAIN_SIZE = domain_size;
		if (isOntologyValid(ontology)) {
			this.axiomStore = new DefinitorialAxiomStore(ontology);
			this.chainCollector = new ELAxiomChainCollector();
			this.clauseStore = new nAxiomToClauseStore(DOMAIN_SIZE);
			this.inseparableChecker = new NElementInseparableChecker(clauseStore);
		}

	}

	private boolean isOntologyValid(Set<OWLLogicalAxiom> ont) throws ExtractorException {
		SHIQOntologyVerifier shiqOntologyVerifier = new SHIQOntologyVerifier();

		if(shiqOntologyVerifier.isSHIQOntology(ont)){
			return true;
		}
		else{
			throw new ExtractorException("Input ontology must be at most SHIQ in expressiveness (no nominals)");
		}
	}

	public Set<OWLLogicalAxiom> getModule(){
		return module;
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

		nDepWatch = Stopwatch.createStarted();

		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		allAxioms = axiomStore.getSubsetAsList(terminology);

		ALCQIwithAtomicLHSFilter filter = new ALCQIwithAtomicLHSFilter();
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

	public Stopwatch getStopwatch(){
		return nDepWatch;
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
			sigUnionSigM.addAll(OWLAPIStreamUtils.asSet(axiom.signature()));
			applyRules(terminology);
		}
	}

	private void moveELChainsToModule(List<OWLLogicalAxiom> acyclicAxioms, boolean[] terminology, DefinitorialAxiomStore axiomStore){
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < acyclicAxioms.size(); i++) {
				OWLLogicalAxiom chosenAxiom = acyclicAxioms.get(i);
				if(chainCollector.hasELISyntacticDependency(chosenAxiom, dependT, sigUnionSigM)){
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




}

//
//	}\




