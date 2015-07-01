package dependencies;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import extractions.ExtractionTests;
import utils.FileHash;

@RunWith(Suite.class)
@SuiteClasses({DependencyGeneration.class, AxiomDependencyGeneration.class, CompareDependencies.class, ExpressiveAxiomDepth.class})
public class SyntacticDependencyTests {

	@BeforeClass
	public static void filesUnchanged() throws IOException{
		assertTrue(FileHash.hashesEqual("TestData/dependencies/simple-dependencies.krss", "718d8dc5ddadf4c62086d980f7fec040"));
		assertTrue(FileHash.hashesEqual("TestData/dependencies/multiple-simple.krss", "f8daf1e0a680194211a28565e5ac2d9f"));
		assertTrue(FileHash.hashesEqual("TestData/dependencies/multiple-shared.krss", "93e79e15f4c046c1c1accca3e8565005"));
	}




}
