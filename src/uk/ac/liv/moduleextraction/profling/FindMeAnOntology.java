package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.util.Arrays;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.ALCValidator;
import uk.ac.liv.ontologyutils.axioms.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.terminology.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.terminology.TerminologyChecker;

public class FindMeAnOntology {
	
	ALCValidator validator = new ALCValidator();
	ELValidator elvalidator = new ELValidator();
	TerminologyChecker termChecker = new TerminologyChecker();
	EquivalentToTerminologyChecker equivTermChecker = new EquivalentToTerminologyChecker();
	private File ontologyDirectory;

	
	public FindMeAnOntology(File ontologyDirectory) {
		this.ontologyDirectory = ontologyDirectory;
	}
	
	public void profileOntologies(){
		File[] ontologyFiles = ontologyDirectory.listFiles();
		Arrays.sort(ontologyFiles);
		for(File f: ontologyFiles){
			System.out.println(f.getName());
			OWLOntology ont = OntologyLoader.loadOntology(f.getAbsolutePath());
			profileOntology(ont);
		}
	}
	
	private void profileOntology(OWLOntology ont){
		System.out.println("Logical Axiom Count: " + ont.getLogicalAxiomCount());
		System.out.println("Is EL?: " + elvalidator.isELOntology(ont));
		System.out.println("Is ALC?: " + validator.isALCOntology(ont));
		boolean isTerm = termChecker.isTerminology(ont);
		System.out.println("Is terminology?: " + isTerm);
		if(!isTerm)
			System.out.println("\t" + termChecker.getCheckStatus());
		System.out.println("Logically equivalent to terminology?: " + equivTermChecker.isEquivalentToTerminology(ont));
	}
	
	public static void main(String[] args) {
		FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/All"));
		find.profileOntologies();
	}
	
	
	
}
