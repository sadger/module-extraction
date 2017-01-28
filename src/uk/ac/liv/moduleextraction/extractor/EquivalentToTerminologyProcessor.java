package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.liv.moduleextraction.util.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EquivalentToTerminologyProcessor {

	private Logger logger = LoggerFactory.getLogger(EquivalentToTerminologyProcessor.class);


	private static final String NEW_IRI_PREFIX = "http://www.csc.liv.ac.uk";
	private HashMap<OWLClass, HashSet<OWLLogicalAxiom>> repeatedAxioms;
	private HashMap<OWLClass, OWLClass> renamingMap;
	//Freshly created axioms for replacement
	private HashSet<OWLLogicalAxiom> freshAxioms = new HashSet<OWLLogicalAxiom>();
	private OWLDataFactory factory;

	private Set<OWLLogicalAxiom> axioms;

	public EquivalentToTerminologyProcessor(OWLOntology ontology) throws NotEquivalentToTerminologyException, OWLOntologyCreationException {
		this(ontology.getLogicalAxioms());
	}

	public EquivalentToTerminologyProcessor(Set<OWLLogicalAxiom> axioms) throws NotEquivalentToTerminologyException {
		EquivalentToTerminologyChecker equivToTermChecker = new EquivalentToTerminologyChecker();
		if(!equivToTermChecker.isEquivalentToTerminology(axioms)){
			throw new NotEquivalentToTerminologyException();
		}
		else{
			this.axioms = axioms;
			countRepeatedAxioms();
		}
	}

	public void countRepeatedAxioms(){
		repeatedAxioms = new HashMap<OWLClass, HashSet<OWLLogicalAxiom>>();
		for(OWLLogicalAxiom axiom : axioms){
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

	public Set<OWLLogicalAxiom> getConvertedAxioms() throws OWLOntologyCreationException{
		//logger.debug("{}","Performing preprocessing on ontology");
		Set<OWLLogicalAxiom> convertedAxioms = new HashSet<OWLLogicalAxiom>();
		factory = OWLManager.getOWLDataFactory();

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
							factory.getOWLClass(IRI.create(NEW_IRI_PREFIX + "#RENAMED_" + nameOfRepeated.getIRI() + "_" + index));

					newNames.add(newClass);
					renamingMap.put(newClass, nameOfRepeated);

					OWLSubClassOfAxiom replacementAxiom = factory.getOWLSubClassOfAxiom(newClass, definitionOfRepeated);

					convertedAxioms.add(replacementAxiom);
					freshAxioms.add(replacementAxiom);

					index++;
				}

				//New conjunction axiom
				OWLSubClassOfAxiom newAxiom = factory.getOWLSubClassOfAxiom(cls,factory.getOWLObjectIntersectionOf(newNames));
				freshAxioms.add(newAxiom);
				convertedAxioms.add(newAxiom);
			}
			else{
				convertedAxioms.addAll(repeatedAxioms.get(cls));
			}
		}
		return convertedAxioms;

	}



	public Set<OWLLogicalAxiom> postProcessModule(Set<OWLLogicalAxiom> module){
		//logger.debug("{}","Performing postprocessing on module");
		HashSet<OWLLogicalAxiom> toAdd = new HashSet<OWLLogicalAxiom>();
		HashSet<OWLLogicalAxiom> toRemove = new HashSet<OWLLogicalAxiom>();
		AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();

		for(OWLLogicalAxiom axiom : module){
			//The axioms to remap and remove are terminological but ontology may
			//contain non-terminological axioms through the hybrid approach
			if(verifier.isSupportedAxiom(axiom)){
				if(freshAxioms.contains(axiom)){
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


		}
		module.removeAll(toRemove);
		module.addAll(toAdd);

		return module;
	}


	public static void main(String[] args) throws OWLOntologyCreationException, NotEquivalentToTerminologyException {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/equivterm.krss");
		EquivalentToTerminologyProcessor terminologyProcessor = new EquivalentToTerminologyProcessor(ont.getLogicalAxioms());
		ModuleUtils.remapIRIs(ont, "X");

		System.out.println(ont.getLogicalAxioms());

		OWLDataFactory f = ont.getOWLOntologyManager().getOWLDataFactory();
		OWLClass a = f.getOWLClass(IRI.create("X#A"));
		OWLClass b = f.getOWLClass(IRI.create("X#B"));
		OWLClass c = f.getOWLClass(IRI.create("X#C"));
		OWLClass d = f.getOWLClass(IRI.create("X#D"));
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.add(a);
		sig.add(b);

		EquivalentToTerminologyExtractor extract = new EquivalentToTerminologyExtractor(ont);
		NDepletingModuleExtractor extract1 = new NDepletingModuleExtractor(1,ont.getLogicalAxioms());
		NDepletingModuleExtractor extract2 = new NDepletingModuleExtractor(2,ont.getLogicalAxioms());

		System.out.println(extract.extractModule(sig));
		System.out.println(extract.getMetrics());
		System.out.println(extract1.extractModule(sig));
		System.out.println(extract1.getMetrics());
		System.out.println(extract2.extractModule(sig));
		System.out.println(extract2.getMetrics());
		System.out.println(extract1.extractModule(sig));
		System.out.println(extract1.getMetrics());
		System.out.println(extract2.extractModule(sig));
		System.out.println(extract2.getMetrics());
	}




}
