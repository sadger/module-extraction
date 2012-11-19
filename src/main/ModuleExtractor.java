package main;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.tools.DiagnosticCollector;

import loader.OntologyLoader;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import checkers.InseperableChecker;
import checkers.LHSSigExtractor;
import checkers.SyntacticDependencyChecker;

import qbf.QBFSolver;
import qbf.QBFSolverException;
import timers.DumpExtractionToDisk;
import util.ModulePaths;
import util.ModuleUtils;

public class ModuleExtractor {
	
	int maxPercentageComplete = 0;

	SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	InseperableChecker insepChecker = new InseperableChecker();
	QBFSolver qbfSolver = new QBFSolver();
	LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	
	private final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);
	
	public HashSet<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> terminology, HashSet<OWLLogicalAxiom> existingModule, Set<OWLClass> signature) throws IOException, QBFSolverException{
		
		HashSet<OWLLogicalAxiom> module = null;
		if(existingModule == null)
		 module = new HashSet<OWLLogicalAxiom>();
		else
		 module = existingModule;
		
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
		
		scheduler.scheduleAtFixedRate(new DumpExtractionToDisk("test",terminology, module, signature), 0, 5, TimeUnit.MINUTES);
		
		//Terminology is the value of T\M as we remove items as we add them to the module
		while(!terminology.equals(W)){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();
			
			W.add(chosenAxiom);
			
		printPercentageComplete(W, terminology, module);
			
			Set<OWLClass> signatureAndM = new HashSet<OWLClass>();
			signatureAndM.addAll(signature);
			signatureAndM.addAll(ModuleUtils.getClassesInSet(module));
			
			HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(W, signatureAndM);
			
			if(syntaxDepChecker.hasSyntacticSigDependency(W, signatureAndM)
					|| insepChecker.isSeperableFromEmptySet(lhsSigT, signatureAndM)){

				terminology.remove(chosenAxiom);
				System.out.println("Adding " + chosenAxiom);
				module.add(chosenAxiom);
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
			
		}
		
		return module;
	}
	
	public HashSet<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> terminology, Set<OWLClass> signature) throws IOException, QBFSolverException{
		return extractModule(terminology,null,signature);
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
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"NCI/Thesaurus_08.11d-terminology.owl");
		
		//OWLOntology nci1 = OntologyLoader.loadOntology("/home/william/PhD/Ontologies/NCI/nci-09.03d.owl");
		//OWLOntology nci2 = OntologyLoader.loadOntology("/home/william/Phd/Ontologies/NCI/nci-10.02d.owl");

		ModuleExtractor mod = null;
		mod = new ModuleExtractor();
		//int percentOfAxioms = (int) Math.max(1, Math.round(((double) ont.getLogicalAxiomCount()/100)*10));
		
		
		OWLOntology chosenOnt = ont;

		
		Set<OWLClass> randomSignature = ModuleUtils.generateRandomClassSignature(chosenOnt,200);
//		System.out.println("Signature: " + randomSignature);
		System.out.println("Signaure Size: " + randomSignature);
		System.out.println("Ontology Size: " + chosenOnt.getLogicalAxiomCount());
		
		HashSet<OWLLogicalAxiom> module = null;
		try {
			System.out.println("Signature size " + randomSignature.size());
			module = mod.extractModule(chosenOnt.getLogicalAxioms(), randomSignature);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		

		System.out.println("\nExtracted Module (" + module.size() + ") :");
		for(OWLLogicalAxiom ax: module){
			System.out.println(ax);
		}
		System.out.println("Size :" + module.size());
	}
}
