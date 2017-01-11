package uk.ac.liv.moduleextraction.extractor;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.HashSet;
import java.util.Set;



public abstract class AbstractHybridExtractor implements Extractor{

    Set<OWLLogicalAxiom> module;

    AbstractHybridExtractor(Set<OWLLogicalAxiom> ont){
        this.module = ont;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {

        //Immutable copy in case extractors modify signature
        ImmutableSet<OWLEntity> immutableSig = ImmutableSet.copyOf(signature);

        module = extractUsingFirstExtractor(new HashSet<>(immutableSig));
        int prevSize = module.size();
        do {
            module = extractUsingSecondExtractor(new HashSet<>(immutableSig));
            if(module.size() < prevSize){
                prevSize = module.size();
                module = extractUsingFirstExtractor(new HashSet<>(immutableSig));
            }
        } while(prevSize != module.size());

        return module;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        return null;
    }

    abstract Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature);
    abstract Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature);

}

