package uk.ac.liv.moduleextraction.checkers;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.qbf.DepQBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.nElementQBFProblemGenerator;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class NElementInseparableChecker {

	
	private long testCount = 0;
	private nAxiomToClauseStore clauseStore;

	public NElementInseparableChecker(nAxiomToClauseStore clauseStore){
		this.clauseStore = clauseStore;
	}
	
	public boolean isSeparableFromEmptySet(Collection<OWLLogicalAxiom> w, Set<OWLEntity> signatureAndSigM) throws IOException, QBFSolverException, ExecutionException {
		boolean isInseparable = true;

		/* If W is empty it IS the empty set so cannot be separable from itself */
		if(!w.isEmpty()){
			nElementQBFProblemGenerator writer = new nElementQBFProblemGenerator(clauseStore,w,signatureAndSigM);
			
			/* An empty clause set need not be checked - in fact the QBF solver complains about
			 * and empty problem*/
			if(writer.convertedClausesAreEmpty()){
				isInseparable = true;
			}
			/* If any of the axioms are logically false the whole problem is unsatisfiable */
			else if(writer.isUnsatisfiable()){
				isInseparable = false;
			}
			else{
				testCount++;
                DepQBFSolver solver = new DepQBFSolver(writer.getUniversalVariables(),writer.getExistentialVariables(),writer.getClauses());
				isInseparable = solver.isSatisfiable();
                //solver.delete();
			}
	
		}

		//We test for inseparablity and return the negationz
		return !isInseparable;
	}

	public long getTestCount(){
		return testCount;
	}


	public LinkedHashMap<String,Long> getQBFMetrics() {
		return new LinkedHashMap<String, Long>();
	}
}
