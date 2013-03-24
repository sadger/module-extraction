package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.ChainAxiomCollector;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.NewSyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class SyntacticFirstModuleExtraction {

	Logger logger = LoggerFactory.getLogger(SyntacticFirstModuleExtraction.class);

	
	/* Syntactic Checking */
	private NewSyntacticDependencyChecker syntaxDepChecker = new NewSyntacticDependencyChecker();

	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();

	/* Data Structures */
	private LinkedHashList<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> signature;
	private HashSet<OWLEntity> sigUnionSigM;

	private static int maxChain = 0;
	private static int chainTotal = 0;
	private static int syntacticIterations = 0;
	
	/* For writing sigs that cause inseperability */
	SigManager sigManager = new SigManager(new File(ModulePaths.getSignatureLocation() + "/insepSigs"));
	

	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> terminology, Set<OWLEntity> signature) {
		this(terminology, null, signature);
	}

	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> sig) {
		LinkedList<OWLLogicalAxiom> depthSortedAxioms = sorting(term);

		this.terminology = new LinkedHashList<OWLLogicalAxiom>(depthSortedAxioms);
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		
		populateSignature();
	}

	/**
	 * @param term
	 * @return
	 */
	private LinkedList<OWLLogicalAxiom> sorting(Set<OWLLogicalAxiom> term) {	
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(term);
		LinkedList<OWLLogicalAxiom> sortedAxioms = new LinkedList<OWLLogicalAxiom>(term);
		Collections.sort(sortedAxioms, new AxiomComparator(definitorialDepth.getDefinitorialMap()));
		
		return sortedAxioms;
	}


	public static void printMetrics(){
		System.out.println("Iterations: " + syntacticIterations);
		System.out.println("Max chain: " + maxChain);
		System.out.println("Average chain: " + (double) chainTotal/syntacticIterations);
	}
	
	public LinkedHashList<OWLLogicalAxiom> getTerminology() {
		return terminology;
	}

	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}

	private void populateSignature() {
		sigUnionSigM = new HashSet<OWLEntity>();
		sigUnionSigM.addAll(signature);
	}


	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{
		collectSyntacticDependentAxioms();

		ChainDependencies tminusMDependencies = new ChainDependencies();
		tminusMDependencies.updateDependenciesWith(terminology);
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(terminology,sigUnionSigM,tminusMDependencies);

		if(insepChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			logger.debug("Collecting semantic dependent axioms");
			SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(terminology, module, signature);
			OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
			logger.trace("Adding {}",insepAxiom);
			logger.info("Adding 1 axiom through semantic check");
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			terminology.remove(insepAxiom);
			extractModule();
		}

		return module;
	}
	


	private void collectSyntacticDependentAxioms() {
		logger.debug("Collecting syntactic dependent axioms");
		ListIterator<OWLLogicalAxiom> axiomIterator = terminology.listIterator();
		ChainDependencies syntacticDependencies = new ChainDependencies();
		ChainAxiomCollector chainCollector = new ChainAxiomCollector();

		int addedCount = 0;
		/* Terminology is the value of T\M as we remove items and add them to the module */
		while(axiomIterator.hasNext()){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			syntacticDependencies.updateDependenciesWith(chosenAxiom);
			
			if(syntaxDepChecker.hasSyntacticSigDependency(chosenAxiom, syntacticDependencies, sigUnionSigM)){
								
//				module.add(chosenAxiom);
//				sigUnionSigM.addAll(chosenAxiom.getSignature());

//				logger.info("Adding {}",chosenAxiom);
				
				/*Find the chain of axioms (including the one we found the initial dependency on */
				Set<OWLLogicalAxiom> axiomChain = chainCollector.collectAxiomChain(axiomIterator, syntacticDependencies, sigUnionSigM);
				module.addAll(axiomChain);
				
//				logger.info("Chain {}",axiomChain);
//				
				addedCount += axiomChain.size();
				
	
				terminology.removeAll(axiomChain);
				sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(axiomChain));
				
				syntacticDependencies = new ChainDependencies();
				
				/* Reset the iterator to start of the list*/
				axiomIterator = terminology.listIterator(0);
			}
		}
		if(addedCount > 0)
			logger.info("Adding {} axiom(s) through syntactic check",addedCount);
	}

}
