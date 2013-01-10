package uk.ac.liv.moduleextraction.checkers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.qbf.QBFFileWriter;
import uk.ac.liv.moduleextraction.qbf.QBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.replacers.InverseRolePropertyReplacer;

public class InseperableChecker {
	
	public boolean isSeperableFromEmptySet(HashSet<OWLLogicalAxiom> w, Set<OWLEntity> signatureAndSigM) throws IOException, QBFSolverException{
		InverseRolePropertyReplacer replacer = new InverseRolePropertyReplacer();
		//Remove inverse roles from the QBF problem
		QBFFileWriter writer = new QBFFileWriter(replacer.convert(w),signatureAndSigM);
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
