package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.ExtendedLHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticRuleExtractor implements Extractor{

	private AxiomDependencies dependencies;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private SyntacticDependencyChecker syntacticDependencyChecker;
	private DefinitorialAxiomStore axiomStore;
	
	private ExtendedLHSSigExtractor eLHSExtractor;
	private InseperableChecker inseperableChecker;
	
	private long syntacticChecks = 0; // A syntactic iteration (total checks = this + qbfchecks)
	private long timeTaken = 0; //Time taken to setup and extract the module (ms)
	private long qbfChecks = 0; //Total number of times we actually call the qbf solver
	private long separabilityChecks = 0; // Number of times we need to search for a separability causing axiom
	
	
	private Logger logger = LoggerFactory.getLogger(SemanticRuleExtractor.class);
			

	
	public SemanticRuleExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public SemanticRuleExtractor(Set<OWLLogicalAxiom> ontology){

		dependencies = new AxiomDependencies(ontology);
		axiomStore = new DefinitorialAxiomStore(dependencies.getDefinitorialSortedAxioms());
		
		syntacticDependencyChecker = new SyntacticDependencyChecker();
		
		eLHSExtractor = new ExtendedLHSSigExtractor();
		inseperableChecker = new InseperableChecker();
	}
	
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		/* Reset all the metrics for new extraction */
		syntacticChecks = 0; 
		timeTaken = 0; 
		qbfChecks = 0;
		inseperableChecker.resetMetrics();
		separabilityChecks = 0;
		
		long startTime = System.currentTimeMillis();
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
		
		timeTaken = System.currentTimeMillis() - startTime;
		return module;
	}
	
	public LinkedHashMap<String, Long> getMetrics() {
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		metrics.put("Module size", (long) module.size());
		metrics.put("Time taken", timeTaken);
		metrics.put("Syntactic Checks", syntacticChecks);
		metrics.put("QBF Checks", qbfChecks);
		metrics.put("Separability Checks", separabilityChecks);
		return metrics;
	}


	public LinkedHashMap<String, Long> getQBFMetrics() {
		return inseperableChecker.getQBFMetrics();
	}

	
	
	private void applyRules(boolean[] terminology) throws IOException, QBFSolverException{
		applySyntacticRule(terminology);
		
		HashSet<OWLLogicalAxiom> lhsSigT = eLHSExtractor.getLHSSigAxioms(axiomStore.getSubsetAsList(terminology), sigUnionSigM, dependencies);
		//HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(axiomStore.getSubsetAsList(terminology),sigUnionSigM,dependT);
		
		qbfChecks++;
		if(inseperableChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			OWLLogicalAxiom insepAxiom = findSeparableAxiom(terminology);
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			axiomStore.removeAxiom(terminology, insepAxiom);
			applyRules(terminology);
		}
	}


	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {
		
		separabilityChecks++;
		SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,dependencies);
		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		logger.debug("Adding (semantic): {}", insepAxiom);
		qbfChecks += search.getCheckCount();
		return insepAxiom;
	}


	private void applySyntacticRule(boolean[] terminology){
		boolean change = true;
		
		while(change){
			change = false;
			for (int i = 0; i < terminology.length; i++) {
				
				if(terminology[i]){
					
					OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
					syntacticChecks++;
					if(syntacticDependencyChecker.hasSyntacticSigDependency(chosenAxiom, dependencies, sigUnionSigM)){
						
						change = true;
						module.add(chosenAxiom);
						terminology[i] = false;
						logger.debug("Adding (syntactic): {}", chosenAxiom);
						sigUnionSigM.addAll(chosenAxiom.getSignature());
						
						
					}
				}
			}
		}
	
	}
	



	 


	

}
