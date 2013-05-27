package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class MultipleExperiments {
	
	public enum ExperimentType{
		ONTOLOGY_SUITABILITY(new OntologySuitability());
		
		private Experiment experiment;
		ExperimentType(Experiment expr){
			this.experiment = expr;
		}
		
		public Experiment getExperiment(){
			return experiment;
		}
	}
	
	public void runExperiments(OWLOntology ontology, File signaturesLocation, ExperimentType experimentType) throws IOException{
		Experiment experiment = experimentType.getExperiment();
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
				experiment.performExperiment(ontology, sigManager.readFile(f.getName()));
				
				//New folder in result location - same name as sig file
				File experimentLocation = new File(newResultFolder.getAbsoluteFile() + "/" + f.getName());
				if(!experimentLocation.exists()){
					experimentLocation.mkdir();
				}
				experiment.writeMetrics(experimentLocation);
			}
		}
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/Bioportal/NatPrO");
		try {
			new MultipleExperiments().runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/axiomtest"), 
					ExperimentType.ONTOLOGY_SUITABILITY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
