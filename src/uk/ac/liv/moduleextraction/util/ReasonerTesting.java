package uk.ac.liv.moduleextraction.util;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class ReasonerTesting {

	
	OWLReasoner reasoner;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private OWLOntology ont;
	
	public ReasonerTesting(OWLOntology ont) throws OWLOntologyCreationException {
		ToStringRenderer stringRender = ToStringRenderer.getInstance();
		DLSyntaxObjectRenderer renderer;
		renderer =  new DLSyntaxObjectRenderer();
		stringRender.setRenderer(renderer);

		this.ont = ont;
		//Use Hermit 
		//OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory(); 
		
		//Use structural reasoner
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		this.reasoner = reasonerFactory.createReasoner(ont);
	}
	
	public void testThings(){
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		OWLClass a = factory.getOWLClass(IRI.create("#A"));
		OWLClass b = factory.getOWLClass(IRI.create("#B"));
		OWLClass c = factory.getOWLClass(IRI.create(ont.getOntologyID() + "#C"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(ont.getOntologyID() + "#r"));
		
		
		OWLSubClassOfAxiom sub = 
				factory.getOWLSubClassOfAxiom(a, factory.getOWLObjectUnionOf(b, factory.getOWLObjectAllValuesFrom(r, factory.getOWLThing())));
		
	
		
		System.out.println("Entailed " + sub + ": "+ reasoner.isEntailed(sub));
	}
	
	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology empty = null;
		try {
			empty = manager.createOntology();
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		try {
			new ReasonerTesting(empty).testThings();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	
}
