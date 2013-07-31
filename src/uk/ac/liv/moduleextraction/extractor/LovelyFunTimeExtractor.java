package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.axioms.ELValidator;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class LovelyFunTimeExtractor implements Extractor {

	private OWLOntology ontology;
	private SupportedAxiomVerifier verifier;
	private OWLOntologyManager manager;
	private int qbfChecks = 0;
	private int starExtractions = 0;
	private int amexExtrations = 0;

	public LovelyFunTimeExtractor(OWLOntology ont) {
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

				if(module.size() != starSize){
					sizeChanged = true;
				}
				else{
					sizeChanged = false;
				}
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

					if(module.size() != starSize){
						sizeChanged = true;
					}
					else{
						sizeChanged = false;
					}
				}
				else{
					sizeChanged = false;
				}
				
			}
			iteration++;


		} while (sizeChanged);


		
		
		return module;
	}
	
	
	private Set<OWLLogicalAxiom> extractStarModule(OWLOntology ontology, Set<OWLEntity> signature){
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
	
	public static void main(String[] args) throws IOException {
		
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_08.09d.OWL");
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "/paper-500random"));
		
		LovelyFunTimeExtractor fun = new LovelyFunTimeExtractor(ont);
		int[] tests = {2,289,268};
		for (int i = 1; i <= 300; i++) {
			System.out.println("===" + i + "===");
			fun.extractModule(man.readFile("random500-" + i));
		}


		


	}

}
