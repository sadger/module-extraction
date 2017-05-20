package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.checkers.NElementInseparableChecker;
import uk.ac.liv.moduleextraction.propositional.nSeparability.nAxiomToClauseStore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class NElementSeparabilityAxiomLocator {


	/* Semantic Checking */
	private NElementInseparableChecker insepChecker;

	private Set<OWLEntity> sigUnionSigM;

	private long checkCount = 0;

	private OWLLogicalAxiom[] axiomList;


	public NElementSeparabilityAxiomLocator(nAxiomToClauseStore clauseStoreMapping, OWLLogicalAxiom[] subsetAsArray, Set<OWLEntity> sigUnionSigM) {
		this.axiomList = subsetAsArray;
		this.sigUnionSigM = sigUnionSigM;
		this.insepChecker = new NElementInseparableChecker(clauseStoreMapping);
	}

	public OWLLogicalAxiom getSeparabilityCausingAxiom() throws IOException, QBFSolverException, ExecutionException {

        /* Represents the last axioms added or removed from the split test */

		OWLLogicalAxiom[] lastAdded = getTopHalf(axiomList);
		OWLLogicalAxiom[] lastRemoved = getBottomHalf(axiomList);

		OWLLogicalAxiom[] W = lastAdded;

		while(lastAdded.length > 0){

			Collection<OWLLogicalAxiom> toCheck = getCheckingSet(W);

			checkCount++;

			/* If inseparable */
			if(!insepChecker.isSeparableFromEmptySet(toCheck, sigUnionSigM)){
				lastAdded = getTopHalf(lastRemoved);
				W = concat(W,lastAdded);
				lastRemoved = Arrays.copyOfRange(lastRemoved,lastAdded.length,lastRemoved.length);
			}
			else{
				lastRemoved = getBottomHalf(lastAdded);
				W = Arrays.copyOfRange(W,0,W.length - lastRemoved.length);
				lastAdded = Arrays.copyOfRange(lastAdded, 0,lastAdded.length -lastRemoved.length);
			}
		}

		return axiomList[W.length];
	}
	

	public Collection<OWLLogicalAxiom> getCheckingSet(OWLLogicalAxiom[] input){
		return Arrays.asList(input);
	}

	public long getCheckCount(){
		return checkCount;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	protected OWLLogicalAxiom[] getTopHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = 0;
		int toIndex = (int) Math.floor(axiomList.length/2);

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}

	protected OWLLogicalAxiom[] getBottomHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = (int) Math.floor(axiomList.length/2);
		int toIndex = axiomList.length;

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}



}
