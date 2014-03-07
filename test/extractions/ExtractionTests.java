package extractions;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.liv.moduleextraction.extractor.SemanticRuleExtractor;

public class ExtractionTests {
	
	static OWLClass a;
	static OWLClass b;
	static OWLClass c;
	static OWLClass d;
	static OWLClass e;
	static OWLObjectProperty r;
	private static OWLClass a1;
	private static OWLClass a2;
	private static OWLClass b2;
	private static OWLClass b1;
	private static OWLSubClassOfAxiom csub1;
	private static OWLSubClassOfAxiom bsub1;
	private static OWLSubClassOfAxiom bsub2;
	private static OWLSubClassOfAxiom asub1;
	private static OWLSubClassOfAxiom a1axiom;
	private static OWLSubClassOfAxiom a2axiom;
	private static OWLEquivalentClassesAxiom aequiv;



	
	@BeforeClass
	public static void createOWLClasses(){
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		a = factory.getOWLClass(IRI.create("X#A"));
		a1 = factory.getOWLClass(IRI.create("X#A1"));
		a2 = factory.getOWLClass(IRI.create("X#A2"));
		b = factory.getOWLClass(IRI.create("X#B"));
		b1 = factory.getOWLClass(IRI.create("X#B1"));
		b2 = factory.getOWLClass(IRI.create("X#B2"));
		c = factory.getOWLClass(IRI.create("X#C"));
		d = factory.getOWLClass(IRI.create("X#D"));
		e = factory.getOWLClass(IRI.create("X#E"));
		r = factory.getOWLObjectProperty(IRI.create("X#r"));
		asub1 = factory.getOWLSubClassOfAxiom(a, b);
		bsub1 = factory.getOWLSubClassOfAxiom(b, factory.getOWLObjectSomeValuesFrom(r, c));
		bsub2 = factory.getOWLSubClassOfAxiom(b, c);
		csub1 = factory.getOWLSubClassOfAxiom(c, d);
		
		a1axiom = factory.getOWLSubClassOfAxiom(a1, factory.getOWLObjectSomeValuesFrom(r, b1));
		a2axiom = factory.getOWLSubClassOfAxiom(a1, factory.getOWLObjectSomeValuesFrom(r, b2));
		aequiv = factory.getOWLEquivalentClassesAxiom(a, factory.getOWLObjectIntersectionOf(b1,b2));
		
		
	}
	
	@Test
	/*
	 * Ont = [A ⊑ B, C ⊑ D, B ⊑ C]
	   Sig = [A, D]
	   Module = Ont
	 */
	public void simpleConceptDependencies(){
		HashSet<OWLLogicalAxiom> inputOntology = new HashSet<OWLLogicalAxiom>();
		inputOntology.add(asub1);
		inputOntology.add(bsub2);
		inputOntology.add(csub1);
		
	
		HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(d);
		
		SemanticRuleExtractor extractor = new SemanticRuleExtractor(inputOntology);
		
		HashSet<OWLLogicalAxiom> expectedModule = new HashSet<OWLLogicalAxiom>();
		expectedModule.add(asub1);
		expectedModule.add(bsub2);
		expectedModule.add(csub1);
		
		
		Set<OWLLogicalAxiom> module = extractor.extractModule(signature);
		
		assertTrue("Extraction whole ontology", module.equals(expectedModule));
		
	}
	
	@Test
	/*
	 * Ont = [A ⊑ B, C ⊑ D, B ⊑ ∃ r.C]
	   Sig = [r, A]
	   M = [A ⊑ B, B ⊑ ∃ r.C]
	 */
	public void simpleRoleDependencies(){
		HashSet<OWLLogicalAxiom> inputOntology = new HashSet<OWLLogicalAxiom>();
		inputOntology.add(asub1);
		inputOntology.add(bsub1);
		inputOntology.add(csub1);
		
	
		HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a);
		signature.add(r);
		
		SemanticRuleExtractor extractor = new SemanticRuleExtractor(inputOntology);
		
		HashSet<OWLLogicalAxiom> expectedModule = new HashSet<OWLLogicalAxiom>();
		expectedModule.add(asub1);
		expectedModule.add(bsub1);
		
		Set<OWLLogicalAxiom> module = extractor.extractModule(signature);
		
		assertTrue("Extraction role name", module.equals(expectedModule));
		
	}
	
	@Test
	/*
	 * Ont = [A ≡ B1 ⊓ B2, A1 ⊑ ∃ r.B2, A1 ⊑ ∃ r.B1]
	 * Sig = [A,A1,A2]
	 * Mod = [A ≡ B1 ⊓ B2, A1 ⊑ ∃ r.B2, A1 ⊑ ∃ r.B1]
	 */
	public void indirectDependencies(){
		HashSet<OWLLogicalAxiom> inputOntology = new HashSet<OWLLogicalAxiom>();
		inputOntology.add(a1axiom);
		inputOntology.add(a2axiom);
		inputOntology.add(aequiv);
		
	
		HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(a1);
		signature.add(a2);
		signature.add(a);
		
		SemanticRuleExtractor extractor = new SemanticRuleExtractor(inputOntology);
		
		HashSet<OWLLogicalAxiom> expectedModule = new HashSet<OWLLogicalAxiom>();
		expectedModule.add(a1axiom);
		expectedModule.add(a2axiom);
		expectedModule.add(aequiv);
		
		
		Set<OWLLogicalAxiom> module = extractor.extractModule(signature);
		System.out.println(module);
		
		assertTrue("Extraction whole ontology", module.equals(expectedModule));
	}
}
