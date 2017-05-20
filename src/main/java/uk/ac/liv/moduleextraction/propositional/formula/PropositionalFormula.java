package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public abstract class PropositionalFormula {
	public abstract <E> E accept(FormulaVisitorEx<E> visitor);
	
	public abstract <E> void accept(FormulaVisitor<E> visitor, E e);

	public abstract void accept(FormulaVisitorVoid visitor);
}
