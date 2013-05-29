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
	
	public enum ExperimentType{
		ONTOLOGY_SUITABILITY(new OntologySuitability()),
		AMEX_STAR(new AMEXvsSTAR()),
		OLD_NEW(new OLDvsNEW());
		
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
		File newResultFolder = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName() + "-" + experimentType.name());
		if(!newResultFolder.exists()){
			System.out.println("Making new directory " + newResultFolder.getAbsolutePath());
			newResultFolder.mkdir();
		}
		
		
		for(File f : files){
			if(f.isFile()){
				System.out.println("Checking " + f.getName());
				Set<OWLEntity> sig = sigManager.readFile(f.getName());
				experiment.performExperiment(ontology, sig);
				
				//New folder in result location - same name as sig file
				File experimentLocation = new File(newResultFolder.getAbsoluteFile() + "/" + f.getName());
				if(!experimentLocation.exists()){
					experimentLocation.mkdir();
				}
				
				//Save the signature with the experiment
				SigManager managerWriter = new SigManager(experimentLocation);
				managerWriter.writeFile(sig, "sig\nature");
				
				//Write any metrics
				experiment.writeMetrics(experimentLocation);
			}
		}
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + 
				"/Bioportal/NOTEL/Terminologies/LiPrO-converted");		
		try {
			new MultipleExperiments().runExperiments(ont, new File(ModulePaths.getSignatureLocation() + "/LiPro-50"), 
					ExperimentType.OLD_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
