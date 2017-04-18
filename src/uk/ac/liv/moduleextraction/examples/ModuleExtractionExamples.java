package uk.ac.liv.moduleextraction.examples;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.AMEX;
import uk.ac.liv.moduleextraction.extractor.STARAMEXHybridExtractor;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.signature.WriteAxiomSignatures;
import uk.ac.liv.moduleextraction.signature.WriteRandomSigs;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.util.Set;

/**
 * Created by william on 18/04/17.
 */
public class ModuleExtractionExamples {


    public static void usingModuleExtractors(){
        //Load the ontology from a file - CHANGE THIS to your own ontology
        //Make sure ontologies are suitable for use with the extractor before using it (no extra checking is done) - i.e don't use more expressive  than ALCQI with AMEX
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/home/william/PhD/Ontologies/NCI/Profile/NCI-star-inc.owl");

        //Create the module extractors - implement the Extractor interface
        AMEX amex = new AMEX(ont);
        STARAMEXHybridExtractor hybrid = new STARAMEXHybridExtractor(ont);

        //Generate a set of 100 random axioms from the ontology
        Set<OWLLogicalAxiom> randomAxs = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(),100);

        //Extract a module for the signature of each axiom
        for(OWLLogicalAxiom ax : randomAxs){

            //Signature of the axiom
            Set<OWLEntity> sig = ax.getSignature();

            //Extract the modules - N.B don't need a new extractor object for each signature
            Set<OWLLogicalAxiom> amexMod = amex.extractModule(sig);
            Set<OWLLogicalAxiom> hybridMod = hybrid.extractModule(sig);

            System.out.println("AMEX: " + amexMod.size());
            System.out.println("HYBRID: " + hybridMod.size());
            System.out.println("STAR: " + hybrid.getStarModule().size()); //STAR module is computed through the hybrid module
            System.out.println();

        }

    }

    public static void generatingSignatures(){
        //Load ontology
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/home/william/PhD/Ontologies/NCI/Profile/NCI-star-inc.owl");

        //Intitalise the signature generator
        SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());

        //Random signature of 100 symbols (roles or concepts)
        Set<OWLEntity> sig = gen.generateRandomSignature(100);

        //Random signature of 100 concept names
        Set<OWLClass> sigCls = gen.generateRandomClassSignature(100);

        //Random signature of 100 role names
        Set<OWLObjectProperty> roleSig = gen.generateRandomRoles(100);
    }



    public static void writeSignaturesToFile(){
        /* Writing signatures to file useful for experiments and reproducing */

        //Ontology
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/home/william/PhD/Ontologies/NCI/Profile/NCI-star-inc.owl");

        //Location to save signatures
        File f = new File("/path/to/Signatures");

        //Writes every axiom signature of the ontology to the given location
        WriteAxiomSignatures axiomSignatures = new WriteAxiomSignatures(ont, f);
        axiomSignatures.writeAxiomSignatures();

        /* Completely random */
        WriteRandomSigs random = new WriteRandomSigs(ont, f);
        //Write 1000 signatures of 50 random symbols to the location
        random.writeSignature(50, 1000);
        //Write 500 signatures of 100 random symbols to the location
        random.writeSignature(100, 500);

        /* Concepts + role percentage */
        //Write 1000 signatures consisting of 50 random concept names + 25% of all roles taken randomly from the ontology
        random.writeSignatureWithRoles(50, 25, 1000);

        //Write 1000 signatures consisting of 100 random concept names 0% of all roles (purely concept signatures)
        random.writeSignatureWithRoles(100, 0, 1000);
    }


    public static void readSignaturesFromFile(){

    }


    public static void main(String[] args) {
        /* Run the examples */

        //ModuleExtractionExamples.usingModuleExtractors();
        //ModuleExtractionExamples.generatingSignatures();
        //ModuleExtractionExamples.writeSignaturesToFile();


    }
}
