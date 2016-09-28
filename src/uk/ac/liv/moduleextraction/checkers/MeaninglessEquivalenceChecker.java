package uk.ac.liv.moduleextraction.checkers;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

import java.util.HashSet;
import java.util.Set;

public class MeaninglessEquivalenceChecker {

	private AxiomDependencies axiomDependencies;
	private Set<OWLLogicalAxiom> module;

	public MeaninglessEquivalenceChecker(Set<OWLLogicalAxiom> axioms) {
		this.module = axioms;
		this.axiomDependencies = new AxiomDependencies(axioms);
	}

	public Set<OWLLogicalAxiom> getMeaninglessEquivalances(){
		Set<OWLLogicalAxiom> meaningless = new HashSet<OWLLogicalAxiom>();

		for(OWLLogicalAxiom axiom : module){
			if(axiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES){
				if(isMeaninglessEquivalence((OWLEquivalentClassesAxiom) axiom)){
					meaningless.add(axiom);
				}
			}
		}
		return meaningless;
	}

	private boolean isMeaninglessEquivalence(OWLEquivalentClassesAxiom equiv){
		Set<OWLEntity> equivDependencies = axiomDependencies.get(equiv);
		Set<OWLEntity> coneSymbols = new HashSet<OWLEntity>(equivDependencies);
		Set<OWLEntity> nonConeSymbols = new HashSet<OWLEntity>();

		for(OWLLogicalAxiom axiom : module){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(!axiom.equals(equiv) && !equivDependencies.contains(name)){
				nonConeSymbols.addAll(axiom.getSignature());
			}
		}

		OWLDataFactory factory = OWLManager.getOWLDataFactory();

		coneSymbols.retainAll(nonConeSymbols);
		coneSymbols.remove(factory.getOWLThing());
		coneSymbols.remove(factory.getOWLNothing());

		return coneSymbols.isEmpty();
	}

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/LOCAL/wgatens/Ontologies/" + "/semantic-only/examples/meaningless.krss");
		Set<OWLLogicalAxiom> module = ont.getLogicalAxioms();
		MeaninglessEquivalenceChecker checker = new MeaninglessEquivalenceChecker(module);
		System.out.println(checker.getMeaninglessEquivalances());
	}
}
