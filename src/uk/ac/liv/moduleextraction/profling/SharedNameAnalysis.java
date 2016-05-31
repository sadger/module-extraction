package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by william on 08/05/16.
 */
public class SharedNameAnalysis {

    public void analysisAxioms(String ontName, Set<OWLLogicalAxiom> axioms){
        AxiomStructureInspector inspector = new AxiomStructureInspector(axioms);
        Set<OWLLogicalAxiom> unsupported = new SupportedExpressivenessFilter().getUnsupportedAxioms(axioms);
        axioms.remove(unsupported);

        Set<OWLClass> sharedName = inspector.getSharedNames();
        Set<OWLClass> repeatedEqualities = inspector.getNamesWithRepeatedEqualities();
        sharedName.addAll(repeatedEqualities);
        // System.out.println("Shared Names: " + sharedName.size());

        if(sharedName.size() > 0){
            int both = 0;
            int inclusions = 0;
            int equalities = 0;

            int both_equal = 0;
            int both_inc = 0;
            int inc = 0;
            int equal = 0;

            for(OWLClass shared : sharedName){

                if(!inspector.getPrimitiveDefinitions(shared).isEmpty() && repeatedEqualities.contains(shared)){
                    both++;
                    both_equal += inspector.getDefinitions(shared).size();
                    both_inc += inspector.getPrimitiveDefinitions(shared).size();

                }
                else if(!inspector.getPrimitiveDefinitions(shared).isEmpty()){
                    inclusions++;
                    inc += inspector.getPrimitiveDefinitions(shared).size();

                }
                else if(repeatedEqualities.contains(shared)){
                    equalities++;
                    equal += inspector.getDefinitions(shared).size();

                }
            }



            System.out.printf("%s, %s, %s, %s, %s, %s, %s, %s, %s \n", ontName, sharedName.size(), inclusions, equalities, both, inc, equal, both_inc, both_equal);



        }

    }


    public static void main(String[] args) {


        File ontDir = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/");
        File[] files = ontDir.listFiles();
        Arrays.sort(files);
        int i = 0;

        System.out.println("Ontology, SharedNames, OnlyInclusions, OnlyEqualities, Both,  TotalIncShared, TotalEqualShared, TotalBothIncShared, TotalBothEqualShared");

        for(File ontFile : files){

            // System.out.println(ontFile.getName());
            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());
            if(ontFile.getName().equals("1cc1729e-720b-44cb-95a6-312bbcf37e00_my%2Fheart-QBF")){
                ont.getLogicalAxioms().forEach(System.out::println);
            }

            new SharedNameAnalysis().analysisAxioms(ontFile.getName(), ont.getLogicalAxioms());

            ++i;

        }

    }


}
