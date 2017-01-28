package uk.ac.liv.moduleextraction.experiments;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import uk.ac.liv.moduleextraction.extractor.NotEquivalentToTerminologyException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class MultipleExperiments {


    private Experiment experiment;

    public void runExperiments(File signaturesLocation, Experiment experimentType) throws IOException{
        this.experiment = experimentType;
        System.out.println("Running for: " + signaturesLocation);
        Experiment experiment = experimentType;
        SigManager sigManager = new SigManager(signaturesLocation);
        File[] files = signaturesLocation.listFiles();
        Arrays.sort(files);

		/* Create new folder in result location with same name as signature
		folder */
        File newResultFolder = copyDirectoryStructure(signaturesLocation, "Signatures",new File(ModulePaths.getResultLocation()));
        if(experimentType instanceof ExactlyNDepletingComparison){
            ExactlyNDepletingComparison ndep = (ExactlyNDepletingComparison) experimentType;
            newResultFolder = new File(newResultFolder.getAbsolutePath() + "/" + "domain_size-" + ndep.getDomainSize());
        }


        int experimentCount = 1;
        for(File f : files){
            if(f.isFile()){

                System.out.println("Experiment " + experimentCount + "/" + files.length + ":" + f.getName());
                experimentCount++;
                //New folder in result location - same name as sig file
                File experimentLocation = new File(newResultFolder.getAbsolutePath() + "/" + f.getName());

                if(!experimentLocation.exists()){
                    experimentLocation.mkdirs();
                }

                //If there is already some metrics the experiment is probably finished
                if(experimentLocation.list().length > 0){
                    System.out.println("Experiment results already exists - skipping");
                    continue;
                }

                Set<OWLEntity> sig = sigManager.readFile(f.getName());
                experiment.performExperiment(sig,f);


                //Save the signature with the experiment
                SigManager managerWriter = new SigManager(experimentLocation);
                managerWriter.writeFile(sig, "signature");

                //Write any metrics
                experiment.writeMetrics(experimentLocation);
            }
        }
    }

    /** Signature location is a list of directories whos subdirectories are all signature files size-100,size-250... etc. */
    public void runAlternatingExperiments(File signaturesLocation, Experiment experiment) throws IOException{
        this.experiment  = experiment;
        File[] signaturedirs;
        signaturesLocation.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        signaturedirs = signaturesLocation.listFiles();

        Arrays.sort(signaturedirs);

        ArrayList<File>[] mapping = new ArrayList[signaturedirs.length];
        int i = 0;
        int max = 0;
        for(File f : signaturedirs){
            List<File> siglisting = Arrays.asList(f.listFiles());
            max = Math.max(max, siglisting.size());
            mapping[i++] = new ArrayList<File>(siglisting);
        }


        int experimentCount = 1;
        for (int j2 = 0; j2 < max; j2++) {
            for (int j = 0; j < mapping.length; j++) {
                ArrayList<File> files = mapping[j];
                Collections.sort(files);
                if(files.size() >= j2){
                    System.out.println("Experiment: " + experimentCount++);

                    File signature = files.get(j2);

                    File experimentLocation = copyDirectoryStructure(signature,"Signatures", new File(ModulePaths.getResultLocation()));
                    experimentLocation = new File(experimentLocation.getAbsolutePath() + "/" + signature.getName());

                    if(new File(experimentLocation.getAbsolutePath() + "/experiment-results").exists()){
                        System.out.println("Experiment results already exists - skipping");
                        continue;
                    }
                    if(!experimentLocation.exists()){
                        experimentLocation.mkdir();
                    }

                    SigManager manager = new SigManager(signature.getParentFile());
                    Set<OWLEntity> sig  = manager.readFile(signature.getName());
                    experiment.performExperiment(sig,signature);

                    //manager.writeFile(sig, "signature");
                    experimentCount++;

                    experiment.writeMetrics(experimentLocation);

                }

            }
        }
    }

    /**
     * Copy the structure of a source directory to another location creating a directory
     * for each directory in the path naming the final folder to highlight the experiment
     * @param source - The directory to begin copying from
     * @param sourceLimit - Only start copying the source from this directory
     * @param destination - The top level to copy the structure too
     * @return File - path of deepest part of new directory structure.
     * Example: copyDirectoryStructure(//a/x/y/z/,"x", /home/)
     * result File /home/y/z/
     */
    private File copyDirectoryStructure(File source, String sourceLimit, File destination) {
        Stack<String> directoriesToWrite = new Stack<String>();

        //Push all the directories from the end backwards to the sourceLimit (if applicable)
        while(!source.getName().equals(sourceLimit) || source.getParent() == null){
            if(source.isDirectory()){
                directoriesToWrite.push(source.getName());
            }
            source = source.getParentFile();
        }

        //Build the path from the start of the destinated using the pushed directory names
        String target = destination.getAbsolutePath();
        while(!directoriesToWrite.isEmpty()){
            target = target + "/" + directoriesToWrite.pop();
        }

        File targetFile = new File(target);

        //Name the folder by experiment
        String newFolderName = targetFile.getName() + "-" + experiment.getClass().getSimpleName();
        targetFile = new File(targetFile.getParent() + "/" + newFolderName);

        if(!targetFile.exists()){
            System.out.println("Making directory: " + targetFile.getAbsolutePath());
            targetFile.mkdirs();
        }


        return targetFile;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, NotEquivalentToTerminologyException, IOException, OWLOntologyStorageException, InterruptedException {

        File ontDir = new File(ModulePaths.getOntologyLocation() + "/Bioportal/at-most-sriq");
        File[] files = ontDir.listFiles();
        HashMap<String,Integer> ontSize = new HashMap<>();

        for(File ontFile : files) {
            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());
            ontSize.put(ontFile.getName(), ont.getLogicalAxiomCount());
            ont.getOWLOntologyManager().removeOntology(ont);
            ont = null;
        }

        Arrays.sort(files, (o1, o2) -> ontSize.get(o1.getName()).compareTo(ontSize.get(o2.getName())));

        System.out.println("Finished sorting ontologies");

        for(File ontFile : files){
            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontFile.getAbsolutePath());
            new MultipleExperiments().runExperiments(
                    new File(ModulePaths.getSignatureLocation() + "/Bioportal/at-most-sriq/" + ontFile.getName()),
                    new NDepletingExperiment(2,ont,ontFile));

            ont.getOWLOntologyManager().removeOntology(ont);
            ont = null;
        }

    }

}










