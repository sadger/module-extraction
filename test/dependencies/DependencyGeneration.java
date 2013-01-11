package dependencies;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.testing.DependencyCalculator;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class DependencyGeneration {

	private HashMap<OWLClass, Set<OWLEntity>> dependencies;
	private OWLOntology ontology;
	private OWLDataFactory factory = OWLManager.getOWLDataFactory();
	
	OWLClass a;
	OWLClass b;
	OWLClass c;
	OWLClass d;
	OWLClass e;
	OWLClass f;
	
	OWLObjectProperty r;
	OWLObjectProperty s;
	
	@Before
	public void setupDependencies(){
		File ontologyLocation = new File("TestData/diff.krss");
		ontology = OntologyLoader.loadOntology(ontologyLocation.getAbsolutePath());
		DependencyCalculator depCalculator = new DependencyCalculator(ontology);
		dependencies = depCalculator.getDependenciesFor(ontology.getLogicalAxioms());
		
		a = createClassOfOntology("A");
		b = createClassOfOntology("B");
		c = createClassOfOntology("C");
		d = createClassOfOntology("D");
		e = createClassOfOntology("E");
		f = createClassOfOntology("F");
		
		r = createRoleofOntology("r");
		s = createRoleofOntology("s");
		
		System.out.println(ontology);
	}
	
	
	public OWLClass createClassOfOntology(String name){
		return factory.getOWLClass(IRI.create(ontology.getOntologyID() + "#" + name));
	}
	
	public OWLObjectProperty createRoleofOntology(String name){
		return factory.getOWLObjectProperty(IRI.create(ontology.getOntologyID() + "#" + name));
	}
	
	@Test
	public void DependencyGeneration1() {
		/* Deeply nested dependencies */
		Set<OWLEntity> depsOfA = dependencies.get(a);
		
		HashSet<OWLEntity> dependencySet = new HashSet<OWLEntity>();
		dependencySet.add(b);
		dependencySet.add(c);
		dependencySet.add(d);
		dependencySet.add(e);
		dependencySet.add(r);
		
		assertEquals("Dependency Set", depsOfA,dependencySet);
		
	}
	
	@Test
	public void DependencyGeneration2() 
	{
		/* Dependencies with 2 nested role names */
		Set<OWLEntity> depsOfF = dependencies.get(f);
		
		HashSet<OWLEntity> dependencySet = new HashSet<OWLEntity>();
		dependencySet.add(s);
		dependencySet.add(b);
		dependencySet.add(r);
		dependencySet.add(d);
		dependencySet.add(e);
		
		assertEquals(depsOfF, dependencySet);
		
	}
	
	@Test
	public void DependencyGeneration3() {
		/* Empty dependency set */
		Set<OWLEntity> depsOfC = dependencies.get(c);
		HashSet<OWLEntity> dependencySet = new HashSet<OWLEntity>();
		
		assertEquals(depsOfC, dependencySet);
	}

}
