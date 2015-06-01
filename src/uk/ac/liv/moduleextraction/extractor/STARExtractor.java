package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper for STAR extraction, implementing the Extractor interface
 */
public class STARExtractor implements Extractor {

    private final OWLOntology ontology;
    private OWLOntologyManager manager;
    private Set<OWLLogicalAxiom> module;
    private Stopwatch starWatch;

    public STARExtractor(OWLOntology ontology){
        this.ontology = ontology;
    }

    public STARExtractor(Set<OWLLogicalAxiom> axioms){
        manager = OWLManager.createOWLOntologyManager();
        Set<OWLAxiom> newOntAxioms = new HashSet<OWLAxiom>();
        newOntAxioms.addAll(axioms);
        OWLOntology ont = null;
        try {
            ont = manager.createOntology(newOntAxioms);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        this.ontology = ont;
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
        starWatch = Stopwatch.createStarted();
        SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
        Set<OWLAxiom> starModule = extractor.extract(signature);
        starWatch.stop();

        //Because STAR can work with annotations etc. we extract logical ones only for a fair comparison of size
        module = ModuleUtils.getLogicalAxioms(starModule);

        return module;
    }

    public ExtractionMetric getMetrics(){
        ExtractionMetric.MetricBuilder builder = new ExtractionMetric.MetricBuilder(ExtractionMetric.ExtractionType.STAR);
        builder.moduleSize(module.size());
        builder.timeTaken(starWatch.elapsed(TimeUnit.MILLISECONDS));
        builder.qbfChecks(0);
        builder.syntacticChecks(0);
        builder.separabilityCausingAxioms(0);
        return builder.createMetric();
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        return null;
    }
}
