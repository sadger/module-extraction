package uk.ac.liv.moduleextraction.checkers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class ELAxiomChainCollector {


	public ArrayList<OWLLogicalAxiom> collectELAxiomChain(boolean[] terminology, int currentIndex, 
			DefinitorialAxiomStore axiomStore, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM) {
		
		ArrayList<OWLLogicalAxiom> chain = new ArrayList<OWLLogicalAxiom>();
	
		for (int i = currentIndex; i >= 0; i--) {
			
			OWLLogicalAxiom axiom = axiomStore.getAxiom(i);
		
			if(terminology[i]){
				if(hasELSyntacticDependency(axiom, dependT, sigUnionSigM)){

					chain.add(axiom);
					//Update signature
					sigUnionSigM.addAll(axiom.getSignature());
					//Remove from ontology
					terminology[i] = false;

				}
			}
			
		}


		return chain;

	}
	
	public boolean hasELSyntacticDependency(OWLLogicalAxiom axiom, AxiomDependencies dependT, Set<OWLEntity> sigUnionSigM){
		OWLClass axiomName = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
		ELValidator validator = new ELValidator();

		if( !sigUnionSigM.contains(axiomName) || !validator.isELAxiom(axiom)){
			return false;
		}
		else{
			HashSet<OWLEntity> intersect = new HashSet<OWLEntity>(dependT.get(axiom));
			intersect.retainAll(sigUnionSigM);
			
			return !intersect.isEmpty();
		}

	}
	
//	public static void main(String[] args) {
//		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/axiomdep.krss");
//		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
//		AxiomDependencies dep = new AxiomDependencies(ont);
//		ELAxiomChainCollector chain = new ELAxiomChainCollector();
//		Set<OWLEntity> sig = gen.generateRandomSignature(3);
//		System.out.println(ont);
//		System.out.println("Sig: " + sig);
//		
//		ArrayList<OWLLogicalAxiom> depsorted = dep.getDefinitorialSortedAxioms();
//		
//		
//		for (int i = depsorted.size()-1; i >= 0 ; i--) {
//			OWLLogicalAxiom axiom = depsorted.get(i);
//			if(chain.hasELSyntacticDependency(axiom, dep, sig)){
//				System.out.println("Has dep: " +  axiom);
//				sig.addAll(axiom.getSignature());
//			}
//		}
		
			
		
//	}
}

