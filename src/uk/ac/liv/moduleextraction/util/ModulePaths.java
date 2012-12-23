package uk.ac.liv.moduleextraction.util;

import java.util.Map;

public class ModulePaths {

    private static Map<String, String> envVariables = System.getenv();
    
    public static String getOntologyLocation(){
    	return envVariables.get("ONTOLOGY_LOCATION");
    }
    
    public static String getQBFSolverLocation(){
    	return envVariables.get("QBF_LOCATION");
    }
}
