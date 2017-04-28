import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.OntologyLoader;
import uk.ac.liv.moduleextraction.util.TerminologyValidator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TerminologyValidationTests {

    File dataDirectory;

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void testTerminologyValidation(){

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/termWithRCIs.krss");
        TerminologyValidator validator = new TerminologyValidator(ont.getLogicalAxioms());

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertTrue("Is terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/terminology.krss");
        validator = new TerminologyValidator(ont.getLogicalAxioms());

        assertTrue("Is a terminology",  validator.isTerminology());
        assertTrue("Is terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/repeatedequalities.krss");
        validator = new TerminologyValidator(ont.getLogicalAxioms());

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertFalse("Is NOT a terminology with RCIs", validator.isTerminologyWithRCIs());

        ont = OntologyLoader.loadOntologyAllAxioms(dataDirectory + "/complexAxiom.krss");
        validator = new TerminologyValidator(ont.getLogicalAxioms());

        assertFalse("Is NOT a terminology",  validator.isTerminology());
        assertFalse("Is NOT a terminology with RCIs", validator.isTerminologyWithRCIs());

    }
}
