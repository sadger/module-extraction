package uk.ac.liv.moduleextraction.qbf;

import depqbf4j.DepQBF4J;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.formula.NamedAtom;
import uk.ac.liv.propositional.formula.PropositionalFormula;
import uk.ac.liv.propositional.nSeparability.ClauseStore;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;
import uk.ac.liv.propositional.nSeparability.nEntityConvertor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by william on 27/11/14.
 */
public class IncrementalSeparabilityAxiomLocator extends NElementSeparabilityAxiomLocator{

    private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private static nAxiomToClauseStore store;
    private static nEntityConvertor entityUnderAllInterpreations;
    private final Set<OWLEntity> signature;
    private OWLLogicalAxiom[] axiomList;
    private final HashSet<OWLEntity> classesNotInSignature = new HashSet<OWLEntity>();

    public IncrementalSeparabilityAxiomLocator(nAxiomToClauseStore clauseStore, OWLLogicalAxiom[] subsetAsArray, Set<OWLEntity> sigUnionSigM) {
        super(clauseStore,subsetAsArray,sigUnionSigM);
        this.axiomList = subsetAsArray;
        this.signature = sigUnionSigM;
        //This SHOULD be cached in order to fetch clauses for the same axioms
        this.store = new nAxiomToClauseStore(clauseStore.getDomainSize());
        this.entityUnderAllInterpreations = new nEntityConvertor(clauseStore.getDomainSize());

        DepQBF4J.create();
        DepQBF4J.configure("--dep-man=simple");
        DepQBF4J.configure("--incremental-use");
        writeQuantifiers();
        populateSignatures();

    }

    private void writeQuantifiers() {

       // System.out.println("Sig size: " + signature.size());
        DepQBF4J.newScopeAtNesting(DepQBF4J.QTYPE_FORALL, 1);
        for(OWLEntity sigEnt : signature){
            for(PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)){
                if(!sigEnt.isTopEntity() && !sigEnt.isBottomEntity()){
                    store.updateMapping((NamedAtom) ent);
                    Integer entValue = store.lookupMapping(ent);
                    if(!(entValue == null)){
                        DepQBF4J.add(entValue);
                    }
                }
             
            }
        }
        //Close universal scope
        DepQBF4J.add(0);

        DepQBF4J.newScopeAtNesting(DepQBF4J.QTYPE_EXISTS, 2);
        for(OWLEntity sigEnt : classesNotInSignature) {
            for (PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)) {
                store.updateMapping((NamedAtom) ent);
                Integer entValue = store.lookupMapping(ent);
                if (!(entValue == null)) {
                    DepQBF4J.add(entValue);
                }
            }
        }
        for(OWLLogicalAxiom ax : axiomList){
            ClauseStore st = store.convertAxiom(ax);
            for(int fresh : st.getFreshVariables()){
                DepQBF4J.add(fresh);
            }
        }
        //Close existential scope
        DepQBF4J.add(0);

    }

    private void populateSignatures() {
        Set<OWLEntity> ontologyEntities = ModuleUtils.getClassAndRoleNamesInSet(new HashSet<OWLLogicalAxiom>(Arrays.asList(axiomList)));

        signature.retainAll(ontologyEntities);
        signature.remove(factory.getOWLThing());
        signature.remove(factory.getOWLNothing());

        classesNotInSignature.addAll(ontologyEntities);
        classesNotInSignature.removeAll(signature);

		/* Remove Top and Bottom classes */
        classesNotInSignature.remove(factory.getOWLThing());
        classesNotInSignature.remove(factory.getOWLNothing());
    }



    public OWLLogicalAxiom findSeparabilityCausingAxiom() throws IOException, QBFSolverException {


        OWLLogicalAxiom[] lastAdded = getTopHalf(axiomList);
        OWLLogicalAxiom[] lastRemoved = getBottomHalf(axiomList);

        OWLLogicalAxiom[] W = lastAdded;

        addClauses(W);

        while(lastAdded.length > 0){
            if(isSatisfiable()){
                //Push more clauses - top half of last removed
                lastAdded = getTopHalf(lastRemoved);
                W = concat(W,lastAdded);
                addClauses(lastAdded);
                lastRemoved = Arrays.copyOfRange(lastRemoved,lastAdded.length,lastRemoved.length);
            }
            else{
                //Pop clauses - bottom half of last added
                lastRemoved = getBottomHalf(lastAdded);
                W = Arrays.copyOfRange(W,0,W.length - lastRemoved.length);
                removeClauses(lastRemoved);
                lastAdded = Arrays.copyOfRange(lastAdded, 0,lastAdded.length - lastRemoved.length);
            }
        }

        DepQBF4J.delete();

        if(lastRemoved.length != 1){
            System.out.println("OH MY LORD AN ERROR");
        }
        System.out.println(axiomList[W.length]);
        return lastRemoved[0];
    }

    private void addClauses(OWLLogicalAxiom[] toAdd){
        for(OWLLogicalAxiom ax :toAdd){
            ClauseStore st = store.convertAxiom(ax);
            for(int[] clause : st.getClauses()){
                DepQBF4J.push();
                for(int v : clause){
                    DepQBF4J.add(v);
                }
                DepQBF4J.add(0);
            }
        }
    }

    private void removeClauses(OWLLogicalAxiom[] toAdd){
        for(OWLLogicalAxiom ax :toAdd){
            ClauseStore st = store.convertAxiom(ax);
            int clauseSize = st.getClauses().size();
            for (int i = 0; i < clauseSize; i++) {
                DepQBF4J.pop();
            }
        }
    }

    private boolean isSatisfiable() throws QBFSolverException, IOException {
        byte exitCode  = DepQBF4J.sat();
        DepQBF4J.reset();
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



    public static void main(String[] args) throws IOException, QBFSolverException, ExecutionException {
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/simple.krss");
        ModuleUtils.remapIRIs(ont,"X");
        Set<OWLLogicalAxiom> axioms = ont.getLogicalAxioms();
        OWLDataFactory fact = ont.getOWLOntologyManager().getOWLDataFactory();
        Set<OWLEntity> sig = new HashSet<OWLEntity>();
        OWLClass a = fact.getOWLClass(IRI.create("X#A"));
        OWLClass b = fact.getOWLClass(IRI.create("X#B"));
        sig.add(a);
        sig.add(b);

        new Testing().doThings(axioms,sig);


    }

}
