package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.util.AcyclicChecker;
import uk.ac.liv.ontologyutils.terminology.EquivalentToTerminologyChecker;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class OntologySuitability implements Experiment {

	
	private boolean isAcyclic = false;
	private boolean isEquivalentToTerminology = false;
	
	@Override
	public void performExperiment(OWLOntology ontology, Set<OWLEntity> signature) {
		OWLOntology moduleAsOnt = extractStarModuleAsOntology(ontology,signature);
		AcyclicChecker acylicChecker = new AcyclicChecker(moduleAsOnt, false);
		EquivalentToTerminologyChecker equivChecker = new EquivalentToTerminologyChecker();
		
		isAcyclic = acylicChecker.isAcyclic();
		isEquivalentToTerminology = equivChecker.isEquivalentToTerminology(moduleAsOnt);
	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		
		boolean suitable = isAcyclic && isEquivalentToTerminology;
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "suitablility-results", false));
		
		writer.write("Suitable,Acyclic,EquivToTerm" + "\n");
		writer.write(suitable + "," + isAcyclic + "," + isEquivalentToTerminology + "\n");
		writer.flush();
		writer.close();
	}
	
	private OWLOntology extractStarModuleAsOntology(OWLOntology ontology, Set<OWLEntity> signature){
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		
		SyntacticLocalityModuleExtractor localityExtractor = 
				new SyntacticLocalityModuleExtractor(ontManager, ontology, ModuleType.STAR);

		Set<OWLAxiom> module = localityExtractor.extract(signature);
		
		OWLOntology moduleAsOntology = null;
		try {
			moduleAsOntology = ontManager.createOntology(module);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		return moduleAsOntology;
		
	}

}
