package uk.ac.liv.moduleextraction.propositional.formula;

public abstract class BooleanOperator extends PropositionalFormula {

	protected PropositionalFormula leftFormula;
	protected PropositionalFormula rightFormula;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BooleanOperator that = (BooleanOperator) o;

		if (leftFormula != null ? !leftFormula.equals(that.leftFormula) : that.leftFormula != null) return false;
		if (rightFormula != null ? !rightFormula.equals(that.rightFormula) : that.rightFormula != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = leftFormula != null ? leftFormula.hashCode() : 0;
		result = 31 * result + (rightFormula != null ? rightFormula.hashCode() : 0);
		return result;
	}

	public BooleanOperator(PropositionalFormula left, PropositionalFormula right) {
		leftFormula = left;
		rightFormula = right;
	}
	
	public PropositionalFormula getLeftFormula() {
		return leftFormula;
	}
	
	public PropositionalFormula getRightFormula() {
		return rightFormula;
	}
}
