package uk.ac.liv.moduleextraction.qbf;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class QBFProblemWriter {

    private Collection<Integer> universal;
    private Collection<Integer> existential;
    private final Set<int[]> clauses;
    private File qbfFile;
    private HashMap<Integer, Integer> remapping;

    public QBFProblemWriter(Collection<Integer> universal, Collection<Integer> existential, Set<int[]> clauses) {
        this.universal = universal;
        this.existential = existential;
        this.clauses = clauses;
        this.remapping = new HashMap<>(universal.size() + existential.size());
        remapNumbering();
        constructQBFProblem();
    }

    /* Map variables contiguously from 1, otherwise some QBF solvers complain */
    private void remapNumbering(){

        int startingNumber = 1;
        for(Integer a : universal){
            remapping.put(a, startingNumber);
            startingNumber++;
        }
        for(Integer e : existential){
            remapping.put(e, startingNumber);
            startingNumber++;
        }
    }


    private void constructQBFProblem() {

        qbfFile = null;
        try {
            qbfFile = File.createTempFile("qbf", ".qdimacs", new File("/tmp/"));
            //System.out.println(qbfFile.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(new FileWriter(qbfFile));


            int variables = universal.size() + existential.size();


            bw.write("p cnf " + variables + " " + clauses.size() + "\n");

            //Universal
            bw.write("a ");
            for(Integer a : universal){
                bw.write(remapping.get(a) + " ");
            }
            bw.write(0 + "\n");

            //Existential
            bw.write("e ");
            for(Integer e : existential){
                bw.write(remapping.get(e) + " ");
            }
            bw.write(0 + "\n");

            for(int[] clause : clauses){
                for(int i : clause){
                    if(i > 0){
                        bw.write(remapping.get(i) + " ");
                    }
                    else{
                        bw.write(-remapping.get(Math.abs(i)) + " ");
                    }

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


}
