package uk.ac.liv.moduleextraction.propositional.formula;

import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitor;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorEx;
import uk.ac.liv.moduleextraction.propositional.visitors.FormulaVisitorVoid;

public class NamedAtom extends Atom {

	private String name;

	public NamedAtom(String n) {
		this.name = n;
	}

	@Override
	public String toString() {
		String stringName = name;
		if(name.contains("#")){
			stringName = name.substring(name.indexOf("#")+1);
		}
		else if(name.contains("/")){
			stringName = name.substring(name.lastIndexOf("/")+1);	
		}
		return stringName;
	}


	public String getName() {
		return name;
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


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NamedAtom namedAtom = (NamedAtom) o;

		if (name != null ? !name.equals(namedAtom.name) : namedAtom.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	public static void main(String[] args) {
		PropositionalFormula a = new Negation(new NamedAtom("A"));
		PropositionalFormula b = new Negation(new Negation(new NamedAtom("A")));
		PropositionalFormula c = new Negation(new NamedAtom("A"));
		System.out.println(a.equals(b));
		System.out.println(a.equals(c));
	}

}
