package uk.ac.liv.moduleextraction.extractor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Stopwatch;

import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class IteratingExtractor implements Extractor {

	private OWLOntology ontology;
	private SupportedAxiomVerifier verifier;
	private OWLOntologyManager manager;
	private int qbfChecks = 0;
	private int starExtractions = 0;
	private int amexExtrations = 0;

	public IteratingExtractor(OWLOntology ont) {
		this.ontology = ont;
		this.manager = ont.getOWLOntologyManager();
		this.verifier = new SupportedAxiomVerifier();
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		
		
		qbfChecks = starExtractions = amexExtrations = 0;
		
		Set<OWLLogicalAxiom> module = ontology.getLogicalAxioms();

		
		int iteration = 1;

		boolean sizeChanged = false;
		do {

			Set<OWLEntity> origSig = new HashSet<OWLEntity>(signature);

			if(iteration == 1){
				module = extractStarModule(createOntologyFromLogicalAxioms(module), origSig);

				int starSize = module.size();

					
				Set<OWLLogicalAxiom> unsupported = getUnsupportedAxioms(module);
				module.removeAll(unsupported);

				module  = extractSemanticModule(createOntologyFromLogicalAxioms(module), unsupported, origSig);

				sizeChanged = (module.size() != starSize);
				
			}
			else{
				int semanticSize = module.size();
				
				module = extractStarModule(createOntologyFromLogicalAxioms(module), origSig);
				
				if(module.size() != semanticSize){
					sizeChanged = true;
					
					int starSize = module.size();
					Set<OWLLogicalAxiom> unsupported = getUnsupportedAxioms(module);
					module.removeAll(unsupported);
					module  = extractSemanticModule(createOntologyFromLogicalAxioms(module), unsupported, origSig);

					sizeChanged = (module.size() != starSize);
				}
				else{
					sizeChanged = false;
				}
				
			}
			iteration++;


		} while (sizeChanged);
//

		
		
		return module;
	}
	
	
	public Set<OWLLogicalAxiom> extractStarModule(OWLOntology ontology, Set<OWLEntity> signature){
		SyntacticLocalityModuleExtractor 
		extractor = new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.STAR);
		
		Set<OWLLogicalAxiom> module = ModuleUtils.getLogicalAxioms(extractor.extract(signature));
		manager.removeOntology(ontology);
		
		starExtractions++;
		return module;
	}
	
	private Set<OWLLogicalAxiom> extractSemanticModule(OWLOntology ontology, Set<OWLLogicalAxiom> existingmodule, Set<OWLEntity> signature){
		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ontology);
		Set<OWLLogicalAxiom> module = extractor.extractModule(existingmodule, signature);
		
		qbfChecks += extractor.getMetrics().get("QBF Checks");
		
		manager.removeOntology(ontology);
		amexExtrations++;
		return module;
	}
	
	public int getQBFChecks(){
		return qbfChecks;
	}
	
	public int getAmexExtrations() {
		return amexExtrations;
	}
	
	public int getStarExtractions() {
		return starExtractions;
	}

	private Set<OWLLogicalAxiom> getUnsupportedAxioms(Set<OWLLogicalAxiom> module){
		Set<OWLLogicalAxiom> unsupported = new HashSet<OWLLogicalAxiom>();
		for(OWLLogicalAxiom axiom : module){
			if(!verifier.isSupportedAxiom(axiom)){
				unsupported.add(axiom);
			}
		}
		return unsupported;
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
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(
			Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		return null;
	}
	

}
