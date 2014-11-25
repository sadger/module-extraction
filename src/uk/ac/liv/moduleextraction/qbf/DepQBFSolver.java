package uk.ac.liv.moduleextraction.qbf;


import depqbf4j.DepQBF4J;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class DepQBFSolver {


    private final Collection<Integer> universal;
    private final Collection<Integer> existential;
    private final Set<int[]> clauses;

    public DepQBFSolver(Collection<Integer> universal, Collection<Integer> existential, Set<int[]> clauses) {
        this.universal = universal;
        this.existential = existential;
        this.clauses = clauses;
        constructQBFProblem();
    }

    private void constructQBFProblem(){
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

    public boolean isSatisfiable() throws QBFSolverException, IOException {
        byte exitCode  = DepQBF4J.sat();
        switch (exitCode){
            case DepQBF4J.RESULT_SAT:
                return true;
            case DepQBF4J.RESULT_UNSAT:
                return false;
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
    }

    public void delete(){
        DepQBF4J.delete();
    }
}
