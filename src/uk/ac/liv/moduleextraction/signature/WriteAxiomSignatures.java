package uk.ac.liv.moduleextraction.signature;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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



		File[] files = new File(ModulePaths.getOntologyLocation() + "/Bioportal/at-most-sriq").listFiles();
		int i = 1;
		for(File f : files){
			System.out.println("Expr: " + i++);
			if(f.exists()){
				System.out.print(f.getName() + ": ");
				OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				 WriteAxiomSignatures writer = new WriteAxiomSignatures(ont,
						 new File(ModulePaths.getSignatureLocation() + "/Bioportal/at-most-sriq/" + f.getName()));
				   writer.writeAxiomSignatures();
				   ont.getOWLOntologyManager().removeOntology(ont);
			}
		}

	}

}
