package uk.ac.liv.moduleextraction.experiments;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.experiments.SharedNameFilter.RemovalMethod;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class OntologyFilters implements SupportedFilter {

	HashSet<SupportedFilter> filters = new HashSet<SupportedFilter>();
	
	public OntologyFilters() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean isRequired() {
		return true;
	}

	public void addFilter(SupportedFilter filter){
		filters.add(filter);
	}
	
	@Override
	public Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> supported = new HashSet<OWLLogicalAxiom>(axioms);
		HashSet<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		for(SupportedFilter filter : filters){
			if(filter.isRequired()){
				unsupported.addAll(filter.getUnsupportedAxioms(supported));
			}
			supported.removeAll(unsupported);
		}
		
		return unsupported;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "interp/diff.krss");
		OntologyFilters filters = new OntologyFilters();
		filters.addFilter(new SupportedExpressivenessFilter());
		filters.addFilter(new SharedNameFilter(new AxiomStructureInspector(ont), RemovalMethod.RANDOM));
		System.out.println(ont.getLogicalAxioms());
		Set<OWLLogicalAxiom> unsup = filters.getUnsupportedAxioms(ont.getLogicalAxioms());
		Set<OWLLogicalAxiom> all = ont.getLogicalAxioms();
		System.out.println("Unsupported: " + unsup);
		all.removeAll(unsup);
		System.out.println("Supported: " + all);
		
	}

}
