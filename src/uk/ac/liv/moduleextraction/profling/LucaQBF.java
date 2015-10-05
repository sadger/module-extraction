package uk.ac.liv.moduleextraction.profling;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.liv.moduleextraction.experiments.NDepletingExperiment;
import uk.ac.liv.moduleextraction.experiments.TwoDepletingExperiment;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Created by wgatens on 26/05/15.
 */
public class LucaQBF {



    public static void main(String[] args) throws IOException {

        String name = "368d0b5a-aead-4245-a8f2-8eab74b86326_DUUL.owl-QBF";
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/" + name);

        System.out.println(ont.getLogicalAxiomCount());

        SigManager man =  new SigManager(new File(ModulePaths.getSignatureLocation() + "/depleting-comparison-only-diff/" + name));

        int i = 0;
        for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
            System.out.println(++i);
            Set<OWLEntity> sig = ax.getSignature();
            NDepletingExperiment n = new NDepletingExperiment(3,ont,new File("/tmp/"));
            n.performExperiment(sig);
            n.writeMetrics(new File("/tmp/"));

        }


    }

}
