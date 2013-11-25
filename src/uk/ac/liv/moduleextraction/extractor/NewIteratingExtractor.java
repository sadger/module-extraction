package uk.ac.liv.moduleextraction.extractor;

import java.util.HashSet;
import java.util.Set;

import junit.extensions.RepeatedTest;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.experiments.OntologyFilters;
import uk.ac.liv.moduleextraction.experiments.SharedNameExperiment;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.experiments.SharedNameFilter.RemovalMethod;
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
	private RemovalMethod removal_method;

	public NewIteratingExtractor(OWLOntology ont, RemovalMethod method) {
		this.ontology = ont;
		this.manager = ont.getOWLOntologyManager();
		this.removal_method = method;
	}
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {

		Set<OWLEntity> origSig = new HashSet<OWLEntity>(signature);
		Set<OWLLogicalAxiom> module = extractStarModule(ontology, signature);
		boolean sizeChanged = false;
		do{
			int starSize = module.size();
			Set<OWLLogicalAxiom> unsupportedAxioms = getUnsupportedAxioms(module);
			module.removeAll(unsupportedAxioms);
			module  = extractSemanticModule(createOntologyFromLogicalAxioms(module), unsupportedAxioms, origSig);

			if(module.size() < starSize){
				int amexSize = module.size();
				module = extractStarModule(createOntologyFromLogicalAxioms(module), origSig);
				sizeChanged = (module.size() < amexSize ? true : false);
			}
			else{
				sizeChanged = false;
			}

		}while(sizeChanged);

		return module;
	}



	private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> axioms){
		OntologyFilters filters = new OntologyFilters();
		filters.addFilter(new SupportedExpressivenessFilter());
		filters.addFilter(new SharedNameFilter(new AxiomStructureInspector(axioms), removal_method));
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

}
