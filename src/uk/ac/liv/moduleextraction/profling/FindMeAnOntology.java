package uk.ac.liv.moduleextraction.profling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.expressions.ALCValidator;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.ontologies.TerminologyChecker;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class FindMeAnOntology {

    ALCValidator validator = new ALCValidator();
    ELValidator elvalidator = new ELValidator();
    TerminologyChecker termChecker = new TerminologyChecker();
    EquivalentToTerminologyChecker equivTermChecker = new EquivalentToTerminologyChecker();
    private File ontologyDirectory;
    int termCount = 0;
    private BufferedWriter writer;
    private String file;

    public FindMeAnOntology(File ontologyDirectory, String file) {
        this.ontologyDirectory = ontologyDirectory;
        this.file = file;
    }

    public void profileOntologies() throws IOException{
        System.gc();
        writer = new BufferedWriter(new FileWriter(ModulePaths.getResultLocation() + "/" + file, false));
        File[] ontologyFiles = ontologyDirectory.listFiles();
        Collections.sort(Arrays.asList(ontologyFiles));
        writer.write("Name,Expressiveness,LogicalAxioms,Inclusions,Equivalences, "
                + "Repeated Inclusions, Repeated Equivalances, SharedNames, Concepts, "
                + "Roles, CoreEL, CoreCyclic, Location\n");
        for(File f: ontologyFiles){
            if(f.isFile()){
                OWLOntology ont = null;
                try{
                    ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
                    if(ont != null){
                        System.out.println(f.getName());
                        profileOntology(f, ont);
                        ont.getOWLOntologyManager().removeOntology(ont);
                        writer.flush();
                    }
                }
                catch(NullPointerException ioe){
                    System.out.println("Oh so null");
                }


                ont = null;
//				System.out.println();


            }
        }

        writer.close();

    }

    private void profileOntology(File location,OWLOntology ont) throws IOException{
//		System.out.println("Logical Axiom Count: " + ont.getLogicalAxiomCount());
        DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
        String express = checker.getDescriptionLogicName();

        String fileName = location.getName();
        String shortName = fileName.substring(Math.max(0, fileName.length() - 20));
        Set<OWLLogicalAxiom> core = ModuleUtils.getCoreAxioms(ont);
//        System.out.println("Core");
//        System.out.println("EL?:" + elvalidator.isELOntology(core));
        OntologyCycleVerifier verifier = new OntologyCycleVerifier(core);


        AxiomStructureInspector inspector = new AxiomStructureInspector(ont);


        writer.write(shortName + "," + express + ","+ ont.getLogicalAxiomCount() + "," + ont.getAxiomCount(AxiomType.SUBCLASS_OF) + "," +
                ont.getAxiomCount(AxiomType.EQUIVALENT_CLASSES) + "," + inspector.countNamesWithRepeatedInclusions() +
                "," + inspector.countNamesWithRepeatedEqualities() + "," + inspector.getSharedNames().size()
                + "," + ont.getClassesInSignature().size() + "," + ont.getObjectPropertiesInSignature().size()
                + "," + elvalidator.isELOntology(core) + "," + verifier.isCyclic() + "," + location.getAbsolutePath());
        writer.write('\n');



    }

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {

//	FindMeAnOntology find = new FindMeAnOntology(new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All"), "corpus-profile.csv");
//	try {
//		find.profileOntologies();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/1not2.krss");
        OWLDataFactory f = ont.getOWLOntologyManager().getOWLDataFactory();

        ModuleUtils.remapIRIs(ont,"X");
        for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
            System.out.println(ax);
        }

        Set<OWLEntity> sig = new HashSet<OWLEntity>();

        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));

        sig.add(a);
        sig.add(r);

        NDepletingModuleExtractor extractor = new NDepletingModuleExtractor(1,ont.getLogicalAxioms());
        System.out.println(extractor.extractModule(sig));


    }




}

