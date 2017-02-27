
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
@RunWith(Suite.class)
@SuiteClasses({HybridModuleExtraction.class, MEXExtraction.class,
        AxiomDependenciesTest.class, CycleTest.class})

public class ModuleExtractionTests {


}

