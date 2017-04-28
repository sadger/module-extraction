import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.ALCQIOntologyVerifier;
import uk.ac.liv.moduleextraction.util.ELIOntologyValidator;
import uk.ac.liv.moduleextraction.util.OntologyLoader;
import uk.ac.liv.moduleextraction.util.SHIQOntologyVerifier;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressivenessTest {


    File dataDirectory;

    SHIQOntologyVerifier shiqVerifier = new SHIQOntologyVerifier();
    ALCQIOntologyVerifier alcqiVerifier = new ALCQIOntologyVerifier();
    ELIOntologyValidator eliOntologyValidator = new ELIOntologyValidator();

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void SHOIQTest() {
        OWLOntology shoiq = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/shoiqexample.owl");

        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(shoiq.getLogicalAxioms()));
        assertFalse("Is NOT an ALCQI ontology", alcqiVerifier.isALCQIOntology(shoiq.getLogicalAxioms()));
        assertFalse("Is NOT a SHIQ ontology", shiqVerifier.isSHIQOntology(shoiq.getLogicalAxioms()));
    }

    @Test
    public void SHIQTest(){
        OWLOntology shiq = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/shiqExample.owl");
        shiq.getLogicalAxioms().forEach(System.out::println);
//
        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(shiq.getLogicalAxioms()));
        assertFalse("Is NOT an ALCQI ontology", alcqiVerifier.isALCQIOntology(shiq.getLogicalAxioms()));
        assertTrue("Is a SHIQ ontology", shiqVerifier.isSHIQOntology(shiq.getLogicalAxioms()));
    }

    @Test
    public void ALCQITest(){
        OWLOntology alcqi = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/rci-extraction.owl");
        assertFalse("Is NOT an ELI ontology", eliOntologyValidator.isELIOntology(alcqi.getLogicalAxioms()));
        assertTrue("Is an ALCQI ontology", alcqiVerifier.isALCQIOntology(alcqi.getLogicalAxioms()));
        assertTrue("Is a SHIQ ontology", shiqVerifier.isSHIQOntology(alcqi.getLogicalAxioms()));
    }

    @Test
    public void ELITest(){
        OWLOntology eli = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/eliPets.owl");

        assertTrue("Is ELI ontology", eliOntologyValidator.isELIOntology(eli.getLogicalAxioms()));
        assertTrue("Is ALCQI ontology", alcqiVerifier.isALCQIOntology(eli.getLogicalAxioms()));
        assertTrue("Is SHIQ ontology", shiqVerifier.isSHIQOntology(eli.getLogicalAxioms()));

    }



}
