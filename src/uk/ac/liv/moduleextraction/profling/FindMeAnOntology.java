package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import uk.ac.liv.ontologyutils.axioms.ALCValidator;
import uk.ac.liv.ontologyutils.axioms.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.main.AcyclicChecker;
import uk.ac.liv.ontologyutils.main.ModulePaths;
import uk.ac.liv.ontologyutils.terminology.AxiomStructureInspector;
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
		Collections.sort(Arrays.asList(ontologyFiles));
		for(File f: ontologyFiles){
			if(f.isFile()){
				System.out.println(f.getName());
				OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				profileOntology(f.getName(), ont);
				System.out.println();

			}
		}
	}

	private void profileOntology(String fileName,OWLOntology ont){
		System.out.println("Logical Axiom Count: " + ont.getLogicalAxiomCount());
		DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
		System.out.println("Expressivity: " + checker.getDescriptionLogicName());
		AxiomTypeProfile p = new AxiomTypeProfile(ont);
		ExpressionTypeProfiler exp = new ExpressionTypeProfiler();
		p.printMetrics();
		exp.profileOntology(ont);
//	
		System.out.println("Class in sig: " + ont.getClassesInSignature().size());
		System.out.println("Roles in sig: " + ont.getObjectPropertiesInSignature().size());
		System.out.println("Sig size: " + ont.getSignature().size());
		System.out.println("");
		
		Set<OWLObjectProperty> prop = new HashSet<OWLObjectProperty>();
		Set<OWLClass> cls = new HashSet<OWLClass>();
		int alcCount = 0;
		for (OWLLogicalAxiom ax : ont.getLogicalAxioms()) {
			prop.addAll(ax.getObjectPropertiesInSignature());
			cls.addAll(ax.getClassesInSignature());
			if(validator.isALCAxiom(ax) && !elvalidator.isELAxiom(ax)){
				alcCount++;
			}
		}
		
		System.out.println("Prop:" + prop.size());
		System.out.println("Cls " + cls.size());
		System.out.println("ALC " + alcCount);
		


//				System.out.println("Is EL?: " + elvalidator.isELOntology(ont));
//				System.out.println("Is ALC?: " + validator.isALCOntology(ont));
//				AcyclicChecker acyclic = new AcyclicChecker(ont, true);
//				acyclic.isAcyclic();
//				acyclic.printMetrics();
		
//			boolean isTerm = termChecker.isTerminology(ont);
//
//				if(!isTerm)
//					System.out.println("\t" + termChecker.getCheckStatus());
//				System.out.println("Is terminology: " + isTerm);
//				System.out.println("Logically equivalent to terminology?: " + equivTermChecker.isEquivalentToTerminology(ont));
//		
		AxiomStructureInspector struct = new AxiomStructureInspector(ont);
		System.out.println("Concept names with repeated inclusions " + struct.countNamesWithRepeatedInclusions());
		System.out.println("Concept names with repeated equalities " + struct.countNamesWithRepeatedEqualities());
		System.out.println("Prim(T) ∩ Def(T)" + struct.getNamesInIntersection().size());
		
		
	}

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
	//FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/Tones/NONEL/Big"));
	FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/NCI/Profile"));
		find.profileOntologies();
		
//		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "NCI/Profile/Thesaurus_08.09d.OWL");
//		OWLOntologyManager man = ont.getOWLOntologyManager();
//		OWLOntology ncistar = man.createOntology(ont.getAxioms());
//		
//		man.saveOntology(ncistar, new OWLXMLOntologyFormat(), IRI.create(new File(ModulePaths.getOntologyLocation() + "NCI/Profile/NCI-star.owl")));
//				
	}




}

	