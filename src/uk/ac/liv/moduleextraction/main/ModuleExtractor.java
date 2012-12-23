package uk.ac.liv.moduleextraction.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;



import uk.ac.liv.moduleextraction.checkers.DefinitorialDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.testing.DependencyCalculator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ModuleExtractor {

	int maxPercentageComplete = 0;

	SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	InseperableChecker insepChecker = new InseperableChecker();
	QBFSolver qbfSolver = new QBFSolver();
	LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	DependencyCalculator dependencyCalculator;

	private Set<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLClass> signature;
	
	public ModuleExtractor(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLClass> sig) {
		this.terminology = term;
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		this.dependencyCalculator = new DependencyCalculator(term);
	}
	
	public ModuleExtractor(Set<OWLLogicalAxiom> terminology, Set<OWLClass> signature) {
		this(terminology,null,signature);
	}
	
	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module
		while(!terminology.equals(W)){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);

			//printPercentageComplete(W, terminology, module);

			Set<OWLClass> signatureAndSigM = new HashSet<OWLClass>();
			signatureAndSigM.addAll(signature);
			signatureAndSigM.addAll(ModuleUtils.getClassesInSet(module));
			
			/* We can reuse this in the LHS check and syntatic check so do it only once */
			HashMap<OWLClass, Set<OWLClass>> dependW = dependencyCalculator.getDependenciesFor(W);

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
		return module;
	}

	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}

	public Set<OWLClass> getSignature() {
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

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/NCI/expr/nci-08.09d-terminology.owl");
		
//		for(OWLLogicalAxiom ax :ont.getLogicalAxioms()){
//			System.out.println(ax);
//			System.out.println(AxiomSplitter.getDefinitionofAxiom(ax));
//		}
		
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		ModuleExtractor mod = new ModuleExtractor(ont.getLogicalAxioms(), gen.generateRandomClassSignature(500));
		try {
			mod.extractModule();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QBFSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
