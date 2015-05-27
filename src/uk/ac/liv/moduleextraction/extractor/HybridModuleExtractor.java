package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter.RemovalMethod;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HybridModuleExtractor implements Extractor {

	private OWLOntologyManager manager;
	private int starExtractions = 0;
	private int amexExtrations = 0;
	private OntologyCycleVerifier cycleVerifier;
	private CycleRemovalMethod method;

	private Set<OWLLogicalAxiom> module;

	private ArrayList<ExtractionMetric> iterationMetrics;
	private Stopwatch hybridWatch;
	private Set<OWLLogicalAxiom> axioms;

	public enum CycleRemovalMethod{
		NAIVE,
		IMPROVED,
	}
	
	public HybridModuleExtractor(OWLOntology ont) {
		this(ont.getLogicalAxioms());
	}

	public HybridModuleExtractor(Set<OWLLogicalAxiom> axioms){
		this.method = CycleRemovalMethod.NAIVE;
		this.manager = OWLManager.createOWLOntologyManager();
		this.iterationMetrics = new ArrayList<ExtractionMetric>();
		this.axioms = axioms;
	}
	
	public CycleRemovalMethod getCycleRemovalMethod(){
		return method;
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		hybridWatch = new Stopwatch().start();
		Set<OWLEntity> origSig = new HashSet<OWLEntity>(signature);
		module = extractStarModule(axioms, signature);
		boolean sizeChanged = false;
		do{
			int starSize = module.size();

			Set<OWLLogicalAxiom> unsupportedAxioms = getUnsupportedAxioms(module);
			module.removeAll(unsupportedAxioms);

			cycleVerifier = new OntologyCycleVerifier(module);
			if(cycleVerifier.isCyclic()){
				Set<OWLLogicalAxiom> cycleCausing = null;
				
				if(method == CycleRemovalMethod.NAIVE){
					 cycleCausing = cycleVerifier.getCycleCausingAxioms();
				}
				else if(method == CycleRemovalMethod.IMPROVED){
					cycleCausing =  cycleVerifier.getBetterCycleCausingAxioms();
				}
				
				unsupportedAxioms.addAll(cycleCausing);
				module.removeAll(cycleCausing);
			}
			
			module  = extractSemanticModule(createOntologyFromLogicalAxioms(module), unsupportedAxioms, origSig);


			if(module.size() < starSize){
				int amexSize = module.size();
				module = extractStarModule(module, origSig);
				sizeChanged = (module.size() < amexSize);
				

			}
			else{
				sizeChanged = false;
			}

		}while(sizeChanged);

		hybridWatch.stop();
		return module;
	}


	private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms){
		OntologyFilters filters = new OntologyFilters();
		AxiomStructureInspector inspector = new AxiomStructureInspector(axioms);
		filters.addFilter(new SupportedExpressivenessFilter());
		filters.addFilter(new SharedNameFilter(inspector,RemovalMethod.REMOVE_EQUALITIES));
		filters.addFilter(new RepeatedEqualitiesFilter(inspector));
		return filters.getUnsupportedAxioms(axioms);
	}

	@Override	
	public Set<OWLLogicalAxiom> extractModule(
			Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		return null;
	}

	public Set<OWLLogicalAxiom> extractStarModule(Set<OWLLogicalAxiom> axioms, Set<OWLEntity> signature){
		STARExtractor starExtractor = new STARExtractor(axioms);
		Set<OWLLogicalAxiom> module = starExtractor.extractModule(signature);
		starExtractions++;
		iterationMetrics.add(starExtractor.getMetrics());
		return module;
	}

	private OWLOntology createOntologyFromLogicalAxioms(Set<OWLLogicalAxiom> axioms){
		Set<OWLAxiom> newOntAxioms = new HashSet<OWLAxiom>();
		newOntAxioms.addAll(axioms);
		OWLOntology ont = null;
		try {
			ont = manager.createOntology(newOntAxioms);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		return ont;
	}


	private Set<OWLLogicalAxiom> extractSemanticModule(OWLOntology inputOnt, Set<OWLLogicalAxiom> existingmodule, Set<OWLEntity> signature){
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(inputOnt);
		Set<OWLLogicalAxiom> module = extractor.extractModule(existingmodule, signature);
		manager.removeOntology(inputOnt);
		amexExtrations++;
		//		System.out.println("AMEX: " + module.size());
		iterationMetrics.add(extractor.getMetrics());
		return module;
	}


	public ArrayList<ExtractionMetric> getIterationMetrics() {
		return iterationMetrics;
	}

	public ExtractionMetric getMetrics(){
		ExtractionMetric.MetricBuilder builder = new ExtractionMetric.MetricBuilder(ExtractionMetric.ExtractionType.HYBRID);
		builder.moduleSize(module.size());
		builder.timeTaken(hybridWatch.elapsed(TimeUnit.MILLISECONDS));
		return builder.createMetric();
	}

	public int getStarExtractions() {
		return starExtractions;
	}
	public int getAmexExtrations() {
		return amexExtrations;
	}


}
