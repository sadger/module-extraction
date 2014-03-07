package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.util.ModuleUtils;


public class ChainDependencies extends HashMap<OWLClass, DependencySet>{

	private static final long serialVersionUID = 5599458330117570660L;
	private DefinitorialDepth depth;
	private ArrayList<OWLLogicalAxiom> sortedAxioms;

	//For OLD approach
	public ChainDependencies() {
		
	}

	public ChainDependencies(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public ChainDependencies(Set<OWLLogicalAxiom> axioms) {
		depth = new DefinitorialDepth(axioms);
		sortedAxioms = depth.getDefinitorialSortedList();
		updateDependenciesWith(sortedAxioms);
	}
	
	private void updateDependenciesWith(List<OWLLogicalAxiom> sortedAxioms){
		for(Iterator<OWLLogicalAxiom> it = sortedAxioms.iterator(); it.hasNext();){
			updateDependenciesWith(it.next());
		}	
	}
	
	//For OLD approach
	public void updateDependenciesWith(OWLLogicalAxiom axiom){
		OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

		DependencySet axiomDeps = new DependencySet();
		addImmediateDependencies(definition,axiomDeps);
		updateFromDefinition(definition, axiomDeps);
		
		put(name, axiomDeps);
	}
	
	public ArrayList<OWLLogicalAxiom> getSortedAxioms() {
		return sortedAxioms;
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
