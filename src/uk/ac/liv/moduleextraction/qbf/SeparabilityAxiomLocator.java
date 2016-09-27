package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ExtendedLHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;

import java.io.IOException;
import java.util.*;

public class SeparabilityAxiomLocator {

	Logger logger = LoggerFactory.getLogger(SeparabilityAxiomLocator.class);

	/* Semantic Checking */
	protected ExtendedLHSSigExtractor lhsExtractor = new ExtendedLHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();

	private Set<OWLEntity> sigUnionSigM;

	private long checkCount = 0;

	private OWLLogicalAxiom[] axiomList;

	private AxiomDependencies dependT;
	
	public SeparabilityAxiomLocator(OWLLogicalAxiom[] subsetAsArray,Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT) {
		this.axiomList = subsetAsArray;
		this.sigUnionSigM = sigUnionSigM;
		this.dependT = dependT;
	}

	public OWLLogicalAxiom getInseperableAxiom() throws IOException, QBFSolverException{	
		/* Represents the last axioms added or removed from the split test */

		logger.debug("Finding separability causing axiom");
		OWLLogicalAxiom[] lastAdded = getTopHalf(axiomList);
		OWLLogicalAxiom[]lastRemoved = getBottomHalf(axiomList);

		OWLLogicalAxiom[] W = lastAdded;

		while(lastAdded.length > 0){

			Collection<OWLLogicalAxiom> toCheck = null;
         
        	toCheck = getCheckingSet(Arrays.asList(W), sigUnionSigM, dependT);

			checkCount++;

			/* If inseperable */
			if(!insepChecker.isSeperableFromEmptySet(toCheck, sigUnionSigM)){

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
	
	public HashSet <OWLLogicalAxiom> getCheckingSet(List<OWLLogicalAxiom> axioms, Set<OWLEntity> sigUnionSigM, AxiomDependencies dependT){
		return lhsExtractor.getLHSSigAxioms(axioms, sigUnionSigM, dependT);
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
