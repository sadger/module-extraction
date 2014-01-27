package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.experiments.SemanticOnlyComparison;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticOnlyExtractor implements Extractor {

	private DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private InseperableChecker inseparableChecker;
	private long qbfChecks = 0;
	private ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;

	public SemanticOnlyExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public SemanticOnlyExtractor(Set<OWLLogicalAxiom> ontology){
		this.dependT = new AxiomDependencies(ontology);
		this.axiomStore = new DefinitorialAxiomStore(dependT.getDefinitorialSortedAxioms());
		this.inseparableChecker = new InseperableChecker();
		this.chainCollector = new ELAxiomChainCollector();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {

		SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,null);

		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		qbfChecks += search.getCheckCount();

		return insepAxiom;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {

		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);

		try {

			applyRules(terminology);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}

		return module;
	}

	public void applyRules(boolean[] terminology) throws IOException, QBFSolverException{

		moveELChainsToModule(terminology);			
		if(inseparableChecker.isSeperableFromEmptySet(axiomStore.getSubsetAsList(terminology),sigUnionSigM)){
			OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
			module.add(axiom);
			axiomStore.removeAxiom(terminology, axiom);
			sigUnionSigM.addAll(axiom.getSignature());
			qbfChecks += inseparableChecker.getTestCount();
			applyRules(terminology);
		}
	}

	private void moveELChainsToModule(boolean[] terminology){
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < terminology.length; i++) {
				if(terminology[i]){
					OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
					if(chainCollector.hasELSyntacticDependency(chosenAxiom, dependT, sigUnionSigM)){
						change = true;
						ArrayList<OWLLogicalAxiom> chain = chainCollector.collectELAxiomChain(terminology, i, axiomStore, dependT, sigUnionSigM);
						module.addAll(chain);

					}
				}
			}
		}
	}

	public long getQBFCount(){
		return qbfChecks;
	}

	public static void main(String[] args) throws IOException {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/GRO_CPGA-core");


		SemanticOnlyExtractor syntactic = new SemanticOnlyExtractor(ont);
		SemanticOnlyExtractor semantic = new SemanticOnlyExtractor(ont);

		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());

		for (int i = 0; i < 100 ; i++) {
			Set<OWLEntity> sig = gen.generateRandomSignature(10);
			System.out.println(sig);
			Set<OWLLogicalAxiom> module = syntactic.extractModule(sig);
			Set<OWLLogicalAxiom> module2 = semantic.extractModule(sig);
			System.out.println("Same module?: " + module.equals(module2));
			System.out.println("Module: " + module.size());
		}


		//		System.out.println(sig);
		//		Set<OWLLogicalAxiom> module = extractor.extractModule(sig);
		//		System.out.println("|M|: " + module);
	}




}
