package dependencies;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModuleUtils;



public class DependencyGeneration {

	@Test
	/*	A ⊑ B
		B ⊑ ∃ r.C
		C ⊑ D */
	public void simpleDependencies(){
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/simple-dependencies.krss");
		ModuleUtils.remapIRIs(ontology, "X");
		
		ChainDependencies dependT = new ChainDependencies(ontology);

		OWLDataFactory factory = OWLManager.getOWLDataFactory();

		OWLClass a = factory.getOWLClass(IRI.create("X#A"));
		OWLClass b = factory.getOWLClass(IRI.create("X#B"));
		OWLClass c = factory.getOWLClass(IRI.create("X#C"));
		OWLClass d = factory.getOWLClass(IRI.create("X#D"));
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create("X#r"));

		DependencySet depSet = new DependencySet();
		assertNull("D dependencies null",dependT.get(d));
		depSet.add(d);
		assertTrue("C dependencies",dependT.get(c).equals(depSet));
		depSet.add(r);
		depSet.add(c);
		assertTrue("B dependencies", dependT.get(b).equals(depSet));
		depSet.add(b);
		assertTrue("A dependencies",dependT.get(a).equals(depSet));
	}
}
