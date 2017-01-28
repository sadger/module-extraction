package uk.ac.liv.moduleextraction.propositional.satclauses;

import uk.ac.liv.moduleextraction.propositional.formula.NamedAtom;

import java.util.HashSet;
import java.util.Set;



public class ClauseSet extends HashSet<Clause>{

	private static final long serialVersionUID = 4062737891391580275L;
	
	private Set<NamedAtom> variables;
	
	public ClauseSet() {
		this.variables = new HashSet<NamedAtom>();
	}
	
	public Set<NamedAtom> getVariables(){
		return variables;
	}
	
	
	public void addVariable(NamedAtom atom){
		variables.add(atom);
	}
	
	public void addClauseSet(ClauseSet clauseSet){
		addAll(clauseSet);
		variables.addAll(clauseSet.getVariables());
	}
	
	@Override
	public String toString() {
		String result = "";

		if(isEmpty()){
			result = "<<EMPTY>>";
		}
		else{
			StringBuilder buff = new StringBuilder();
			String sep = "";

			for (Clause clause : this) {
				buff.append(sep);
				buff.append(clause.toString());
				sep = " & ";
			}
			result = buff.toString();
		}

		return result;
	}



	
	
}
