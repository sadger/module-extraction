package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.experiments.OneDepletingComparison;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class OneDepletingEvaluation {
	public static void main(String[] args) throws IOException {

		String ontologyName = "2dd72f17-3daa-493e-a596-04fe70110fff_tology.owl-QBF";
		String axiomName = "axiom-1912091674";

		File ontLoc =
				new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/" + ontologyName);
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());

		OneDepletingComparison comp = new OneDepletingComparison(ont, ontLoc);
		SigManager man = new SigManager(new File(ModulePaths.getResultLocation() + "/onedepcompare/" + ontologyName + "-OneDepletingComparison/" + axiomName));
		Set<OWLEntity> signature = man.readFile("signature");

		comp.performExperiment(signature);
		comp.printMetrics();

        /*
        for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
            System.out.println(axiom);
        }
        */

	}
}

