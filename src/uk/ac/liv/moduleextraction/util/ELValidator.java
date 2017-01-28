package uk.ac.liv.moduleextraction.util;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Set;


public class ELValidator implements OWLClassExpressionVisitorEx<Boolean> {


	public OWLOntology extractELOntology(OWLOntology ontology){
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			if(!isELAxiom(axiom)){
				man.removeAxiom(ontology, axiom);
			}
		}
		
		return ontology;
	}
	
	public boolean isELOntology(OWLOntology ontology){
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			if(!isELAxiom(axiom))
				return false;
		}
		
		return true;
	}
	
	public boolean isELOntology(Set<OWLLogicalAxiom> coreAxioms) {
		for(OWLLogicalAxiom axiom : coreAxioms){
			if(!isELAxiom(axiom)){
				//System.out.println(axiom);
				return false;
			}
		}
		
		return true;
	}

	
	
	public boolean isELAxiom(OWLLogicalAxiom axiom){
		OWLClassExpression lhs = AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression rhs = AxiomSplitter.getDefinitionofAxiom(axiom);

        //Anything that isn't a concept inclusion or equality may have a null lhs/rhs
        if(rhs == null || lhs == null) { return false; };
		return lhs.accept(this) && rhs.accept(this);
	}
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
