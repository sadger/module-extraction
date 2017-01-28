package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class Implication extends BooleanOperator {

	public Implication(PropositionalFormula left, PropositionalFormula right) {
		super(left, right);
	}

	public Disjunction asDisjunction(){
		return new Disjunction(new Negation(leftFormula), rightFormula);
	}
	
	@Override
	public String toString() {
		return "(" + leftFormula + "->" + rightFormula + ")";
	}
	
//	@Override
//	public void accept(FormulaVisitor visitor){
//		visitor.visit(this);
//	}
	
	@Override
	public <E> E accept(FormulaVisitorEx<E> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public <E> void accept(FormulaVisitor<E> visitor, E e) {
		visitor.visit(this, e);
	}

	@Override
	public void accept(FormulaVisitorVoid visitor) {
		visitor.visit(this);
	}
}
