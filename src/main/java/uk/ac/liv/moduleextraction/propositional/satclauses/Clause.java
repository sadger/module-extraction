package uk.ac.liv.moduleextraction.propositional.satclauses;

import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;

import java.util.HashSet;
import java.util.Set;


	public class Clause{
		/* A clause is a disjunction of literals */
		private Set<PropositionalFormula> literals;

		public Clause() {
			this.literals = new HashSet<PropositionalFormula>();
		}

		public void add(PropositionalFormula formula){
			literals.add(formula);
		}

		public Set<PropositionalFormula> getLiterals() {
			return literals;
		}

		@Override
		public String toString() {
			StringBuilder buff = new StringBuilder();
			String sep = "";

			buff.append("(");
			for (PropositionalFormula prop : literals) {
				buff.append(sep);
				buff.append(prop.toString());
				sep = " v ";
			}
			buff.append(")");

			return buff.toString();
		}
	}