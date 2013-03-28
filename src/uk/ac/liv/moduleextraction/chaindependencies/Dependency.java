package uk.ac.liv.moduleextraction.chaindependencies;

import org.semanticweb.owlapi.model.OWLEntity;

public class Dependency {
	private OWLEntity value;
	
	public Dependency(OWLEntity value) {
		this.value = value;
	}
	
	public OWLEntity getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Dependency){
			Dependency d = (Dependency) obj;
			return d.getValue().equals(value);
		}
		else{
			return false;
		}
		
	}
	
	@Override
	public int hashCode() {
		return value.getIRI().hashCode();
	}
}
