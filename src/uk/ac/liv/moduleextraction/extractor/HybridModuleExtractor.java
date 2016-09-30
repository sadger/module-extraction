package uk.ac.liv.moduleextraction.extractor;

import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;
import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.liv.moduleextraction.filters.OntologyFilters;
import uk.ac.liv.moduleextraction.filters.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.filters.SharedNameFilter;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.metrics.ExtractionMetric;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HybridModuleExtractor implements Extractor {

	private Logger logger = LoggerFactory.getLogger(HybridModuleExtractor.class);


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
		hybridWatch = Stopwatch.createStarted();
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
		filters.addFilter(new SharedNameFilter(inspector));
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

		logger.debug("{}:{}", "STAR", module);

		return module;
	}

	private Set<OWLLogicalAxiom> extractSemanticModule(OWLOntology inputOnt, Set<OWLLogicalAxiom> unsupportedAxioms, Set<OWLEntity> signature){


        EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(inputOnt);
		Set<OWLLogicalAxiom> module = extractor.extractModule(unsupportedAxioms, signature);
		manager.removeOntology(inputOnt);
		amexExtrations++;
		iterationMetrics.add(extractor.getMetrics());

        logger.debug("{}:{}", "AMEX", module);

/*        if(module.size() == 68){
            ModuleUtils.writeOntology(module, ModulePaths.getOntologyLocation() + "/iterative/first-amex");
        }*/


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

	public static void main(String[] args) throws IOException {

		String signatureDir = "/run/media/william/WilliamGatensPassport/Thesis-Results/DP/depleting-comparison/domain-elements-1/";
		String name = "5ffeb906-dbd7-442f-bb1d-75e0e99591e3_food.owl";
		String sigName = "axiom1492231698";
		File ontLoc =  new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/" + name + "-QBF");
		//OWLOntology example = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());
		//System.out.println("Ont: " + example.getLogicalAxiomCount());
		SigManager man = new SigManager(new File(signatureDir + ontLoc.getName() + "-NDepletingComparison/domain_size-1/" + sigName));

//axiom2042143976 7-2-1-1
//axiom-413792558 6-3-1-1
//axiom-133873025 6-3-1-1
//axiom-1763471491




        OWLOntology example = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/iterative/new-example.owl");

        System.out.println("Ont: " + example.getLogicalAxiomCount());



        System.out.println("Ont: " + example.getLogicalAxiomCount());




/*

		SigManager example_man = new SigManager(new File(ModulePaths.getOntologyLocation() + "/iterative/"));
		System.out.println(example.getLogicalAxiomCount());
		//example.getLogicalAxioms().forEach(System.out::println);
*/


	    Set<OWLEntity> sig = man.readFile("signature-mod");
		ArrayList<OWLLogicalAxiom> ontList = new ArrayList<>(example.getLogicalAxioms());
		System.out.println(ontList);


		System.out.println("Initial sig: " + sig);

		HybridModuleExtractor extractor = new HybridModuleExtractor(example);
		Set<OWLLogicalAxiom> module = extractor.extractModule(sig);


		NDepletingModuleExtractor nDepletingModuleExtractor = new NDepletingModuleExtractor(1,module);
		SyntacticLocalityModuleExtractor star = new SyntacticLocalityModuleExtractor(example.getOWLOntologyManager(), example, ModuleType.STAR);

		System.out.println("n-depleting:" + nDepletingModuleExtractor.extractModule(sig));
		System.out.println("hybrid: " + module.size());

        Set<OWLLogicalAxiom> starMod = ModuleUtils.getLogicalAxioms(star.extract(sig));

		System.out.println("star: " + starMod.size());


        ModuleUtils.writeOntology(starMod, ModulePaths.getOntologyLocation() + "/iterative/new-example-mod.owl");


		SyntacticLocalityEvaluator topeval = new SyntacticLocalityEvaluator(LocalityClass.TOP_TOP);
		SyntacticLocalityEvaluator boteval = new SyntacticLocalityEvaluator(LocalityClass.BOTTOM_BOTTOM);



		System.out.println(ontList);

		Set<OWLLogicalAxiom> toRemove = new HashSet<>();
		toRemove.add(ontList.get(3));
		toRemove.add(ontList.get(4));

		ontList.removeAll(toRemove);

		System.out.println(ontList);

		System.out.printf("%-60s %-10s %s\n", "", "TOP", "BOT");
		ontList.forEach(
				p -> System.out.printf("%-60s %-10s %s\n", p, topeval.isLocal(p, sig), boteval.isLocal(p, sig))
				);


		System.out.println(ontList);
		sig.addAll(ontList.get(3).getSignature());
		ontList.remove(3);



		System.out.printf("%-60s %-10s %s\n", "", "TOP", "BOT");
		ontList.forEach(
				p -> System.out.printf("%-60s %-10s %s\n", p, topeval.isLocal(p, sig), boteval.isLocal(p, sig))
		);





	}


}
