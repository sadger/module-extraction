package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor;
import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor.CycleRemovalMethod;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;

public class HybridModuleInspector implements Experiment {

	OWLOntology ontology;
	int unsupportedCount;
	private Set<OWLLogicalAxiom> module;

	HybridModuleInspector(OWLOntology ontology){
		this.ontology = ontology;
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		unsupportedCount = 0;
		SupportedAxiomVerifier validator = new SupportedAxiomVerifier();
		HybridModuleExtractor extractor = new HybridModuleExtractor(ontology);
		module = extractor.extractModule(signature);
		
		for(OWLLogicalAxiom axiom : module){
			if(validator.isSupportedAxiom(axiom)){
				unsupportedCount++;
			}
		}
	}
	
	@Override
	public void performExperiment(Set<OWLEntity> sig, File f) {
		performExperiment(sig);
	}


	@Override
	public void writeMetrics(File experimentLocation) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));
		writer.write("IteratedSize, UnsupportedCount" + "\n");
		writer.write(module.size() + "," + unsupportedCount + "\n");
		writer.flush();
		writer.close();
	}



	
}
