package uk.ac.liv.moduleextraction.qbf;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.formula.PropositionalFormula;
import uk.ac.liv.propositional.nSeparability.ClauseStore;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;
import uk.ac.liv.propositional.nSeparability.nEntityConvertor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class nElementQBFProblemGenerator {

    private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private final Collection<OWLLogicalAxiom> ontology;
    private final HashSet<OWLEntity> signature;
    private final HashSet<OWLEntity> classesNotInSignature;
    private final int DOMAIN_SIZE;
    private static LoadingCache<Integer,nAxiomToClauseStore> convertors;
    private nAxiomToClauseStore mapper;

    private HashSet<int[]> clauses;
    private HashSet<Integer> variables;
    private HashSet<Integer> freshVariables;
    private nEntityConvertor entityUnderAllInterpreations;

    private HashSet<Integer> existentialVariables;
    private HashSet<Integer> universalVariables;

    private boolean isUnsatisfiable = false;

    public nElementQBFProblemGenerator(nAxiomToClauseStore clauseStoreMapping, Collection<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) throws IOException, ExecutionException {
        this.DOMAIN_SIZE = clauseStoreMapping.getDomainSize();
        this.ontology = ontology;
        this.signature = new HashSet<OWLEntity>(signatureAndSigM);
        this.classesNotInSignature = new HashSet<OWLEntity>();
        this.entityUnderAllInterpreations = new nEntityConvertor(DOMAIN_SIZE);
        this.clauses = new HashSet<int[]>();
        this.variables = new HashSet<Integer>();
        this.freshVariables = new HashSet<Integer>();
        this.existentialVariables = new HashSet<Integer>();
        this.universalVariables = new HashSet<Integer>();
        this.mapper = clauseStoreMapping;
        populateSignatures();
        collectClausesAndVariables();
        generateQBFProblem();
    }

    public boolean isUnsatisfiable(){
        return isUnsatisfiable;
    }

    public boolean convertedClausesAreEmpty(){
        return clauses.isEmpty();
    }

    public HashSet<Integer> getUniversalVariables() {
        return universalVariables;
    }

    public HashSet<Integer> getExistentialVariables() {
        return existentialVariables;
    }

    public HashSet<int[]> getClauses() {
        return clauses;
    }

    private void populateSignatures() {
        Set<OWLEntity> ontologyEntities = ModuleUtils.getClassAndRoleNamesInSet(ontology);

        signature.retainAll(ontologyEntities);
        signature.remove(factory.getOWLThing());
        signature.remove(factory.getOWLNothing());

        classesNotInSignature.addAll(ontologyEntities);
        classesNotInSignature.removeAll(signature);

		/* Remove Top and Bottom classes */
        classesNotInSignature.remove(factory.getOWLThing());
        classesNotInSignature.remove(factory.getOWLNothing());
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
            }
        }
    }

    public void generateQBFProblem() throws IOException{
        writeUniversalQuantifiers();
        writeExistentialQuantifiers();
    }


    private void writeUniversalQuantifiers() throws IOException{
        for(OWLEntity sigEnt : signature){
            for(PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)){
                Integer entValue = mapper.lookupMapping(ent);
                if(!(entValue == null)){
                    universalVariables.add(entValue);
                }
            }
        }
    }

    private void writeExistentialQuantifiers() throws IOException{
        for(OWLEntity sigEnt : classesNotInSignature){
            for(PropositionalFormula ent : sigEnt.accept(entityUnderAllInterpreations)){
                Integer entValue = mapper.lookupMapping(ent);
                if(!(entValue == null)){
                    existentialVariables.add(entValue);
                }
            }
        }
        for(Integer fresh : freshVariables){
            existentialVariables.add(fresh);
        }
    }

    

}
