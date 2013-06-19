package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.NewSyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class SemanticRuleExtractor implements Extractor{

	ChainDependencies dependT;
	Set<OWLLogicalAxiom> module;
	Set<OWLEntity> sigUnionSigM;
	NewSyntacticDependencyChecker syntacticDependencyChecker;
	DefinitorialAxiomStore axiomStore;
	LHSSigExtractor lhsExtractor;
	InseperableChecker inseperableChecker;
	
	long syntacticChecks = 0; // A syntactic iteration (total checks = this + qbfchecks)
	long timeTaken = 0; //Time taken to setup and extract the module (ms)
	long qbfChecks = 0;
	
	
	public static class DefinitorialAxiomStore{
		final OWLLogicalAxiom[] axioms;
		
		public DefinitorialAxiomStore(List<OWLLogicalAxiom> axs) {
			axioms = axs.toArray(new OWLLogicalAxiom[axs.size()]);	
		}
		
		public OWLLogicalAxiom getAxiom(int index){
			return axioms[index];
		}
		
		public boolean[] allAxiomsAsBoolean(){
			boolean[] booleanAxioms = new boolean[axioms.length];
			for (int i = 0; i < booleanAxioms.length; i++) {
				booleanAxioms[i] = true;
			}
			return cloneSubset(booleanAxioms);
		}
		
		private boolean[] cloneSubset(boolean[] oldSubset) {
			boolean[] clonedSubset = new boolean[oldSubset.length];
			System.arraycopy(oldSubset, 0, clonedSubset, 0, oldSubset.length);
			return clonedSubset;
		}
		
		public int getSubsetCardinality(boolean[] subset){
			int cardinality = 0;
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					cardinality++;
				}
			}
			return cardinality;
		}
		
		public OWLLogicalAxiom[] getSubsetAsArray(boolean[] subset){
			OWLLogicalAxiom[] newArray = new OWLLogicalAxiom[getSubsetCardinality(subset)];
			int currentIndex = 0;
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					newArray[currentIndex] = getAxiom(i);
					currentIndex++;
				}
			}
			return newArray;
		}

		public List<OWLLogicalAxiom> getSubsetAsList(boolean[] subset){
			ArrayList<OWLLogicalAxiom> newSet = new ArrayList<OWLLogicalAxiom>();
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					newSet.add(axioms[i]);
				}
			}
			return newSet;
		}

		/* Worse case linear time - can improve this with binary search probably */
		public void removeAxiom(boolean[] subset, OWLLogicalAxiom axiom){
			for (int i = 0; i < axioms.length; i++) {
				if(axioms[i].equals(axiom)){
					subset[i] = false;
					break;
				}
			}
		}
	}
	
	
	public SemanticRuleExtractor(OWLOntology ontology) {
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(ontology);
		ArrayList<OWLLogicalAxiom> depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();
		dependT = new ChainDependencies();
		dependT.updateDependenciesWith(depthSortedAxioms);
		axiomStore = new DefinitorialAxiomStore(depthSortedAxioms);
		
		syntacticDependencyChecker = new NewSyntacticDependencyChecker();
		lhsExtractor = new LHSSigExtractor();
		inseperableChecker = new InseperableChecker();
	}
	

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		/* Reset all the metrics for new extraction */
		syntacticChecks = 0; 
		timeTaken = 0; 
		qbfChecks = 0;
		inseperableChecker.resetMetrics();
		
		long startTime = System.currentTimeMillis();
		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);
		
		try {
			applyRules(terminology);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		timeTaken = System.currentTimeMillis() - startTime;
		return module;
	}
	
	public LinkedHashMap<String, Long> getMetrics() {
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		metrics.put("Module size", (long) module.size());
		metrics.put("Time taken", timeTaken);
		metrics.put("Syntactic Checks", syntacticChecks);
		metrics.put("QBF Checks", qbfChecks);
		return metrics;
	}


	public LinkedHashMap<String, Long> getQBFMetrics() {
		return inseperableChecker.getQBFMetrics();
	}

	
	
	private void applyRules(boolean[] terminology) throws IOException, QBFSolverException{
		applySyntacticRule(terminology);
		
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(axiomStore.getSubsetAsList(terminology),sigUnionSigM,dependT);
		qbfChecks++;
		if(inseperableChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			OWLLogicalAxiom insepAxiom = findSeparableAxiom(terminology);
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			axiomStore.removeAxiom(terminology, insepAxiom);
			applyRules(terminology);
		}
	}


	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {
		System.err.println("Removing inseparability cause");
		SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,dependT);
		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		qbfChecks += search.getCheckCount();
		return insepAxiom;
	}


	private void applySyntacticRule(boolean[] terminology){
		boolean change = true;
		
		while(change){
			change = false;
			for (int i = 0; i < terminology.length; i++) {
				
				if(terminology[i]){
					
					OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
					syntacticChecks++;
					if(syntacticDependencyChecker.hasSyntacticSigDependency(chosenAxiom, dependT, sigUnionSigM)){
						
						change = true;
						
						module.add(chosenAxiom);
						terminology[i] = false;
						sigUnionSigM.addAll(chosenAxiom.getSignature());
						
					}
				}
			}
		}
	
	}



	



}
