package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class BooleanAtom extends Atom{

	Boolean value;
	
	public BooleanAtom(Boolean value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}

	public Boolean getValue() {
		return value;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BooleanAtom that = (BooleanAtom) o;

		if (value != null ? !value.equals(that.value) : that.value != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public void accept(FormulaVisitorVoid visitor) {
		visitor.visit(this);
	}
}
