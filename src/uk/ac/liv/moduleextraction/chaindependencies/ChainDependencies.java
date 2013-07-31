package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.main.ModuleUtils;


public class ChainDependencies extends HashMap<OWLClass, DependencySet>{

	private static final long serialVersionUID = 5599458330117570660L;


	public ChainDependencies() {

	}
	
	public void updateDependenciesWith(List<OWLLogicalAxiom> sortedAxioms){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			updateDependenciesWith(it.next());
		}	
	}
	
	public void updateDependenciesWith(OWLLogicalAxiom[] axiomArray){
		for(OWLLogicalAxiom ax : axiomArray){
			updateDependenciesWith(ax);
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
				axiomDeps.add(e);
		}
	}

	private void updateFromDefinition(OWLClassExpression definition, DependencySet axiomDeps) {
		for(OWLClass cls : ModuleUtils.getNamedClassesInSignature(definition)){
			DependencySet clsDependencies = get(cls);
			if(clsDependencies != null)
				axiomDeps.addAll(clsDependencies);
		}

	}
 
}
