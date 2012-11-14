package experiments;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;
import main.ModuleExtractor;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import qbf.QBFSolverException;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import util.ModulePaths;
import util.ModuleUtils;

public class ExtractionComparision {
	private static final int SIGNATURE_SIZE = 100;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	public static void main(String[] args) {
		
		ExtractionComparision compare = new ExtractionComparision();
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"NCI/nci_08_09d-terminology.owl");
		OWLOntologyManager manager = compare.getManager();
		
		
		
		SyntacticLocalityModuleExtractor syntaxModExtractor = 
				new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.BOT);
		ModuleExtractor moduleExtractor = new ModuleExtractor();
		
		/* The tests use the same signature (just OWLClass) but one must be converted to OWLEntities as expected 
		 * by the OWLAPI*/
		Set<OWLClass> randomClassSignature = ModuleUtils.generateRandomClassSignature(ontology, SIGNATURE_SIZE);
		Set<OWLEntity> randomSignature = 
				new HashSet<OWLEntity>(randomClassSignature);
		
		System.out.println(randomClassSignature);
		

		HashSet<OWLLogicalAxiom> syntacticModule = (HashSet<OWLLogicalAxiom>) compare.getLogicalAxioms(syntaxModExtractor.extract(randomSignature));
		@SuppressWarnings("unchecked")
		HashSet<OWLLogicalAxiom> syntacticCopy = (HashSet<OWLLogicalAxiom>) syntacticModule.clone();
		System.out.println("DONE size=" + syntacticCopy.size());
		Set<OWLLogicalAxiom> syntThenSemanticModule = null;
		try {
			syntThenSemanticModule = moduleExtractor.extractModule(syntacticCopy, randomClassSignature);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		System.out.println("Signature Size: " + SIGNATURE_SIZE);
		System.out.println("Syntatic Size: " + syntacticModule.size());
		System.out.println("Synt->Semantic Size: " + syntThenSemanticModule.size());
		
		System.out.println(syntThenSemanticModule);

	}

	public Set<OWLLogicalAxiom> getLogicalAxioms(Set<OWLAxiom> axioms){
		HashSet<OWLLogicalAxiom> result = new HashSet<OWLLogicalAxiom>();
		for(OWLAxiom ax : axioms){
			if(ax.isLogicalAxiom()){
				result.add((OWLLogicalAxiom) ax);
			}
		}
		return result;
	}
	
	public OWLOntologyManager getManager() {
		return manager;
	}
	

}
