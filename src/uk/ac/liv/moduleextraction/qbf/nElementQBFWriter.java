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
import uk.ac.liv.propositional.nSeparability.nAxiomToSATMapping;
import uk.ac.liv.propositional.nSeparability.nEntityConvertor;
import uk.ac.liv.propositional.satclauses.SATSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    private static LoadingCache<Integer,nAxiomToSATMapping> convertors;
    private nAxiomToSATMapping mapper;
    private ArrayList<SATSet> clauses;
    private HashSet<Integer> variables;
    private nEntityConvertor entityUnderAllInterpreations;

    public nElementQBFWriter(int domainSize, Set<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) throws IOException {
        this.DOMAIN_SIZE = domainSize;
        File directoryToWrite = new File("/tmp/");
        this.qbfFile = File.createTempFile("qbf", ".qdimacs",directoryToWrite);
        this.ontology = ontology;
        this.signature = new HashSet<OWLEntity>(signatureAndSigM);
        this.classesNotInSignature = new HashSet<OWLEntity>();
        this.entityUnderAllInterpreations = new nEntityConvertor(domainSize);
        this.clauses = new ArrayList<SATSet>();
        this.variables = new HashSet<Integer>();
        if (convertors == null){
            convertors = CacheBuilder.newBuilder().
                    build(new CacheLoader<Integer, nAxiomToSATMapping>() {
                        @Override
                        public nAxiomToSATMapping load(Integer i){
                            return new nAxiomToSATMapping(i);
                        }
                    });
        }
        try {
            this.mapper = convertors.get(DOMAIN_SIZE);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        collectAndCountQBFClauses();
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

    private void collectAndCountQBFClauses() {
        for(OWLLogicalAxiom axiom: ontology){
            ArrayList<SATSet> converted = mapper.convertAxiom(axiom);;
            for(SATSet clause : converted){
                variables.addAll(clause.getVariables());
                clauses.add(clause);
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
            for (OWLEntity ent : signature) {
                for (PropositionalFormula underEach : ent.accept(entityUnderAllInterpreations)) {
                    Integer mapping = mapper.lookupMapping(underEach);
                    if (mapping != null) {
                        writer.write(mapping + " ");
                    }
                }
            }
            writer.write("0");
            writer.newLine();
        }
    }

    private void writeExistentialQuantifiers(BufferedWriter writer) throws IOException{
        if(!classesNotInSignature.isEmpty()){
            writer.write("e ");
            for(OWLEntity ent : classesNotInSignature){
                    for (PropositionalFormula underEach : ent.accept(entityUnderAllInterpreations)) {
                        Integer mapping = mapper.lookupMapping(underEach);
                        if (mapping != null) {
                            writer.write(mapping + " ");
                        }
                    }
            }
            writer.write("0");
            writer.newLine();
        }
    }

    private void writeClauses(BufferedWriter writer) throws IOException{
        for(SATSet s : clauses){
            for(int i : s){
                writer.write(i  + " ");
            }
            writer.write("0");
            writer.newLine();
        }
    }

    

    public static void main(String[] args) {
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Profile/NCI-star.owl");
        Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(), 500);
        System.out.println("LOADED");
        OWLDataFactory f = ont.getOWLOntologyManager().getOWLDataFactory();
//        ModuleUtils.remapIRIs(ont,"X");
        HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLClass b = f.getOWLClass(IRI.create("X#B"));
        OWLClass c = f.getOWLClass(IRI.create("X#C"));
        OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));
        signature.add(a);
        signature.add(r);
        try {
            new nElementQBFWriter(2,subset,signature);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
