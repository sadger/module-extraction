package uk.ac.liv.moduleextraction.main;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class CommandLineInterface {

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.class.path"));
		
		if(args.length != 2)
			System.out.println("Usage: module-extraction.jar <ontology location> <signature size>");
		else{
			String ontologyLocation = args[0];
			int sigSize = Integer.parseInt(args[1]);

			OWLOntology ontology = OntologyLoader.loadOntology(ontologyLocation);
			System.out.println("Loaded ontology containing " + ontology.getLogicalAxiomCount() + " logical axioms.");

			SignatureGenerator sigGen = new SignatureGenerator(ontology.getLogicalAxioms());

			Set<OWLEntity> signature = sigGen.generateRandomSignature(sigSize);

//			System.out.println("Random signature size " + signature.size() + " generated:");
//			System.out.println(signature);
			
			OWLDataFactory f = OWLManager.getOWLDataFactory();
			OWLClass a = f.getOWLClass(IRI.create(ontology.getOntologyID() + "#A"));
			OWLClass b = f.getOWLClass(IRI.create(ontology.getOntologyID() + "#B"));
			OWLClass c = f.getOWLClass(IRI.create(ontology.getOntologyID() + "#C"));
			
			Set<OWLEntity> ents = new HashSet<OWLEntity>();
			ents.add(a);
			ents.add(b);
			ents.add(c);

			System.out.println("Signature: " + ents);
			
			ModuleExtractor extractor = new ModuleExtractor(ontology.getLogicalAxioms(), ents);

			Set<OWLLogicalAxiom> module = null;
			try {
				System.out.println("Starting Module Extraction...");
				module = extractor.extractModule();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (QBFSolverException e) {
				e.printStackTrace();
			}

			System.out.println("==Module==");
			for(OWLLogicalAxiom ax : module)
				System.out.println(ax);
			System.out.println("Size: " + module.size());
			System.out.println("Done.");
		}




	}
}
