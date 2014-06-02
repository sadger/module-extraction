package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.ExtendedLHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class OneDepletingModuleExtractor implements Extractor {

	private DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private InseperableChecker inseparableChecker;
	private long qbfChecks = 0;
	private ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;
	private ExtendedLHSSigExtractor lhsExtractor;

	public OneDepletingModuleExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public OneDepletingModuleExtractor(Set<OWLLogicalAxiom> ontology){
		this.dependT = new AxiomDependencies(ontology);
		this.axiomStore = new DefinitorialAxiomStore(dependT.getDefinitorialSortedAxioms());
		this.inseparableChecker = new InseperableChecker();
		this.chainCollector = new ELAxiomChainCollector();
		this.lhsExtractor = new ExtendedLHSSigExtractor();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {

		SeparabilityAxiomLocator search = 
				new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,dependT);

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
		Set<OWLLogicalAxiom> lhs = lhsExtractor.getLHSSigAxioms(terminology, axiomStore, sigUnionSigM, dependT);
		if(inseparableChecker.isSeperableFromEmptySet(lhs,sigUnionSigM)){
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






}
