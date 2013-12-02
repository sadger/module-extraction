package uk.ac.liv.moduleextraction.signature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;
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
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(ModulePaths.getSignatureLocation() + "NewIteratingExperiments/acyclic-supported-no-nci"));
			String line;
			while ((line = br.readLine()) != null) {
			   File ontologyLocation = new File(line);
			   System.out.println(ontologyLocation.getName());
			   OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ontologyLocation.getAbsolutePath());
			   Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ModuleUtils.getCoreAxioms(ontology), 500);
			   WriteAxiomSignatures writer = new WriteAxiomSignatures(subset, new File(ModulePaths.getSignatureLocation() + "/NewIteratingExperiments/CoreAxiomSignatures/" + ontologyLocation.getName()));
			   writer.writeAxiomSignatures();
			   ontology.getOWLOntologyManager().removeOntology(ontology);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		



	}

}
