package checkers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import qbf.QBFConvertor;
import qbf.QBFSolver;
import qbf.QBFSolverException;
import replacers.InverseRolePropertyReplacer;

public class InseperableChecker {
	public boolean isInseperableFromEmptySet(HashSet<OWLLogicalAxiom> w, Set<OWLClass> sig) throws IOException, QBFSolverException{
		QBFConvertor convertor = new QBFConvertor();
		QBFSolver solver =  new QBFSolver();
//
//		System.out.println(w);
//		System.out.println(sig);
		//Remove inverse roles from the QBF problem
		InverseRolePropertyReplacer replacer = new InverseRolePropertyReplacer();
		File qbfProblem = convertor.generateQBFProblem(replacer.convert(w), sig);
		boolean result = solver.isSatisfiable(qbfProblem);

		if(!result){
			System.out.println("Separable from ∅?: " + !result);
		}

		return !result;
	}

}
