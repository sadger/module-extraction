package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class Conjunction extends BooleanOperator {

	public Conjunction(PropositionalFormula left, PropositionalFormula right) {
		super(left,right);
	}
	
	@Override
	public String toString() {
		return "(" + super.getLeftFormula() + " & " + super.getRightFormula() + ")";  
	}
	
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
