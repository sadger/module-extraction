package uk.ac.liv.moduleextraction.qbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.extractor.SemanticRuleExtractor;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class QBFSolver {

	private String solverText;
	private File qbfFile;
	
	public boolean isSatisfiable (File dimacsLocation) throws QBFSolverException{

		this.qbfFile = dimacsLocation;
		
		solverText = "";
		
		File qbfSolverLocation = new File(ModulePaths.getQBFSolverLocation());
		
		ProcessBuilder pb = new ProcessBuilder("./" + qbfSolverLocation.getName() , dimacsLocation.getAbsolutePath());
		
		pb.directory(qbfSolverLocation.getParentFile());
		
		Process proc = null;

		try {
			proc = pb.start();
		} catch (IOException e) {
			if(!qbfSolverLocation.exists()){
				System.err.println("ERROR: The location you specified for QBF_LOCATION variable does not exist");
				System.exit(-1);
			}
			else if(!qbfSolverLocation.isFile()){
				System.err.println("ERROR: The location you specified for QBF_LOCATION variable is a directory " +
						"please point the variable to the sKizzo binary");
				System.exit(-1);
			}
			else{
				e.printStackTrace();  
			}

		}

		InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

	
		String line;

		try {
			while ((line = br.readLine()) != null) {
				solverText += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try{
				isr.close();
				br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return handleExitCode(proc);

	}		

	private boolean handleExitCode(Process proc) throws QBFSolverException {
		int exitValue = -1; 

		try{
			exitValue = proc.exitValue();
			/* Delete the qbf file */
			qbfFile.delete();
		}
		catch(IllegalThreadStateException t){
			t.printStackTrace();
		}
		finally{
			if(qbfFile.exists()){
				System.out.println("WARNING: QBF File not deleted");
			}
			proc.destroy();
		}
	

		if(exitValue == 10){
			return true;
		}
		else if(exitValue == 20){
			return false;
		}
		else{
			System.err.println("There was an error with the QBF solver, EXIT CODE " + exitValue);
			System.out.println(solverText);
			throw new QBFSolverException();
		}

	}
	
	public static void main(String[] args) throws IOException {

			OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
			SemanticRuleExtractor extract = new SemanticRuleExtractor(ont);
			SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
			ont.getOWLOntologyManager().removeOntology(ont);
			
			int Min = 100;
			int Max = 1000;
	
			
			for (int i = 1; i <= 100; i++) {
				int rand = Min + (int)(Math.random() * ((Max - Min) + 1));
				System.out.println("Sig size: " + rand);
				System.out.println("Module " + i + " size: " + extract.extractModule(gen.generateRandomSignature(rand)).size());
			}
			

	
	}




}
