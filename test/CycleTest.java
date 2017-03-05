import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

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
        assertEquals(axioms, new ArrayList<>(verifier.getCycleCausingAxioms(true)));

    }

    @Test //Thesis example
    public void complexCycle(){
        OWLOntology complex = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/complexcycle.krss");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(complex.getLogicalAxioms());
        Collections.sort(axioms);

        /*
        0: C ≡ D ⊓ F
        1: W ≡ Z ⊓ (∀ r.W)
        2: A ⊑ B
        3: A ⊑ ∃ r.E
        4: B ⊑ C ⊓ E
        5: D ⊑ ∀ r.A
        6: E ⊑ G
        7: G ⊑ H ⊓ I
        8: H ⊑ ∃ r.G
        9: X ⊑ Y ⊔ Z
        */

        OntologyCycleVerifier verifier = new OntologyCycleVerifier(axioms);
        //Whole ontology is cyclic
        assertTrue(verifier.isCyclic());

        //Cycle: [B ⊑ C ⊓ E, C ≡ D ⊓ F, D ⊑ ∀ r.A, A ⊑ B] + [A ⊑ ∃ r.E]
        HashSet<OWLLogicalAxiom> cyclicSubset = new HashSet<>(
                Arrays.asList(axioms.get(4), axioms.get(0),
                        axioms.get(5), axioms.get(2), axioms.get(3)));

        verifier = new OntologyCycleVerifier(cyclicSubset);

        assertTrue("Subset is cyclic", verifier.isCyclic());

        Set<OWLLogicalAxiom> cycleCausing = verifier.getCycleCausingAxioms(true);

        OWLLogicalAxiom unnecessaryAxiom = axioms.get(3);
        //Does not contain [A ⊑ ∃ r.E] even though uses concept name A
        assertTrue("Cycle causing set contains unnecessary axiom: " + unnecessaryAxiom, !cycleCausing.contains(unnecessaryAxiom));

        //Cycle [H ⊑ ∃ r.G, G ⊑ H ⊓ I] + [E ⊑ G]
        cyclicSubset = new HashSet<>(
                Arrays.asList(axioms.get(6), axioms.get(7),
                        axioms.get(8)));


        verifier = new OntologyCycleVerifier(cyclicSubset);

        assertTrue("Subset is cyclic", verifier.isCyclic());

        cycleCausing = verifier.getCycleCausingAxioms(true);

        System.out.println(cycleCausing);

        assertTrue("Cycle causing set contains unnecessary axiom" + " " + axioms.get(6), !cycleCausing.contains(axioms.get(6)));

        //Single axiom  W ≡ Z ⊓ (∀ r.W)
        cyclicSubset = new HashSet<>(Arrays.asList(axioms.get(1)));

        verifier = new OntologyCycleVerifier(cyclicSubset);

        assertTrue("Subset is cyclic", verifier.isCyclic());
    }

    @Test //Thesis example
    public void removeCycle(){
        OWLOntology complex = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/complexcycle.krss");

        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(complex.getLogicalAxioms());
        OntologyCycleVerifier verifier = new OntologyCycleVerifier(axioms);

        assertTrue("Ontology is cyclic", verifier.isCyclic());

        //Remove those causing a cycle
        axioms.removeAll(verifier.getCycleCausingAxioms(true));

        System.out.println(axioms);

        //Check the result is acyclic
        verifier = new OntologyCycleVerifier(axioms);

        assertFalse("Ontology should be acyclic",  verifier.isCyclic());
    }

}

