import java.io.File;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assert.*;

import dependencies.SyntacticDependencyTests;
import extractions.ExtractionTests;

@RunWith(Suite.class)
@SuiteClasses({SyntacticDependencyTests.class, ExtractionTests.class})

public class ModuleExtractionTests {

	@BeforeClass
	public static void printTestFileHashes(){
		File testFiles = new File("TestData");
		
	}
	

}
  