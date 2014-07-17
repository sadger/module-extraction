package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.expressions.ALCValidator;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.ontologies.TerminologyChecker;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class FindMeAnOntology {

	ALCValidator validator = new ALCValidator();
	ELValidator elvalidator = new ELValidator();
	TerminologyChecker termChecker = new TerminologyChecker();
	EquivalentToTerminologyChecker equivTermChecker = new EquivalentToTerminologyChecker();
	private File ontologyDirectory;
	int termCount = 0;

	public FindMeAnOntology(File ontologyDirectory) {
		this.ontologyDirectory = ontologyDirectory;
	}

	public void profileOntologies(){

		File[] ontologyFiles = ontologyDirectory.listFiles();
		Collections.sort(Arrays.asList(ontologyFiles));
		System.out.println("Name,Expressiveness,LogicalAxioms,Inclusions,Equivalences, "
				+ "Repeated Inclusions, Repeated Equivalances, SharedNames, Concepts, "
				+ "Roles, CoreEL, CoreCyclic");
		for(File f: ontologyFiles){
			if(f.isFile()){
				OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				profileOntology(f.getName(), ont);
//				System.out.println();


			}
		}
	}

	private void profileOntology(String fileName,OWLOntology ont){
//		System.out.println("Logical Axiom Count: " + ont.getLogicalAxiomCount());
		DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
		String express = checker.getDescriptionLogicName();
		
        String shortName = fileName.substring(Math.max(0, fileName.length() - 20));
        Set<OWLLogicalAxiom> core = ModuleUtils.getCoreAxioms(ont);
//        System.out.println("Core");
//        System.out.println("EL?:" + elvalidator.isELOntology(core));
        OntologyCycleVerifier verifier = new OntologyCycleVerifier(core);

		
		AxiomStructureInspector inspector = new AxiomStructureInspector(ont);
		
		System.out.println(shortName + "," + express + ","+ ont.getLogicalAxiomCount() + "," + ont.getAxiomCount(AxiomType.SUBCLASS_OF) + "," + 
		ont.getAxiomCount(AxiomType.EQUIVALENT_CLASSES) + "," + inspector.countNamesWithRepeatedInclusions() +
		"," + inspector.countNamesWithRepeatedEqualities() + "," + inspector.getSharedNames().size() 
		+ "," + ont.getClassesInSignature().size() + "," + ont.getObjectPropertiesInSignature().size() 
		+ "," + elvalidator.isELOntology(core) + "," + verifier.isCyclic());

		

	}

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {

	FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/Bioportal"));
	find.profileOntologies();
			
	}




}

