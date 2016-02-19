package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.experiments.ExactlyNDepletingComparison;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class OneDepletingEvaluation {
	public static void main(String[] args) throws IOException {

        String ontologyName = "3ac2a2b1-a86e-453b-830d-6814b286da46_owl%2Fcoma-QBF";
		String axiomName = "axiom-851071629";



		File ontLoc =
				new File(ModulePaths.getOntologyLocation() + "dep-explore.owl");


		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());

        System.out.println(ont.getLogicalAxiomCount());
        ExactlyNDepletingComparison compare = new ExactlyNDepletingComparison(2,ont,ontLoc);
//		SigManager man =
//                new SigManager(
//                        new File(ModulePaths.getResultLocation() +
//                                "/depleting-comparison/domain-elements-2/" + ontologyName + "-NDepletingComparison/domain_size-2/" + axiomName));


        SigManager man = new SigManager(new File(ModulePaths.getOntologyLocation()));
		Set<OWLEntity> signature = man.readFile("explore");

	    compare.performExperiment(signature);
		compare.printMetrics();

        /*
        for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
            System.out.println(axiom);
        }
        */
   // /LOCAL/wgatens/Results/depleting-comparison/domain-elements-2/55e5e251-5c11-4b64-8860-066e0c8e2a77_bility.owl-QBF-NDepletingComparison/axiom-797412710
   //   /LOCAL/wgatens/Results/depleting-comparison/domain-elements-2/55e5e251-5c11-4b64-8860-066e0c8e2a77_bility.owl-QBF-NDepletingComparison/domain_size-2/axiom1004489501

    }
}

