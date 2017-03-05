package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public abstract class AbstractHybridExtractor implements Extractor{

    protected Set<OWLLogicalAxiom> module;
    private ArrayList<ExtractionMetric> iterationMetrics;
    private Stopwatch hybridWatch;
    private Set<OWLLogicalAxiom> axioms;

    AbstractHybridExtractor(Set<OWLLogicalAxiom> ont){
        iterationMetrics = new ArrayList<>();
        this.axioms = ont;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
        this.module = new HashSet<>(axioms);
        //Immutable copy in case extractors modify signature
        ImmutableSet<OWLEntity> immutableSig = ImmutableSet.copyOf(signature);

        hybridWatch = Stopwatch.createStarted();

        module = extractUsingFirstExtractor(new HashSet<>(immutableSig));
        int prevSize = module.size();
        do {
            module = extractUsingSecondExtractor(new HashSet<>(immutableSig));
            if(module.size() < prevSize){
                prevSize = module.size();
                module = extractUsingFirstExtractor(new HashSet<>(immutableSig));
            }
        } while(prevSize != module.size());

        hybridWatch.stop();

        return module;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        return null;
    }

    public ExtractionMetric getMetrics(){
        ExtractionMetric.MetricBuilder builder = new ExtractionMetric.MetricBuilder(ExtractionMetric.ExtractionType.HYBRID);
        builder.moduleSize(module.size());
        builder.timeTaken(hybridWatch.elapsed(TimeUnit.MILLISECONDS));
        return builder.createMetric();
    }


    abstract Set<OWLLogicalAxiom> extractUsingFirstExtractor(Set<OWLEntity> signature);
    abstract Set<OWLLogicalAxiom> extractUsingSecondExtractor(Set<OWLEntity> signature);

}

