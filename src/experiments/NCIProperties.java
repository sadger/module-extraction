package experiments;

import java.io.File;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLOntology;

import terminology.EquivalentToTerminologyChecker;
import terminology.TerminologyChecker;

public class NCIProperties {

	TerminologyChecker termChecker;
	EquivalentToTerminologyChecker equivChecker;
	
	public NCIProperties() {
		termChecker = new TerminologyChecker();
		equivChecker = new EquivalentToTerminologyChecker();
	}
	public void getProperties(){
		File directory = new File("/media/2E33-E265/NCI/");
		for (final File file : directory.listFiles()) {
	        if (file.isFile()) {
	        	System.out.println(file.getName());
	            OWLOntology ontology = OntologyLoader.loadOntology(file.getAbsolutePath());
	        	System.out.println("Is terminology: " + termChecker.isTerminology(ontology));
	        	System.out.println("Equivalent to terminology: " + equivChecker.isEquivalentToTerminology(ontology));
	        	System.out.println();
	        }
	    }
	}
	
	public static void main(String[] args) {
		NCIProperties prop = new NCIProperties();
		prop.getProperties();
	}
}
