package uk.ac.liv.moduleextraction.propositional.nSeparability;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.propositional.formula.NamedAtom;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;
import uk.ac.liv.moduleextraction.propositional.satclauses.NumberMap;

import java.util.concurrent.ExecutionException;

/**
 * Created by william on 17/11/14.
 */
public class nAxiomToClauseStore {

    private LoadingCache<OWLLogicalAxiom, ClauseStore> axiomMapping;
    private static NumberMap numberMap = new NumberMap();
    private final int DOMAIN_SIZE;

    public nAxiomToClauseStore(int domain_size) {
        this.DOMAIN_SIZE = domain_size;
        final nAxiomConvertor convertor = new nAxiomConvertor(domain_size);
        if (axiomMapping == null) {
            axiomMapping = CacheBuilder.newBuilder().build(new CacheLoader<OWLLogicalAxiom, ClauseStore>() {
                @Override
                public ClauseStore load(OWLLogicalAxiom owlLogicalAxiom) throws Exception {
                    PropositionalFormula propAxiom = owlLogicalAxiom.accept(convertor);
                    return new ClauseStore(propAxiom,numberMap);
                }
            });
        }
    }

    public int getDomainSize(){
        return DOMAIN_SIZE;
    }

    public ClauseStore convertAxiom(OWLLogicalAxiom axiom){
        ClauseStore converted = null;
        try {
            converted = axiomMapping.get(axiom);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return converted;
    }

    public static NumberMap getNumberMap() {
        return numberMap;
    }

    public Integer lookupMapping(PropositionalFormula literal) {
        return numberMap.get(literal);
    }

    public void updateMapping(NamedAtom literal){
        numberMap.updateNumberMap(literal);
    }



}
