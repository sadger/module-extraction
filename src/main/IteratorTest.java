package main;

import java.io.IOException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import checkers.ALCAxiomChecker;
import checkers.ELChecker;
import checkers.InseperableChecker;
import checkers.LHSSigExtractor;
import checkers.SyntacticDependencyChecker;

import qbf.QBFSolver;
import qbf.QBFSolverException;
import replacers.ModuleUtils;
import temp.ontologyloader.OntologyLoader;

public class IteratorTest {
	
	int maxPercentageComplete = 0;

	SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	InseperableChecker insepChecker = new InseperableChecker();
	QBFSolver qbfSolver = new QBFSolver();
	LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	
	public HashSet<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> terminology, Set<OWLClass> signature) throws IOException, QBFSolverException{
		HashSet<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
		
		
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
				module.add(chosenAxiom);
				W.clear();
				//reset the iterator
				axiomIterator = terminology.iterator();
			}
			
			
			
		}
		
		return module;
		
	}
	/**
	 * Gets a random signature consisting only of class names
	 * @param ontology - Ontology to extract signature from
	 * @param desiredSize - Desired size of signatureA simple SMS text message costs literally nothing to send. Whether you are charged per text, or pay for unlimited text messages, it is all profit for your wireless carrier.

	 * @return Subset of ontology class names representing the random signature
	 */
	public Set<OWLClass> generateRandomClassSignature(OWLOntology ontology, int desiredSize){
		Set<OWLClass> result = null;
		Set<OWLClass> signature = ontology.getClassesInSignature();
	
		if(desiredSize >= signature.size()){
			result = signature;
		}
		else{
			ArrayList<OWLClass> listOfNames = new ArrayList<OWLClass>(signature);
			Collections.shuffle(listOfNames);
			result = new HashSet<OWLClass>(listOfNames.subList(0, desiredSize));
		}
		
		return result;
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
	
		OWLOntology ont = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/module/pathway.obo");
		
		OWLOntology ont2 = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/module/material.owl");

		OWLOntology one = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/interp/diff.krss");
		OWLOntology two = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/interp/diff2.krss");
		
		OWLOntology nci1 = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/NCI/nci-09.03d.owl");
		//OWLOntology nci2 = OntologyLoader.loadOntology("/users/loco/wgatens/Ontologies/NCI/nci-10.02d.owl");

		IteratorTest mod = null;
		mod = new IteratorTest();
		int percentOfAxioms = (int) Math.max(1, Math.round(((double) ont.getLogicalAxiomCount()/100)*10));
		
		
		OWLOntology chosenOnt = nci1;

		
		Set<OWLClass> randomSignature = mod.generateRandomClassSignature(chosenOnt,1000);
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
