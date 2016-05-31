package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.qbf.DepQBFSolver;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.nElementQBFProblemGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by william on 23/05/16.
 */
public class NominalTestingArea {

    public static void main(String[] args) throws IOException, ExecutionException, QBFSolverException {


        OWLOntology nom = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/filter/nom.owl");




        ArrayList<OWLEntity> entities = new ArrayList<>(ModuleUtils.getSignatureOfAxioms(nom.getLogicalAxioms()));
        HashSet<OWLEntity> signature = Stream.of(entities.get(1)).collect(Collectors.toCollection(HashSet::new));

        System.out.println("Signature: " + signature);


        int DOMAIN_SIZE = 2;
        nAxiomToClauseStore store = new nAxiomToClauseStore(DOMAIN_SIZE);

        nom.getLogicalAxioms().forEach(store::convertAxiom);
        nom.getLogicalAxioms().forEach(System.out::println);

        nElementQBFProblemGenerator gen = new nElementQBFProblemGenerator(store,nom.getLogicalAxioms(),signature);

        System.out.println(store.getNumberMap());


        System.out.println("A: " + gen.getUniversalVariables());
        System.out.println("E: " + gen.getExistentialVariables());
        for(int[] clause :gen.getClauses()){
            System.out.println(Arrays.toString(clause));
        }

        DepQBFSolver solver = new DepQBFSolver(gen.getUniversalVariables(),gen.getExistentialVariables(),gen.getClauses());
        System.out.println("SAT: " + solver.isSatisfiable());





    }
}
