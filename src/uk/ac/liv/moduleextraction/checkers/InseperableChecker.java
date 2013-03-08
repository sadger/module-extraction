package uk.ac.liv.moduleextraction.checkers;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.experiments.ExtractionComparision;
import uk.ac.liv.moduleextraction.qbf.QBFFileWriter;
import uk.ac.liv.moduleextraction.qbf.QBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.replacers.InverseRolePropertyReplacer;

public class InseperableChecker {
	Logger logger = LoggerFactory.getLogger(InseperableChecker.class);
	
	static int testCount = 0;
	
	public boolean isSeperableFromEmptySet(Set<OWLLogicalAxiom> w, Set<OWLEntity> signatureAndSigM) throws IOException, QBFSolverException{
		InverseRolePropertyReplacer replacer = new InverseRolePropertyReplacer();
		//Remove inverse roles from the QBF problem
		QBFFileWriter writer = new QBFFileWriter(replacer.convert(w),signatureAndSigM);
		QBFSolver solver =  new QBFSolver();
		

		boolean isInseperable = true;

		/* If W is empty it IS the empty set so cannot be separable from itself */
		if(!w.isEmpty()){
			testCount++;
			File qbfProblem = writer.generateQBFProblem();
			isInseperable = solver.isSatisfiable(qbfProblem);

			if(!isInseperable){
				logger.debug("Separable from ∅?: {}",!isInseperable);
			}
		}

		//We test for inseparablity and return the negation
		return !isInseperable;
	}
	
	public static int getTestCount() {
		return testCount;
	}

}
