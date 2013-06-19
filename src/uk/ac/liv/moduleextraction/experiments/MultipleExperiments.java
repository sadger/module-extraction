package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class MultipleExperiments {
	
	
	
	public void runExperiments(OWLOntology ontology, File signaturesLocation, Experiment experimentType) throws IOException{
		Experiment experiment = experimentType;
		SigManager sigManager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 
		
		/* Create new folder in result location with same name as signature
		folder */
		File newResultFolder = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName());
		if(!newResultFolder.exists()){
			System.out.println("Making new directory " + newResultFolder.getAbsolutePath());
			newResultFolder.mkdir();
		}
		
		
		for(File f : files){
			if(f.isFile()){
				System.out.println("Checking " + f.getName());
				Set<OWLEntity> sig = sigManager.readFile(f.getName());
				experiment.performExperiment(sig);
				
				//New folder in result location - same name as sig file
				File experimentLocation = new File(newResultFolder.getAbsoluteFile() + "/" + f.getName());
				if(!experimentLocation.exists()){
					experimentLocation.mkdir();
				}
				
				//Save the signature with the experiment
				SigManager managerWriter = new SigManager(experimentLocation);
				managerWriter.writeFile(sig, "signature");
				
				//Write any metrics
				experiment.writeMetrics(experimentLocation);
			}
		}
	}
	
	public static void main(String[] args) {
		String ontName = "NCI-08.09d";
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/" + ontName);
		
		
		try {
			int[] testSizes = {750,1000};
			
			
			for (int i = 0; i < testSizes.length; i++) {
				if(testSizes[i] == 1000){
					new MultipleExperiments().
					runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/" + ontName + "-" + testSizes[i] + "-0"), new AMEXvsSTAR(ont));
					new MultipleExperiments().
					runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/" + ontName + "-" + testSizes[i] + "-25"), new AMEXvsSTAR(ont));
				}
				new MultipleExperiments().
				runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/" + ontName + "-" + testSizes[i] + "-50"), new AMEXvsSTAR(ont));
				new MultipleExperiments().
				runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/" + ontName + "-" + testSizes[i] + "-75"), new AMEXvsSTAR(ont));
				new MultipleExperiments().
				runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/" + ontName + "-" + testSizes[i] + "-100"), new AMEXvsSTAR(ont));
		 
			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
