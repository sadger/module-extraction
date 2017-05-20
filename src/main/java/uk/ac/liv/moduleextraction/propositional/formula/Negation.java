package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class Negation extends PropositionalFormula {

	PropositionalFormula complement; 

	public Negation(PropositionalFormula value) {
		this.complement = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Negation negation = (Negation) o;

		if (complement != null ? !complement.equals(negation.complement) : negation.complement != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return complement != null ? complement.hashCode() : 0;
	}

	public PropositionalFormula getComplement() {
		return complement;
	}

	@Override
	public String toString() {
		return "~" + complement.toString();
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
