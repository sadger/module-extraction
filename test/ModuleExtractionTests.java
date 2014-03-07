import java.io.File;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assert.*;

import dependencies.SyntacticDependencyTests;

@RunWith(Suite.class)
@SuiteClasses({SyntacticDependencyTests.class})

public class ModuleExtractionTests {

	@BeforeClass
	public static void printTestFileHashes(){
		File testFiles = new File("TestData");
		
	}
	

}
  