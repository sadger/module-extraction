package uk.ac.liv.moduleextraction.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.liv.moduleextraction.chaindependencies.Dependency;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class AcyclicChecker {
	
	private HashMap<OWLClass, DependencySet> immediateDependencies = new HashMap<OWLClass, DependencySet>();
	private OWLOntology ontology;

	private HashSet<OWLClass> cyclicDefinitions = new HashSet<OWLClass>();
	private HashSet<OWLClass> cycleCausingNames = new HashSet<OWLClass>();
	
	
	public AcyclicChecker(OWLOntology ontology) {
		this.ontology = ontology;
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			addImmediateDependencies(axiom);
		}		
		
	}
	
	private void addImmediateDependencies(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		DependencySet axiomDeps = createAxiomDependencySet(definition);
		
		populateImmediateDependency(name, axiomDeps);
		
		
	}
	
	private DependencySet createAxiomDependencySet(OWLClassExpression definition) {
		DependencySet axiomDeps = new DependencySet();
		for(OWLEntity e : definition.getClassesInSignature()){
			if(!e.isTopEntity() && !e.isBottomEntity()){
				axiomDeps.add(new Dependency(e));
			}
		}
		return axiomDeps;
	}
	
	private void populateImmediateDependency(OWLClass name, DependencySet axiomDeps) {
		DependencySet nameDependencies = immediateDependencies.get(name);
		if(nameDependencies == null){
			immediateDependencies.put(name, axiomDeps);
		}
		else{
			// Respect names with multiple definitions
			nameDependencies.addAll(axiomDeps);
			immediateDependencies.put(name, nameDependencies);
		}
	}


	public boolean isAcyclic(){
		int axiomCount = 0;
		boolean result = true ;

		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			axiomCount++;
			System.out.println("Checking axiom " + axiomCount + "/" + ontology.getLogicalAxiomCount());
			System.out.println(axiom);
//			
			boolean causedCycle = causesCycle(axiom);
//			if(causedCycle){
//				System.out.println(axiom);
//			}
			result = result && !causedCycle;
			
		}

		
		return result;
	}
	
	public void printMetrics(){
		System.out.println();
		boolean acyclic = isAcyclic();
		System.out.println("Is acyclic " + acyclic);
		if(!acyclic){
			System.out.println("Concepts in sig: " + ontology.getClassesInSignature().size());
			int lhsSize = immediateDependencies.keySet().size();
			System.out.println("LHS size: " + lhsSize);
			System.out.println("Cycle causing: " + cycleCausingNames.size() + " (" + Math.round(((double) cycleCausingNames.size()/lhsSize)*100) + "%)");
			cyclicDefinitions.removeAll(cycleCausingNames);
			System.out.println("Depends on cycle: " + cyclicDefinitions.size() + " (" + Math.round(((double) cyclicDefinitions.size()/lhsSize)*100) + "%)");
			int doesNotDepend = lhsSize - cycleCausingNames.size() - cyclicDefinitions.size();
			System.out.println("Does not depend on cycle: " + doesNotDepend + " (" + Math.round(((double) doesNotDepend/lhsSize)*100) + "%)");

			
			System.out.println(cycleCausingNames);
		}

	}

	/* Follow the definition of the axioms see if you find 
	 * any name on the LHS of its unfolding on the RHS
	 */
	private boolean causesCycle(OWLLogicalAxiom axiom) {
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		

		/* All the names seen on the LHS of an axiom */
		HashSet<OWLClass> names = new HashSet<OWLClass>();
		
		/*Contains the immediate dependencies for last names added */
		DependencySet toCheck = immediateDependencies.get(name);
		
		while(!toCheck.isEmpty()){
			
			names.add(name);
			
			/* If we see an axiom on the RHS which has appeared on
			 * the LHS we have a cycle
			 */
			for(OWLClass cls : names){
				if(toCheck.contains(new Dependency(cls))){
					cyclicDefinitions.add(name);
					cycleCausingNames.add(cls);
					return true;
				}
			}

			toCheck = updateToCheckAndNames(toCheck, names);
		
		}

		return false;
		
	}
	
	public void writeCycley(){
		HashSet<OWLAxiom> cycley = new HashSet<OWLAxiom>();
		
		for(OWLLogicalAxiom ax : ontology.getLogicalAxioms()){
			OWLClass axName = (OWLClass) AxiomSplitter.getNameofAxiom(ax);
			if(cycleCausingNames.contains(axName)){
				cycley.add(ax);
			}
		}
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLFunctionalSyntaxOntologyFormat owlxmlFormat = new OWLFunctionalSyntaxOntologyFormat();
		try {
			manager.saveOntology(manager.createOntology(cycley), owlxmlFormat,IRI.create(new File(ModulePaths.getOntologyLocation()+ "/cycley2")));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DependencySet updateToCheckAndNames(DependencySet toCheck,
			HashSet<OWLClass> names) {
		DependencySet newDependencies = new DependencySet();

		for(Dependency d : toCheck){
			// Set toCheck as the immediate dependencies of these names
			DependencySet depSet = immediateDependencies.get(d.getValue());
			if(depSet != null){
				newDependencies.addAll(depSet);
			}
		}

		return newDependencies;
	}
	
	
	public static void main(String[] args) {
	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/moduletest/acyclic.krss");
	//	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
	//	OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
	// OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/Bioportal/NatPrO");
	//OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/smallcycley");
	
	//System.out.println(ont);
	
	AcyclicChecker checker = new AcyclicChecker(ont);

	System.out.println("Logical axioms: " + ont.getLogicalAxiomCount());
	System.out.println("Is acyclic: " + checker.isAcyclic());
	checker.printMetrics();



		
	} 
	
	
	
	
	
}
