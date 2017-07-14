package uk.ac.liv.moduleextraction;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDefinitorialDepth;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.extractor.AMEX;
import uk.ac.liv.moduleextraction.extractor.ExtractorException;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AxiomDependenciesTest {

    File dataDirectory;

    @Before
    public void locateFiles(){
        Path resourceDirectory = Paths.get("src/test/data/");
        dataDirectory = resourceDirectory.toFile();
    }

    @Test
    public void definitorialDepthCorrect(){
        //Example of definitorial depth with RCIs from thesis
        OWLOntology depthanimals = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/defdepth.owl");
        ModuleUtils.remapIRIs(depthanimals, "Animals");
        List<OWLLogicalAxiom> axioms = depthanimals.logicalAxioms().collect(Collectors.toList());
        Collections.sort(axioms,new AxiomNameComparator());

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

        List<OWLLogicalAxiom> axioms = animals.logicalAxioms().collect(Collectors.toList());
        Collections.sort(axioms,new AxiomNameComparator());

        /*
        0:Cat ⊑ Pet
        1:Dog ⊑ Pet
        2:Dog ⊑ ∀ eats.Meat
        3:Pet ≡ Animal ⊓ ⊤
        */

        OWLDataFactory f = animals.getOWLOntologyManager().getOWLDataFactory();

        OWLClass animal = f.getOWLClass(IRI.create("Animals#Animal"));
        OWLClass pet = f.getOWLClass(IRI.create("Animals#Pet"));
        OWLClass meat = f.getOWLClass(IRI.create("Animals#Meat"));
        OWLObjectProperty eats = f.getOWLObjectProperty(IRI.create("Animals#eats"));

        //Calculate axiom dependencies
        AxiomDependencies dependencies = new AxiomDependencies(animals);

        // Cat ⊑ Pet=[Animal, Pet]
        assertEquals(dependencies.get(axioms.get(0)), new HashSet<>(Arrays.asList(pet,animal)));

        //Dog ⊑ Pet=[Animal, Pet]
        assertEquals(dependencies.get(axioms.get(1)), new HashSet<>(Arrays.asList(pet,animal)));

        // Dog ⊑ ∀ eats.Meat=[Meat, eats]}
        assertEquals(dependencies.get(axioms.get(2)), new HashSet<>(Arrays.asList(eats, meat)));

        //Pet ≡ Animal ⊓ ⊤=[Animal]
        assertEquals(dependencies.get(axioms.get(3)), new HashSet<>(Arrays.asList(animal)));


    }


    @Test
    public void AMEXExtractionWithRCIs() throws ExtractorException {
        //Thesis AMEX extraction from ontology with RCIs
        OWLOntology animals = OntologyLoader.loadOntologyAllAxioms(dataDirectory.getAbsolutePath() + "/rci-extraction.owl");
        ModuleUtils.remapIRIs(animals, "RCI");
        List<OWLLogicalAxiom> axioms = animals.logicalAxioms().collect(Collectors.toList());
        Collections.sort(axioms,new AxiomNameComparator());


        /*
        0:AnimalGroup ⊑ ≥ 2 has.Animal
        1:Lion ⊑ Cat
        2:Lion ⊑ Mammal
        3:Mammal ⊑ ∀ has.WarmBlood
        4:Mammal ⊑ Animal
        */

        AMEX amexExtractor = new AMEX(animals);

        OWLDataFactory f = animals.getOWLOntologyManager().getOWLDataFactory();

        OWLClass animalgroup = f.getOWLClass(IRI.create("RCI#AnimalGroup"));
        OWLClass lion = f.getOWLClass(IRI.create("RCI#Lion"));

        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(animalgroup,lion));


        // M = [Mammal ⊑ ∀ has.WarmBlood, Mammal ⊑ Animal, AnimalGroup ⊑ ≥ 2 has.Animal, Lion ⊑ Mammal]
        Set<OWLLogicalAxiom> expectedModule = new HashSet<>(Arrays.asList(axioms.get(0), axioms.get(2), axioms.get(3), axioms.get(4)));
        Set<OWLLogicalAxiom> amexModule = amexExtractor.extractModule(sig);

        assertEquals(expectedModule,amexModule);
    }


}
