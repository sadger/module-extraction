package main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import temp.ontologyloader.OntologyLoader;

public class IteratorTest {

	
	public static void main(String[] args) {

		HashSet<OWLLogicalAxiom> axioms = (HashSet<OWLLogicalAxiom>) OntologyLoader.loadOntology().getLogicalAxioms();
		HashSet<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> w  = new HashSet<OWLLogicalAxiom>();
		

		
		Random r = new Random();
		
		for (int i = 0; i < 4; i++) {
			HashSet<OWLLogicalAxiom> toRemove = new HashSet<OWLLogicalAxiom>();
			System.out.println(i+1);
			
			axioms.removeAll(toRemove);
			Iterator<OWLLogicalAxiom> axiomIterator = axioms.iterator();
			
			while(axiomIterator.hasNext()){
				OWLLogicalAxiom axiom = axiomIterator.next();
				float chance = r.nextFloat();
				if(chance <= 0.50f){
					toRemove.add(axiom);
					module.add(axiom);
					break;
				}
			}
			
		}
		System.out.println(axioms);
		System.out.println(module);
	}
}
