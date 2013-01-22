package uk.ac.liv.moduleextraction.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.testing.DependencyCalculator;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class ModuleExtractor {

	int maxPercentageComplete = 0;

	SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	InseperableChecker insepChecker = new InseperableChecker();
	LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	DependencyCalculator dependencyCalculator;

	private Set<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> signature;
	
	
	//TODO should only accept own entities which are ClassNames or RoleNames
	public ModuleExtractor(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		System.out.println(term.size() + ":" + signature.size());
		this.terminology = term;
		this.signature = signature;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		this.dependencyCalculator = new DependencyCalculator(term);
	}
	
	public ModuleExtractor(Set<OWLLogicalAxiom> terminology, Set<OWLEntity> signature) {
		this(terminology,null,signature);
	}
	
	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{
		
		DumpExtractionToDisk dump = new DumpExtractionToDisk(
				"pos",terminology, 
				module, signature);
		
		new Thread(dump).run();
		
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module
		while(!terminology.equals(W)){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);

			//printPercentageComplete(W, terminology, module);
			Set<OWLEntity> signatureAndSigM = new HashSet<OWLEntity>();
			signatureAndSigM.addAll(signature);
			signatureAndSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
			
			/* We can reuse this in the LHS check and syntactic check so do it only once */
			HashMap<OWLClass, Set<OWLEntity>> dependW = dependencyCalculator.getDependenciesFor(W);
			HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(dependW, W, signatureAndSigM);

			if(syntaxDepChecker.hasSyntacticSigDependency(dependW, signatureAndSigM)
					|| insepChecker.isSeperableFromEmptySet(lhsSigT, signatureAndSigM)){

				terminology.remove(chosenAxiom);
				System.out.println("Adding " + chosenAxiom);
				
				module.add(chosenAxiom);
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
			dependW.clear();
		}
		new Thread(dump).run();
		return module;
	}

	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}

	public Set<OWLEntity> getSignature() {
		return signature;
	}
	public Set<OWLLogicalAxiom> getTerminology() {
		return terminology;
	}


	@SuppressWarnings("unused")
	private void printPercentageComplete(Set<OWLLogicalAxiom> W, Set<OWLLogicalAxiom> terminology, Set<OWLLogicalAxiom> module) {
		int terminologySize = terminology.size();
		int wSize = W.size();
		int percentage = (int) Math.floor(((double)wSize/terminologySize)*100);
		if(percentage > maxPercentageComplete){
			maxPercentageComplete = percentage;
			System.out.print((maxPercentageComplete % 10 == 0)? maxPercentageComplete + "% complete \n" : "");
		}
		System.out.println(wSize + ":" + module.size());
	}


}
