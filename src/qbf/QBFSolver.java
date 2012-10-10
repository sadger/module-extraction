package qbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QBFSolver {

	public boolean isSatisfiable (File dimacsLocation) throws QBFSolverException{
		ProcessBuilder pb = new ProcessBuilder("./sKizzo", dimacsLocation.getAbsolutePath());
		pb.directory(new File("/users/loco/wgatens/QBF/Skizzo"));
		Process proc = null;

		try {
			proc = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		@SuppressWarnings("unused")
		String line;

		try {
			while ((line = br.readLine()) != null) {
		//		System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
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
		}
		catch(IllegalThreadStateException t){
			t.printStackTrace();
		}

		if(exitValue == 10){
			return true;
		}
		else if(exitValue == 20){
			return false;
		}
		else{
			System.err.println("There was an error with the QBF solver, EXIT CODE " + exitValue);
			throw new QBFSolverException();
		}

	}





}
