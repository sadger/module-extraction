package uk.ac.liv.moduleextraction.signature;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class WriteAxiomSignatures {

	File saveLocation;
	OWLOntology ontology;
	public WriteAxiomSignatures(OWLOntology ontology, File saveLocation) {
		this.ontology = ontology;
		this.saveLocation = saveLocation;
	}
	
	
	public void writeAxiomSignatures(){
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		SigManager sigmanager = new SigManager(saveLocation);
		int i = 0;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
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
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		OWLOntology subOnt = gen.randomAxioms(20000);
		
		WriteAxiomSignatures writer = new WriteAxiomSignatures(subOnt, new File(ModulePaths.getSignatureLocation() + "/Paper/NCI-20k-axioms"));
		writer.writeAxiomSignatures();
		


	}
}
