package uk.ac.liv.moduleextraction.extractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class EquivalentToTerminologyProcessor {

	private Logger logger = LoggerFactory.getLogger(EquivalentToTerminologyProcessor.class);
	

	private static final String NEW_IRI_PREFIX = "http://www.csc.liv.ac.uk";
	private OWLOntology equivalentToTerminology;
	private HashMap<OWLClass, HashSet<OWLLogicalAxiom>> repeatedAxioms;
	private HashMap<OWLClass, OWLClass> renamingMap;
	private HashSet<OWLLogicalAxiom> newAxioms = new HashSet<OWLLogicalAxiom>();
	private OWLDataFactory factory;
	
	public EquivalentToTerminologyProcessor(OWLOntology ontology) throws NotEquivalentToTerminologyException, OWLOntologyCreationException {
		EquivalentToTerminologyChecker equivToTermChecker = new EquivalentToTerminologyChecker();
	
		if(!equivToTermChecker.isEquivalentToTerminology(ontology)){
			throw new NotEquivalentToTerminologyException();
		}
		else{
			this.equivalentToTerminology = ontology;
		}		
		countRepeatedAxioms();
	}
	
	public void countRepeatedAxioms(){
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
	}
	
	public OWLOntology getConvertedOntology() throws OWLOntologyCreationException{
		logger.debug("{}","Performing preprocessing on ontology");
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		factory = OWLManager.getOWLDataFactory();
		
		OWLOntology convertedOntology = ontologyManager.createOntology();
		
		renamingMap = new HashMap<OWLClass, OWLClass>();
		
		for(OWLClass cls : repeatedAxioms.keySet()){
			HashSet<OWLLogicalAxiom> repeated = repeatedAxioms.get(cls);

			if(repeated.size() > 1){
				HashSet<OWLClass> newNames = new HashSet<OWLClass>();
				int index = 1;
				for(OWLLogicalAxiom repeatedAxiom : repeated){
					OWLClass nameOfRepeated = (OWLClass) AxiomSplitter.getNameofAxiom(repeatedAxiom);
					OWLClassExpression definitionOfRepeated = AxiomSplitter.getDefinitionofAxiom(repeatedAxiom);
					OWLClass newClass = 
							factory.getOWLClass(IRI.create(NEW_IRI_PREFIX + "#RENAMED_" + nameOfRepeated.getIRI().getFragment() + "_" + index));

					newNames.add(newClass);
					renamingMap.put(newClass, nameOfRepeated);
			
					OWLSubClassOfAxiom replacementAxiom = factory.getOWLSubClassOfAxiom(newClass, definitionOfRepeated);
					
					ontologyManager.addAxiom(convertedOntology, replacementAxiom);
					
					index++;
				}
				
				OWLSubClassOfAxiom newAxiom = factory.getOWLSubClassOfAxiom(cls,factory.getOWLObjectIntersectionOf(newNames));
				newAxioms.add(newAxiom);
				ontologyManager.addAxiom(convertedOntology, newAxiom);
			}
			else{
				ontologyManager.addAxioms(convertedOntology, repeatedAxioms.get(cls));
			}
		}
		return convertedOntology;
	
	}

	
	
	public Set<OWLLogicalAxiom> postProcessModule(Set<OWLLogicalAxiom> module){
		logger.debug("{}","Performing postprocessing on module");
		HashSet<OWLLogicalAxiom> toAdd = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> toRemove = new HashSet<OWLLogicalAxiom>();

		
		for(OWLLogicalAxiom axiom : module){
			
			if(newAxioms.contains(axiom)){
				toRemove.add(axiom);
			}
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
			
		
			if(renamingMap.keySet().contains(name)){
				OWLSubClassOfAxiom revertedSubclass =
						factory.getOWLSubClassOfAxiom(renamingMap.get(name), definition);
				
				toRemove.add(axiom);
				toAdd.add(revertedSubclass);

			}

		}
		module.removeAll(toRemove);
		module.addAll(toAdd);
		
		return module;
	}
	


	
	
}
