package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class SyntacticFirstModuleExtraction {

	Logger logger = LoggerFactory.getLogger(SyntacticFirstModuleExtraction.class);

	
	/* Syntactic Checking */
	private SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();

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
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(term);
		ArrayList<OWLLogicalAxiom> depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();

		this.terminology = new LinkedHashList<OWLLogicalAxiom>(depthSortedAxioms);
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		
		populateSignature();
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
			SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(terminology, module, signature);
			OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
			logger.info("Adding {}",insepAxiom);
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			terminology.remove(insepAxiom);
			extractModule();
		}

		return module;
	}

	private void collectSyntacticDependentAxioms() {
		System.out.println("Collecting Syntactic dependent axioms");
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
		ChainDependencies syntaticDependencies = new ChainDependencies();

		int addedCount = 0;
		/* Terminology is the value of T\M as we remove items and add them to the module */
		while(!(terminology.size() == W.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);
			
			syntaticDependencies.updateDependenciesWith(chosenAxiom);
			

			if(syntaxDepChecker.hasSyntacticSigDependency(W, syntaticDependencies, sigUnionSigM)){
				syntacticIterations++;
				Set<OWLLogicalAxiom> axiomsWithDeps = syntaxDepChecker.getAxiomsWithDependencies();
				module.addAll(axiomsWithDeps);
				
				int addingSize = axiomsWithDeps.size();
				addedCount += addingSize;
				
				maxChain = Math.max(maxChain, addingSize);
				chainTotal += addingSize;
				
				terminology.removeAll(axiomsWithDeps);
				
				logger.trace("Adding {}",axiomsWithDeps);
				logger.debug("Adding {} axiom(s)",axiomsWithDeps.size());
				
				sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(axiomsWithDeps));

				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
		}
		if(addedCount > 0)
			logger.info("Total axioms added: {}",addedCount);
	}

}
