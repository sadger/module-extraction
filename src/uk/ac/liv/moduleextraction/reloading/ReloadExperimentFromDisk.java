package uk.ac.liv.moduleextraction.reloading;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ReloadExperimentFromDisk {

	//TODO make these global to dumping/loading
	private static final String SIGNATURE_FILE = "sig";
	private static final String MOD_FILE = "module.owl";
	private static final String SYNT_FILE = "syntacticModule.owl";

	private final String EXPERIMENT_LOCATION;
	
	private Set<OWLEntity> signature;
	private Set<OWLLogicalAxiom> module;

	public ReloadExperimentFromDisk(String location) throws IOException {
		this.EXPERIMENT_LOCATION = new File(location).getAbsolutePath();
		this.signature = populateSignature();
		this.module = restoreOntology(MOD_FILE);
		System.out.println("Reloaded: " + location);
		System.out.format("M:%d, S:%d\n", module.size(), signature.size());
	}

	private Set<OWLLogicalAxiom> restoreOntology(String ontLocation){
		OWLOntology ontology = OntologyLoader.loadOntologyInclusionsAndEqualities(EXPERIMENT_LOCATION + "/" + ontLocation);
		if(ontology == null)
			System.err.println("Ontology " + ontLocation + " not found");
		
		return ontology.getLogicalAxioms();
	}
	
	private Set<OWLEntity> populateSignature() throws IOException{
		SigManager manager = new SigManager(new File(EXPERIMENT_LOCATION));
		return manager.readFile(SIGNATURE_FILE);
	}
	
	public Set<OWLLogicalAxiom> getSyntacticModule(){
		return restoreOntology(SYNT_FILE);
	}

	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}
	
	public Set<OWLEntity> getSignature() {
		return signature;
	}

}