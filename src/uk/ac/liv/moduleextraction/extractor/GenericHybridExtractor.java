package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.Set;

/**
 * Created by william on 30/09/16.
 */
public abstract class GenericHybridExtractor implements Extractor{



    Set<OWLLogicalAxiom> module;

    GenericHybridExtractor(Set<OWLLogicalAxiom> ont){
        this.module = ont;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
        module = extractUsingFirstExtractor(signature);
        int prevSize = module.size();
        do {
            module = extractUsingSecondExtractor(signature);
            if(module.size() < prevSize){
                prevSize = module.size();
                module = extractUsingFirstExtractor(signature);
            }
        }while(prevSize != module.size());

        return module;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        return null;
    }

    abstract Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature);
    abstract Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature);

}
