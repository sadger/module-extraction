import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.STARAMEXHybridExtractor;
import uk.ac.liv.moduleextraction.extractor.STARExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class HybridModuleExtraction {


    File dataDirectory;

    @Before
    public void locateFiles(){
        URL file = getClass().getResource("data");
        dataDirectory = new File(file.getFile());
    }

    @Test
    public void thesisFoodExample(){
        OWLOntology food = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/food.owl");
        ModuleUtils.remapIRIs(food, "X");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(food.getLogicalAxioms());

  /*
        axioms = {
            0 DessertCourse ≡ MealCourse ⊓ (∀ hasFood.Dessert)
            1 EdibleThing ⊓ MealCourse ⊑ ⊥
            2 MealCourse ⊑ ∀ hasFood.EdibleThing
            3 ∃ hasFood.⊤ ⊑ MealCourse
            4 Dessert ⊓ SeaFood ⊑ ⊥
            5 SeafoodCourse ≡ MealCourse ⊓ (∀ hasFood.SeaFood)
        }
  */

        assert(axioms.size() == 6);

        OWLDataFactory f = food.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty hasFood = f.getOWLObjectProperty(IRI.create("X#hasFood"));
        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(hasFood));

        STARExtractor starExtractor = new STARExtractor(food.getLogicalAxioms());
        Set<OWLLogicalAxiom> starModule = starExtractor.extractModule(sig);

        //STAR contains the whole ontology
        assertEquals(starModule, food.getLogicalAxioms());

        STARAMEXHybridExtractor starAmex = new STARAMEXHybridExtractor(food.getLogicalAxioms());
        Set<OWLLogicalAxiom> hybridModule = starAmex.extractModule(sig);

        // Hybrid Module = [EdibleThing ⊓ MealCourse ⊑ ⊥, MealCourse ⊑ ∀ hasFood.EdibleThing, ∃ hasFood.⊤ ⊑ MealCourse]
        HashSet<OWLLogicalAxiom> expectedHybridModule = new HashSet<>(Arrays.asList(axioms.get(1), axioms.get(2), axioms.get(3)));

        assertEquals(expectedHybridModule,hybridModule);

    }

}
