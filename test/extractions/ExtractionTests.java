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

import uk.ac.liv.moduleextraction.extractor.AMEX;

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
		
		AMEX extractor = new AMEX(inputOntology);
		
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
		
		AMEX extractor = new AMEX(inputOntology);
		
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
		
		AMEX extractor = new AMEX(inputOntology);
		
		HashSet<OWLLogicalAxiom> expectedModule = new HashSet<OWLLogicalAxiom>();
		expectedModule.add(a1axiom);
		expectedModule.add(a2axiom);
		expectedModule.add(aequiv);
		
		
		Set<OWLLogicalAxiom> module = extractor.extractModule(signature);
		
		assertTrue("Extraction whole ontology", module.equals(expectedModule));
	}
	
	@Test
	public void alcNCIExtraction(){
		//Constructing classes and axioms
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		OWLClass renal = factory.getOWLClass(IRI.create("X#Renal_Pelvis_and_U"));
		OWLClass kidney = factory.getOWLClass(IRI.create("X#K_and_U"));
		OWLClass kidney_neo = factory.getOWLClass(IRI.create("X#K_and_U_Neoplasm"));
		OWLClass ut_neo = factory.getOWLClass(IRI.create("X#U_T_Neoplasm"));
		OWLClass malignant = factory.getOWLClass(IRI.create("X#Malignant_Cell"));
		OWLClass mal_ut_neo = factory.getOWLClass(IRI.create("X#Malignant_U_T_Neoplasm"));
		OWLClass ben_ut_neo = factory.getOWLClass(IRI.create("X#Benign_U_T_Neoplasm"));
		
		OWLObjectProperty partOf = factory.getOWLObjectProperty(IRI.create("X#partOf"));
		OWLObjectProperty hasSite = factory.getOWLObjectProperty(IRI.create("X#hasSite"));
		OWLObjectProperty hasAbnCell = factory.getOWLObjectProperty(IRI.create("X#hasAbnCell"));
		OWLObjectProperty excludesAbnCell = factory.getOWLObjectProperty(IRI.create("X#excludesAbnCell"));
		
		OWLSubClassOfAxiom renal_inc = factory.getOWLSubClassOfAxiom(renal, factory.getOWLObjectSomeValuesFrom(partOf, kidney));
		OWLEquivalentClassesAxiom kidney_eq = 
				factory.getOWLEquivalentClassesAxiom(kidney_neo,
						factory.getOWLObjectIntersectionOf(ut_neo,factory.getOWLObjectAllValuesFrom(hasSite, kidney)));
		OWLEquivalentClassesAxiom mal_ut_eq = 
				factory.getOWLEquivalentClassesAxiom(mal_ut_neo,
						factory.getOWLObjectIntersectionOf(ut_neo,factory.getOWLObjectAllValuesFrom(hasAbnCell, malignant)));
		OWLEquivalentClassesAxiom ben_ut_eq = 
				factory.getOWLEquivalentClassesAxiom(ben_ut_neo,
						factory.getOWLObjectIntersectionOf(ut_neo,factory.getOWLObjectAllValuesFrom(excludesAbnCell, malignant)));
		
		//Constructing ontology and signature
		Set<OWLLogicalAxiom> ontology = new HashSet<OWLLogicalAxiom>();
		ontology.add(renal_inc);
		ontology.add(kidney_eq);
		ontology.add(mal_ut_eq);
		ontology.add(ben_ut_eq);
		
		HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(renal);
		signature.add(mal_ut_neo);
		signature.add(kidney_neo);
		
		HashSet<OWLLogicalAxiom> expectedModule = new HashSet<OWLLogicalAxiom>();
		expectedModule.add(renal_inc);
		expectedModule.add(kidney_eq);
		expectedModule.add(mal_ut_eq);
		

		//Extraction testing 
		AMEX extractor = new AMEX(ontology);
		assertTrue("Module extraction",extractor.extractModule(signature).equals(expectedModule));
		
		
		
	}

}
