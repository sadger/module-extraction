package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.liv.moduleextraction.extractor.SemanticOnlyExtractor;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticOnlyComparison implements Experiment {


	private File originalLocation;
	private OWLOntology ontology;
	private NewIteratingExperiment starIterExperiment;
	private File sigLocation;
	private SemanticOnlyExtractor semanticExtractor;
	private Set<OWLLogicalAxiom> semanticModule;

	public SemanticOnlyComparison(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {

		starIterExperiment = 
				new NewIteratingExperiment(ontology,originalLocation);


		starIterExperiment.performExperiment(signature);
		Set<OWLLogicalAxiom> hybridModule = starIterExperiment.getHybridModule();

		semanticExtractor = new SemanticOnlyExtractor(hybridModule);
		semanticModule = semanticExtractor.extractModule(signature);



	}

	@Override
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
		this.sigLocation = signatureLocation;
		performExperiment(signature);

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		int qbfSmaller = (semanticModule.size() < starIterExperiment.getHybridModule().size()) ? 1 : 0;

		writer.write("StarSize, HybridSize, QBFSize, QBFSmaller, QBFChecks, SignatureLocation" + "\n");
		writer.write(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size() + "," + String.valueOf(qbfSmaller) + "," + semanticExtractor.getQBFCount() + 
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}



	public static void main(String[] args) throws IOException, OWLOntologyCreationException {
		//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/test.krss");
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/GRO_CPGA-core");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont, null);

		//	System.out.println(ont);
		for (int i = 1; i <= 100; i++) {
			System.out.println(i);
			Set<OWLEntity> sig = gen.generateRandomSignature(75);
			//System.out.println(sig);
			compare.performExperiment(sig);
			compare.writeMetrics(null);

		}


	}

}
