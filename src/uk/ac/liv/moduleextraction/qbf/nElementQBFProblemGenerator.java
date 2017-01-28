package uk.ac.liv.moduleextraction.qbf;

import com.google.common.cache.LoadingCache;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.propositional.formula.PropositionalFormula;
import uk.ac.liv.propositional.nSeparability.ClauseStore;
import uk.ac.liv.propositional.nSeparability.nAxiomToClauseStore;
import uk.ac.liv.propositional.nSeparability.nEntityConvertor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class nElementQBFProblemGenerator {

    private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
    private final Collection<OWLLogicalAxiom> ontology;
    private final HashSet<OWLEntity> signature;
    private final HashSet<OWLEntity> classesNotInSignature;
    private final int DOMAIN_SIZE;
    private static LoadingCache<Integer,nAxiomToClauseStore> convertors;
    private nAxiomToClauseStore mapper;

    private HashSet<int[]> clauses;
    private HashSet<Integer> freshVariables;
    private nEntityConvertor underAllInterpretations;

    private HashSet<Integer> existentialVariables;
    private HashSet<Integer> universalVariables;

    private boolean isUnsatisfiable = false;
    private Set<OWLEntity> ontologyEntities;

    public nElementQBFProblemGenerator(nAxiomToClauseStore clauseStoreMapping, Collection<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) throws IOException, ExecutionException {
        this.DOMAIN_SIZE = clauseStoreMapping.getDomainSize();
        this.ontology = ontology;
        this.signature = new HashSet<>(signatureAndSigM);
        this.classesNotInSignature = new HashSet<>();
        this.underAllInterpretations = new nEntityConvertor(DOMAIN_SIZE);
        this.clauses = new HashSet<>();
        this.freshVariables = new HashSet<>();
        this.existentialVariables = new HashSet<>();
        this.universalVariables = new HashSet<>();
        this.mapper = clauseStoreMapping;
        populateSignatures();
        collectClausesAndVariables();
        collectQuantifiers();
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
        //Class, roles, and named individuals (nominals)
        ontologyEntities = ModuleUtils.getSignatureOfAxioms(ontology);
        ontologyEntities.remove(factory.getOWLThing());
        ontologyEntities.remove(factory.getOWLNothing());

        //Discard any symbols not in the signature of the ontology
        signature.retainAll(ontologyEntities);

        //Sig(O) \ Σ
        classesNotInSignature.addAll(ontologyEntities);
        classesNotInSignature.removeAll(signature);

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
                freshVariables.addAll(clauseStore.getFreshVariables());
            }
        }

        /* Additional constraints for nominals in the ontology
          (not necessary in single element interpretations) */
        if(DOMAIN_SIZE > 1){
            ontologyEntities.stream()
                    .filter(e -> e.isOWLNamedIndividual())
                    .forEach(e -> clauses.addAll(getNominalConstraint((OWLNamedIndividual) e)));
        }
    }

    public HashSet<int[]> getNominalConstraint(OWLNamedIndividual indiv){
        HashSet<int[]> constraintClauses = new HashSet<>();
        ArrayList<PropositionalFormula> underAll = new ArrayList<>(indiv.accept(underAllInterpretations));
        System.out.println(underAll);
        //Pairwise disjointness = n(n-1)/2 clauses
        for(int i = 0; i <= DOMAIN_SIZE - 1 ; i++){
            for (int j = i+1; j <= DOMAIN_SIZE - 1 ; j++) {
                int[] constraint = {-mapper.lookupMapping(underAll.get(i)), -mapper.lookupMapping(underAll.get(j))};
                System.out.println(Arrays.toString(constraint));
                constraintClauses.add(constraint);
            }
        }
        return constraintClauses;
    }


    public void collectQuantifiers() throws IOException{
        writeUniversalQuantifiers();
        writeExistentialQuantifiers();
    }


    private void writeUniversalQuantifiers() throws IOException{
        for(OWLEntity sigEnt : signature){
            for(PropositionalFormula ent : sigEnt.accept(underAllInterpretations)){
                Integer entValue = mapper.lookupMapping(ent);
                if(!(entValue == null)){
                    universalVariables.add(entValue);
                }
            }
        }
    }

    private void writeExistentialQuantifiers() throws IOException{
        for(OWLEntity sigEnt : classesNotInSignature){
            for(PropositionalFormula ent : sigEnt.accept(underAllInterpretations)){
                Integer entValue = mapper.lookupMapping(ent);
                if(!(entValue == null)){
                    existentialVariables.add(entValue);
                }
            }
        }
        existentialVariables.addAll(freshVariables.stream().collect(Collectors.toSet()));
    }

    

}
