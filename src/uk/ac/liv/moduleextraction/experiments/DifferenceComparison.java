package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class DifferenceComparison {

	Logger logger = LoggerFactory.getLogger(DifferenceComparison.class);

	private ExtractionComparision compare;
	private SigManager sigManager;
	private OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();

	public DifferenceComparison(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException {
		ontology = new AxiomExtractor().extractInclusionsAndEqualities(ontology);
			
		logger.info("Extracted inclusions and equalities only ({} logical axioms)", ontology.getLogicalAxiomCount());
		this.sigManager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 
		
		Set<OWLLogicalAxiom> diffAxioms = new HashSet<OWLLogicalAxiom>();
		for(File f : files){
			if(f.isFile()){
				logger.info("Testing {}",f.getName());
				Set<OWLEntity> signature  = sigManager.readFile(f.getName());
				
				/*Extract Semantic Module */
				SyntacticFirstModuleExtraction semanticMod = new SyntacticFirstModuleExtraction(ontology.getLogicalAxioms(), signature);
				Set<OWLLogicalAxiom> module = semanticMod.extractModule();
				
				/*Create ontology from module */
				OWLOntology moduleAsOnt = ontManager.createOntology(new HashSet<OWLAxiom>(module));
				
				SyntacticLocalityModuleExtractor syntactic = new SyntacticLocalityModuleExtractor
						(ontManager, moduleAsOnt, ModuleType.STAR);
				
				
				Set<OWLLogicalAxiom> syntModule = getLogicalAxioms(syntactic.extract(signature));
				
				
				ontManager.removeOntology(moduleAsOnt);
				
				boolean modsEqual = syntModule.equals(module);
				logger.info("Modules equal? {}",modsEqual);
				if(!modsEqual){
					module.removeAll(syntModule);
					logger.info("Difference size {}",module.size());
					System.out.println(module);
					diffAxioms.addAll(module);
				}
				System.out.println();
	
				
			}
		}
		logger.info("Difference axioms {}",diffAxioms.size());
	}  
	
	public Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom()){
				result.add((OWLLogicalAxiom) ax);
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
		try {
			DifferenceComparison diff = new DifferenceComparison(ont, new File(ModulePaths.getSignatureLocation() + "/sig-250random"));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}


	}
}
