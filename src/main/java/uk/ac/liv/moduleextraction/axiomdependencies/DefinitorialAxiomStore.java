package uk.ac.liv.moduleextraction.axiomdependencies;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefinitorialAxiomStore{
	final OWLLogicalAxiom[] axioms;
	
	public DefinitorialAxiomStore(Collection<OWLLogicalAxiom> axs) {
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