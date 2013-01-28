package uk.ac.liv.moduleextraction.chaindependencies;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

public class DependencySet extends HashSet<Dependency>{

	private static final long serialVersionUID = 5147803484884184934L;
	
	public Set<OWLEntity> asOWLEntities(){
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		for(Dependency d : this){
			entities.add(d.getValue());
		}
		return entities;
	}
	
	public void mergeWith(DependencySet dependencySet){
		addAll(dependencySet);
	}
	
}
