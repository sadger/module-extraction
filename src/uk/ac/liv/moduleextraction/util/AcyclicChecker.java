package uk.ac.liv.moduleextraction.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.extractor.SyntacticFirstModuleExtraction;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class AcyclicChecker {
	Logger logger = LoggerFactory.getLogger(AcyclicChecker.class);
	
	private HashMap<OWLClass, Set<OWLClass>> classDependencies = new HashMap<OWLClass, Set<OWLClass>>();
	private OWLOntology ontology;

	private HashSet<OWLClass> dependsOnCycle = new HashSet<OWLClass>();
	private HashSet<OWLClass> causesCycle = new HashSet<OWLClass>();
	
	private boolean collectingMetrics = false;
	private boolean metricsHaveBeenCollected = false;
	private boolean isAcyclic = false;
	
	/**
	 * Initialse an ontology for acyclic checking - preprocessing
	 * @param ontology
	 * @param collectMetrics - searches all paths for cycles to collected information
	 * about the structure of the ontology - much less efficent when this flag is set.
	 */
	public AcyclicChecker(OWLOntology ontology, boolean collectMetrics) {
		this.ontology = ontology;
		this.collectingMetrics = collectMetrics;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			addImmediateDependencies(axiom);
		}		
		
	}
	
	public void printMetrics(){
		if(metricsHaveBeenCollected){
			boolean acyclic = isAcyclic;
			System.out.println("Is acyclic: " + acyclic);
			if(!acyclic){
				System.out.println("Concepts in sig: " + ontology.getClassesInSignature().size());
				int lhsSize = classDependencies.keySet().size();
				System.out.println("LHS size: " + lhsSize);
				System.out.println("Cycle causing: " + getPercentageMetric(lhsSize, causesCycle.size()));
				dependsOnCycle.removeAll(causesCycle);
				System.out.println("Depends on cycle: " + getPercentageMetric(lhsSize, dependsOnCycle.size()));
				int doesNotDepend = lhsSize - causesCycle.size() - dependsOnCycle.size();
				System.out.println("Does not depend on cycle: " + getPercentageMetric(lhsSize, doesNotDepend));
//				
//				System.out.println("Causes: " + causesCycle);
//				System.out.println("Depends " + dependsOnCycle);
			}
		}
		else{
			System.out.println("No metrics have been collected - run acyclic checker with flag set");
		}


	}
	
	private String getPercentageMetric(int lhsSize, int comparisonMetric){
		long result = Math.round(((double) comparisonMetric/lhsSize)*100);
		if(result == 0){
			return comparisonMetric + " (<1%)";
		}		
		else if(result == 100 && comparisonMetric != lhsSize){
			return comparisonMetric + " (>99%)";
		}
		return comparisonMetric + " (" + result + "%)";
	}
	
	private void addImmediateDependencies(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		Set<OWLClass> axiomDeps = createAxiomDependencySet(definition);
		
		populateImmediateDependency(name, axiomDeps);
		
		
	}
	
	private Set<OWLClass> createAxiomDependencySet(OWLClassExpression definition) {
		HashSet<OWLClass> axiomDeps = new HashSet<OWLClass>();
		for(OWLClass cls : definition.getClassesInSignature()){
			if(!cls.isTopEntity() && !cls.isBottomEntity()){
				axiomDeps.add(cls);
			}
		}
		return axiomDeps;
	}
	
	private void populateImmediateDependency(OWLClass name, Set<OWLClass> axiomDeps) {
		Set<OWLClass> nameDependencies = classDependencies.get(name);
		if(nameDependencies == null){
			classDependencies.put(name, axiomDeps);
		}
		else{
			// Respect names with multiple definitions
			nameDependencies.addAll(axiomDeps);
			classDependencies.put(name, nameDependencies);
		}
	}


	public boolean isAcyclic(){
		int axiomCount = 0;
		boolean result = true ;

		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			axiomCount++;
			logger.debug("Checking axiom {}/{}: {}",axiomCount,ontology.getLogicalAxiomCount(), axiom);

			/* Allow short circuting of conditional if not 
			 * collecting metrics - so do not check every axiom
			 */
			if(collectingMetrics){
				boolean noCycle = doesNotContainCycle(axiom);
				result = result && noCycle;
			}
			else{
				result = result && doesNotContainCycle(axiom);
			}

			
		}
		
		if(collectingMetrics){
			metricsHaveBeenCollected = true;
		}

		this.isAcyclic = result;
		return isAcyclic;
	}
	
	public boolean doesNotContainCycle(OWLLogicalAxiom axiom){
		
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		boolean containsNoCycle = 
				noCycleExistsInAxiom(new HashSet<OWLClass>(Collections.singleton(name)), definition.getClassesInSignature());
		
		if(!containsNoCycle){
			dependsOnCycle.add(name);
		}
		
		return containsNoCycle;
	}
	
	
	public boolean noCycleExistsInAxiom(Set<OWLClass> names, Set<OWLClass> toCheck){
		
		logger.trace("{}|{}",names,toCheck);
		
		if(toCheck.isEmpty()){
			return true;
		}
		else{
			boolean cycleCauseFound = false;
			
			for(OWLClass cls : names){
				if(collectingMetrics){
					if(toCheck.contains(cls)){
						causesCycle.add(cls);
						cycleCauseFound = cycleCauseFound || true;
					}
				}			
				else{
					if(toCheck.contains(cls)){
						return false;
					}
				}
			}
	
			
			if(cycleCauseFound){
				return false;
			}
			
			boolean result = true;
			
			for(OWLClass check : toCheck){
				HashSet<OWLClass> newNames = new HashSet<OWLClass>();
				newNames.addAll(names);
				newNames.add(check);
				
				HashSet<OWLClass> newCheck = new HashSet<OWLClass>();
				
				Set<OWLClass> checkDepends = classDependencies.get(check);
				
				if(checkDepends != null){
					newCheck.addAll(checkDepends);
				}
				
				/* Check all paths if collecting metrics otherwise
				 * allow short circuting of conditional
				 */
				if(collectingMetrics){
					boolean noCycle = noCycleExistsInAxiom(newNames, newCheck);
					result = result && noCycle;
				}
				else{
					result = result && noCycleExistsInAxiom(newNames, newCheck);
				}
	
			}
			
			return result;
		}
	}

	


	
	public static void main(String[] args) {
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "moduletest/acyclic2.krss");
	//	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
	OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/Bioportal/NOTEL/Big/Acyclic/CL");
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/smallcycley");
	
	System.out.println(ont);
		System.out.println("Logical axioms: " + ont.getLogicalAxiomCount());
	AcyclicChecker checker = new AcyclicChecker(ont, true);


	System.out.println("Is acyclic: " + checker.isAcyclic());
	checker.printMetrics();




		
	} 
	
	
	
	
	
}
