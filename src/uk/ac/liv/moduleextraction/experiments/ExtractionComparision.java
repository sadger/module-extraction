package uk.ac.liv.moduleextraction.experiments;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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


import uk.ac.liv.moduleextraction.main.SyntacticFirstModuleExtraction;
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

	OWLOntologyManager manager;
	OWLOntology ontology;
	SyntacticFirstModuleExtraction moduleExtractor;
	SyntacticLocalityModuleExtractor syntaxModExtractor;
	private final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);

	private Set<OWLEntity> signature;

	private DumpExtractionToDisk dump;

	Set<OWLLogicalAxiom> semanticModule = null;
	private int syntaticSize = 0;

	private boolean resumingExperiments = false;
	private File experimentLocation;

	public ExtractionComparision(OWLOntology ontology, Set<OWLEntity> sig, File experimentLocation) {
		AxiomExtractor extractor = new AxiomExtractor();
		this.experimentLocation = experimentLocation;
		this.signature = sig;
		this.ontology = extractor.extractInclusionsAndEqualities(ontology);
		System.out.println("Extracted inclusions and equalities only");
		System.out.println("Ont:" + ontology.getLogicalAxiomCount());
	}


	public void compareExtractionApproaches() throws IOException, QBFSolverException, OWLOntologyStorageException, OWLOntologyCreationException{	
		File experimentResultFile = new File(experimentLocation + "/" + "experiment-results");
		if(experimentResultFile.exists()){
			System.out.println("Already complete");
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

			/* Store the size here as the semantic approach is destructive */
			this.moduleExtractor = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(),signature);
		


		syntaticSize = syntacticModule.size();
		System.out.println("Syntsize: " + syntaticSize);


		this.dump = new DumpExtractionToDisk(
				experimentLocation,
				moduleExtractor.getModule(), signature);

		semanticModule = moduleExtractor.extractModule();
		writeResults(semanticModule);

		System.out.println(ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));

	}

	public void writeResults(Set<OWLLogicalAxiom> semanticModule) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		
		writer.write("#Syntactic Size\t Synt->Semantic Size\n");
		writer.write(syntaticSize + "," +semanticModule.size() + "\n");
		writer.flush();
		writer.close();

		/* Dump the results one last time before finishing */
		new Thread(dump).start();
	}


	public Set<OWLLogicalAxiom> getSemanticModule() {
		return semanticModule;
		
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
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
		System.out.println("Loaded Ontology");

		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());

		
		Set<OWLEntity> sig = gen.generateRandomSignature(100);
			
		try {
			ExtractionComparision comp = new ExtractionComparision(ont,sig,new File(ModulePaths.getResultLocation() + "/random-100-nci"));
			//ExtractionComparision comp = new ExtractionComparision(new File(getResultLocation() + "random-100-nci");
			comp.compareExtractionApproaches();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
	}


}