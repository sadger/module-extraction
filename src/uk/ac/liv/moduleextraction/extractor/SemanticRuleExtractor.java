package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.ChainAxiomCollector;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.NewSyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class SemanticRuleExtractor implements Extractor{

	ChainDependencies dependT;
	ArrayList<OWLLogicalAxiom> depthSortedAxioms;
	Set<OWLLogicalAxiom> module;
	Set<OWLEntity> sigUnionSigM;
	NewSyntacticDependencyChecker syntacticDependencyChecker;
	LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	InseperableChecker insepChecker = new InseperableChecker();
	
	public SemanticRuleExtractor(OWLOntology ontology) {
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(ontology.getLogicalAxioms());
		this.depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();
		this.dependT = new ChainDependencies();
		this.syntacticDependencyChecker = new NewSyntacticDependencyChecker();
		dependT.updateDependenciesWith(depthSortedAxioms);
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature){
		ArrayList<OWLLogicalAxiom> terminology = new ArrayList<OWLLogicalAxiom>(depthSortedAxioms);

		module = new HashSet<OWLLogicalAxiom>();
		sigUnionSigM = signature;
		
		System.out.println("Term size " + terminology.size());
		try {
			applyRules(terminology);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		return module;
	}
	
	public void applyRules(List<OWLLogicalAxiom> terminology) throws IOException, QBFSolverException{
		applySyntacticCheck(terminology);
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(terminology,sigUnionSigM,dependT);
		if(insepChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(terminology, module, sigUnionSigM, dependT);
			OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			terminology.remove(insepAxiom);
			applyRules(terminology);
		}
	}
	
	private void applySyntacticCheck(List<OWLLogicalAxiom> terminology) {
		ListIterator<OWLLogicalAxiom> axiomIterator = terminology.listIterator();
		ChainAxiomCollector chainCollector = new ChainAxiomCollector();

		/* Terminology is the value of T\M as we remove items and add them to the module */
		while(axiomIterator.hasNext()){
		
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();
			
			if(syntacticDependencyChecker.hasSyntacticSigDependency(chosenAxiom, dependT, sigUnionSigM)){
												
				/*Find the chain of axioms and remove them from ontology (including the one we found the initial dependency on */
				Set<OWLLogicalAxiom> axiomChain = chainCollector.collectAxiomChain(axiomIterator, dependT, sigUnionSigM);
				module.addAll(axiomChain);
				
				sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(axiomChain));
				
				terminology.removeAll(axiomChain);

				/* Reset the iterator to start of the list*/
				axiomIterator = terminology.listIterator(0);
			}
		}

	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/NCI/nci-08.09d-terminology.owl");
		SemanticRuleExtractor extractor = new SemanticRuleExtractor(ont);
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		
		Set<OWLEntity> sig = gen.generateRandomSignature(0);
		for (int i = 0; i < 1000; i++) {
			System.out.println(extractor.extractModule(sig).size());
		}
		
	}

}
