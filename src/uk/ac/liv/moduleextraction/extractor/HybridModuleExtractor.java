package uk.ac.liv.moduleextraction.extractor;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.experiments.OntologyFilters;
import uk.ac.liv.moduleextraction.experiments.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter.RemovalMethod;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HybridModuleExtractor implements Extractor {

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private int starExtractions = 0;
	private int amexExtrations = 0;
	private OntologyCycleVerifier cycleVerifier;
	private CycleRemovalMethod method;

	private ArrayList<ExtractionMetric> iterationMetrics;

	public enum CycleRemovalMethod{
		NAIVE,
		IMPROVED,
	}
	
	public HybridModuleExtractor(OWLOntology ont, CycleRemovalMethod method) {
		this.ontology = ont;
		this.manager = ont.getOWLOntologyManager();
		this.method = method;
		this.iterationMetrics = new ArrayList<ExtractionMetric>();
	}
	
	public CycleRemovalMethod getCycleRemovalMethod(){
		return method;
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {

		Set<OWLEntity> origSig = new HashSet<OWLEntity>(signature);
		Set<OWLLogicalAxiom> module = extractStarModule(ontology, signature);
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
				module = extractStarModule(createOntologyFromLogicalAxioms(module), origSig);
				sizeChanged = (module.size() < amexSize);
				

			}
			else{
				sizeChanged = false;
			}

		}while(sizeChanged);

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

	public Set<OWLLogicalAxiom> extractStarModule(OWLOntology ontology, Set<OWLEntity> signature){
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
		Stopwatch starwatch = new Stopwatch().start();
		Set<OWLLogicalAxiom> module = ModuleUtils.getLogicalAxioms(extractor.extract(signature));
		starwatch.stop();
		starExtractions++;
		ExtractionMetric.MetricBuilder builder = new ExtractionMetric.MetricBuilder(ExtractionMetric.ExtractionType.STAR);
		builder.moduleSize(module.size());
		builder.timeTaken(starwatch.elapsed(TimeUnit.MILLISECONDS));
		iterationMetrics.add(builder.createMetric());
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


	private Set<OWLLogicalAxiom> extractSemanticModule(OWLOntology ontology, Set<OWLLogicalAxiom> existingmodule, Set<OWLEntity> signature){
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ontology);
		Set<OWLLogicalAxiom> module = extractor.extractModule(existingmodule, signature);

		manager.removeOntology(ontology);
		amexExtrations++;
		//		System.out.println("AMEX: " + module.size());
		iterationMetrics.add(extractor.getMetrics());
		return module;
	}


	public ArrayList<ExtractionMetric> getIterationMetrics() {
		return iterationMetrics;
	}

	public int getStarExtractions() {
		return starExtractions;
	}
	public int getAmexExtrations() {
		return amexExtrations;
	}


}
