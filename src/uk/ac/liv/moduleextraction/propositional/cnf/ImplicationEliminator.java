package uk.ac.liv.moduleextraction.propositional.cnf;

import uk.ac.liv.moduleextraction.propositional.formula.*;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;

public class ImplicationEliminator implements FormulaVisitorEx<PropositionalFormula>{

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
		return new Negation(negation.getComplement().accept(this));
	}
	
	@Override
	public PropositionalFormula visit(Implication implication) {
		PropositionalFormula leftFormula = implication.getLeftFormula();
		PropositionalFormula rightFormula = implication.getRightFormula();
		Disjunction equivalentImplication = new Disjunction(new Negation(leftFormula), rightFormula);
		
		return equivalentImplication.accept(this);
	}
	
	@Override
	public PropositionalFormula visit(Equality equality) {
		PropositionalFormula leftFormula = equality.getLeftFormula();
		PropositionalFormula rightFormula = equality.getRightFormula();
		Conjunction twoImplications =
				new Conjunction(new Implication(leftFormula, rightFormula), new Implication(rightFormula, leftFormula));
		
		return twoImplications.accept(this);
	}


	
}
