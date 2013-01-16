package uk.ac.liv.moduleextraction.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class SignatureGenerator {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private DependencyHierarchy hierarchy;
	
	private Set<OWLClass> axiomsClasses;
	private Set<OWLEntity> axiomsSignature;

	public SignatureGenerator(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
//		this.hierarchy = new DependencyHierarchy(logicalAxioms);
		this.axiomsClasses =  ModuleUtils.getClassesInSet(logicalAxioms);
		this.axiomsSignature = ModuleUtils.getClassAndRoleNamesInSet(logicalAxioms);
	}

//	public Set<OWLClass> dependenciesUpToHierarchyDepth(OWLClass cls, int depth){
//		Set<OWLClass> signature = new HashSet<OWLClass>();
//		int maxDepth = hierarchy.getMaxHierarchyDepth(cls);
//		
//		for(int i = 1; i<=depth && i<=maxDepth; i++)
//			signature.addAll(hierarchy.getDependencyForDepth(cls, i));
//		
//		return signature;
//	}
	
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
	




}
