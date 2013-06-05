package uk.ac.liv.moduleextraction.signature;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class WriteRandomSigs {

	private File location;
	private OWLOntology ontology;
	
	public WriteRandomSigs(OWLOntology ont, File loc) {
		this.location = loc;
		this.ontology = ont;
	}
	
	
	/* Writes signatures of size "sigSize" + |all roles in ont| */
	public void writeSignatureWithRoles(int sigSize, int numberOfTests){
		SigManager sigManager = new SigManager(location);
		SignatureGenerator sigGen = new  SignatureGenerator(ontology.getLogicalAxioms());
		
		for(int i=1; i<=numberOfTests; i++){
			Set<OWLEntity> signature = new HashSet<OWLEntity>();
			signature.addAll(sigGen.generateRandomClassSignature(sigSize));	
			signature.addAll(ontology.getObjectPropertiesInSignature());
					
			try {
				sigManager.writeFile(signature, "random" + sigSize + "-"+i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Written " + numberOfTests + " signatures");
	}
	
	
	
	public void writeSignature(int sigSize, int numberOfTests){
		SigManager sigManager = new SigManager(location);
		SignatureGenerator sigGen = new  SignatureGenerator(ontology.getLogicalAxioms());
		
		for(int i=1; i<=numberOfTests; i++){
			Set<OWLEntity> signature = sigGen.generateRandomSignature(sigSize);
			try {
				sigManager.writeFile(signature, "random" + sigSize + "-"+i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Written " + numberOfTests + " signatures");
	}
	
	public static void main(String[] args) {
//		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl-sub");
		
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + 
				"/Bioportal/LiPrO-converted");

		int sigSize = 50;
		WriteRandomSigs writer = new WriteRandomSigs(ont, new File(ModulePaths.getSignatureLocation() + "/LiPro-" + sigSize));
		writer.writeSignature(sigSize, 100);
	}
}
