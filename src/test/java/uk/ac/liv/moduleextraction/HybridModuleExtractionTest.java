package uk.ac.liv.moduleextraction;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.STARAMEXHybridExtractor;
import uk.ac.liv.moduleextraction.extractor.STARExtractor;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class HybridModuleExtractionTest {


    File dataDirectory;

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("src/test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void thesisFoodExample(){
        OWLOntology food = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/food.owl");
        ModuleUtils.remapIRIs(food, "X");
        List<OWLLogicalAxiom> axioms = food.logicalAxioms().collect(Collectors.toList());
        Collections.sort(axioms, new AxiomNameComparator());

        Set<OWLLogicalAxiom> logicalAxioms = ImmutableSet.copyOf(axioms);


  /*
        axioms = {
            0:Dessert ⊓ SeaFood ⊑ ⊥
            1:DessertCourse ≡ MealCourse ⊓ (∀ hasFood.Dessert)
            2:EdibleThing ⊓ MealCourse ⊑ ⊥
            3:MealCourse ⊑ ∀ hasFood.EdibleThing
            4:SeafoodCourse ≡ MealCourse ⊓ (∀ hasFood.SeaFood)
            5:∃ hasFood.⊤ ⊑ MealCourse
        }
  */

        assert(axioms.size() == 6);

        OWLDataFactory f = food.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty hasFood = f.getOWLObjectProperty(IRI.create("X#hasFood"));
        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(hasFood));

        STARExtractor starExtractor = new STARExtractor(logicalAxioms);
        Set<OWLLogicalAxiom> starModule = starExtractor.extractModule(sig);

        //STAR contains the whole ontology
        assertEquals(starModule, logicalAxioms);

        STARAMEXHybridExtractor starAmex = new STARAMEXHybridExtractor(logicalAxioms);
        Set<OWLLogicalAxiom> hybridModule = starAmex.extractModule(sig);

        // Hybrid Module = [EdibleThing ⊓ MealCourse ⊑ ⊥, MealCourse ⊑ ∀ hasFood.EdibleThing, ∃ hasFood.⊤ ⊑ MealCourse]
        Set<OWLLogicalAxiom> expectedHybridModule = Stream.of(axioms.get(2), axioms.get(3), axioms.get(5)).collect(Collectors.toSet());

        assertEquals(expectedHybridModule, hybridModule);

    }

}
