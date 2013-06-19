package uk.ac.liv.moduleextraction.signature;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
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
	
	
	public void writeSignatureWithRoles(int sigSize, double rolePercentage, int numberOfTests){
		SigManager sigManager = new SigManager(location);
		SignatureGenerator sigGen = new  SignatureGenerator(ontology.getLogicalAxioms());
		
		for(int i=1; i<=numberOfTests; i++){
			Set<OWLEntity> signature = new HashSet<OWLEntity>();
			signature.addAll(sigGen.generateRandomClassSignature(sigSize));	
			
			int roleCount = ontology.getObjectPropertiesInSignature().size();
			int numberOfRoles = (int) Math.floor(((double) roleCount / 100 ) * rolePercentage);
			
			signature.addAll(sigGen.generateRandomRoles(numberOfRoles));
			
			try {
				sigManager.writeFile(signature, "random" + "_" + sigSize + "_" + ((int) rolePercentage) + "-" + i);
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

		String ontName = "NCI-08.09d";
		
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/" + ontName);

		int[] sigSizes = {100,250,500,750,1000};
		double[] rolePercentages = {0,25,50,75,100};
		int numberOfTests = 1000;

		for (int i = 0; i < sigSizes.length; i++) {
				
			for (int j = 0; j < sigSizes.length; j++) {
				
				WriteRandomSigs writer = 
						new WriteRandomSigs(ont, new File(ModulePaths.getSignatureLocation() + ontName + "-" + sigSizes[i] + "-" + ((int) rolePercentages[j])));
				writer.writeSignatureWithRoles(sigSizes[i], rolePercentages[j],numberOfTests);
			}
//			
			
		}		
		
		
	
	}
}
