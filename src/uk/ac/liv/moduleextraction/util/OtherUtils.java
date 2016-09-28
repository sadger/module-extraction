package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.OWLEntity;
import uk.ac.liv.moduleextraction.axiomdependencies.DependencySet;

import java.util.Set;


public class OtherUtils {
	public static DependencySet convertToDependencySet(Set<OWLEntity> entities){
		DependencySet dependencies = new DependencySet();
		for(OWLEntity e : entities){
			if(!e.isTopEntity() && !e.isBottomEntity())
				dependencies.add(e);
		}
		
		return dependencies;
	}
	
}
