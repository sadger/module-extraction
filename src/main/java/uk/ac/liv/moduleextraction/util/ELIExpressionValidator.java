package uk.ac.liv.moduleextraction.util;


import org.semanticweb.owlapi.model.*;


public class ELIExpressionValidator implements OWLClassExpressionVisitorEx<Boolean> {

	@Override
	public Boolean visit(OWLClass ce) {
		return true;
	}

	@Override
	public Boolean visit(OWLObjectIntersectionOf ce) {
		boolean result = true;
		for(OWLClassExpression expr : ce.getOperands()){
			result = result && expr.accept(this);
		}
		return result;
	}

	@Override
	public Boolean visit(OWLObjectSomeValuesFrom ce) {
		OWLClassExpression filler = ce.getFiller();
		return filler.accept(this);
	}
	
	@Override
	public Boolean visit(OWLObjectUnionOf ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectComplementOf ce) {
		return false;
	}



	@Override
	public Boolean visit(OWLObjectAllValuesFrom ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectHasValue ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectMinCardinality ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectExactCardinality ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectMaxCardinality ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectHasSelf ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectOneOf ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataSomeValuesFrom ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataAllValuesFrom ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataHasValue ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataMinCardinality ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataExactCardinality ce) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataMaxCardinality ce) {
		return false;
	}





}
