package uk.ac.liv.moduleextraction.qbf;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.formula.PropositionalFormula;
import uk.ac.liv.propositional.nSeparability.ClauseStore;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;
import uk.ac.liv.propositional.nSeparability.nEntityConvertor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by william on 04/11/14.
 */
public class nElementQBFWriter {

    private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private final File qbfFile;
    private final Set<OWLLogicalAxiom> ontology;
    private final HashSet<OWLEntity> signature;
    private final HashSet<OWLEntity> classesNotInSignature;
    private final int DOMAIN_SIZE;
    private static LoadingCache<Integer,nAxiomToClauseStore> convertors;
    private nAxiomToClauseStore mapper;

    private HashSet<int[]> clauses;
    private HashSet<Integer> variables;
    private HashSet<Integer> freshVariables;
    private nEntityConvertor entityUnderAllInterpreations;

    private boolean isUnsatisfiable = false;

    public nElementQBFWriter(int domainSize, Set<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) throws IOException, ExecutionException {
        this.DOMAIN_SIZE = domainSize;
        File directoryToWrite = new File("/tmp/");
        this.qbfFile = File.createTempFile("qbf", ".qdimacs",directoryToWrite);
        this.ontology = ontology;
        this.signature = new HashSet<OWLEntity>(signatureAndSigM);
        this.classesNotInSignature = new HashSet<OWLEntity>();
        this.entityUnderAllInterpreations = new nEntityConvertor(domainSize);
        this.clauses = new HashSet<int[]>();
        this.variables = new HashSet<Integer>();
        this.freshVariables = new HashSet<Integer>();
        if (convertors == null){
            convertors = CacheBuilder.newBuilder().
                    build(new CacheLoader<Integer, nAxiomToClauseStore>() {
                        @Override
                        public nAxiomToClauseStore load(Integer i){
                            return new nAxiomToClauseStore(i);
                        }
                    });
        }

        this.mapper = convertors.get(DOMAIN_SIZE);
        collectClausesAndVariables();
        populateSignatures();
        generateQBFProblem();
    }

    private void populateSignatures() {
        Set<OWLEntity> ontologyEntities = ModuleUtils.getClassAndRoleNamesInSet(ontology);

        signature.retainAll(ontologyEntities);

        classesNotInSignature.addAll(ontologyEntities);
        classesNotInSignature.removeAll(signature);

		/* Remove Top and Bottom classes */
        classesNotInSignature.remove(factory.getOWLThing());
        classesNotInSignature.remove(factory.getOWLNothing());
    }

    private boolean isUnsatisfiable(){
        return isUnsatisfiable;
    }

    private boolean convertedClausesAreEmpty(){
        return clauses.isEmpty();
    }

    private void collectClausesAndVariables() {
        for(OWLLogicalAxiom axiom: ontology){
            ClauseStore clauseStore = mapper.convertAxiom(axiom);
            if(clauseStore.hasConstantValue()){
                boolean constantValue = clauseStore.getConstantValue();
                if(constantValue == false){
                    isUnsatisfiable = true;
                    break;
                }
                //Skip constant "true" clauses
            }
            else{
                clauses.addAll(clauseStore.getClauses());
                variables.addAll(clauseStore.getVariables());
                freshVariables.addAll(clauseStore.getFreshVariables());
                System.out.println(clauseStore);
            }
        }
    }

    public File generateQBFProblem() throws IOException{
        File qbf = createQBFFile();
        System.out.println();
        System.out.println("./sKizzo " + qbfFile.getAbsolutePath());
        ModuleUtils.printFile(qbfFile);
        return qbf;
    }

    private File createQBFFile() throws IOException {
        if (!qbfFile.exists()) {
            try {
                qbfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(qbfFile));

        try{
            writeHeaders(writer);
            writeUniversalQuantifiers(writer);
            writeExistentialQuantifiers(writer);
            writeClauses(writer);
        }
        finally{
            try {
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return qbfFile;
    }



    private void writeHeaders(BufferedWriter writer) throws IOException {
        writer.write("p cnf " + variables.size() + " " + clauses.size());
        writer.newLine();
    }

    private void writeUniversalQuantifiers(BufferedWriter writer) throws IOException{
        if(!signature.isEmpty()) {
            writer.write("a ");
            for(OWLEntity sigEnt : signature){
                for(PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)){
                    int entValue = mapper.lookupMapping(ent);
                    writer.write(entValue + " ");
                }
            }
            writer.write("0");
            writer.newLine();
        }
    }

    private void writeExistentialQuantifiers(BufferedWriter writer) throws IOException{
        if(!classesNotInSignature.isEmpty() || !freshVariables.isEmpty()){
            writer.write("e ");
            for(OWLEntity sigEnt : classesNotInSignature){
                for(PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)){
                    int entValue = mapper.lookupMapping(ent);
                    writer.write(entValue + " ");
                }
            }
            for(Integer fresh : freshVariables){
                writer.write(fresh + " ");
            }
            writer.write("0");
            writer.newLine();
        }
    }

    private void writeClauses(BufferedWriter writer) throws IOException{
        for(int[] clause : clauses){
            for(int var : clause){
                writer.write(var + " ");
            }
            writer.write("0");
            writer.newLine();
        }
    }

    

    public static void main(String[] args) {
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/simple.krss");

        System.out.println("LOADED");

        OWLDataFactory f = ont.getOWLOntologyManager().getOWLDataFactory();
        ModuleUtils.remapIRIs(ont,"X");
        for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
            System.out.println(ax);
        }

        HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLClass b = f.getOWLClass(IRI.create("X#B"));
        OWLClass c = f.getOWLClass(IRI.create("X#C"));
        OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));
        signature.add(a);
        signature.add(b);
        signature.add(r);

        try {
            new nElementQBFWriter(1,ont.getLogicalAxioms(),signature);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
