package uk.ac.liv.moduleextraction.checkers;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.experiments.ExtractionComparision;
import uk.ac.liv.moduleextraction.qbf.QBFFileWriter;
import uk.ac.liv.moduleextraction.qbf.QBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.replacers.InverseRolePropertyReplacer;

public class InseperableChecker {
	Logger logger = LoggerFactory.getLogger(InseperableChecker.class);
	
	private long testCount = 0;
	private long maxClause = 0;
	private long maxVar= 0;
	private long totalClause = 0;
	private long totalVar = 0;
	
	public boolean isSeperableFromEmptySet(Set<OWLLogicalAxiom> w, Set<OWLEntity> signatureAndSigM) throws IOException, QBFSolverException{
		boolean isInseperable = true;

		/* If W is empty it IS the empty set so cannot be separable from itself */
		if(!w.isEmpty()){
			InverseRolePropertyReplacer replacer = new InverseRolePropertyReplacer();
			//Remove inverse roles from the QBF problem
			QBFFileWriter writer = new QBFFileWriter(replacer.convert(w),signatureAndSigM);
			
			/* An empty clause set need not be checked - in fact the QBF solver complains about
			 * and empty problem*/
			if(writer.convertedClauseSetIsEmpty()){
				isInseperable = true;
			}
			else{
				testCount++;
				QBFSolver solver =  new QBFSolver();
				File qbfProblem = writer.generateQBFProblem();
				isInseperable = solver.isSatisfiable(qbfProblem);

				if(!isInseperable){
					logger.trace("Separable from ∅?: {}",!isInseperable);
				}
				
				totalClause += writer.getClauseCount();
				totalVar += writer.getVariableCount();
				maxClause = Math.max(maxClause, writer.getClauseCount());
				maxVar = Math.max(maxVar, writer.getVariableCount());
			}
			
	
			
	
		
		}

		//We test for inseparablity and return the negation
		return !isInseperable;
	}
	
	public long getTestCount(){
		return testCount;
	}
	
	public LinkedHashMap<String,Long> getQBFMetrics(){
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		metrics.put("Total clause", totalClause);
		metrics.put("Max Clause", maxClause);
		metrics.put("Total var", totalVar);
		metrics.put("Max Var", maxVar);

		return metrics;
	}

}
