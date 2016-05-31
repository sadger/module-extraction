package dependencies;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class AxiomDependencyGeneration {
	
	static OWLClass a;
	static OWLClass b;
	static OWLClass c;
	static OWLClass d;
	static OWLClass e;
	static OWLObjectProperty r;
	private static OWLSubClassOfAxiom csub1;
	private static OWLSubClassOfAxiom csub2;
	private static OWLSubClassOfAxiom bsub1;
	private static OWLSubClassOfAxiom bsub2;
	private static OWLSubClassOfAxiom asub1;
	private static OWLSubClassOfAxiom asub2;
	private static OWLSubClassOfAxiom bsub3;

	
	@BeforeClass
	public static void createOWLClasses(){
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		a = factory.getOWLClass(IRI.create("X#A"));
		b = factory.getOWLClass(IRI.create("X#B"));
		c = factory.getOWLClass(IRI.create("X#C"));
		d = factory.getOWLClass(IRI.create("X#D"));
		e = factory.getOWLClass(IRI.create("X#E"));
		r = factory.getOWLObjectProperty(IRI.create("X#r"));
		asub1 = factory.getOWLSubClassOfAxiom(a, b);
		asub2 = factory.getOWLSubClassOfAxiom(a, c);
		bsub1 = factory.getOWLSubClassOfAxiom(b, factory.getOWLObjectSomeValuesFrom(r, c));
		bsub2 = factory.getOWLSubClassOfAxiom(b, c);
		bsub3 = factory.getOWLSubClassOfAxiom(b, factory.getOWLObjectIntersectionOf(c,d));
		csub1 = factory.getOWLSubClassOfAxiom(c, d);
		csub2 = factory.getOWLSubClassOfAxiom(c, e);
	}
	
	@Test
	/*	A ⊑ B
		B ⊑ ∃ r.C
		C ⊑ D */
	public void simpleAxiomDependencies(){
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/simple-dependencies.krss");
		ModuleUtils.remapIRIs(ontology, "X");

		AxiomDependencies dependencies = new AxiomDependencies(ontology);
		
		DependencySet depSet = new DependencySet();
		depSet.add(d);
		assertTrue("C axiom dependencies", dependencies.get(csub1).equals(depSet));
		depSet.add(r);
		depSet.add(c);
		assertTrue("B axiom dependencies", dependencies.get(bsub1).equals(depSet));
		depSet.add(b);
		assertTrue("A axiom dependencies", dependencies.get(asub1).equals(depSet));
		
	}
	
	@Test
	/* A ⊑ B
	   B ⊑ C
	   C ⊑ D
	   C ⊑ E
	 */
	public void repeatedDefinitionDependencies(){
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/multiple-simple.krss");
		ModuleUtils.remapIRIs(ontology, "X");
		AxiomDependencies dependencies = new AxiomDependencies(ontology);
		
		
		DependencySet depSet = new DependencySet();
		depSet.add(c);
		depSet.add(e);
		depSet.add(d);
		assertTrue("B axiom dependencies", dependencies.get(bsub2).equals(depSet));
		depSet.add(b);
		assertTrue("A axiom dependencies", dependencies.get(asub1).equals(depSet));
	}
	
	@Test
	public void sameNameIsDifferent(){
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/multiple-simple.krss");
		ModuleUtils.remapIRIs(ontology, "X");
		AxiomDependencies dependencies = new AxiomDependencies(ontology);
		
		DependencySet depSet1 = new DependencySet();
		depSet1.add(d);
		DependencySet depSet2 = new DependencySet();
		depSet2.add(e);
		
		assertTrue("C axiom 1", dependencies.get(csub1).equals(depSet1));
		assertTrue("C axiom 2", dependencies.get(csub2).equals(depSet2));
	}
	
	@Test
	/*
	 A ⊑ B
	 A ⊑ C
     B ⊑ C ⊓ D
     C ⊑ E
	 */
	public void overlappingDefinitions(){
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/multiple-shared.krss");
		ModuleUtils.remapIRIs(ontology, "X");
		AxiomDependencies dependencies = new AxiomDependencies(ontology);
		System.out.println(ontology);	
		
		DependencySet a1depends = new DependencySet();
		a1depends.add(b);
		a1depends.add(c);
		a1depends.add(d);
		a1depends.add(e);
		assertTrue("A axiom 1 depends",dependencies.get(asub1).equals(a1depends));
		
		DependencySet a2depends = new DependencySet();
		a2depends.add(c);
		a2depends.add(e);
		assertTrue("A axiom 2 depends",dependencies.get(asub2).equals(a2depends));
		
		DependencySet bdepends = new DependencySet();
		bdepends.add(c);
		bdepends.add(d);
		bdepends.add(e);
		assertTrue("B axiom depends",dependencies.get(bsub3).equals(bdepends));
	}
}
