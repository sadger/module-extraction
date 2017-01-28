package uk.ac.liv.moduleextraction.signature;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class SignatureGenerator {

	private Set<OWLLogicalAxiom> logicalAxioms;
	
	private Set<OWLClass> axiomsClasses;
	private Set<OWLEntity> axiomsSignature;
	private Set<OWLObjectProperty> axiomsRoles;

	public SignatureGenerator(Set<OWLLogicalAxiom> axioms) {
		this.axiomsClasses =  ModuleUtils.getClassesInSet(axioms);
		this.axiomsSignature = ModuleUtils.getClassAndRoleNamesInSet(axioms);
		this.axiomsRoles = ModuleUtils.getRolesInSet(axioms);
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
	
	public Set<OWLObjectProperty> generateRandomRoles(int desiredSize) {
		
		Set<OWLObjectProperty> result = null;
		
		if(desiredSize >= axiomsRoles.size()){
			result = axiomsRoles;
		}
		else{
			ArrayList<OWLObjectProperty> listOfRoles = new ArrayList<OWLObjectProperty>(axiomsRoles);
			Collections.shuffle(listOfRoles);
			result = new HashSet<OWLObjectProperty>(listOfRoles.subList(0, desiredSize));
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
