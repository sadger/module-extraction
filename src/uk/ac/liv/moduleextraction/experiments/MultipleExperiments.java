package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class MultipleExperiments {


	private Experiment experiment;

	public void runExperiments(OWLOntology ontology, File signaturesLocation, Experiment experimentType) throws IOException{
		this.experiment = experimentType;

		Experiment experiment = experimentType;
		SigManager sigManager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 

		/* Create new folder in result location with same name as signature
		folder */
		File newResultFolder = copyDirectoryStructure(signaturesLocation, "Signatures",new File(ModulePaths.getResultLocation()));





		int experimentCount = 1;
		for(File f : files){
			if(f.isFile()){

				System.out.println("Experiment " + experimentCount + ": " + f.getName());
				experimentCount++;
				//New folder in result location - same name as sig file
				File experimentLocation = new File(newResultFolder.getAbsoluteFile() + "/" + f.getName());
				if(!experimentLocation.exists()){
					experimentLocation.mkdir();
				}
				
				if(new File(experimentLocation.getAbsolutePath() + "/experiment-results").exists()){
					System.out.println("Experiment results already exists - skipping");
					continue;
				}

				Set<OWLEntity> sig = sigManager.readFile(f.getName());
				experiment.performExperiment(sig,f);


				//Save the signature with the experiment
				SigManager managerWriter = new SigManager(experimentLocation);
				managerWriter.writeFile(sig, "signature");

				//Write any metrics
				experiment.writeMetrics(experimentLocation);

			}
		}
	}

	/**
	 * Copy the structure of a source directory to another location creating a directory
	 * for each directory in the path naming the final folder to highlight the experiment
	 * @param source - The directory to begin copying from 
	 * @param sourceLimit - Only start copying the source from this directory 
	 * @param destination - The top level to copy the structure too
	 * @return File - path of deepest part of new directory structure.
	 * Example: copyDirectoryStructure(//a/x/y/z/,"x", /home/) 
	 * result File /home/y/z/  
	 */
	private File copyDirectoryStructure(File source, String sourceLimit, File destination) {
		Stack<String> directoriesToWrite = new Stack<String>();

		//Push all the directories from the end backwards to the sourceLimit (if applicable)
		while(!source.getName().equals(sourceLimit) || source.getParent() == null){
			if(source.isDirectory()){
				directoriesToWrite.push(source.getName());	
			}
			source = source.getParentFile();
		}

		//Build the path from the start of the destinated using the pushed directory names
		String target = destination.getAbsolutePath();
		while(!directoriesToWrite.isEmpty()){
			target = target + "/" + directoriesToWrite.pop();
		}

		File targetFile = new File(target);
		//Name the folder by experiment
		String newFolderName = targetFile.getName() + "-" + experiment.getClass().getSimpleName();
		targetFile = new File(targetFile.getParent() + "/" + newFolderName);


		if(!targetFile.exists()){
			System.out.println("Making directory: " + targetFile.getAbsolutePath());
			targetFile.mkdirs();
		}


		return targetFile;
	} 

	public static void main(String[] args) throws OWLOntologyCreationException, NotEquivalentToTerminologyException, IOException, OWLOntologyStorageException {




		


		File ontLoc = new File(ModulePaths.getOntologyLocation() + "/Thesaurus_08.09d.OWL-QBF");

		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());


	
		MultipleExperiments multi = new MultipleExperiments();
		multi.runExperiments(ont, 
				new File(ModulePaths.getSignatureLocation() + "/qbf-only/AxiomSignatures/" + ontLoc.getName()),
				new SemanticOnlyComparison(ont, ontLoc));
		

	}



}
