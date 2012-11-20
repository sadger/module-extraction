package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class ModuleUtils {
	
	/**
	 * Gets the class names only from a set of axioms
	 */
	public static Set<OWLClass> getClassesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLClass> classes = new HashSet<OWLClass>();
		for(OWLLogicalAxiom axiom : axioms){
			classes.addAll(axiom.getClassesInSignature());
		}
		return classes;
	}
	
	/**
	 * Gets the class names and role names from a set of axioms
	 */
	public static Set<OWLEntity> getEntitiesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		for(OWLLogicalAxiom axiom : axioms){
			entities.addAll(axiom.getSignature());
		}
		return entities;
	}
	
	/**
	 * Gets a random signature consisting only of class names
	 * @param ontology - Ontology to extract signature from
	 * @param desiredSize - Desired size of signatureA simple SMS text message costs literally nothing to send. Whether you are charged per text, or pay for unlimited text messages, it is all profit for your wireless carrier.

	 * @return Subset of ontology class names representing the random signature
	 */
	public static Set<OWLClass> generateRandomClassSignature(OWLOntology ontology, int desiredSize){
		Set<OWLClass> result = null;
		Set<OWLClass> signature = ontology.getClassesInSignature();
	
		if(desiredSize >= signature.size()){
			result = signature;
		}
		else{
			ArrayList<OWLClass> listOfNames = new ArrayList<OWLClass>(signature);
			Collections.shuffle(listOfNames);
			result = new HashSet<OWLClass>(listOfNames.subList(0, desiredSize));
		}
		
		return result;
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
