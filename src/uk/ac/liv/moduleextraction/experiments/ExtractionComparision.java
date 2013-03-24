package uk.ac.liv.moduleextraction.experiments;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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

	private int syntaticSize = 0;
	private long timeTaken = 0;
	
	private File experimentLocation;
	
	
	
	

	public ExtractionComparision(OWLOntology ontology, Set<OWLEntity> sig, File experimentLocation) {
		AxiomExtractor extractor = new AxiomExtractor();
		this.experimentLocation = experimentLocation;
		this.signature = sig;
		this.ontology = extractor.extractInclusionsAndEqualities(ontology);

	}


	public void compareExtractionApproaches() throws IOException, QBFSolverException, OWLOntologyStorageException, OWLOntologyCreationException{	
		File experimentResultFile = new File(experimentLocation + "/" + "experiment-results");
		if(experimentResultFile.exists()){
			logger.info("Already complete" + "\n");
			return;
		}


		
		long startTime = System.currentTimeMillis();

		Set<OWLLogicalAxiom> syntacticModule = null;
			manager = OWLManager.createOWLOntologyManager();
			syntaxModExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
			

			Set<OWLAxiom> syntacticOntology = syntaxModExtractor.extract(signature);

			OWLXMLOntologyFormat owlFormat = new OWLXMLOntologyFormat();
			manager.saveOntology(manager.createOntology(syntacticOntology),owlFormat, 
					IRI.create(new File(experimentLocation + "/" + "syntacticModule.owl")));

			syntacticModule = getLogicalAxioms(syntacticOntology);

			
			this.moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(),signature);
		

		syntaticSize = syntacticModule.size();
		System.out.println("Syntsize: " + syntaticSize);


		this.dump = new DumpExtractionToDisk(
				experimentLocation,
				moduleExtractor.getModule(), signature);

		Set<OWLLogicalAxiom> semanticModule = moduleExtractor.extractModule();
		
		timeTaken = System.currentTimeMillis() - startTime;
		writeResults(semanticModule);

		logger.info("Complete - Time taken {} \n",ModuleUtils.getTimeAsHMS(timeTaken));

	}

	public void writeResults(Set<OWLLogicalAxiom> semanticModule) throws IOException{
		ReloadExperimentFromDisk reload = new ReloadExperimentFromDisk(ModulePaths.getResultLocation() + "/ruletest-old/" + experimentLocation.getName());
		Set<OWLLogicalAxiom> module = reload.getModule();
		logger.info("Modules are the same? {}",semanticModule.equals(module));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		
		writer.write("#Syntactic Size\t Semantic Size\t Time taken (ms)\n");
		writer.write(syntaticSize + "," + semanticModule.size() + "," + timeTaken + "\n");
		writer.flush();
		writer.close();

		/* Dump the results one last time before finishing */
		new Thread(dump).start();
	}


	public Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom())
				result.add((OWLLogicalAxiom) ax);
		}
		return result;
	}


}
