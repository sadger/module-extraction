package uk.ac.liv.moduleextraction;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.ALCQIOntologyVerifier;
import uk.ac.liv.moduleextraction.util.ELIOntologyValidator;
import uk.ac.liv.moduleextraction.util.OntologyLoader;
import uk.ac.liv.moduleextraction.util.SHIQOntologyVerifier;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressivenessTest {


    File dataDirectory;

    SHIQOntologyVerifier shiqVerifier = new SHIQOntologyVerifier();
    ALCQIOntologyVerifier alcqiVerifier = new ALCQIOntologyVerifier();
    ELIOntologyValidator eliOntologyValidator = new ELIOntologyValidator();

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("src/test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void SHOIQTest() {
        OWLOntology shoiq = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/shoiqexample.owl");
        Set<OWLLogicalAxiom> shoiqLogicalAxioms = shoiq.logicalAxioms().collect(Collectors.toSet());
        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(shoiqLogicalAxioms));
        assertFalse("Is NOT an ALCQI ontology", alcqiVerifier.isALCQIOntology(shoiqLogicalAxioms));
        assertFalse("Is NOT a SHIQ ontology", shiqVerifier.isSHIQOntology(shoiqLogicalAxioms));
    }

    @Test
    public void SHIQTest(){
        OWLOntology shiq = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/shiqExample.owl");
        Set<OWLLogicalAxiom> shiqLogicalAxioms = shiq.logicalAxioms().collect(Collectors.toSet());
        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(shiqLogicalAxioms));
        assertFalse("Is NOT an ALCQI ontology", alcqiVerifier.isALCQIOntology(shiqLogicalAxioms));
        assertTrue("Is a SHIQ ontology", shiqVerifier.isSHIQOntology(shiqLogicalAxioms));

    }

    @Test
    public void ALCQITest(){
        OWLOntology alcqi = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/rci-extraction.owl");
        Set<OWLLogicalAxiom> alcqiLogicalAxioms = alcqi.logicalAxioms().collect(Collectors.toSet());
        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(alcqiLogicalAxioms));
        assertTrue("Is an ALCQI ontology", alcqiVerifier.isALCQIOntology(alcqiLogicalAxioms));
        assertTrue("Is a SHIQ ontology", shiqVerifier.isSHIQOntology(alcqiLogicalAxioms));
    }

    @Test
    public void ELITest(){
        OWLOntology eli = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/eliPets.owl");
        Set<OWLLogicalAxiom> eliLogicalAxioms = eli.logicalAxioms().collect(Collectors.toSet());
        assertTrue("Is ELI ontology", eliOntologyValidator.isELIOntology(eliLogicalAxioms));
        assertTrue("Is ALCQI ontology", alcqiVerifier.isALCQIOntology(eliLogicalAxioms));
        assertTrue("Is SHIQ ontology", shiqVerifier.isSHIQOntology(eliLogicalAxioms));

    }



}
