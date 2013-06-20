package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.extractor.LovelyFunTimeExtractor;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class IteratingExperiment implements Experiment {

	private SyntacticLocalityModuleExtractor starExtractor;
	private LovelyFunTimeExtractor iteratingExtractor;
	private int starSize = 0;
	private int itSize = 0;

	public IteratingExperiment(OWLOntology ont) {
		OWLOntologyManager manager = ont.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ont, ModuleType.STAR);
		this.iteratingExtractor = new LovelyFunTimeExtractor(ont);
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		Set<OWLLogicalAxiom> starModule = ModuleUtils.getLogicalAxioms(starExtractor.extract(signature));
		starSize = starModule.size();
		Set<OWLLogicalAxiom> itModule = iteratingExtractor.extractModule(signature);
		itSize = itModule.size();

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		writer.write("StarSize, IteratedSize, Difference" + "\n");
		writer.write(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "\n");
		writer.flush();
		writer.close();
	}

}
