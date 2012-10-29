package checkers;



import ontologyutils.AxiomSplitter;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;


public class ELChecker implements OWLClassExpressionVisitorEx<Boolean>{

	
	public boolean isELAxiom(OWLLogicalAxiom axiom){
		OWLClassExpression name = AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
		return name.accept(this) && definition.accept(this);
	}
	
	
	@Override
	public Boolean visit(OWLClass arg0) {
		if(arg0.isBottomEntity()){
			return false;
		}
		else{
			return true;
		}
	}

	@Override
	public Boolean visit(OWLObjectIntersectionOf arg0) {
		boolean result = true;
		for(OWLClassExpression cls : arg0.getOperands()){
			result = result && cls.accept(this);
		}
		return result;
	}

	@Override
	public Boolean visit(OWLObjectUnionOf arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectComplementOf arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectSomeValuesFrom arg0) {
		return arg0.getFiller().accept(this);
	}

	@Override
	public Boolean visit(OWLObjectAllValuesFrom arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectHasValue arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectMinCardinality arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectExactCardinality arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectMaxCardinality arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectHasSelf arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLObjectOneOf arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataSomeValuesFrom arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataAllValuesFrom arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataHasValue arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataMinCardinality arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataExactCardinality arg0) {
		return false;
	}

	@Override
	public Boolean visit(OWLDataMaxCardinality arg0) {
		return false;
	}

}
