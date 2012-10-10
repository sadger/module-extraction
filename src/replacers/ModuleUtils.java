package replacers;

import java.util.HashSet;
import java.util.Set;

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
