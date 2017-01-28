package uk.ac.liv.moduleextraction.propositional.visitors;

import uk.ac.liv.moduleextraction.propositional.formula.*;

public interface FormulaVisitorVoid {

	public void visit(NamedAtom a);
	
	public void visit(BooleanAtom b);
	
	public void visit(Conjunction conj);
	
	public void visit(Disjunction disj);

	public void visit(Negation negation);
	
	public void visit(Equality equality);

	public void visit(Implication implication);
}
