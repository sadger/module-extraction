import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by william on 27/02/17.
 */
public class CycleTest {

    File dataDirectory;

    @Before
    public void locateFiles(){
        URL file = getClass().getResource("data");
        dataDirectory = new File(file.getFile());
    }


    @Test
    public void simpleCycle(){
        OWLOntology simple = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/simplecycle.krss");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(simple.getLogicalAxioms());

        /*
        0: D ⊑ A
        1: A ⊑ B
        2: B ⊑ C
        3: C ⊑ D
        */

        OntologyCycleVerifier verifier = new OntologyCycleVerifier(axioms);

        //Ontology is cyclic and all axioms contribute to cycle
        assertTrue(verifier.isCyclic());
        assertEquals(axioms, new ArrayList<>(verifier.getCycleCausingAxioms()));

    }

    @Test //Thesis example
    public void complexCycle(){
        OWLOntology complex = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/complexcycle.krss");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(complex.getLogicalAxioms());
        axioms.forEach(System.out::println);

    }

}

