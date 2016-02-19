package dependencies;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDefinitorialDepth;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class ExpressiveAxiomDepth {

	
	@Test
	public void disjointAxiomDepth(){
		
		//Construct expressive axioms
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLClass a = f.getOWLClass(IRI.create("X#A"));
		OWLClass b = f.getOWLClass(IRI.create("X#B"));
		OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));
		OWLObjectProperty s = f.getOWLObjectProperty(IRI.create("X#s"));
		OWLObjectPropertyRangeAxiom range = f.getOWLObjectPropertyRangeAxiom(r, b);
		OWLDisjointClassesAxiom disjoint1 = f.getOWLDisjointClassesAxiom(a,b);
		OWLSubObjectPropertyOfAxiom roleInc = f.getOWLSubObjectPropertyOfAxiom(r, s);
		
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms("TestData/dependencies/simple-dependencies.krss");
		
		Set<OWLLogicalAxiom> inputOntology = ontology.getLogicalAxioms();
		inputOntology.add(disjoint1);
		inputOntology.add(range);
		inputOntology.add(roleInc);
		
		AxiomDefinitorialDepth d = new AxiomDefinitorialDepth(inputOntology);

		for(OWLLogicalAxiom ax : d.getDefinitorialSortedList()){
			System.out.println(d.lookup(ax) + ":" + ax);
		}
			
		assertTrue("Disjoint defined as max", d.lookup(disjoint1) == 4);
		assertTrue("Range defined as max", d.lookup(range) == 4);
		assertTrue("Role inclusion defined as max", d.lookup(roleInc) == 4);


	}
}
 