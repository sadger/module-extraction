package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import qbf.QBFSolver;
import qbf.QBFSolverException;
import replacers.ModuleUtils;

import checkers.InseperableChecker;
import checkers.SyntacticDependencyChecker;

import temp.ontologyloader.OntologyLoader;

public class ModuleExtractor {

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	InseperableChecker insepChecker = new InseperableChecker();
	QBFSolver qbfSolver = new QBFSolver();
	int maxPercentageComplete = 0;

	public ModuleExtractor() throws OWLOntologyCreationException {
	
	}
	
	public HashSet<OWLLogicalAxiom> extractModule(OWLOntology ontology, Set<OWLClass> signature) throws 
	OWLOntologyCreationException, IOException, QBFSolverException{		
		HashSet<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> W = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> terminology = (HashSet<OWLLogicalAxiom>) ontology.getLogicalAxioms();
		
		int iterations = 1;
		
		while(!hasTheSameAxioms(getDifference(terminology, module),W)){		
			HashSet<OWLLogicalAxiom> differenceOfAll = getDifference(getDifference(terminology, module),W);
			OWLLogicalAxiom a = chooseAxiomOfSet(differenceOfAll);
			//System.out.println("Chosen axiom: " + a);

			W.add(a);
			
			Set<OWLClass> signatureAndM = new HashSet<OWLClass>();
			signatureAndM.addAll(signature);
			signatureAndM.addAll(ModuleUtils.getClassesInSet(module));
			printPercentageComplete(W, terminology, module);

			if(syntaxDepChecker.hasSyntacticSigDependency(W, signatureAndM) 
					|| insepChecker.isInseperableFromEmptySet(W, signatureAndM)){
				
				//System.out.println("Adding " + a + " to module");
				module.add(a);
				W.clear();
			}
			iterations++;
		}

		System.out.format("\n100%% complete - Complete in %d iterations\n", iterations);
		return module;
	}

	public HashSet<OWLLogicalAxiom> getDifference(HashSet<OWLLogicalAxiom> ontology1, HashSet<OWLLogicalAxiom> ontology2){
		@SuppressWarnings("unchecked")
		HashSet<OWLLogicalAxiom> temp = (HashSet<OWLLogicalAxiom>) ontology1.clone();
		temp.removeAll(ontology2);
		return temp;
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
	
	private boolean hasTheSameAxioms(Set<OWLLogicalAxiom> ontology1, Set<OWLLogicalAxiom> ontology2){
		return ontology1.equals(ontology2);
	}




	private OWLLogicalAxiom chooseAxiomOfSet(HashSet<OWLLogicalAxiom> ont){
		@SuppressWarnings("unchecked")
		HashSet<OWLLogicalAxiom> axs = (HashSet<OWLLogicalAxiom>) ont.clone();
		//TreeSet<OWLLogicalAxiom> axioms = new TreeSet<OWLLogicalAxiom>(axs);
		
		ArrayList<OWLLogicalAxiom> listy = new ArrayList<OWLLogicalAxiom>(axs);
		
		return listy.get(0);
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

		ModuleExtractor mod = null;
		try {
			mod = new ModuleExtractor();
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		int percentOfAxioms = (int) Math.max(1, Math.round(((double) ont.getLogicalAxiomCount()/100)*10));
		
		
		OWLOntology chosenOnt = nci1;
		Set<OWLClass> randomSignature = mod.generateRandomClassSignature(chosenOnt,50);
//		System.out.println("Signature: " + randomSignature);
		System.out.println("Signaure Size: " + randomSignature);
		System.out.println("Ontology Size: " + chosenOnt.getLogicalAxiomCount());
		
		HashSet<OWLLogicalAxiom> module = null;
		try {
			module = mod.extractModule(chosenOnt, randomSignature);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
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
