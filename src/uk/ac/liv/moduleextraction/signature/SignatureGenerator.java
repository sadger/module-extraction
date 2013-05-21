package uk.ac.liv.moduleextraction.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class SignatureGenerator {

	private Set<OWLLogicalAxiom> logicalAxioms;
	
	private Set<OWLClass> axiomsClasses;
	private Set<OWLEntity> axiomsSignature;

	public SignatureGenerator(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
		this.axiomsClasses =  ModuleUtils.getClassesInSet(logicalAxioms);
		this.axiomsSignature = ModuleUtils.getClassAndRoleNamesInSet(logicalAxioms);
	}

	
	/**
	 * Gets a random signature consisting only of class names
	 * @param ontology - Ontology to extract signature from
	 * @param desiredSize - Desired size of signature

	 * @return Subset of ontology class names representing the random signature
	 */
	public Set<OWLClass> generateRandomClassSignature(int desiredSize){
		Set<OWLClass> result = null;
	
		if(desiredSize >= axiomsClasses.size())
			result = axiomsClasses;
		else{
			ArrayList<OWLClass> listOfNames = new ArrayList<OWLClass>(axiomsClasses);
			Collections.shuffle(listOfNames);
			result = new HashSet<OWLClass>(listOfNames.subList(0, desiredSize));
		}
		
		return result;
	}
	
	public Set<OWLEntity> generateRandomSignature(int desiredSize) {
		Set<OWLEntity> result = null;
	
		if(desiredSize >= axiomsSignature.size())
			result = axiomsSignature;
		else{
			ArrayList<OWLEntity> listOfNames = new ArrayList<OWLEntity>(axiomsSignature);
			Collections.shuffle(listOfNames);
			result = new HashSet<OWLEntity>(listOfNames.subList(0, desiredSize));
		}
		
		return result;
	}
	
	public OWLOntology randomAxioms(int size) throws OWLOntologyCreationException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		
		if(logicalAxioms.size() <= size){
			newAxioms.addAll(logicalAxioms);
		}
		else{
			ArrayList<OWLLogicalAxiom> axioms = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
			Collections.shuffle(axioms);
			newAxioms.addAll(axioms.subList(0, size));
		}
		
		return manager.createOntology(newAxioms);
		
	}




}
