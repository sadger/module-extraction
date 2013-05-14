package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import uk.ac.liv.moduleextraction.util.AcyclicChecker;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.ALCValidator;
import uk.ac.liv.ontologyutils.axioms.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.terminology.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.terminology.TerminologyChecker;
import uk.ac.liv.ontologyutils.terminology.ToTerminologyConvertor;

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
		Arrays.sort(ontologyFiles);
		for(File f: ontologyFiles){
			if(f.isFile()){
				System.out.println(f.getName());
				OWLOntology ont = OntologyLoader.loadOntology(f.getAbsolutePath());

				System.out.println(ont.getAxiomCount());
				
				
	
				

				//System.out.println(ont);
				
				profileOntology(f.getAbsolutePath(),ont);
//				
			
//				
				
				
//				if(!termChecker.isTerminology(ont) && equivTermChecker.isEquivalentToTerminology(ont)){
//					ToTerminologyConvertor convertor = new ToTerminologyConvertor(ont);
//					try {
//						convertor.writeConvertedOntology(ModulePaths.getOntologyLocation() + "Acyclic/Broken/Testing/" + f.getName() + "-converted");
//						f.delete();
//					} catch (OWLOntologyStorageException e) {
//						e.printStackTrace();
//					}
//				}
//				
				
				
				System.out.println();


			}
		}
	}

	private void profileOntology(String fileName,OWLOntology ont){
		System.out.println("Logical Axiom Count: " + ont.getLogicalAxiomCount());
//		AxiomTypeProfile p = new AxiomTypeProfile(ont);
//		ExpressionTypeProfiler exp = new ExpressionTypeProfiler();
//		p.printMetrics();
//		exp.profileOntology(ont);
//		
//		System.out.println("Class in sig: " + ont.getClassesInSignature().size());
//		System.out.println("Roles in sig: " + ont.getObjectPropertiesInSignature().size());
//		System.out.println("Sig size: " + ont.getSignature().size());
//		System.out.println("");
//
//				System.out.println("Is EL?: " + elvalidator.isELOntology(ont));
//				System.out.println("Is ALC?: " + validator.isALCOntology(ont));
				AcyclicChecker acyclic = new AcyclicChecker(ont);
				System.out.println("Is acyclic: " + acyclic.isAcyclic());
//		DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
//		System.out.println("Expressivity: " + checker.getDescriptionLogicName());
//			boolean isTerm = termChecker.isTerminology(ont);
//////
//				if(!isTerm)
//					System.out.println("\t" + termChecker.getCheckStatus());
//				System.out.println("Is terminology: " + isTerm);
				System.out.println("Logically equivalent to terminology?: " + equivTermChecker.isEquivalentToTerminology(ont));
		
	}

	public static void main(String[] args) {
		FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/All"));
	//FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "Acyclic/Broken"));
		find.profileOntologies();
	}





}
