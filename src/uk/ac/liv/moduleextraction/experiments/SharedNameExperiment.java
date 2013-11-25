package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.experiments.SharedNameFilter.RemovalMethod;
import uk.ac.liv.moduleextraction.extractor.IteratingExtractor;
import uk.ac.liv.moduleextraction.extractor.NewIteratingExtractor;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SharedNameExperiment implements Experiment {

	private Object ontology;
	private SyntacticLocalityModuleExtractor starExtractor;
	private NewIteratingExtractor removeInclusionsExtractor;
	private NewIteratingExtractor removeEqualitiesExtractor;
	private NewIteratingExtractor removeRandomExtractor;
	
	private Set<OWLLogicalAxiom> starModule;
	private Set<OWLLogicalAxiom> inclusionsModule;
	private Set<OWLLogicalAxiom> equalitiesModule;
	private Set<OWLLogicalAxiom> randomModule;
	
	int starSize;

	public SharedNameExperiment(OWLOntology ont) {
		ont = NewIteratingExtractor.createOntology(ont.getLogicalAxioms());
		
		OWLOntologyManager manager = ont.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ont, ModuleType.STAR);
		this.removeInclusionsExtractor = new NewIteratingExtractor(ont, RemovalMethod.REMOVE_INCLUSIONS);
		this.removeEqualitiesExtractor = new NewIteratingExtractor(ont, RemovalMethod.REMOVE_EQUALITIES);
		this.removeRandomExtractor = new NewIteratingExtractor(ont, RemovalMethod.RANDOM);
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		Set<OWLAxiom> starAxioms = starExtractor.extract(signature);
		starModule = ModuleUtils.getLogicalAxioms(starAxioms);
		starSize = starModule.size();
		
		inclusionsModule = removeInclusionsExtractor.extractModule(signature);
		equalitiesModule = removeEqualitiesExtractor.extractModule(signature);
		randomModule = removeRandomExtractor.extractModule(signature);
		

		if(!inclusionsModule.equals(equalitiesModule)){
			System.out.println(starSize + "," + inclusionsModule.size() + "," + equalitiesModule.size() + "," + randomModule.size());
			System.out.println();
		}
		
		
	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		writer.write("StarSize, RemoveInclusions, RemoveEqualities, RandomRemoval" + "\n");
		writer.write(starSize + "," + inclusionsModule.size() + "," + equalitiesModule.size() + "," + randomModule.size() + "\n");
		writer.flush();
		writer.close();
		
	}

	@Override
	public void performExperiment(Set<OWLEntity> sig, File f) {
		// TODO Auto-generated method stub
		
	}

}
