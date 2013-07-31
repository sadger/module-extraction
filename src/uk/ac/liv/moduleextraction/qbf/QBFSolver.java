package uk.ac.liv.moduleextraction.qbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.ac.liv.ontologyutils.main.ModulePaths;

public class QBFSolver {

	private String solverText;
	private File qbfFile;
	
	public boolean isSatisfiable (File dimacsLocation) throws QBFSolverException{

		this.qbfFile = dimacsLocation;

		solverText = "";
		
		ProcessBuilder pb = new ProcessBuilder("./sKizzo", dimacsLocation.getAbsolutePath());
		
		pb.directory(new File(ModulePaths.getQBFSolverLocation()));
		Process proc = null;

		try {
			proc = pb.start();
		} catch (IOException e) {
			e.printStackTrace();  
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




}
