package uk.ac.liv.moduleextraction.profling;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.experiments.TwoDepletingExperiment;
import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wgatens on 26/05/15.
 */
public class LucaQBF {



    public static void main(String[] args) throws IOException {
        /*LUCA - potentially difficult qbf
        382c102a-268a-4083-b6ef-618578fe3748_mvco.owl-QBF

        045064bb-f429-496f-9fa6-e998003a5400_bulary.owl-QBF
        1f149e90-466b-48b6-aa68-7b3bafacf532_gn.v12.owl-QBF

        */

        /*
            ZERO MODULE!!
            32f9b463-b76d-4e89-aa1e-32a036ea29ed_ics_v4.owl-QBF
         */


        String[] difficultOnts = {
               //"162af88a-a305-4d90-902c-243748280544_rogram.owl-QBF",
              "2003a12d-586c-482c-ab03-556a6d9003fd_ly.rdf.owl-QBF",
                "1950f729-0968-4af6-8e58-90b990c1e90d_r3.rdf-QBF",
                "293e6a33-a1d4-4474-bb51-9e43dac93448_DUL_v25.owl-QBF"
        };


        for(File f : new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/").listFiles()){
            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
            System.out.println(f.getName() + ": " + ont.getLogicalAxiomCount());
//            for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
//                System.out.println(ax);
//            }

            Set<OWLLogicalAxiom> randomSample = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(), 5);




            Stopwatch samplewatch = Stopwatch.createStarted();
            for(OWLLogicalAxiom axiom : randomSample){


                Set<OWLEntity> sig = axiom.getSignature();
                TwoDepletingExperiment expr = new TwoDepletingExperiment(ont,f);
                expr.performExperiment(sig);
                expr.writeMetrics(new File("/tmp"));


            }
            samplewatch.stop();
            System.out.println(samplewatch);

            ont.getOWLOntologyManager().removeOntology(ont);
            ont = null;

        }

//        OWLOntology onty = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/twodep.krss");
//        ModuleUtils.remapIRIs(onty,"X");
//        for(OWLLogicalAxiom ax : onty.getLogicalAxioms()){
//            System.out.println(ax);
//        }
//
//        OWLDataFactory f = onty.getOWLOntologyManager().getOWLDataFactory();
//        OWLEntity a = f.getOWLClass(IRI.create("X#A"));
//
//        HashSet<OWLEntity> sig = Sets.newHashSet(a);
//
//
//        TwoDepletingExperiment expr = new TwoDepletingExperiment(onty,null);
//        expr.performExperiment(sig);

    }

}
