package reloading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;
import main.ModuleExtractor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import qbf.QBFSolverException;
import util.ModulePaths;

public class ReloadExperimentFromDisk {

	//TODO make these global to dumping/loading
	private static final String SIGNATURE_FILE = "/sig";
	private static final String TERM_FILE = "/terminology.owl";
	private static final String MOD_FILE = "/module.owl";
	private static final String SYNT_FILE = "/syntacticModule.owl";

	private final String EXPERIMENT_LOCATION;
	
	private Set<OWLClass> signature;
	private Set<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;

	public ReloadExperimentFromDisk(String location) throws IOException {
		this.EXPERIMENT_LOCATION = new File(location).getAbsolutePath();
		this.signature = populateSignature();
		this.terminology = restoreOntology(TERM_FILE);
		this.module = restoreOntology(MOD_FILE);
		System.out.println("Reloaded: " + location);
		System.out.format("T:%d, M:%d, S:%d\n", terminology.size(), module.size(), signature.size());
	}

	private Set<OWLLogicalAxiom> restoreOntology(String ontLocation){
		OWLOntology ontology = OntologyLoader.loadOntology(EXPERIMENT_LOCATION + ontLocation);
		if(ontology == null)
			System.err.println("Ontology " + ontLocation + " not found");
		
		return ontology.getLogicalAxioms();
	}
	
	private Set<OWLClass> populateSignature() throws IOException{
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		File signatureFile = new File(EXPERIMENT_LOCATION + SIGNATURE_FILE);
		Set<OWLClass> signature = new HashSet<OWLClass>();
		
		if(signatureFile.exists()){
			BufferedReader br = new BufferedReader(new FileReader(signatureFile));
			String line;
			while((line = br.readLine()) != null) {
				String classIRI = line.trim();
				signature.add(factory.getOWLClass(IRI.create(classIRI)));
			}
		}
		else
			System.err.println("No signature file found");
	
		return signature;
	}
	
	public Set<OWLLogicalAxiom> getSyntacticModule(){
		return restoreOntology(SYNT_FILE);
	}

	public Set<OWLLogicalAxiom> getTerminology() {
		return terminology;
	}
	
	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}
	
	public Set<OWLClass> getSignature() {
		return signature;
	}

	public static void main(String[] args) {
		try {
			ReloadExperimentFromDisk reload = new ReloadExperimentFromDisk(ModulePaths.getOntologyLocation() + "/Results/pathway-random-100/");
			System.out.println("Terminology Size: " + reload.getTerminology().size());
			System.out.println("Module Size: " + reload.getModule().size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
