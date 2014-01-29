package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.checkers.MeaninglessEquivalenceChecker;
import uk.ac.liv.moduleextraction.experiments.OntologyFilters;
import uk.ac.liv.moduleextraction.experiments.RepeatedEqualitiesFilter;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter.RemovalMethod;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class NewIteratingExtractor implements Extractor {

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private int starExtractions = 0;
	private int amexExtrations = 0;

	public NewIteratingExtractor(OWLOntology ont) {
		this.ontology = ont;
		this.manager = ont.getOWLOntologyManager();
	}
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {

		Set<OWLEntity> origSig = new HashSet<OWLEntity>(signature);
		Set<OWLLogicalAxiom> module = extractStarModule(ontology, signature);
		boolean sizeChanged = false;
		do{
			int starSize = module.size();
			
			
			MeaninglessEquivalenceChecker checker = new MeaninglessEquivalenceChecker(module);
			module.removeAll(checker.getMeaninglessEquivalances());

			Set<OWLLogicalAxiom> unsupportedAxioms = getUnsupportedAxioms(module);
			module.removeAll(unsupportedAxioms);
			module  = extractSemanticModule(createOntologyFromLogicalAxioms(module), unsupportedAxioms, origSig);
			
			
			if(module.size() < starSize){
				int amexSize = module.size();
				module = extractStarModule(createOntologyFromLogicalAxioms(module), origSig);
				sizeChanged = (module.size() < amexSize);

			}
			else{
				sizeChanged = false;
			}

		}while(sizeChanged);

		return module;
	}



	private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms){
		OntologyFilters filters = new OntologyFilters();
		AxiomStructureInspector inspector = new AxiomStructureInspector(axioms);
		filters.addFilter(new SupportedExpressivenessFilter());
		filters.addFilter(new SharedNameFilter(inspector,RemovalMethod.REMOVE_EQUALITIES));
		filters.addFilter(new RepeatedEqualitiesFilter(inspector));
		return filters.getUnsupportedAxioms(axioms);
	}

	@Override	
	public Set<OWLLogicalAxiom> extractModule(
			Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		return null;
	}

	public Set<OWLLogicalAxiom> extractStarModule(OWLOntology ontology, Set<OWLEntity> signature){
		SyntacticLocalityModuleExtractor 
		extractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);

		Set<OWLLogicalAxiom> module = ModuleUtils.getLogicalAxioms(extractor.extract(signature));
		manager.removeOntology(ontology);

		starExtractions++;
		//		System.out.println("STAR: " + module.size());
		return module;
	}

	private OWLOntology createOntologyFromLogicalAxioms(Set<OWLLogicalAxiom> axioms){
		Set<OWLAxiom> newOntAxioms = new HashSet<OWLAxiom>();
		newOntAxioms.addAll(axioms);
		OWLOntology ont = null;
		try {
			ont = manager.createOntology(newOntAxioms);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		return ont;
	}




	private Set<OWLLogicalAxiom> extractSemanticModule(OWLOntology ontology, Set<OWLLogicalAxiom> existingmodule, Set<OWLEntity> signature){
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ontology);
		Set<OWLLogicalAxiom> module = extractor.extractModule(existingmodule, signature);

		manager.removeOntology(ontology);
		amexExtrations++;
		//		System.out.println("AMEX: " + module.size());
		return module;
	}

	


	public static OWLOntology createOntology(Set<OWLLogicalAxiom> axioms) {
		Set<OWLAxiom> newOntAxioms = new HashSet<OWLAxiom>();
		newOntAxioms.addAll(axioms);
		OWLOntology ont = null;
		try {
			ont = OWLManager.createOWLOntologyManager().createOntology(newOntAxioms);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		return ont;

	}
	public int getStarExtractions() {
		return starExtractions;
	}
	public int getAmexExtrations() {
		return amexExtrations;
	}
	
	public static void main(String[] args) {
//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/LOCAL/wgatens/Ontologies/" + "/semantic-only/examples/meaningless.krss");
//		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
//		Set<OWLEntity> signature;
//		Set<OWLLogicalAxiom> module = new NewIteratingExtractor(ont).extractModule(gen.generateRandomSignature(2));
//		System.out.println(module);
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		OWLClass top = factory.getOWLThing();
		for(File f: new File("/LOCAL/wgatens/Ontologies/" + "/semantic-only/EL").listFiles()){
			if(f.isFile()){
				System.out.println(f.getName());
				OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
				
					if(axiom.getSignature().contains(top)){
						System.out.println(axiom);
					}
				}
			}
		}
	}

}
