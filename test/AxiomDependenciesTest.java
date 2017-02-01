import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDefinitorialDepth;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.extractor.AMEX;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AxiomDependenciesTest {

    File dataDirectory;

    @Before
    public void locateFiles(){
        URL file = getClass().getResource("data");
        dataDirectory = new File(file.getFile());
    }

    @Test
    public void definitorialDepthCorrect(){
        //Example of definitorial depth with RCIs from thesis
        OWLOntology depthanimals = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/defdepth.owl");
        ModuleUtils.remapIRIs(depthanimals, "Animals");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(depthanimals.getLogicalAxioms());

        /*
           0:Animal ⊑ ∃ eats.Meat
           1:Dog ⊑ Animal
           2:Dog ⊑ ∀ eats.Meat
           3:Meat ⊑ Food

           Those axioms with RCIs get the max def. depth of either axiom

         */

        AxiomDefinitorialDepth depth = new AxiomDefinitorialDepth(depthanimals);
        assertEquals(2, depth.lookup(axioms.get(0)));
        assertEquals(3, depth.lookup(axioms.get(1)));
        assertEquals(3, depth.lookup(axioms.get(2)));
        assertEquals(1, depth.lookup(axioms.get(3)));

    }

    @Test
    public void constructDependencies(){
        //Example of axioms dependencies from thesis
        OWLOntology animals = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/axiomdeps.owl");
        ModuleUtils.remapIRIs(animals, "Animals");

        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(animals.getLogicalAxioms());

        /*
        0:Dog ⊑ Pet
        1:Pet ≡ Animal ⊓ ⊤  <- Hack to force Pet on the LHS otherwise dependencies are different
        2:Cat ⊑ Pet
        3:Dog ⊑ ∀ eats.Meat
        */

        OWLDataFactory f = animals.getOWLOntologyManager().getOWLDataFactory();

        OWLClass animal = f.getOWLClass(IRI.create("Animals#Animal"));
        OWLClass pet = f.getOWLClass(IRI.create("Animals#Pet"));
        OWLClass meat = f.getOWLClass(IRI.create("Animals#Meat"));
        OWLObjectProperty eats = f.getOWLObjectProperty(IRI.create("Animals#eats"));

        //Calculate axiom dependencies
        AxiomDependencies dependencies = new AxiomDependencies(animals);

        System.out.println(dependencies);

        //Dog ⊑ Pet=[Animal, Pet]
        assertEquals(dependencies.get(axioms.get(0)), new HashSet<>(Arrays.asList(pet,animal)));

        //Pet ≡ Animal ⊓ ⊤=[Animal]
        assertEquals(dependencies.get(axioms.get(1)), new HashSet<>(Arrays.asList(animal)));

        // Cat ⊑ Pet=[Animal, Pet]
        assertEquals(dependencies.get(axioms.get(2)), new HashSet<>(Arrays.asList(pet,animal)));

        // Dog ⊑ ∀ eats.Meat=[Meat, eats]}
        assertEquals(dependencies.get(axioms.get(3)), new HashSet<>(Arrays.asList(eats, meat)));

    }

    @Test
    public void AMEXExtractionWithRCIs(){
        //Thesis AMEX extraction from ontology with RCIs
        OWLOntology animals = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/rci-extraction.owl");
        ModuleUtils.remapIRIs(animals, "RCI");
        ArrayList<OWLLogicalAxiom> axioms = new ArrayList<>(animals.getLogicalAxioms());

        /*
        0:Lion ⊑ Cat
        1:Mammal ⊑ ∀ has.WarmBlood
        2:Mammal ⊑ Animal
        3:AnimalGroup ⊑ ≥ 2 has.Animal
        4:Lion ⊑ Mammal
        */

        AMEX amexExtractor = new AMEX(animals);

        OWLDataFactory f = animals.getOWLOntologyManager().getOWLDataFactory();

        OWLClass animalgroup = f.getOWLClass(IRI.create("RCI#AnimalGroup"));
        OWLClass lion = f.getOWLClass(IRI.create("RCI#Lion"));

        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(animalgroup,lion));


        // M = [Mammal ⊑ ∀ has.WarmBlood, Mammal ⊑ Animal, AnimalGroup ⊑ ≥ 2 has.Animal, Lion ⊑ Mammal]
        Set<OWLLogicalAxiom> expectedModule = new HashSet<>(Arrays.asList(axioms.get(1), axioms.get(2), axioms.get(3), axioms.get(4)));
        Set<OWLLogicalAxiom> amexModule = amexExtractor.extractModule(sig);

        assertEquals(expectedModule,amexModule);
    }


}
