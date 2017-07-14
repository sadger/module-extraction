package uk.ac.liv.moduleextraction;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.OntologyLoader;
import uk.ac.liv.moduleextraction.util.TerminologyValidator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TerminologyValidationTest {

    File dataDirectory;

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("src/test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void testTerminologyValidation(){

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/termWithRCIs.krss");
        Set<OWLLogicalAxiom> logicalAxioms = ont.logicalAxioms().collect(Collectors.toSet());
        TerminologyValidator validator = new TerminologyValidator(logicalAxioms);

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertTrue("Is terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/terminology.krss");
        logicalAxioms = ont.logicalAxioms().collect(Collectors.toSet());
        validator = new TerminologyValidator(logicalAxioms);

        assertTrue("Is a terminology",  validator.isTerminology());
        assertTrue("Is terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/repeatedequalities.krss");
        logicalAxioms = ont.logicalAxioms().collect(Collectors.toSet());
        validator = new TerminologyValidator(logicalAxioms);

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertFalse("Is NOT a terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/complexAxiom.krss");
        logicalAxioms = ont.logicalAxioms().collect(Collectors.toSet());
        validator = new TerminologyValidator(logicalAxioms);

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertFalse("Is NOT a terminology with RCIs", validator.isTerminologyWithRCIs());

    }
}
