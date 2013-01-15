package uk.ac.liv.moduleextraction.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class ModuleUtils {

	private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
	/**
	 * Gets the class names only from a set of axioms
	 */
	public static Set<OWLClass> getClassesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLClass> classes = new HashSet<OWLClass>();
		for(OWLLogicalAxiom axiom : axioms){
			classes.addAll(axiom.getClassesInSignature());
			classes.remove(factory.getOWLThing());
			classes.remove(factory.getOWLNothing());
		}
		return classes;
	}

	/**
	 * Gets the class names and role names from a set of axioms
	 */
	public static Set<OWLEntity> getClassAndRoleNamesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		for(OWLLogicalAxiom axiom : axioms){
			for(OWLEntity e : axiom.getSignature()){
				if(e.isOWLClass() || e.isOWLObjectProperty())
					entities.add(e);
			}
		}

		entities.remove(factory.getOWLThing());
		entities.remove(factory.getOWLNothing());

		return entities;
	}

	public static Set<OWLClass> getNamedClassesInSignature(OWLClassExpression cls){
		Set<OWLClass> classes = cls.getClassesInSignature();
		classes.remove(factory.getOWLThing());
		classes.remove(factory.getOWLNothing());

		return classes;
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
