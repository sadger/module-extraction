package uk.ac.liv.moduleextraction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({HybridModuleExtractionTest.class, MEXExtractionTest.class,
        AxiomDependenciesTest.class, CycleTest.class, TerminologyValidationTest.class,
        ExpressivenessTest.class})

public class ModuleExtractionTests {

        /*
               TODO: Example where improved cycle method creates a smaller module
               TODO: Make sure QBF doesn't accept nominals
               TODO: Error checking around supported ontologies for extractors
         */
}


