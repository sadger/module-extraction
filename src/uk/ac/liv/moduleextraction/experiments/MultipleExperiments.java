package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

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
		
		int experimentCount = 1;
		for(File f : files){
			if(f.isFile()){
				
				System.out.println("Experment " + experimentCount + ": " + f.getName());
			
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
				experimentCount++;
			}
		}
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, NotEquivalentToTerminologyException, IOException, OWLOntologyStorageException {
	
		OWLOntology nci_full = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "Thesaurus_08.09d.OWL");
	
		new MultipleExperiments().runExperiments(nci_full, new File(ModulePaths.getSignatureLocation() + "/mystery"), new IteratingExperiment(nci_full));
		

	}
	
	
	
}
