package uk.ac.liv.moduleextraction.propositional.satclauses;

import uk.ac.liv.moduleextraction.propositional.formula.NamedAtom;
import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

import java.util.HashMap;


public class NumberMap extends HashMap<PropositionalFormula, Integer>{

	private static final long serialVersionUID = -5427264596872498949L;

	private ClauseSet clauseSet;
	private int variableCount = 0;

	public NumberMap(){

	}

	public NumberMap(ClauseSet clauseSet) {
		this.clauseSet = clauseSet;
		populateNumberMap();
	}
	
	public void populateNumberMap(){
		for(NamedAtom atom : clauseSet.getVariables()){
			updateNumberMap(atom);
		}
	}
	
	public void updateNumberMap(NamedAtom atom){
		//If we don't already have a mapping for this atom
		if(get(atom) == null){
			int i = ++variableCount;
			put(atom, i);
		}

	}
	
	public int getVariableCount(){
		return variableCount;
	}

	@Override
	public Integer put(PropositionalFormula formula, Integer value) {
		return super.put(formula, value);
	}

}
