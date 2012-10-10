package main;

import java.util.HashSet;
import java.util.Set;

import interpretation.util.AxiomSplitter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import temp.ontologyloader.OntologyLoader;

import checkers.Dependencies;

public class SignatureDependencyChecker {

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	Dependencies deps;

	public OWLOntology retrieveSignatureDependencies(OWLOntology ontology, Set<OWLClass> signature){
		deps = new Dependencies(ontology.getLogicalAxioms());
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(!signature.contains(name) && !isInDependencies(name, signature)){
				manager.removeAxiom(ontology, axiom);
			}
		}
		return ontology;
	}

	public boolean isInDependencies(OWLClass name, Set<OWLClass> signature){
		for(OWLClass sigElem : signature){
			Set<OWLClass> sigDeps = deps.getDependenciesFor(sigElem);
			if(sigDeps.contains(name)){
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println();
		OWLOntology ont = OntologyLoader.loadOntology();
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		System.out.println(ont);
		HashSet<OWLClass> signature = new HashSet<OWLClass>();
		OWLClass a = f.getOWLClass(IRI.create(ont.getOntologyID() + "#A"));
		OWLClass b = f.getOWLClass(IRI.create(ont.getOntologyID() + "#B"));
		signature.add(a);
		signature.add(b);

		SignatureDependencyChecker s = new SignatureDependencyChecker();
		s.retrieveSignatureDependencies(ont, signature);

		System.out.println(ont);

	}

}
