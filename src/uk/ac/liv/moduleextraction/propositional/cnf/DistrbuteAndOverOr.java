package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;

public class DistrbuteAndOverOr implements FormulaVisitorEx<PropositionalFormula> {

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
		PropositionalFormula leftCNF = disj.getLeftFormula().accept(this);
		PropositionalFormula rightCNF = disj.getRightFormula().accept(this);
	
		if(leftCNF instanceof Conjunction){
			PropositionalFormula leftDisj = ((Conjunction) leftCNF).getLeftFormula();
			PropositionalFormula rightDisj = ((Conjunction) leftCNF).getRightFormula();
			
			return new Conjunction(new Disjunction(leftDisj, rightCNF), new Disjunction(rightDisj, rightCNF)).accept(this);
		}
		else if(rightCNF instanceof Conjunction){
			PropositionalFormula leftDisj = ((Conjunction) rightCNF).getLeftFormula();
			PropositionalFormula rightDisj = ((Conjunction) rightCNF).getRightFormula();
			
			return new Conjunction(new Disjunction(leftDisj, leftCNF), new Disjunction(rightDisj, leftCNF)).accept(this);
		}
		else
			return new Disjunction(leftCNF, rightCNF);
	}

	@Override
	public PropositionalFormula visit(Negation negation) {
		return negation;
	}

	@Override
	public PropositionalFormula visit(Equality equality) {
		return equality;
	}

	@Override
	public PropositionalFormula visit(Implication implication) {
		return implication;
	}

}
