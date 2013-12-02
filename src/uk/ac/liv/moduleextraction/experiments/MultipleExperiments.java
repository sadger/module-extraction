package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.extractor.EquivalentToTerminologyExtractor;
import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.signature.WriteRandomSigs;
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

				System.out.println("Experment " + experimentCount + ": " + f.getName());

				Set<OWLEntity> sig = sigManager.readFile(f.getName());
				experiment.performExperiment(sig,f);

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

		//Build the path from the start of the destinated using the pushed directory names2
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

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try{
			BufferedReader br = new BufferedReader(new FileReader(ModulePaths.getSignatureLocation() + "NewIteratingExperiments/acyclic-supported-no-nci"));
			String line;
			while ((line = br.readLine()) != null) {
			   File ontologyLocation = new File(line);
			   System.out.println(ontologyLocation.getName());
			   OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ontologyLocation.getAbsolutePath());
			   Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>();
			   for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
				   logicalAxioms.add(axiom);
			   }
			   OWLOntology logicalOntology = ontology.getOWLOntologyManager().createOntology(logicalAxioms);
			   ontology = null;
			   new MultipleExperiments().runExperiments(
					   logicalOntology, 
					   new File(ModulePaths.getSignatureLocation() + "/NewIteratingEvaluation/AxiomSignatures/" + ontologyLocation.getName()), 
					   new IteratingModuleInspector(logicalOntology));
			  
			  
			   logicalOntology = null;
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

	}



}
