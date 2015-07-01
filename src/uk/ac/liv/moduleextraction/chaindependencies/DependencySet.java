package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLEntity;

public class  DependencySet extends HashSet<OWLEntity>{
	public DependencySet() {
		super();
	}
	
	private static final long serialVersionUID = 5147803484884184934L;
	
	public void mergeWith(DependencySet dependencySet){
		addAll(dependencySet);
	}
	
}
