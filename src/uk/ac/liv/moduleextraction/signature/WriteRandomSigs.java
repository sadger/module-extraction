package uk.ac.liv.moduleextraction.signature;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
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
		this.ontology = ont;
		this.sigGen = new  SignatureGenerator(ontology.getLogicalAxioms());
	}
	
	
	public void writeSignatureWithRoles(int sigSize, double rolePercentage, int numberOfTests){
		SigManager sigManager = new SigManager(location);
		
		
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
		try{
			BufferedReader br = new BufferedReader(new FileReader(ModulePaths.getSignatureLocation() + "NewIteratingExperiments/acyclic-supported-no-nci"));
			String line;
			while ((line = br.readLine()) != null) {
			   File ontologyLocation = new File(line);
			   System.out.println(ontologyLocation.getName());
			   OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ontologyLocation.getAbsolutePath());
			   WriteRandomSigs writer =  new WriteRandomSigs(ontology, new File(ModulePaths.getSignatureLocation() + "/NewIteratingExperiments/" + ontologyLocation.getName()));
			   int percentage = (int) Math.round(Math.min(ModuleUtils.getCoreSize(ontology.getLogicalAxioms()) * 0.1, 1000));
			   writer.writeSignature(percentage, 200);
			   ontology.getOWLOntologyManager().removeOntology(ontology);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
