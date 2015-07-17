package uk.ac.liv.moduleextraction.profling;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.liv.moduleextraction.experiments.TwoDepletingExperiment;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Created by wgatens on 26/05/15.
 */
public class LucaQBF {



    public static void main(String[] args) throws IOException {


        Set<String> skip = Sets.newHashSet("13ee048d-0403-4ac4-b733-6147d4bc08ac_quality.owl", "96ddfabf-9413-436c-ad27-febd29e457a4_hI=1%2FCH4");

        for(File f : new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/").listFiles()){
            if(!skip.contains(f.getName())){

                if(f.isFile()){
                    OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());

                    if(ont != null){
                        System.out.println(f.getName() + ": " + ont.getLogicalAxiomCount());

                        if(ont.getLogicalAxiomCount() > 2000){
                            DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
                            System.out.println("Expressivity: " + checker.getDescriptionLogicName());


                        }

                        ont.getOWLOntologyManager().removeOntology(ont);
                        ont = null;
                    }

                }


            }





        }



    }

}
