package uk.ac.liv.moduleextraction.propositional.visitors;

import uk.ac.liv.moduleextraction.propositional.formula.*;

public interface FormulaVisitor<E> {

	public void visit(NamedAtom a, E e);
	
	public void visit(BooleanAtom b, E e);
	
	public void visit(Conjunction conj, E e);
	
	public void visit(Disjunction disj, E e);

	public void visit(Negation negation, E e);
	
	public void visit(Equality equality, E e);

	public void visit(Implication implication, E e);
}
