package uk.ac.liv.moduleextraction.signature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;

import uk.ac.liv.moduleextraction.extractor.CyclicOneDepletingModuleExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class WriteAxiomSignatures {

	File saveLocation;
	Set<OWLLogicalAxiom> axioms;
	public WriteAxiomSignatures(OWLOntology ontology, File saveLocation) {
		this.axioms = ontology.getLogicalAxioms();
		this.saveLocation = saveLocation;
	}
	
	public WriteAxiomSignatures(Set<OWLLogicalAxiom> axioms, File saveLocation){
		this.axioms = axioms;
		this.saveLocation = saveLocation;
	}
	
	public void writeAxiomSignatures(){
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		SigManager sigmanager = new SigManager(saveLocation);
		int i = 0;
		for(OWLLogicalAxiom axiom : axioms){
			i++;
			Set<OWLEntity> signature = axiom.getSignature();
			signature.remove(factory.getOWLThing());
			signature.remove(factory.getOWLNothing());
			try {
				//Give each axiom a unique file name
				sigmanager.writeFile(signature, "axiom" + axiom.toString().hashCode());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("Written " + i + " signatures");
	}
	
	
	public static void main(String[] args) throws OWLOntologyCreationException {




        File ontologyLocation = new File(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_15.04d.owl");
        System.out.println(ontologyLocation.getName());
        OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ontologyLocation.getAbsolutePath());
        Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ModuleUtils.getCoreAxioms(ontology), 500);
        WriteAxiomSignatures writer = new WriteAxiomSignatures(subset, new File(ModulePaths.getSignatureLocation() + "/two-depleting/" + ontologyLocation.getName()));
        writer.writeAxiomSignatures();
        ontology.getOWLOntologyManager().removeOntology(ontology);


/*
		File[] files = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only").listFiles();
		int i = 1;
		for(File f : files){
			System.out.println("Expr: " + i++);
			if(f.exists()){
				System.out.print(f.getName() + ": ");
				OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				 WriteAxiomSignatures writer = new WriteAxiomSignatures(ont, 
						 new File(ModulePaths.getSignatureLocation() + "/onedepletingcomparison/AxiomSignatures/" + f.getName()));
				   writer.writeAxiomSignatures();
				   ont.getOWLOntologyManager().removeOntology(ont);
			}
		}
*/




	}

}
