package experiments;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import main.ModuleExtractor;

import ontologyutils.AxiomExtractor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import qbf.QBFSolverException;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import util.ModuleUtils;

public class ExtractionComparision {
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	public static void main(String[] args) {
		ExtractionComparision compare = new ExtractionComparision();
		
		///OWLOntology ontology = compare.loadOntology("/users/loco/wgatens/Ontologies/module/pathway.obo");
		OWLOntology ontology = compare.loadOntology("/home/william/PhD/Ontologies/NCI/nci-09.03d.owl");
		//"/users/loco/wgatens/Ontologies/NCI/nci-09.03d.owl"
		//"/users/loco/wgatens/Ontologies/NCI/nci-10.02d.owl";
		OWLOntologyManager manager = compare.getManager();
		
		
		
		SyntacticLocalityModuleExtractor syntaxModExtractor = 
				new SyntacticLocalityModuleExtractor(manager, ontology, ModuleType.BOT);
		ModuleExtractor moduleExtractor = new ModuleExtractor();
		
		/* The tests use the same signature (just OWLClass) but one must be converted to OWLEntities as expected 
		 * by the OWLAPI*/
		Set<OWLClass> randomClassSignature = ModuleUtils.generateRandomClassSignature(ontology, 200);
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
	
	private OWLOntology loadOntology(String pathName) {
		ToStringRenderer stringRender = ToStringRenderer.getInstance();
		DLSyntaxObjectRenderer renderer;
		renderer =  new DLSyntaxObjectRenderer();
		stringRender.setRenderer(renderer);

		OWLOntology ontology = null;
		
		try {
			ontology =  
					manager.loadOntologyFromOntologyDocument
					(new File(pathName));
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		AxiomExtractor extract = new AxiomExtractor();
		ontology = extract.extractInclusionsAndEqualities(ontology);
		
		return ontology;

	}
	

}
