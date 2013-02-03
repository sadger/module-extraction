package uk.ac.liv.moduleextraction.testing;

import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

public class AlternativeApproach {
	
	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();
	
	/* Data structures */
	private LinkedHashList<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	

	
	
	public AlternativeApproach(LinkedHashList<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> mod, Set<OWLEntity> sig) throws IOException, QBFSolverException{
		System.out.println("|Term|: " + term.size());
		System.out.println("|Mod|: " + mod.size());
		System.out.println("|Sig|: " + sig.size());
		
		this.terminology = term;
		this.module = mod;
		
		this.sigUnionSigM = sig;
		sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
		
		splitTest(  );
	}
	
	private void splitTest() throws IOException, QBFSolverException{	
		

		/* Represents the last axioms added or removed from the split test */
		LinkedHashList<OWLLogicalAxiom> lastAdded = getTopHalf(terminology);
		LinkedHashList<OWLLogicalAxiom> bottom = getBottomHalf(terminology);
		
		
		LinkedHashList<OWLLogicalAxiom> W = lastAdded;
		LinkedHashList<OWLLogicalAxiom> x = new LinkedHashList<OWLLogicalAxiom>(); 
		x.addAll(lastAdded);
		x.addAll(bottom);
		
		int i = 0;
		for(OWLLogicalAxiom ax : terminology){
			if(!x.contains(ax)){
				System.out.println(i + ":" + ax);
			}
			i++;
		}
		
		System.out.println("X: " + x.size());
		
		while(lastAdded.size() >= 1){
			
			ChainDependencies Wdeps = new ChainDependencies();
			Wdeps.updateDependenciesWith(W);
			Set<OWLLogicalAxiom> lhsW = lhsExtractor.getLHSSigAxioms(W, sigUnionSigM, Wdeps);
			if(!insepChecker.isSeperableFromEmptySet(lhsW, sigUnionSigM)){
				lastAdded = getTopHalf(bottom);
				W.addAll(lastAdded);
				bottom.removeAll(lastAdded);

			}
			else{
				System.out.println("Seperable");
				
			LinkedHashList<OWLLogicalAxiom> toRemove = getBottomHalf(lastAdded);
			lastAdded = getTopHalf(lastAdded);
			
			W.removeAll(toRemove);
			
			LinkedHashList<OWLLogicalAxiom> tmp = new LinkedHashList<OWLLogicalAxiom>(toRemove);
			tmp.addAll(bottom);
			
			bottom = tmp;
			
				
				
			}
			System.out.println("W :" + W.size());
		}

		
		
	}

	
	private LinkedHashList<OWLLogicalAxiom> getTopHalf(LinkedHashList<OWLLogicalAxiom> axiomList){
		
		int fromIndex = 0;
		int toIndex = (int) Math.floor(axiomList.size()/2);
		
		LinkedHashList<OWLLogicalAxiom> topHalf =
				new LinkedHashList<OWLLogicalAxiom>(axiomList.subList(fromIndex, toIndex));
		System.out.println("|Top Half|: " + topHalf.size());
		return topHalf;

	}
	
	private LinkedHashList<OWLLogicalAxiom> getBottomHalf(LinkedHashList<OWLLogicalAxiom> axiomList){
		
		int fromIndex = (int) Math.floor(axiomList.size()/2);
		int toIndex = axiomList.size();
			
		LinkedHashList<OWLLogicalAxiom> bottomHalf =
				new LinkedHashList<OWLLogicalAxiom>(axiomList.subList(fromIndex, toIndex));
		System.out.println("|Bottom Half|: " + bottomHalf.size());
		return bottomHalf;

	}
	
	
	
}
