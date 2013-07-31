package uk.ac.liv.moduleextraction.profling;

import java.util.HashMap;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
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
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;

public class ExpressionTypeProfiler implements OWLClassExpressionVisitor{

	private HashMap<ClassExpressionType, Integer> typeMap = new HashMap<ClassExpressionType, Integer>();
	
	
	public void profileOntology(OWLOntology ontology) {
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms())
			profileAxiom(axiom);
		
		printMetrics();
	}
	
	public void profileAxiom(OWLLogicalAxiom axiom){
		OWLClassExpression name = AxiomSplitter.getNameofAxiom(axiom);
		OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);

		if(name != null && definition != null){
			name.accept(this);
			definition.accept(this);
		}
		else{
			System.out.println("Not profiling " + axiom);
		}
	}
	
	public void printMetrics(){
		System.out.println("== Expression Types ==");
		for(ClassExpressionType type : typeMap.keySet()){
			System.out.println(type.getName() + ":" + typeMap.get(type));
		}
	}
	
	private void addBaseExpression(OWLClassExpression arg0){
		ClassExpressionType type = arg0.getClassExpressionType();
		Integer count = typeMap.get(type);
		if(count == null)
			typeMap.put(type, 1);
		else
			typeMap.put(type, ++count);
	}
	

	@Override
	public void visit(OWLClass arg0) {
		addBaseExpression(arg0);
	}

	@Override
	public void visit(OWLObjectIntersectionOf arg0) {
		addBaseExpression(arg0);
		for(OWLClassExpression cls : arg0.getOperands()){
			cls.accept(this);
		}
	}

	@Override
	public void visit(OWLObjectUnionOf arg0) {
		addBaseExpression(arg0);
		for(OWLClassExpression cls : arg0.getOperands()){
			cls.accept(this);
		}
	}

	@Override
	public void visit(OWLObjectComplementOf arg0) {
		addBaseExpression(arg0);
		(arg0.getComplementNNF()).accept(this);
		
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom arg0) {
		addBaseExpression(arg0);
		(arg0.getFiller()).accept(this);
		
	}

	@Override
	public void visit(OWLObjectAllValuesFrom arg0) {
		addBaseExpression(arg0);
		(arg0.getFiller()).accept(this);
	}

	@Override
	public void visit(OWLObjectHasValue arg0) {
		addBaseExpression(arg0);
	}

	@Override
	public void visit(OWLObjectMinCardinality arg0) {
		addBaseExpression(arg0);
		(arg0.getFiller()).accept(this);	
	}

	@Override
	public void visit(OWLObjectExactCardinality arg0) {
		addBaseExpression(arg0);
		(arg0.getFiller()).accept(this);
		
	}

	@Override
	public void visit(OWLObjectMaxCardinality arg0) {
		addBaseExpression(arg0);
		(arg0.getFiller()).accept(this);
		
	}

	@Override
	public void visit(OWLObjectHasSelf arg0) {
		addBaseExpression(arg0);
		
	}

	@Override
	public void visit(OWLObjectOneOf arg0) {
		addBaseExpression(arg0);

	}
	
	/* Data constructors */
	@Override
	public void visit(OWLDataSomeValuesFrom arg0) {
	}
	@Override
	public void visit(OWLDataAllValuesFrom arg0) {
	}
	@Override
	public void visit(OWLDataHasValue arg0) {
	}
	@Override
	public void visit(OWLDataMinCardinality arg0) {
	}
	@Override
	public void visit(OWLDataExactCardinality arg0) {
	}
	@Override
	public void visit(OWLDataMaxCardinality arg0) {

	}

}
