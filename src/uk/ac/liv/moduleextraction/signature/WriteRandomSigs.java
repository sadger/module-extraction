package uk.ac.liv.moduleextraction.signature;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


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
		//File ontloc = new File("/users/loco/wgatens/ecai-testing/Ontologies/Thesaurus_14.05d.owl-QBF");

		File ontloc = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/0a3f75bb-693b-4adb-b277-dc7fe493d3f4_DUL.owl-QBF");

		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontloc.getAbsolutePath());
		WriteRandomSigs writer = new WriteRandomSigs(
				ont, 
				new File(ModulePaths.getSignatureLocation() + "qbfzoom/" + ontloc.getName()));

		writer.writeSignature(10,10);

		

		
		
	}
}
