package checkers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import qbf.QBFConvertor;
import qbf.QBFFileWriter;
import qbf.QBFSolver;
import qbf.QBFSolverException;
import replacers.InverseRolePropertyReplacer;

public class InseperableChecker {
	
	public boolean isSeperableFromEmptySet(HashSet<OWLLogicalAxiom> w, Set<OWLClass> sig) throws IOException, QBFSolverException{
		InverseRolePropertyReplacer replacer = new InverseRolePropertyReplacer();
		//Remove inverse roles from the QBF problem
		QBFFileWriter writer = new QBFFileWriter(replacer.convert(w),sig);
		QBFSolver solver =  new QBFSolver();


		boolean isInseperable = true;
		
		//If W is empty of course it IS the empty set so is not inseperable from itself
		if(!w.isEmpty()){
			File qbfProblem = writer.generateQBFProblem();
			isInseperable = solver.isSatisfiable(qbfProblem);

			if(!isInseperable){
				System.out.println("Separable from ∅?: " + !isInseperable);
			}
		}

		//We test for inseperablity and return the negation
		return !isInseperable;
	}

}
