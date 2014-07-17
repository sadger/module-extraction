package uk.ac.liv.moduleextraction.extractor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.EquivalentToTerminologyChecker;
import uk.ac.liv.ontologyutils.ontologies.TerminologyChecker;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class AcylicNotTerminologyExtractor implements Extractor {
	
	enum RemovalStrategy{
		REMOVE_SUBSUMPTIONS,
		REMOVE_EQUALITIES
	}
	
	OWLOntology ontology;
	RemovalStrategy strategy;
	
	public AcylicNotTerminologyExtractor(OWLOntology ontology, RemovalStrategy strategy) {
		this.ontology = ontology;
		this.strategy = strategy;
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		TerminologyChecker termChecker = new TerminologyChecker();
		EquivalentToTerminologyChecker equivChecker = new EquivalentToTerminologyChecker();
		OWLOntologyManager ontManager = ontology.getOWLOntologyManager();
		
		//Extract STAR Module
		SyntacticLocalityModuleExtractor starExtractor = 
				new SyntacticLocalityModuleExtractor(ontManager, ontology, ModuleType.STAR);
		
		
		OWLOntology starOntology = null;
		int starSize = 0;
		try {
			starOntology = starExtractor.extractAsOntology(signature, IRI.create(UUID.randomUUID().toString()));
			starSize = starOntology.getLogicalAxiomCount();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Set<OWLLogicalAxiom> module = null;
	
		if(termChecker.isTerminology(starOntology)){
			System.out.println("Is terminology");
			module = new AMEX(starOntology).extractModule(signature);
		}
		else if(equivChecker.isEquivalentToTerminology(starOntology)){
			System.out.println("Is equivalent to terminology");
			module = new EquivalentToTerminologyExtractor(starOntology).extractModule(signature);
		}
		else{
			AxiomStructureInspector structureInspector = new AxiomStructureInspector(starOntology);
			if(structureInspector.countNamesWithRepeatedEqualities() > 0){
				module = starOntology.getLogicalAxioms();
			}
			else{
				Set<OWLLogicalAxiom> initialModule = new HashSet<OWLLogicalAxiom>();
				
					for(OWLClass cls : structureInspector.getSharedNames()){
						if(strategy == RemovalStrategy.REMOVE_SUBSUMPTIONS){
							initialModule.addAll(structureInspector.getPrimitiveDefinitions(cls));
						}
						else if(strategy == RemovalStrategy.REMOVE_EQUALITIES){
							initialModule.addAll(structureInspector.getDefinitions(cls));
						}
					}
				ontManager.removeAxioms(starOntology, initialModule);
				module = new EquivalentToTerminologyExtractor(starOntology).extractModule(initialModule, signature);
				
		
			}
		}
		
		ontManager.removeOntology(starOntology);
		System.out.println("Star/Sem:" + starSize + "/" + module.size());
		return module;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		System.err.println("NOT YET SUPPORTED");
		return null;
	}

	
}
