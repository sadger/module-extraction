package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class Equality extends BooleanOperator {

	public Equality(PropositionalFormula left, PropositionalFormula right) {
		super(left, right);
	}
	
	public Conjunction asConjunction(){
		//Transform P <==> Q to (~P v Q) & (~Q v P)
		
		PropositionalFormula firstImplication = new Implication(leftFormula, rightFormula).asDisjunction();
		PropositionalFormula secondImplication = new Implication(leftFormula, rightFormula).asDisjunction();
		
		return new Conjunction(firstImplication, secondImplication);
		
	}
	
	@Override
	public String toString() {
		return leftFormula.toString() + "<=>" + rightFormula.toString();
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
