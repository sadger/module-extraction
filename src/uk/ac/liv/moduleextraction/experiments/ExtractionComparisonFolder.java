package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.AcyclicChecker;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ExtractionComparisonFolder {

	Logger logger = LoggerFactory.getLogger(ExtractionComparisonFolder.class);

	private ExtractionComparision compare;
	private SigManager manager;

	public ExtractionComparisonFolder(OWLOntology ontology, File signaturesLocation) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, QBFSolverException, NotEquivalentToTerminologyException {
		ontology = new AxiomExtractor().extractInclusionsAndEqualities(ontology);
		logger.info("Extracted inclusions and equalities only ({} logical axioms)", ontology.getLogicalAxiomCount());
		this.manager = new SigManager(signaturesLocation);
		File[] files = signaturesLocation.listFiles();
		Arrays.sort(files); 
		for(File f : files){
			if(f.isFile()){
				logger.info("Testing signature: {}",f.getName());
				File experimentLocation = new File(ModulePaths.getResultLocation() + "/" + signaturesLocation.getName() + "/" + f.getName());
				compare = new ExtractionComparision(ontology, manager.readFile(f.getName()), experimentLocation);
				compare.compareExtractionApproaches();
			}
		}
	}  
	
	public static void main(String[] args) {
		OWLOntology uberon = OntologyLoader.loadOntologyInclusionsAndEqualities("/LOCAL/wgatens/Ontologies/Bioportal/NOTEL/Big/toCheck/UBERON");
		System.out.println(uberon.getLogicalAxiomCount());
		AcyclicChecker checker = new AcyclicChecker(uberon, true);
		checker.isAcyclic();
		checker.printMetrics();
		
		
//		//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
//		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
//		//OWLOntology natpro = OntologyLoader.loadOntology("/LOCAL/wgatens/Ontologies/Bioportal/NOTEL/Terminologies/Acyclic/Big/NatPrO-converted");
//		//OWLOntology lipro= OntologyLoader.loadOntology("/LOCAL/wgatens/Ontologies/Bioportal/NOTEL/Terminologies/Acyclic/Big/LiPrO-converted");
//
//		System.out.println(ont.getLogicalAxiomCount());
//		System.out.println(ont.getClassesInSignature().size());
//		System.out.println(ont.getObjectPropertiesInSignature().size());
//	
		
		
//
//		try {
//			
//			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/sig-100equivterm"));
//			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/sig-250equivterm"));
//			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/sig-500equivterm"));
//			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/sig-750equivterm"));
//			new ExtractionComparisonFolder(ont, new File(ModulePaths.getSignatureLocation() + "/sig-1000equivterm"));
//			
//			
//
//
//		} catch (OWLOntologyStorageException e) {
//			e.printStackTrace();
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (QBFSolverException e) {
//			e.printStackTrace();
//		} catch (NotEquivalentToTerminologyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}