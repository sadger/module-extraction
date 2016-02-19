package uk.ac.liv.moduleextraction.qbf;

import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class QBFSolver {

	private File qbfFile;
    private int solverIndex;
    private String solverText = "";

    //How long should we wait before using another solver
    private static final int SOLVER_TIMEOUT_SECONDS = 10;
    private boolean withTimeout;

    private static String[] qbfLocations = ModulePaths.getQBFSolverLocation().split(File.pathSeparator, -1);

    private boolean isSatisfiable(File dimacsLocation, int solverIndex, boolean withTimeout) throws QBFSolverException{

        System.out.println(dimacsLocation);

        this.withTimeout = withTimeout;
        this.solverIndex = solverIndex;


        if(withTimeout && solverIndex == qbfLocations.length){
            return isSatisfiable(dimacsLocation,0,false);
        }

        if(solverIndex > qbfLocations.length - 1){
            System.err.println("CANNOT SOLVE QBF - NO VALID SOLVER LEFT TO TRY");
			System.out.println("QBF FILE: " + qbfFile.getAbsolutePath());
			System.out.println(solverText);
            throw new QBFSolverException();
        }


        File qbfSolverLocation = new File(qbfLocations[solverIndex]);

//        if(solverIndex != 0){
//          System.out.println(qbfSolverLocation.getName());
//        }


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
                        "please point the variable to a qbf binary");
                System.exit(-1);
            }
            else{
                e.printStackTrace();
            }

        }

//        System.out.println("HHHHH");
//        InputStream is = proc.getInputStream();
//        InputStreamReader isr = new InputStreamReader(is);
//        BufferedReader br = new BufferedReader(isr);
//
//        String line;
//
//        try {
//            while ((line = br.readLine()) != null) {
//                solverText += line + "\n";
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        finally{
//            try{
//                isr.close();
//                br.close();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//        }



        try {
            if(withTimeout){
                proc.waitFor(SOLVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            else{
                proc.waitFor();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return handleExitCode(proc);
    }
	
	public boolean isSatisfiable (File dimacsLocation) throws QBFSolverException{
        this.qbfFile = dimacsLocation;
        this.solverText = "";
        return isSatisfiable(dimacsLocation,0,true);
	}


	private boolean handleExitCode(Process proc) throws QBFSolverException {
		int exitValue = -1; 

		try{
			exitValue = proc.exitValue();
		}
		catch(IllegalThreadStateException t){
            //System.out.println("Process timed out solving");
            //t.printStackTrace();
		}
		finally{
			proc.destroy();
		}
	
		boolean result;
		if(exitValue == 10){
			result = true;
		}
		else if(exitValue == 20){
			result = false;
		}
		else{
            //Try the next solver
            return isSatisfiable(qbfFile,++solverIndex,withTimeout);
		}

        //qbfFile.delete();
		if(qbfFile.exists()){
			System.out.println("WARNING: QBF File not deleted");
		}

        System.out.println(result);
        return  result;

	}
	
	public static void main(String[] args) throws IOException, QBFSolverException {

		QBFSolver solver = new QBFSolver();
        System.out.println(solver.isSatisfiable(new File(ModulePaths.getResultLocation() + "/qbf-slow/qbf1707796416043093684.qdimacs")));

	}




}
