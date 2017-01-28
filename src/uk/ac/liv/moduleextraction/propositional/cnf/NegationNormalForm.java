package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;

public class NegationNormalForm implements FormulaVisitorEx<PropositionalFormula> {

	@Override
	public PropositionalFormula visit(NamedAtom a) {
		return a;
	}

	@Override
	public PropositionalFormula visit(BooleanAtom b) {
		return b;
	}

	@Override
	public PropositionalFormula visit(Conjunction conj) {
		return new Conjunction(conj.getLeftFormula().accept(this), conj.getRightFormula().accept(this));
	}

	@Override
	public PropositionalFormula visit(Disjunction disj) {
		return new Disjunction(disj.getLeftFormula().accept(this), disj.getRightFormula().accept(this));
	}

	@Override
	public PropositionalFormula visit(Negation negation) {
		PropositionalFormula complement = negation.getComplement();
		
		/* Handle double negated atoms */
		if(complement instanceof Negation){
			return ((Negation) complement).getComplement().accept(this);
		}
		else if(complement instanceof BooleanAtom){
			BooleanAtom bool = (BooleanAtom) complement;
			return new BooleanAtom(!bool.getValue()).accept(this);
		}
		else if(complement instanceof Conjunction){
			Conjunction conj = (Conjunction) complement;
			PropositionalFormula lhsFormula = conj.getLeftFormula();
			PropositionalFormula rhsFormula = conj.getRightFormula();
			return new Disjunction(new Negation(lhsFormula), new Negation(rhsFormula)).accept(this);
		}
		else if (complement instanceof Disjunction){
			Disjunction disj = (Disjunction) complement;
			PropositionalFormula lhsFormula = disj.getLeftFormula();
			PropositionalFormula rhsFormula = disj.getRightFormula();
			return new Conjunction(new Negation(lhsFormula), new Negation(rhsFormula)).accept(this);
		}
		else{
			return new Negation(complement.accept(this));
		}
	}

	/* These shouldn't exist any more we need to remove implications
	 * and equalities before pushing negations inward
	 */
	@Override
	public PropositionalFormula visit(Equality equality) {
		return null;
	}
	@Override
	public PropositionalFormula visit(Implication implication) {
		return null;
	}

}
