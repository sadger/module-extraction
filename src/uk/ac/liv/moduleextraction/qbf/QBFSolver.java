package uk.ac.liv.moduleextraction.qbf;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.AMEX;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.*;

public class QBFSolver {

	private String solverText;
	private File qbfFile;
	
	public boolean isSatisfiable (File dimacsLocation) throws QBFSolverException{

		this.qbfFile = dimacsLocation;
		
		solverText = "";
		//File qbfSolverLocation = new File("/home/william/Programs/sKizzo/sKizzo");
		File qbfSolverLocation = new File("/home/william/Programs/quantor-3.2/quantor");


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

	private void deleteQBFFile(){
		qbfFile.delete();
	}

	private boolean handleExitCode(Process proc) throws QBFSolverException {
		int exitValue = -1; 

		try{
			exitValue = proc.exitValue();
		}
		catch(IllegalThreadStateException t){
			t.printStackTrace();
		}
		finally{
			proc.destroy();
		}
	
		boolean result;
		if(exitValue == 10){
			deleteQBFFile();
			result = true;
		}
		else if(exitValue == 20){
			deleteQBFFile();
			result = false;
		}
		else{
			System.err.println("There was an error with the QBF solver, EXIT CODE " + exitValue);
			System.out.println(qbfFile.getAbsolutePath());
			System.out.println(solverText);
			throw new QBFSolverException();
		}

		if(qbfFile.exists()){
			System.out.println("WARNING: QBF File not deleted");
		}

		return  result;

	}
	
	public static void main(String[] args) throws IOException {

			OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
			AMEX extract = new AMEX(ont);
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
