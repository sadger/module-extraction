package uk.ac.liv.moduleextraction.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.extractor.EquivalentToTerminologyExtractor;
import uk.ac.liv.moduleextraction.extractor.Extractor;
import uk.ac.liv.moduleextraction.extractor.AMEX;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.ontologies.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.ontologies.TerminologyChecker;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class CommandLineInterface {
	
	@SuppressWarnings("static-access")
	public static void main(String[] args){
		//Make everything render like description logics
		ToStringRenderer stringRender = ToStringRenderer.getInstance();
		DLSyntaxObjectRenderer renderer;
		renderer =  new DLSyntaxObjectRenderer();
		stringRender.setRenderer(renderer);
		
		Options options = new Options();

		Option ontologyChoice = OptionBuilder.withArgName("ontology").hasArg().
				withDescription("Specify ontology to extract module from").create("ont");

		Option sigChoice = OptionBuilder.withArgName("signature").hasArg().
				withDescription("Specify signature").create("sig");

		Option outputChoice = OptionBuilder.withArgName("output.owl").hasArg().
				withDescription("Output the resutling module to an owl file").create("o");

		options.addOption("v", "verbose", false, "Be verbose about axioms added and display module");
		options.addOption("h", "help", false, "print this message" );
		options.addOption(ontologyChoice);
		options.addOption(sigChoice);
		options.addOption(outputChoice);

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		File ontologyFile = null;
		File sigFile = null;
		Logger logger = null;
		OWLOntologyManager manager = null;
		
		try {
			cmd = parser.parse( options, args);
			
			if(cmd.hasOption("h") || cmd.hasOption("help") || args.length == 0){
				formatter.printHelp("java -jar amex.jar", options);
			}
			else{
				if(cmd.hasOption("v") || cmd.hasOption("verbose")){
					System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");  
					 logger = LoggerFactory.getLogger(CommandLineInterface.class);
				}
			
				if(cmd.hasOption("ont") && cmd.hasOption("sig")){
					String pathToOnt = cmd.getOptionValue("ont");
					String pathToSig = cmd.getOptionValue("sig");
					
					ontologyFile = new File(pathToOnt).getAbsoluteFile();
					sigFile = new File(pathToSig).getAbsoluteFile();
					
					//Parse ontology
					manager = OWLManager.createOWLOntologyManager();
					OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
					
					//Parse signature
					SigManager sigManager = new SigManager(sigFile.getParentFile());
					Set<OWLEntity> signature = sigManager.readFile(sigFile.getName());
					
					Extractor moduleExtractor = null;

					TerminologyChecker termChecker = new TerminologyChecker();
					EquivalentToTerminologyChecker equivChecker = new EquivalentToTerminologyChecker();
					
					SupportedAxiomVerifier supported = new SupportedAxiomVerifier();
					
					ArrayList<RemoveAxiom> toRemove = new ArrayList<RemoveAxiom>();
					for(OWLLogicalAxiom ax : ontology.getLogicalAxioms()){
						if(!supported.isSupportedAxiom(ax)){
							toRemove.add(new RemoveAxiom(ontology, ax));
						}
					}
					if(toRemove.size() > 0){
						System.out.println("Removing " + toRemove.size() + " unsupported axioms");
						manager.applyChanges(toRemove);
					}
	
					
					if(termChecker.isTerminology(ontology)){
						moduleExtractor = new AMEX(ontology);
					}
					else if(equivChecker.isEquivalentToTerminology(ontology)){
						moduleExtractor = new EquivalentToTerminologyExtractor(ontology);
					}
					else{
						System.out.println("Ontology not supported - must be an acyclic terminology with optional repeated inclusions");
						System.exit(-1);
					}
					
					long startTime = System.currentTimeMillis();
					Set<OWLLogicalAxiom> module = moduleExtractor.extractModule(signature);
					long endTime = System.currentTimeMillis() - startTime;
					

					if(logger != null && logger.isDebugEnabled()){
						System.out.println(" -- Module -- ");
						for(OWLLogicalAxiom ax : module){
							System.out.println(ax);
						}
					}
					
					System.out.println("Extracted module: " + module.size() + " axiom(s) in " + ((double)endTime/1000) + " seconds");
					
					if(cmd.hasOption("o")){
						File outputFile = new File(cmd.getOptionValue("o")).getAbsoluteFile();
						OWLXMLOntologyFormat xmlFormat = new OWLXMLOntologyFormat();
						
						Set<OWLAxiom> moduleAxioms = new HashSet<OWLAxiom>();
						for(OWLLogicalAxiom ax :  module){
							moduleAxioms.add(ax);
						}
						
						OWLOntology moduleAsOnt = manager.createOntology(moduleAxioms);
						manager.saveOntology(moduleAsOnt,xmlFormat,IRI.create(outputFile));
						
						System.out.println("Output module to: " + outputFile.getAbsolutePath());
					}
				}
				else{
					System.out.println("You must specify both an ontology and signature file");
					formatter.printHelp("java -jar amex.jar", options);
				}

			}	
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			System.out.println("See --help for options");
		} catch (OWLOntologyCreationException e) {
			System.out.println("Cannot parse supplied ontology: " + ontologyFile.getAbsolutePath() + " please use a format compatable with the OWL-API " +
					"(http://owlapi.sourceforge.net/)");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Unable to parse supplied signature file " + sigFile.getAbsolutePath() + " please use the format specified by AMEX.");
		} catch (OWLOntologyStorageException e) {
			System.out.println("Unable to save file - check target folder permissions");
		}
	


	}
}
