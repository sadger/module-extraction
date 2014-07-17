package uk.ac.liv.moduleextraction.replacers;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class InverseRoleAxiomReplacer implements  OWLAxiomVisitorEx<OWLLogicalAxiom>{
	
	InverseRolePropertyReplacer replacer;
	OWLDataFactory f;
	public InverseRoleAxiomReplacer() {
		replacer = new InverseRolePropertyReplacer();
		f = OWLManager.getOWLDataFactory();
	}
	
	

	
	public static void main(String[] args) {
		InverseRoleAxiomReplacer aRepl = new InverseRoleAxiomReplacer();
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/manchester.owl");
		for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
			System.out.println(axiom);
			System.out.println(axiom.accept(aRepl));
		}
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLClass a = f.getOWLClass(IRI.create("X#A"));
		OWLClass b = f.getOWLClass(IRI.create("X#B"));
		OWLClass c = f.getOWLClass(IRI.create("X#C"));
		OWLClass d = f.getOWLClass(IRI.create("X#C"));
		

		OWLLogicalAxiom axiom = f.getOWLSubClassOfAxiom(a, b);
		System.out.println(axiom);
		
	}




	@Override
	public OWLLogicalAxiom visit(OWLSubClassOfAxiom sub) {
		return f.getOWLSubClassOfAxiom(sub.getSubClass().accept(replacer), 
									   sub.getSuperClass().accept(replacer));
	}




	@Override
	public OWLLogicalAxiom visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLReflexiveObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDisjointClassesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDataPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLObjectPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDifferentIndividualsAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDisjointDataPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDisjointObjectPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLObjectPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLObjectPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLFunctionalObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSubObjectPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDisjointUnionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDeclarationAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLAnnotationAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSymmetricObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDataPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLFunctionalDataPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLEquivalentDataPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLClassAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLEquivalentClassesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDataPropertyAssertionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLTransitiveObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSubDataPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSameIndividualAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSubPropertyChainOfAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLInverseObjectPropertiesAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLHasKeyAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLDatatypeDefinitionAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(SWRLRule arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLAnnotationPropertyDomainAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public OWLLogicalAxiom visit(OWLAnnotationPropertyRangeAxiom arg0) {
		// TODO Auto-generated method stub
		return null;
	}




	

}
