package uk.ac.liv.moduleextraction.replacers;


import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
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
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;



public class InverseRolePropertyReplacer implements OWLClassExpressionVisitorEx<OWLClassExpression>{

	private OWLDataFactory factory = OWLManager.getOWLDataFactory();
	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	private boolean isInverseRole(OWLObjectPropertyExpression role){
		return (role instanceof OWLObjectInverseOf);
	}

	/**
	 * Takes an acyclic ALCI terminology and replaces all inverse roles with
	 * simple roles, helper class for module extraction, do not use for other purposes.
	 * @param ontology
	 * @return Ontology where all inverse roles r^- are replaced by the simple role r. 
	 * @throws OWLOntologyCreationException
	 */
	
	public OWLOntology convert(OWLOntology ontology) throws OWLOntologyCreationException{
		OWLOntology convertedOntology = manager.createOntology();

		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			//System.out.println(axiom);
			manager.addAxiom(convertedOntology, visit(axiom));
		}
		
		return convertedOntology;
	}
	
	/* 
	 * Convert a set of axioms rather than an ontology
	 */
	public Set<OWLLogicalAxiom> convert(Set<OWLLogicalAxiom> axioms){
		Set<OWLLogicalAxiom> newAxiomSet = new HashSet<OWLLogicalAxiom>();
		for(OWLLogicalAxiom axiom : axioms){
			newAxiomSet.add(visit(axiom));
		}
		return newAxiomSet;
	}
	
	public OWLLogicalAxiom visit(OWLLogicalAxiom axiom){
		AxiomType<?> axiomType = axiom.getAxiomType();
		OWLClassExpression axiomName = AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression axiomDefinition = AxiomSplitter.getDefinitionofAxiom(axiom);
		
		OWLLogicalAxiom newAxiom = null;
		
		/* Axiom name never contains inverse roles it's always a concept name as we are dealing with
		terminologies */
		if(axiomType == AxiomType.SUBCLASS_OF){
			newAxiom = factory.getOWLSubClassOfAxiom(axiomName, axiomDefinition.accept(this));
		}
		else if(axiomType == AxiomType.EQUIVALENT_CLASSES){
			newAxiom = factory.getOWLEquivalentClassesAxiom(axiomName,axiomDefinition.accept(this));
		}
		
		return newAxiom;
	}
	
	
	@Override
	public OWLClassExpression visit(OWLClass cls) {
		return cls;
	}

	@Override
	public OWLClassExpression visit(OWLObjectIntersectionOf intersection) {
		
		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for(OWLClassExpression expr : intersection.getOperands()){
			result.add(expr.accept(this));
		}
		return factory.getOWLObjectIntersectionOf(result);
	}

	@Override
	public OWLClassExpression visit(OWLObjectUnionOf union) {
		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for(OWLClassExpression expr : union.getOperands()){
			result.add(expr.accept(this));
		}
		
		return factory.getOWLObjectIntersectionOf(result);
	}

	@Override
	public OWLClassExpression visit(OWLObjectComplementOf neg) {
		return factory.getOWLObjectComplementOf(neg.getComplementNNF().accept(this));
	}


	@Override
	public OWLClassExpression visit(OWLObjectSomeValuesFrom exists) {
		OWLObjectPropertyExpression role = exists.getProperty();
		OWLClassExpression filler = exists.getFiller();
		
		if(isInverseRole(role)){
			role = role.getInverseProperty().getSimplified();
		}
		
		return factory.getOWLObjectSomeValuesFrom(role, filler.accept(this));
	}

	@Override
	public OWLClassExpression visit(OWLObjectAllValuesFrom all) {
		OWLObjectPropertyExpression role = all.getProperty();
		OWLClassExpression filler = all.getFiller();
		
		if(isInverseRole(role)){
			role = role.getInverseProperty().getSimplified();
		}
		
		return factory.getOWLObjectAllValuesFrom(role, filler.accept(this));
	}
	
	
	@Override
	public OWLClassExpression visit(OWLObjectMinCardinality ce) {
		int cardinality = ce.getCardinality();
		OWLObjectPropertyExpression role = ce.getProperty();
		OWLClassExpression filler = ce.getFiller();
		
		if(isInverseRole(role)){
			role = role.getInverseProperty().getSimplified();
		}
		
		return factory.getOWLObjectMinCardinality(cardinality, role, filler.accept(this));
	}



	@Override
	public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
		int cardinality = ce.getCardinality();
		OWLObjectPropertyExpression role = ce.getProperty();
		OWLClassExpression filler = ce.getFiller();
		
		if(isInverseRole(role)){
			role = role.getInverseProperty().getSimplified();
		}
		
		return factory.getOWLObjectMaxCardinality(cardinality, role, filler.accept(this));
	}
	
	@Override
	public OWLClassExpression visit(OWLObjectExactCardinality ce) {
		int cardinality = ce.getCardinality();
		OWLObjectPropertyExpression role = ce.getProperty();
		OWLClassExpression filler = ce.getFiller();
		
		if(isInverseRole(role)){
			role = role.getInverseProperty().getSimplified();
		}
		
		
		return factory.getOWLObjectExactCardinality(cardinality, role, filler.accept(this));
	}

	
	
	/* Nominals we cannot currently support them - DO Nothing */
	@Override
	public OWLClassExpression visit(OWLObjectHasValue ce) {
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectHasSelf ce) {
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectOneOf ce) {
		return ce;
	}

	/* Data Properties - Do nothing */
	@Override
	public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
		return ce;
	}
	@Override
	public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
		return ce;
	}
	@Override
	public OWLClassExpression visit(OWLDataHasValue ce) {
		return ce;
	}
	@Override
	public OWLClassExpression visit(OWLDataMinCardinality ce) {
		return ce;
	}
	@Override
	public OWLClassExpression visit(OWLDataExactCardinality ce) {
		return ce;
	}
	@Override
	public OWLClassExpression visit(OWLDataMaxCardinality ce) {
		return ce;
	}
}
