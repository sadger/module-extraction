package uk.ac.liv.moduleextraction.signature;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;


public class WriteRandomSigs {

	private File location;
	private OWLOntology ontology;
	SignatureGenerator sigGen;
	
	public WriteRandomSigs(OWLOntology ont, File loc) {
		this.location = loc;
		if(!loc.exists()){
			loc.mkdirs();
		}
		this.ontology = ont;
		this.sigGen = new  SignatureGenerator(ontology.getLogicalAxioms());
	}
	
	
	public void writeSignatureWithRoles(int sigSize, double rolePercentage, int numberOfTests){
		
		File rolePct = new File(location.getAbsolutePath() + "/role-" + ((int) rolePercentage) + "/size-" + sigSize);
		if(!rolePct.exists()){
			rolePct.mkdirs();
		}
		SigManager sigManager = new SigManager(rolePct);
		
		
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
		File ontloc = new File("/users/loco/wgatens/ecai-testing/Ontologies/" + "NCI-star-equiv.owl");

		int[] intervals = {100,250,500,750,1000};
		double[] roles = {0,50,100};
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontloc.getAbsolutePath());
		WriteRandomSigs writer = new WriteRandomSigs(
				ont, 
				new File("/users/loco/wgatens/ecai-testing/Signatures/" + "/RandomSignatures/" + ontloc.getName()));
		
		for(int i : intervals){
			for(double r : roles){
				writer.writeSignatureWithRoles(i, r, 200);
			}
		}

		

		
		
	}
}
