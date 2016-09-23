package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by william on 07/05/16.
 */
public class OntologyCorpusExpressitivty {

    public static void main(String[] args) throws OWLOntologyCreationException, NotEquivalentToTerminologyException, IOException, OWLOntologyStorageException, InterruptedException {

        File ontDir = new File(ModulePaths.getOntologyLocation() + "/tones-named/at-most-sriq/");
        File[] files = ontDir.listFiles();
        Arrays.sort(files);
        HashMap<String,Integer> partition = new HashMap<>();
        HashMap<Integer,Integer> sizeBins = new HashMap<>();

        int min = 0;
        int max = 0;

        System.out.println(files.length);

        for(File ontFile : files){

            String name = ontFile.getName();

            if(!name.equals("rename-list.txt")){
                System.out.println(name);

                OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());

                System.out.println(ont.getLogicalAxiomCount());
                if(ont != null){

                    int count = ont.getLogicalAxiomCount();
                    if(min == 0){ min = count;}
                    min = Math.min(min,count);
                    max = Math.max(max,count);


                    int binsize = 100;
                    Integer bin = (int) Math.ceil((double) count/binsize) * binsize;

                    Integer currentBinValue = sizeBins.get(bin);
                    if(currentBinValue == null){
                        sizeBins.put(bin,1);
                    }
                    else{
                        sizeBins.put(bin,++currentBinValue);
                    }



                    DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
                    String DLName = checker.getDescriptionLogicName();

                    Integer dlcount = partition.get(DLName);
                    if(dlcount  == null){
                        partition.put(DLName,1);
                    }
                    else{
                        partition.put(DLName,++dlcount );
                    }

                    ont.getOWLOntologyManager().removeOntology(ont);
                    ont = null;
                }
            }








        }




        ArrayList<String> keys = new ArrayList<>(partition.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return partition.get(t1).compareTo(partition.get(s));
            }
        });

        keys.forEach(p -> System.out.println(p + ": " + partition.get(p)));

        int bins = 8;
        double total_length = max - min;
        double subrange_length = total_length/bins;

        double current_start = min;
        for (int i = 0; i < bins; ++i) {
            System.out.println("Smaller range: [" + current_start + ", " + (current_start + subrange_length) + "]");
            current_start += subrange_length;
        }

        System.out.println(max);
        System.out.println(min);

        System.out.println(sizeBins);


    }

}

