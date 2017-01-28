package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ModuleUtils {

	private static OWLDataFactory factory = OWLManager.getOWLDataFactory();
	/**
	 * Gets the class names only from a set of axioms
	 */
	public static Set<OWLClass> getClassesInSet(Set<OWLLogicalAxiom> axioms){
		Set<OWLClass> classes = new HashSet<OWLClass>();
		for(OWLLogicalAxiom axiom : axioms){
			classes.addAll(axiom.getClassesInSignature());
			removeTopAndBottomConcept(classes);
		}
		return classes;
	}

	/**
	 * Gets the class names and role names from a set of axioms
	 */
	public static Set<OWLEntity> getClassAndRoleNamesInSet(Collection<OWLLogicalAxiom> axioms){
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		for(OWLLogicalAxiom axiom : axioms){
			entities.addAll(axiom.getSignature().stream().filter(e -> e.isOWLClass() || e.isOWLObjectProperty()).collect(Collectors.toList()));
		}
		removeTopAndBottomConcept(entities);
		return entities;
	}

	public static Set<OWLEntity> getSignatureOfAxioms(Collection<OWLLogicalAxiom> axioms){
		Set<OWLEntity> entities = new HashSet<>();
		axioms.forEach(axiom ->
				entities.addAll(axiom.getSignature()
						.stream()
						.filter(e -> e.isOWLClass() || e.isOWLObjectProperty() || e.isOWLNamedIndividual())
						.collect(Collectors.toSet()))
		);
		return  entities;
	}



	public static boolean isInclusionOrEquation(OWLLogicalAxiom axiom){
		return (axiom.getAxiomType() == AxiomType.SUBCLASS_OF || axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES);
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
		if(timeInMilliseconds < 1000){
			return "< 1 second";
		}
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



	public static void remapIRIs(HashSet<OWLOntology> ontologies, String prefix) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLEntityRenamer renamer = new OWLEntityRenamer(manager, ontologies);
		SimpleShortFormProvider spm = new SimpleShortFormProvider();

		for(OWLOntology ont : ontologies){
			Set<OWLEntity> sig = ont.getSignature();
			String newPrefix = prefix + "#";
			for(OWLEntity ent : sig){
				if(!(ent.isTopEntity() || ent.isBottomEntity())){
					manager.applyChanges(renamer.changeIRI(ent.getIRI(), IRI.create(newPrefix
							+ spm.getShortForm(ent))));
				}

			}
		}
	}

	public static void remapIRIs(OWLOntology ontology, String prefix) {
		HashSet<OWLOntology> onts = new HashSet<OWLOntology>();
		onts.add(ontology);
		remapIRIs(onts, prefix);
	}

	public static Set<OWLLogicalAxiom> getAxiomsForSignature(OWLOntology ontology, Set<OWLEntity> signature){
		Set<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();

		for(OWLLogicalAxiom ax : ontology.getLogicalAxioms()){
			if(ax.getSignature().equals(signature)){
				axioms.add(ax);
			}
		}
		return axioms;
	}

	public static int getCoreSize(Set<OWLLogicalAxiom> axioms){
		int count = 0;

		for(OWLLogicalAxiom axiom : axioms){
			AxiomType<?> type = axiom.getAxiomType();
			if(type == AxiomType.EQUIVALENT_CLASSES || type == AxiomType.SUBCLASS_OF){
				count++;
			}
		}
		return count;
	}

	public static Set<OWLLogicalAxiom> getCoreAxioms(OWLOntology ontology){
		Set<OWLLogicalAxiom> coreAxioms = new HashSet<OWLLogicalAxiom>();
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			AxiomType<?> type = axiom.getAxiomType();
			if(type == AxiomType.SUBCLASS_OF || type == AxiomType.EQUIVALENT_CLASSES){
				coreAxioms.add(axiom);
			}
		}
		return coreAxioms;
	}
	
	public static Set<OWLLogicalAxiom> getSupportedAxioms(Set<OWLLogicalAxiom> axioms){
		Set<OWLLogicalAxiom> supported = new HashSet<OWLLogicalAxiom>();
		AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();
		for(OWLLogicalAxiom axiom : axioms){
			if(verifier.isSupportedAxiom(axiom)){
				supported.add(axiom);
			}
		}
		return supported;
	}

	public static void writeOntology(Set<OWLLogicalAxiom> axioms, String location){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();

		OWLOntology newOntology = null;
		try {
			newOntology = manager.createOntology();
			for(OWLLogicalAxiom axiom : axioms){
				manager.addAxiom(newOntology, axiom);
			}
			manager.saveOntology(newOntology,owlxmlFormat,IRI.create(new File(location)));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}


	}

	public static String getExpressiveness(OWLOntology ont){
		DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
		return checker.getDescriptionLogicName();
	}
	
	public static ArrayList<File> getListAsFiles(File list) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(list));
		ArrayList<File> files = new ArrayList<File>();
		String line;
		while ((line = br.readLine()) != null) {
		   files.add(new File(line));
		}
		br.close();
		return files;
	}

	public static boolean isClassSignature(Set<OWLEntity> sig) {
		for(OWLEntity e : sig){
			if(!(e instanceof OWLClass)){
				return false;
			}
		}
		return true;
		
	}

	public static void printFile(File f){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
