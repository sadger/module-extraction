package uk.ac.liv.moduleextraction.propositional.cnf;



import uk.ac.liv.moduleextraction.propositional.formula.PropositionalFormula;



public class CNFFormula {

	private PropositionalFormula propositional;
	private ImplicationEliminator implicationElim;
	private NegationNormalForm negationNormalForm;
	private DistrbuteAndOverOr distrbuteAndOverOr;
	
	
	public CNFFormula(PropositionalFormula formula) {
		this.propositional = formula;
		this.implicationElim = new ImplicationEliminator();
		this.negationNormalForm = new NegationNormalForm();
		this.distrbuteAndOverOr = new DistrbuteAndOverOr();
		convertPropositionalToCNF();
	}
	
	public void convertPropositionalToCNF(){
		propositional = propositional.accept(implicationElim);
		propositional = propositional.accept(negationNormalForm);
		propositional = propositional.accept(distrbuteAndOverOr);
	}
	
	public PropositionalFormula asPropositionalFormula(){
		return propositional;
	}
	
	@Override
	public String toString() {
		return propositional.toString();
	}
	

}
