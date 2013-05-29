package uk.ac.liv.moduleextraction.experiments;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.reloading.ReloadExperimentFromDisk;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ExtractionComparision {

	Logger logger = LoggerFactory.getLogger(ExtractionComparision.class);
	
	OWLOntologyManager manager;
	OWLOntology ontology;
	SyntacticFirstModuleExtraction moduleExtractor;
	SyntacticLocalityModuleExtractor syntaxModExtractor;

	private Set<OWLEntity> signature;
	private DumpExtractionToDisk dump;

	private long timeTaken = 0;
	
	private File experimentLocation;
	
	
	public ExtractionComparision(OWLOntology ontology, Set<OWLEntity> sig, File experimentLocation) {
		AxiomExtractor extractor = new AxiomExtractor();
		this.experimentLocation = experimentLocation;
		if(!experimentLocation.exists()){
			experimentLocation.mkdirs();
		}
		this.signature = sig;
		this.ontology = extractor.extractInclusionsAndEqualities(ontology);
	}

	public void compareExtractionApproaches() throws IOException, QBFSolverException, OWLOntologyStorageException, OWLOntologyCreationException{	
		File experimentResultFile = new File(experimentLocation + "/" + "experiment-results");
		if(experimentResultFile.exists()){
			logger.info("Already complete" + "\n");
			return;
		}


			manager = OWLManager.createOWLOntologyManager();
			syntaxModExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
			
			Set<OWLLogicalAxiom> syntacticModule = null;
			Set<OWLAxiom> syntacticOntology = syntaxModExtractor.extract(signature);

			syntacticModule = getLogicalAxioms(syntacticOntology);

			long startTime = System.currentTimeMillis();
			
			this.moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(),signature);
		

	


		this.dump = new DumpExtractionToDisk(
				experimentLocation,
				moduleExtractor.getModule(), signature);

		Set<OWLLogicalAxiom> semanticModule = moduleExtractor.extractModule();
		
		timeTaken = System.currentTimeMillis() - startTime;

		writeResults(semanticModule,syntacticModule);
		writeMetrics();
		writeQBFMetrics();
		
		logger.debug("{}",moduleExtractor.getMetrics());
		logger.debug("{}",moduleExtractor.getQBFMetrics());
		logger.info("Semantic module size {}",semanticModule.size());

		logger.info("Complete - Time taken {} \n",ModuleUtils.getTimeAsHMS(timeTaken));


	}

	public void writeResults(Set<OWLLogicalAxiom> semanticModule, Set<OWLLogicalAxiom> syntacticModule) throws IOException{
//		ReloadExperimentFromDisk reload = new ReloadExperimentFromDisk(ModulePaths.getResultLocation() + "//" + experimentLocation.getName());
//		Set<OWLLogicalAxiom> module = reload.getModule();
//		logger.info("Modules are the same? {}",semanticModule.equals(module));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		
		writer.write("Syntactic Size,Semantic Size, ModulesSame, SemSubsetOfStar, StarSubsetSem" + "\n");
		writer.write(syntacticModule.size() + "," + semanticModule.size() + "," 
		+ semanticModule.equals(syntacticModule) + "," + syntacticModule.containsAll(semanticModule) + "," + semanticModule.containsAll(syntacticModule) + "\n");
		if(!syntacticModule.containsAll(semanticModule)){
			System.out.println("Semantic size: " + semanticModule.size());
			System.out.println("Syntactic size: " + syntacticModule.size());
			semanticModule.removeAll(syntacticModule);
			System.out.println("Difference: " +  semanticModule.size());
			System.out.println(semanticModule);
		}
		writer.flush();
		writer.close();

		/* Dump the results before finishing */
		new Thread(dump).start();
	}

	public void writeMetrics() throws IOException{
		LinkedHashMap<String, Long> metrics = moduleExtractor.getMetrics();
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "metrics", false));
		

		Object[] keysetArray = metrics.keySet().toArray();
		/*Write header - the keyset values */
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(keysetArray[i] + ",");
		}
		writer.write(keysetArray[keysetArray.length-1] + "\n");
		
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(metrics.get(keysetArray[i]) + ",");
		}
		writer.write(metrics.get(keysetArray[keysetArray.length-1]) + "\n");
		writer.flush();
		writer.close();
	}
	
	public void writeQBFMetrics() throws IOException{
		LinkedHashMap<String, Long> metrics = moduleExtractor.getQBFMetrics();
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "qbf-metrics", false));
			
		Object[] keysetArray = metrics.keySet().toArray();
		
		/*Write header - the keyset values */
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(keysetArray[i] + ",");
		}
		writer.write(keysetArray[keysetArray.length-1] + "\n");
		
		/*Write content*/
		for (int i = 0; i < keysetArray.length-1; i++) {
			writer.write(metrics.get(keysetArray[i]) + ",");
		}
		writer.write(metrics.get(keysetArray[keysetArray.length-1]) + "\n");
		writer.flush();
		writer.close();
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom())
				result.add((OWLLogicalAxiom) ax);
		}
		return result;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl-sub");
		SigManager sigManager = new SigManager(new File(ModulePaths.getSignatureLocation() + "/nci-sub-300"));
		
		Set<OWLEntity> sig = null;
		try {
			sig = sigManager.readFile("random300-116");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			new ExtractionComparision(ont, sig, new File(ModulePaths.getResultLocation()+ "/superfuntesting")).compareExtractionApproaches();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QBFSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
