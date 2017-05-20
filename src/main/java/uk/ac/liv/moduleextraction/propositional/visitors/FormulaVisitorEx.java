package uk.ac.liv.moduleextraction.propositional.visitors;


import uk.ac.liv.moduleextraction.propositional.formula.*;

public interface FormulaVisitorEx<E> {

	public E visit(NamedAtom a);
	
	public E visit(BooleanAtom b);
	
	public E visit(Conjunction conj);
	
	public E visit(Disjunction disj);

	public E visit(Negation negation);
	
	public E visit(Equality equality);

	public E visit(Implication implication);

	
}
