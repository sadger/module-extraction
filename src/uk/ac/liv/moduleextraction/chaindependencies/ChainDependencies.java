package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;


public class ChainDependencies extends HashMap<OWLClass, DependencySet>{

	private static final long serialVersionUID = 5599458330117570660L;


	public ChainDependencies() {

	}
	
	public void updateDependenciesWith(List<OWLLogicalAxiom> sortedAxioms){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			updateDependenciesWith(it.next());
		}	
	}
	

	public void updateDependenciesWith(OWLLogicalAxiom axiom){
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

		DependencySet axiomDeps = new DependencySet();
		addImmediateDependencies(definition,axiomDeps);
		updateFromDefinition(definition, axiomDeps);
		
		put(name, axiomDeps);
	}

	private void addImmediateDependencies(OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLEntity e : definition.getSignature()){
			if(!e.isTopEntity() && !e.isBottomEntity())
				axiomDeps.add(new Dependency(e));
		}
	}

	private void updateFromDefinition(OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){
			DependencySet clsDependencies = get(cls);
			if(clsDependencies != null)
				axiomDeps.addAll(clsDependencies);
		}
	}
 

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "moduletest/test1.krss");
		HashMap<OWLClass, Integer> definitorialMap = new DefinitorialDepth(ont.getLogicalAxioms()).getDefinitorialMap();
		ArrayList<OWLLogicalAxiom> allAxiomsSorted = new ArrayList<OWLLogicalAxiom>(ont.getLogicalAxioms());
		Collections.sort(allAxiomsSorted, new AxiomComparator(definitorialMap));

		System.out.println(allAxiomsSorted);

		ChainDependencies chain = new ChainDependencies();
		for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
			chain.updateDependenciesWith(axiom);
			System.out.println(chain);
		}

	}
}
