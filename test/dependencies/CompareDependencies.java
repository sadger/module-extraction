package dependencies;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;

public class CompareDependencies {

	@Test
	public void dependenciesGiveSameResults(){
		OWLDataFactory factory = OWLManager.getOWLDataFactory();

		OWLClass a = factory.getOWLClass(IRI.create("X#A"));
		OWLClass b = factory.getOWLClass(IRI.create("X#B"));
		OWLClass c = factory.getOWLClass(IRI.create("X#C"));
		OWLClass d = factory.getOWLClass(IRI.create("X#D"));
		
		OWLSubClassOfAxiom asub = factory.getOWLSubClassOfAxiom(a, b);
		OWLSubClassOfAxiom bsub = factory.getOWLSubClassOfAxiom(b, c);
		OWLSubClassOfAxiom csub = factory.getOWLSubClassOfAxiom(c, d);
		
		HashSet<OWLLogicalAxiom> ontology = new HashSet<OWLLogicalAxiom>();
		ontology.add(asub);
		ontology.add(bsub);
		ontology.add(csub);
		
		ChainDependencies simpleDepends = new ChainDependencies(ontology);
		AxiomDependencies axiomDepends = new AxiomDependencies(ontology);
		
		assertTrue("A depends same", simpleDepends.get(a).equals(axiomDepends.get(asub)));
		assertTrue("B depends same", simpleDepends.get(b).equals(axiomDepends.get(bsub)));
		assertTrue("C depends same", simpleDepends.get(c).equals(axiomDepends.get(csub)));
	}
}
