package uk.ac.liv.moduleextraction.experiments;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.extractor.EquivalentToTerminologyExtractor;
import uk.ac.liv.moduleextraction.extractor.Extractor;
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
		
		Set<OWLLogicalAxiom> module = extractStarModule(ontology, signature);

		return module;
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
		return module;
	}

}
