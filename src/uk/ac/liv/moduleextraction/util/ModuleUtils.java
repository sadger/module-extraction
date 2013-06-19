package uk.ac.liv.moduleextraction.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.liv.moduleextraction.chaindependencies.Dependency;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.ontologyutils.caching.AxiomCache;
import uk.ac.liv.ontologyutils.caching.AxiomMetricStore;

import com.google.common.cache.LoadingCache;

public class ModuleUtils {

	private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
	/**
	 * Gets the class names only from a set of axioms
	 */
	public static Set<OWLClass> getClassesInSet(Set<OWLLogicalAxiom> axioms){
		LoadingCache<OWLLogicalAxiom,AxiomMetricStore> 
		axiomCache = AxiomCache.getCache();

		Set<OWLClass> classes = new HashSet<OWLClass>();
		for(OWLLogicalAxiom axiom : axioms){
			AxiomMetricStore axiomStore = null;
			try {
				axiomStore = axiomCache.get(axiom);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			classes.addAll(axiomStore.getNamedClasses());
			removeTopAndBottomConcept(classes);
		}
		return classes;
	}

	/**
	 * Gets the class names and role names from a set of axioms
	 */
	public static Set<OWLEntity> getClassAndRoleNamesInSet(Set<OWLLogicalAxiom> axioms){
		LoadingCache<OWLLogicalAxiom,AxiomMetricStore> 
		axiomCache = AxiomCache.getCache();
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		AxiomMetricStore axiomStore = null;
		for(OWLLogicalAxiom axiom : axioms){
			try {
				axiomStore = axiomCache.get(axiom);
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			}
			for(OWLEntity e : axiomStore.getSignature()){
				if(e.isOWLClass() || e.isOWLObjectProperty())
					entities.add(e);
			}
		}
		removeTopAndBottomConcept(entities);
		return entities;
	}
	
	public static Set<OWLObjectProperty> getRolesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLObjectProperty> result = new HashSet<OWLObjectProperty>();
		
		for(OWLLogicalAxiom axiom : axioms){
			result.addAll(axiom.getObjectPropertiesInSignature());
		}
		
		return result;
	}

	public static Set<OWLClass> getNamedClassesInSignature(OWLClassExpression cls){
		Set<OWLClass> classes = cls.getClassesInSignature();
		removeTopAndBottomConcept(classes);
		return classes;
	}

	private static void removeTopAndBottomConcept(Set<? extends OWLEntity> entities){
		entities.remove(factory.getOWLThing());
		entities.remove(factory.getOWLNothing());
	}

	public static OWLClass getRandomClass(Set<OWLClass> classes){
		ArrayList<OWLClass> listOfClasses = new ArrayList<OWLClass>(classes);
		Collections.shuffle(listOfClasses);
		return listOfClasses.get(0);
	}

	public static Set<OWLLogicalAxiom> generateRandomAxioms(Set<OWLLogicalAxiom> originalOntology, int desiredSize){
		Set<OWLLogicalAxiom> result = null;

		if(desiredSize >= originalOntology.size())
			result = originalOntology;
		else{
			ArrayList<OWLLogicalAxiom> listOfAxioms = new ArrayList<OWLLogicalAxiom>(originalOntology);
			Collections.shuffle(listOfAxioms);
			result = new HashSet<OWLLogicalAxiom>(listOfAxioms.subList(0, desiredSize));
		}

		return result;
	}

	public static String getTimeAsHMS(long timeInMilliseconds){
		long hours = TimeUnit.MILLISECONDS.toHours(timeInMilliseconds);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliseconds));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds));
		return hours + "hrs " + minutes + "mins " + seconds + "s";
	}


	public static Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom())
				result.add((OWLLogicalAxiom) ax);
		}
		return result;
	}

	public static DependencySet convertToDependencySet(Set<OWLEntity> entities){
		DependencySet dependencies = new DependencySet();
		for(OWLEntity e : entities){
			if(!e.isTopEntity() && !e.isBottomEntity())
				dependencies.add(new Dependency(e));
		}
		
		return dependencies;
	}
	
	public static void remapIRIs(HashSet<OWLOntology> ontologies, String prefix) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLEntityRenamer renamer = new OWLEntityRenamer(manager, ontologies);
		SimpleShortFormProvider spm = new SimpleShortFormProvider();

		for(OWLOntology ont : ontologies){
			Set<OWLEntity> sig = ont.getSignature();
			String IRIprefix = ont.getOntologyID().toString() + "#";
			String newPrefix = prefix + "#";
			for(OWLEntity ent : sig){
				IRI iri = IRI.create(IRIprefix + spm.getShortForm(ent));
				manager.applyChanges(renamer.changeIRI(iri, IRI.create(newPrefix
						+ spm.getShortForm(ent))));
			}
		}
	}
}
