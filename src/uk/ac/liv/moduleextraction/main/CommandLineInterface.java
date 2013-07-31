package uk.ac.liv.moduleextraction.main;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.extractor.EquivalentToTerminologyExtractor;
import uk.ac.liv.moduleextraction.extractor.Extractor;
import uk.ac.liv.moduleextraction.extractor.SemanticRuleExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.main.ModuleUtils;
import uk.ac.liv.ontologyutils.terminology.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.terminology.TerminologyChecker;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class CommandLineInterface {
//	public static Logger logger = LoggerFactory.getLogger(CommandLineInterface.class);
	public static boolean debugMode = false;
	public static void main(String[] args) {
		
		// 0: Flags, 1: Ontology Loc, 2:Signature loc
		
		OWLOntology ontology = null;
		File sigLocation = null;
		
		
	    if(args.length == 2){
			ontology = OntologyLoader.loadOntologyAllAxioms(args[0]);
			sigLocation = new File(args[1]);
		}
		else if(args.length == 3){
			if(args[0].equals("-v")){
				debugMode = true;
				System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");	
			}

			ontology = OntologyLoader.loadOntologyAllAxioms(args[1]);
			sigLocation = new File(args[2]);
		}
		else{
			System.out.println("Usage: java -jar amex.jar {flags} <ontology location> <signature location>");
			System.exit(-1);
		}

		
		String sigName = sigLocation.getName();
		sigLocation = sigLocation.getParentFile();
		SigManager sigManager = new SigManager(sigLocation);
		
		System.out.println("Ontology (logical axioms): " + ontology.getLogicalAxiomCount());
		if(debugMode){
			System.out.println(ontology);
		}

		Set<OWLEntity> signature = null;
		try {
			signature = sigManager.readFile(sigName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		System.out.println("Signature: " + signature);
		System.out.println();
		
		Extractor moduleExtractor = null;
		
		TerminologyChecker termChecker = new TerminologyChecker();
		EquivalentToTerminologyChecker equivChecker = new EquivalentToTerminologyChecker();
		
		if(termChecker.isTerminology(ontology)){
			moduleExtractor = new SemanticRuleExtractor(ontology);
		}
		else if(equivChecker.isEquivalentToTerminology(ontology)){
			moduleExtractor = new EquivalentToTerminologyExtractor(ontology);
		}
		else{
			System.err.println("Ontology not supported - must be an acyclic terminology with optional repeated inclusions");
			System.exit(-1);
		}
		
		SyntacticLocalityModuleExtractor starExtractor = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.STAR);
		
		long startTime = System.currentTimeMillis();
		Set<OWLLogicalAxiom> module = moduleExtractor.extractModule(signature);
		long endTime = System.currentTimeMillis() - startTime;
		
		long SstartTime = System.currentTimeMillis();
		Set<OWLLogicalAxiom> starMod = ModuleUtils.getLogicalAxioms(starExtractor.extract(signature));
		long SendTime = System.currentTimeMillis() - SstartTime;

		
		
	

	
		if(debugMode){
			System.out.println();
			System.out.println("== AMEX Module ==");
			for(OWLLogicalAxiom ax : module){
				System.out.println(ax);
			}


			System.out.println();
			
			System.out.println("== STAR Module ==");
			for(OWLLogicalAxiom ax : starMod){
				System.out.println(ax);
			}

		}
			
	
		System.out.println();
		
		System.out.println("AMEX Size (logical axioms): " + module.size());
		System.out.println("Time taken: " + (double) endTime / 1000 + " seconds");
	
		System.out.println("STAR Size (logical axioms): " + starMod.size());
		System.out.println("Time taken: " + (double) SendTime / 1000 + " seconds");
		
		

	}
}
