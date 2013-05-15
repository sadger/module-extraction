package uk.ac.liv.moduleextraction.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.AxiomExtractor;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.terminology.EquivalentToTerminologyChecker;

public class EquivalentToTerminologyProcessor {

	
	private static final String NEW_IRI_PREFIX = "http://www.csc.liv.ac.uk";
	private OWLOntology equivalentToTerminology;
	private HashMap<OWLClass, HashSet<OWLLogicalAxiom>> repeatedAxioms;
	private HashMap<OWLClass, OWLClass> renamingMap;
	private HashSet<OWLLogicalAxiom> newAxioms = new HashSet<OWLLogicalAxiom>();
	OWLOntologyManager manager;
	OWLDataFactory factory;
	
	public EquivalentToTerminologyProcessor(OWLOntology ontology) throws NotEquivalentToTerminologyException {
		AxiomExtractor extractor = new AxiomExtractor();
		EquivalentToTerminologyChecker equivToTermChecker = new EquivalentToTerminologyChecker();
		ontology = extractor.extractInclusionsAndEqualities(ontology);
	
		if(!equivToTermChecker.isEquivalentToTerminology(ontology)){
			throw new NotEquivalentToTerminologyException();
		}
		else{
			this.equivalentToTerminology = ontology;
		}
		manager = equivalentToTerminology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		collectRepeatedAxioms();
		renameAxioms();
		
		System.out.println(revertAxioms(equivalentToTerminology.getLogicalAxioms()));

	}
	
	public void collectRepeatedAxioms(){
		repeatedAxioms = new HashMap<OWLClass, HashSet<OWLLogicalAxiom>>();
		for(OWLLogicalAxiom axiom : equivalentToTerminology.getLogicalAxioms()){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			HashSet<OWLLogicalAxiom> namedAxioms = repeatedAxioms.get(name);
			if(namedAxioms == null){
				repeatedAxioms.put(name, new HashSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
			}
			else{
				namedAxioms.add(axiom);
			}
		}
		System.out.println(repeatedAxioms);
	}
	
	public void renameAxioms(){
		
	
		renamingMap = new HashMap<OWLClass, OWLClass>();
		ArrayList<OWLOntologyChange> ontologyChanges = new ArrayList<OWLOntologyChange>();
		
		for(OWLClass cls : repeatedAxioms.keySet()){
			HashSet<OWLLogicalAxiom> repeated = repeatedAxioms.get(cls);

			if(repeated.size() > 1){
				HashSet<OWLClass> newNames = new HashSet<OWLClass>();
				int index = 1;
				for(OWLLogicalAxiom repeatedAxiom : repeated){
					OWLClass nameOfRepeated = (OWLClass) AxiomSplitter.getNameofAxiom(repeatedAxiom);
					OWLClassExpression definitionOfRepeated = AxiomSplitter.getDefinitionofAxiom(repeatedAxiom);
					OWLClass newClass = 
							factory.getOWLClass(IRI.create(NEW_IRI_PREFIX + "#" + nameOfRepeated.getIRI().getFragment() + index));

					newNames.add(newClass);
					renamingMap.put(newClass, nameOfRepeated);
			
					OWLSubClassOfAxiom replacementAxiom = factory.getOWLSubClassOfAxiom(newClass, definitionOfRepeated);
					
					ontologyChanges.add(new AddAxiom(equivalentToTerminology, replacementAxiom));
					ontologyChanges.add(new RemoveAxiom(equivalentToTerminology, repeatedAxiom));
					
					index++;
				}
				
				OWLSubClassOfAxiom newAxiom = factory.getOWLSubClassOfAxiom(cls,factory.getOWLObjectIntersectionOf(newNames));
				newAxioms.add(newAxiom);
				ontologyChanges.add(new AddAxiom(equivalentToTerminology, newAxiom));
				
				
			}
		}
		
		manager.applyChanges(ontologyChanges);
		
		System.out.println(equivalentToTerminology.getLogicalAxioms());
		System.out.println(renamingMap);
		System.out.println(newAxioms);

		
	}
	
	public Set<OWLLogicalAxiom> revertAxioms(Set<OWLLogicalAxiom> axioms) {
		HashSet<OWLLogicalAxiom> revertedAxioms = new HashSet<OWLLogicalAxiom>();
		for(OWLLogicalAxiom axiom : axioms){
			//Don't add the newly made axioms
			if(!newAxioms.contains(axiom)){
				OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
				
				if(renamingMap.keySet().contains(name)){
					OWLSubClassOfAxiom revertedSubclass =
							factory.getOWLSubClassOfAxiom(renamingMap.get(name), definition);
					revertedAxioms.add(revertedSubclass);
					
				}
				else{
					revertedAxioms.add(axiom);
				}
			}
		}
		
		return revertedAxioms;
	}
	
	private class NotEquivalentToTerminologyException extends Exception{
		
		private static final long serialVersionUID = -4211072461164166268L;

		@Override
		public String toString() {
			return "Ontology not logically equivalent to terminology - cannot extract module";
		}
	}
	
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "/moduletest/equiv.krss");
		System.out.println(ont.getLogicalAxioms());
		try {
			EquivalentToTerminologyProcessor extractor = new EquivalentToTerminologyProcessor(ont);
		} catch (NotEquivalentToTerminologyException e) {
			e.printStackTrace();
		}
	}
}
