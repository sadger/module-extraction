package experiments;


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

import javax.print.attribute.standard.SheetCollate;

import loader.OntologyLoader;
import main.ModuleExtractor;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import qbf.QBFSolverException;
import reloading.DumpExtractionToDisk;
import reloading.ReloadExperimentFromDisk;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import util.ModulePaths;
import util.ModuleUtils;

public class ExtractionComparision {

	OWLOntologyManager manager;
	OWLOntology ontology;
	ModuleExtractor moduleExtractor;
	SyntacticLocalityModuleExtractor syntaxModExtractor;
	private final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);

	private static final int SIGNATURE_SIZE = 100;
	private String experimentName = "temp";

	private Set<OWLClass> signature;

	private DumpExtractionToDisk dump;
	private ScheduledFuture<?> dumpHandle;
	private ReloadExperimentFromDisk reloader;

	private int syntaticSize = 0;

	private boolean resumingExperiments = false;

	public ExtractionComparision(OWLOntology ontology, Set<OWLClass> sig, String name) {
		this.experimentName = name;
		this.signature = sig;
		this.ontology = ontology;
	}

	public ExtractionComparision(String experimentLocation) throws IOException{
		this.experimentName = experimentLocation.replaceFirst(".*/([^/?]+).*", "$1");
		this.resumingExperiments = true;
		reloader = new ReloadExperimentFromDisk(experimentLocation);
		this.signature = reloader.getSignature();
	}


	public void compareExtractionApproaches() throws IOException, QBFSolverException, OWLOntologyStorageException, OWLOntologyCreationException{	
		/* The tests use the same signature (just OWLClass) 
		 * but one must be converted to OWLEntities as expected 
		 * by the OWLAPI*/

		long startTime = System.currentTimeMillis();
		
		Set<OWLClass> classSignature = signature;


		Set<OWLLogicalAxiom> syntacticModule = null;
		if(!resumingExperiments){
			manager = OWLManager.createOWLOntologyManager();
			syntaxModExtractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.BOT);
			Set<OWLEntity> entitySignature = 
					new HashSet<OWLEntity>(classSignature);

			Set<OWLAxiom> syntacticOntology = syntaxModExtractor.extract(entitySignature);

			OWLXMLOntologyFormat owlFormat = new OWLXMLOntologyFormat();
			manager.saveOntology(manager.createOntology(syntacticOntology),owlFormat, 
					IRI.create(new File(ModulePaths.getOntologyLocation() + "/Results/" + experimentName  + "/" + "syntacticModule.owl")));

			syntacticModule = getLogicalAxioms(syntacticOntology);

			/* Store the size here as the semantic approach is destructive */
			this.moduleExtractor = new ModuleExtractor(syntacticModule,signature);
		}
		else{
			this.moduleExtractor = new ModuleExtractor(reloader.getTerminology(), reloader.getModule(),signature);
			syntacticModule = reloader.getSyntacticModule();
		}

		syntaticSize = syntacticModule.size();


		this.dump = new DumpExtractionToDisk(
				experimentName,moduleExtractor.getTerminology(), 
				moduleExtractor.getModule(), signature);

		this.dumpHandle = scheduler.scheduleAtFixedRate(dump,
				30, 30, TimeUnit.MINUTES);

		Set<OWLLogicalAxiom> semanticModule = moduleExtractor.extractModule();
		writeResults(semanticModule);
		
		System.out.println(ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));

	}

	public void writeResults(Set<OWLLogicalAxiom> semanticModule) throws IOException{
	
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(ModulePaths.getOntologyLocation() + "/Results/" + experimentName  + "/" + "experiment-results", false));
		writer.write("Signature Size: " + SIGNATURE_SIZE);
		writer.write("Syntatic Size: " + syntaticSize);
		writer.write("Synt->Semantic Size: " + semanticModule.size());
		
		writer.flush();
		writer.close();
		

		/* Dump the results one last time before finishing */
		new Thread(dump).run();
		
		/* Finish the scheduling of ontology dumps */
		dumpHandle.cancel(true);
		scheduler.shutdownNow();
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
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"NCI/expr/nci-08.09d-terminology.owl");
		//OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"NCI/pathway.obo");
		ExtractionComparision compare = null;
		
		try {
			/* Restart experiment */
			compare = new ExtractionComparision(ModulePaths.getOntologyLocation() + "/Results/nci-08.09d-random-100/");
			/* Start new experiment */
			//compare =  new ExtractionComparision(ontology, ModuleUtils.generateRandomClassSignature(ontology, SIGNATURE_SIZE), "nci-0s11238.09d-random-100");
			compare.compareExtractionApproaches();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


}
