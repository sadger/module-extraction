package uk.ac.liv.moduleextraction.qbf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SeparabilityAxiomLocator {

	Logger logger = LoggerFactory.getLogger(SeparabilityAxiomLocator.class);

	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();

	/* Data structures */
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;

	private long checkCount = 0;

	private OWLLogicalAxiom[] axiomList;

	private ChainDependencies termDependencies; 
	
	public SeparabilityAxiomLocator(List<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> mod, Set<OWLEntity> sig, 
			ChainDependencies dependencies) throws IOException, QBFSolverException{
		this.module = mod;
		this.axiomList = term.toArray(new OWLLogicalAxiom[0]);

		this.sigUnionSigM = sig;
		
		this.termDependencies = dependencies;
		sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
	}

	public SeparabilityAxiomLocator(OWLLogicalAxiom[] subsetAsArray,
			Set<OWLEntity> sigUnionSigM2, ChainDependencies dependT) {
		this.axiomList = subsetAsArray;
		this.sigUnionSigM = sigUnionSigM2;
		this.termDependencies = dependT;
	}

	public OWLLogicalAxiom getInseperableAxiom() throws IOException, QBFSolverException{	
		/* Represents the last axioms added or removed from the split test */

		logger.debug("Finding separability causing axiom");
		OWLLogicalAxiom[] lastAdded = getTopHalf(axiomList);
		OWLLogicalAxiom[]lastRemoved = getBottomHalf(axiomList);

		OWLLogicalAxiom[] W = lastAdded;

		while(lastAdded.length > 0){

			ArrayList<OWLLogicalAxiom> toCheck = new ArrayList<OWLLogicalAxiom>();
			for (int i = 0; i < W.length; i++) {
				toCheck.add(W[i]);
			}

			Set<OWLLogicalAxiom> lhsW = lhsExtractor.getLHSSigAxioms(toCheck, sigUnionSigM, termDependencies);

			checkCount++;

			/* If inseperable */
			if(!insepChecker.isSeperableFromEmptySet(lhsW, sigUnionSigM)){

				lastAdded = getTopHalf(lastRemoved);

				//W.addAll(lastAdded);
				W = concat(W,lastAdded);

				//lastRemoved.removeAll(lastAdded);
				lastRemoved = Arrays.copyOfRange(lastRemoved,lastAdded.length,lastRemoved.length);

				logger.trace("Adding: {}",lastAdded.length);

			}
			else{
				lastRemoved = getBottomHalf(lastAdded);

				//	W.removeAll(lastRemoved);
				W = Arrays.copyOfRange(W,0,W.length - lastRemoved.length);
				//	lastAdded.removeAll(lastRemoved);
				lastAdded = Arrays.copyOfRange(lastAdded, 0,lastAdded.length -lastRemoved.length);

				logger.trace("Removing: {}",lastRemoved.length);

			}

		}
		
		return  axiomList[W.length];
	}

	public long getCheckCount(){
		return checkCount;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private OWLLogicalAxiom[] getTopHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = 0;
		int toIndex = (int) Math.floor(axiomList.length/2);

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}

	private OWLLogicalAxiom[] getBottomHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = (int) Math.floor(axiomList.length/2);
		int toIndex = axiomList.length;

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}



}
