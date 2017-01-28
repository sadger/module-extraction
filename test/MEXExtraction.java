import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.MEX;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;


/**
 * Created by william on 28/01/17.
 */
public class MEXExtraction {


    File dataDirectory;

    @Before
    public void locateFiles(){
        URL file = getClass().getResource("data");
        dataDirectory = new File(file.getFile());
    }

    @Test
    public void simpleIndirectDependencyExtraction(){
        OWLOntology equiv = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/equiv.krss");
        ModuleUtils.remapIRIs(equiv, "X");

        /*
        0:C ⊑ A2
        1:A ≡ B1 ⊓ B2
        2:A1 ⊑ B1
        3:A2 ⊑ B2
        */
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(equiv.getLogicalAxioms());

        OWLDataFactory f = equiv.getOWLOntologyManager().getOWLDataFactory();
        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLClass a1 = f.getOWLClass(IRI.create("X#A1"));
        OWLClass a2 = f.getOWLClass(IRI.create("X#A2"));

        //  Σ = [A1, A, A2]
        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(a, a1, a2));

        MEX mex = new MEX(equiv.getLogicalAxioms());

        Set<OWLLogicalAxiom> expectedMexModule = new HashSet<>(Arrays.asList(axioms.get(1), axioms.get(2), axioms.get(3)));
        Set<OWLLogicalAxiom> mexModule = mex.extractModule(sig);

        //Module  {A ≡ B1 ⊓ B2, A1 ⊑ B1, A2 ⊑ B2}
        assertEquals(expectedMexModule, mexModule);
    }
}
