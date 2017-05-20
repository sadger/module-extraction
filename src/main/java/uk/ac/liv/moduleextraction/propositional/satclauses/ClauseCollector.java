package uk.ac.liv.moduleextraction.propositional.satclauses;

import uk.ac.liv.moduleextraction.propositional.cnf.CNFFormula;
import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;

public class ClauseCollector implements FormulaVisitor<Clause>{

	private ClauseSet clauseSet;

	public ClauseCollector(CNFFormula formula) {
		clauseSet = new ClauseSet();
		/* If our formula is a single disjunction we must pass in a new clause */
		(formula.asPropositionalFormula()).accept(this, new Clause());
	}

	public ClauseSet getClauseSet() {
		return clauseSet;
	}

	@Override
	public void visit(NamedAtom a, Clause clause){
		if(clause.getLiterals().contains(new Negation(a))){
			clauseSet.remove(clause);
		}
		else{
			clause.add(a);
			clauseSet.addVariable(a);
		}
	}

	@Override
	public void visit(BooleanAtom b, Clause clause) {
		/* No need to add false to a clause as simplifying removes it
		   False v P = P, If a clause contains true then we can
		   simple discard that clause as it makes the whole clause 
		   True and (True & Clause) = Clause */
		if(b.getValue()){
			clauseSet.remove(clause);
		}

	}

	@Override
	public void visit(Conjunction conj, Clause c) {
		/*Create a new clause for each side of the conjunction */
		conj.getLeftFormula().accept(this,new Clause());
		conj.getRightFormula().accept(this, new Clause());
	}

	@Override
	public void visit(Disjunction disj, Clause clause) {
		clauseSet.add(clause);
		/* Add each side of the disjunction to the current clause */
		disj.getLeftFormula().accept(this,clause);
		disj.getRightFormula().accept(this,clause);

	}

	@Override
	public void visit(Negation negation, Clause clause) {
		/* Add named atoms under negation */
		PropositionalFormula complement = negation.getComplement();
		if(complement instanceof NamedAtom){
			NamedAtom positiveAtom = (NamedAtom) negation.getComplement();
			if(clause.getLiterals().contains(positiveAtom)){
				clauseSet.remove(clause);
			}
			else{
				clauseSet.addVariable(positiveAtom);
				clause.add(negation);
			}
		}
		/* Handle negated boolean by creating a new
		 * boolean with the complement value */
		else if(complement instanceof BooleanAtom){
			BooleanAtom b = (BooleanAtom) complement;
			new BooleanAtom(!b.getValue()).accept(this,clause);
		}

	}

	/*These don't appear in CNF formulas */
	@Override
	public void visit(Equality equality, Clause c) {
	}
	@Override
	public void visit(Implication implication, Clause c) {
	}

}
