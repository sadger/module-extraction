package uk.ac.liv.moduleextraction.qbf;

import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class SeparabilityAxiomLocator {
	
	Logger logger = LoggerFactory.getLogger(SeparabilityAxiomLocator.class);
	
	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();
	
	/* Data structures */
	private LinkedHashList<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	
	
	public SeparabilityAxiomLocator(LinkedHashList<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> mod, Set<OWLEntity> sig) throws IOException, QBFSolverException{
		this.terminology = term;
		this.module = mod;
		
		this.sigUnionSigM = sig;
		sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
	}
	
	public OWLLogicalAxiom getInseperableAxiom() throws IOException, QBFSolverException{	
		/* Represents the last axioms added or removed from the split test */
		
		logger.debug("Finding separability causing axiom");
		LinkedHashList<OWLLogicalAxiom> lastAdded = getTopHalf(terminology);
		LinkedHashList<OWLLogicalAxiom> lastRemoved = getBottomHalf(terminology);
		
		LinkedHashList<OWLLogicalAxiom> W = lastAdded;

		while(lastAdded.size() > 0){
			
			ChainDependencies Wdeps = new ChainDependencies();
			Wdeps.updateDependenciesWith(W);
			Set<OWLLogicalAxiom> lhsW = lhsExtractor.getLHSSigAxioms(W, sigUnionSigM, Wdeps);
			
			if(!insepChecker.isSeperableFromEmptySet(lhsW, sigUnionSigM)){
				lastAdded = getTopHalf(lastRemoved);
				W.addAll(lastAdded);
				lastRemoved.removeAll(lastAdded);
				
				logger.trace("Adding: {}",lastAdded.size());

			}
			else{
				
			lastRemoved = getBottomHalf(lastAdded);
			W.removeAll(lastRemoved);
			lastAdded.removeAll(lastRemoved);

			logger.trace("Removing: {}",lastRemoved.size());

			
			}

		}
		return  terminology.get(W.size());
	}


	
	private LinkedHashList<OWLLogicalAxiom> getTopHalf(LinkedHashList<OWLLogicalAxiom> axiomList){
		int fromIndex = 0;
		int toIndex = (int) Math.floor(axiomList.size()/2);
		
		LinkedHashList<OWLLogicalAxiom> topHalf =
				new LinkedHashList<OWLLogicalAxiom>(axiomList.subList(fromIndex, toIndex));
		return topHalf;

	}
	
	private LinkedHashList<OWLLogicalAxiom> getBottomHalf(LinkedHashList<OWLLogicalAxiom> axiomList){
		int fromIndex = (int) Math.floor(axiomList.size()/2);
		int toIndex = axiomList.size();
			
		LinkedHashList<OWLLogicalAxiom> bottomHalf =
				new LinkedHashList<OWLLogicalAxiom>(axiomList.subList(fromIndex, toIndex));
		return bottomHalf;

	}
	
	
	
}
