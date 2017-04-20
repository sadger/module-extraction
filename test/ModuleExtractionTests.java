
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({HybridModuleExtraction.class, MEXExtraction.class,
        AxiomDependenciesTest.class, CycleTest.class})

public class ModuleExtractionTests {

        /*
               TODO: Example where improved cycle method creates a smaller module
               TODO: Make sure QBF doesn't accept nominals
               TODO: Error checking around supported ontologies for extractors
         */
}


