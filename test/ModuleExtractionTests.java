
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({HybridModuleExtraction.class, MEXExtraction.class,
        AxiomDependenciesTest.class, CycleTest.class})

public class ModuleExtractionTests {


        /*
               TODO: Example where improved cycle method creates a smaller module
               TODO: QBF cleanup
               TODO: MEX hybrid module + tests
               TODO: README for boris/examples of generating things
         */
}

