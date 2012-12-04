package signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import util.ModulePaths;
import util.ModuleUtils;

public class SignatureGenerator {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private DependencyHierarchy hierarchy;

	public SignatureGenerator(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
		this.hierarchy = new DependencyHierarchy(logicalAxioms);
	}

	public Set<OWLClass> dependenciesUpToHierarchyDepth(OWLClass cls, int depth){
		Set<OWLClass> signature = new HashSet<OWLClass>();
		int maxDepth = hierarchy.getMaxHierarchyDepth(cls);
		
		for(int i = 1; i<=depth && i<=maxDepth; i++)
			signature.addAll(hierarchy.getDependencyForDepth(cls, i));
		
		return signature;
	}
	
	/**
	 * Gets a random signature consisting only of class names
	 * @param ontology - Ontology to extract signature from
	 * @param desiredSize - Desired size of signature

	 * @return Subset of ontology class names representing the random signature
	 */
	public Set<OWLClass> generateRandomClassSignature(int desiredSize){
		Set<OWLClass> result = null;
		Set<OWLClass> signature = ModuleUtils.getClassesInSet(logicalAxioms);
	
		if(desiredSize >= signature.size())
			result = signature;
		else{
			ArrayList<OWLClass> listOfNames = new ArrayList<OWLClass>(signature);
			Collections.shuffle(listOfNames);
			result = new HashSet<OWLClass>(listOfNames.subList(0, desiredSize));
		}
		
		return result;
	}

	public static void main(String[] args) {
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "interp/diff.krss");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		OWLClass cls = f.getOWLClass(IRI.create(ont.getOntologyID() + "#A"));
		System.out.println(gen.dependenciesUpToHierarchyDepth(cls, 2));
	}

}
