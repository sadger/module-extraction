package uk.ac.liv.moduleextraction.qbf;


import com.google.common.collect.Sets;
import depqbf4j.DepQBF4J;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DepQBFSolver {

    private static int i = 0;
    private final Collection<Integer> universal;
    private final Collection<Integer> existential;
    private final Set<int[]> clauses;
    private File qbfFile;

    public DepQBFSolver(Collection<Integer> universal, Collection<Integer> existential, Set<int[]> clauses) {
        this.universal = universal;
        this.existential = existential;
        this.clauses = clauses;
        constructQBFProblem();
    }

    private void constructQBFProblem() {

        qbfFile = null;
        try {
            qbfFile = File.createTempFile("qbf", ".qdimacs", new File("/tmp/"));
            qbfFile.createNewFile();
            //System.out.println(qbfFile.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(new FileWriter(qbfFile));

            //The value of the variables needs to be as big as the numerical value of largest variable even if all other
            //lower values don't exist (I hope this doesn't break anything)
            int maxExists = (existential.size() == 0) ? 0 : Collections.max(existential);
            int maxUniversal = (universal.size() == 0) ? 0 : Collections.max(universal);

            int variables = Math.max((universal.size() + existential.size()), Math.max(maxExists,maxUniversal));


            bw.write("p cnf " + variables + " " + clauses.size() + "\n");

            //Universal
            bw.write("a ");
            for(Integer a : universal){
                bw.write(a + " ");
            }
            bw.write(0 + "\n");

            //Existential
            bw.write("e ");
            for(Integer e : existential){
                bw.write(e + " ");
            }
            bw.write(0 + "\n");

            for(int[] clause : clauses){
                for(int i : clause){
                    bw.write(i + " ");
                }
                bw.write(0 + "\n");
            }

            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }




    }


    public boolean isSatisfiable() throws QBFSolverException, IOException {
        QBFSolver solver = new QBFSolver();
        return solver.isSatisfiable(qbfFile);
    }

    public boolean DEPQBFisSatisfiable() throws QBFSolverException, IOException {
        boolean returnCode;
        byte exitCode  = DepQBF4J.sat();
        switch (exitCode){
            case DepQBF4J.RESULT_SAT:
                returnCode = true;
                break;
            case DepQBF4J.RESULT_UNSAT:
                returnCode = false;
                break;
            default:
                if(exitCode == DepQBF4J.RESULT_UNKNOWN){
                    System.err.println("The result is unknown");
                }
                else{
                    System.err.println("There was an error with the QBF solver, EXIT CODE " + exitCode);
                }
                File qbf = File.createTempFile("qbf",".qdimacs",new File("/tmp/"));
                DepQBF4J.printToFile(qbf.getAbsolutePath());
                System.err.println("QBF file dumped to: " + qbf.getAbsolutePath());
                throw new QBFSolverException();

        }
        return returnCode;

    }

    private void DEPQBFconstructQBFProblem(){
        int nestingLevel = 1;
        DepQBF4J.create();
        DepQBF4J.configure("--dep-man=simple");

        if(!universal.isEmpty()){
            DepQBF4J.newScopeAtNesting(DepQBF4J.QTYPE_FORALL, nestingLevel++);
            for(int forall : universal){
                DepQBF4J.add(forall);
            }
            DepQBF4J.add(0);
        }

        if(!existential.isEmpty()){
            DepQBF4J.newScopeAtNesting(DepQBF4J.QTYPE_EXISTS, nestingLevel++);
            for(int exists : existential){
                DepQBF4J.add(exists);
            }
            DepQBF4J.add(0);
        }

        for(int[] clause : clauses){
            for(int val : clause){
                DepQBF4J.add(val);
            }
            DepQBF4J.add(0);
        }
    }


    public void delete(){
        DepQBF4J.delete();
    }

    public static void main(String[] args) {

        HashSet<Integer> universal = Sets.newHashSet(1);
        HashSet<Integer> exist = Sets.newHashSet(2,3);

        int[] clause1 = {1, 2};
        int[] clause2 = {2, -3};

        HashSet<int[]> clauses = new HashSet<int[]>();
        clauses.add(clause1);
        clauses.add(clause2);

        DepQBFSolver solver = new DepQBFSolver(universal,exist,clauses);


    }
}
