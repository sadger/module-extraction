package timers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import main.ModuleExtractor;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import qbf.QBFSolverException;

public class RestartExperimentFromDisk {
	
	//TODO make these global to dumping/loading
	private static final String SIGNATURE_FILE = "/sig";
	private static final String TERM_FILE = "/terminology.owl";
	private static final String MOD_FILE = "/module.owl";

	public RestartExperimentFromDisk() {
		
	}
	
	public void restartExperiment(File experimentLocation) throws IOException, QBFSolverException{
		Set<OWLClass> signature = new HashSet<OWLClass>();
		Set<OWLLogicalAxiom> terminology = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> module = new HashSet<OWLLogicalAxiom>();
		
		new ModuleExtractor().extractModule(terminology, module, signature);
		
		File signatureFile = new File(experimentLocation.getAbsolutePath() + SIGNATURE_FILE);

	}
	
	public static void main(String[] args) {
		try {
			new RestartExperimentFromDisk().restartExperiment(new File("/home/william/PhD/Ontologies/Results/test_200_19-11-12_00:33/"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
	}
}
