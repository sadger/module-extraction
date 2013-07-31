package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.NewSyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class SemanticRuleExtractor implements Extractor{

	ChainDependencies dependT;
	Set<OWLLogicalAxiom> module;
	Set<OWLEntity> sigUnionSigM;
	NewSyntacticDependencyChecker syntacticDependencyChecker;
	DefinitorialAxiomStore axiomStore;
	LHSSigExtractor lhsExtractor;
	InseperableChecker inseperableChecker;
	
	long syntacticChecks = 0; // A syntactic iteration (total checks = this + qbfchecks)
	long timeTaken = 0; //Time taken to setup and extract the module (ms)
	long qbfChecks = 0;
	
	
	Logger logger = LoggerFactory.getLogger(SemanticRuleExtractor.class);
			
	public static class DefinitorialAxiomStore{
		final OWLLogicalAxiom[] axioms;
		
		public DefinitorialAxiomStore(List<OWLLogicalAxiom> axs) {
			axioms = axs.toArray(new OWLLogicalAxiom[axs.size()]);	
		}
		
		public OWLLogicalAxiom getAxiom(int index){
			return axioms[index];
		}
		
		public boolean[] allAxiomsAsBoolean(){
			boolean[] booleanAxioms = new boolean[axioms.length];
			for (int i = 0; i < booleanAxioms.length; i++) {
				booleanAxioms[i] = true;
			}
			return cloneSubset(booleanAxioms);
		}
		
		private boolean[] cloneSubset(boolean[] oldSubset) {
			boolean[] clonedSubset = new boolean[oldSubset.length];
			System.arraycopy(oldSubset, 0, clonedSubset, 0, oldSubset.length);
			return clonedSubset;
		}
		
		public int getSubsetCardinality(boolean[] subset){
			int cardinality = 0;
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					cardinality++;
				}
			}
			return cardinality;
		}
		
		public OWLLogicalAxiom[] getSubsetAsArray(boolean[] subset){
			OWLLogicalAxiom[] newArray = new OWLLogicalAxiom[getSubsetCardinality(subset)];
			int currentIndex = 0;
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					newArray[currentIndex] = getAxiom(i);
					currentIndex++;
				}
			}
			return newArray;
		}

		public List<OWLLogicalAxiom> getSubsetAsList(boolean[] subset){
			ArrayList<OWLLogicalAxiom> newSet = new ArrayList<OWLLogicalAxiom>();
			for (int i = 0; i < subset.length; i++) {
				if(subset[i]){
					newSet.add(axioms[i]);
				}
			}
			return newSet;
		}

		/* Worse case linear time - can improve this with binary search probably */
		public void removeAxiom(boolean[] subset, OWLLogicalAxiom axiom){
			for (int i = 0; i < axioms.length; i++) {
				if(axioms[i].equals(axiom)){
					subset[i] = false;
					break;
				}
			}
		}
	}
	
	
	public SemanticRuleExtractor(OWLOntology ontology) {
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(ontology);
		ArrayList<OWLLogicalAxiom> depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();
		dependT = new ChainDependencies();
		dependT.updateDependenciesWith(depthSortedAxioms);
		axiomStore = new DefinitorialAxiomStore(depthSortedAxioms);
		
		syntacticDependencyChecker = new NewSyntacticDependencyChecker();
		lhsExtractor = new LHSSigExtractor();
		inseperableChecker = new InseperableChecker();
	}
	

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
		/* Reset all the metrics for new extraction */
		syntacticChecks = 0; 
		timeTaken = 0; 
		qbfChecks = 0;
		inseperableChecker.resetMetrics();
		
		long startTime = System.currentTimeMillis();
		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);
		
		try {
			applyRules(terminology);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		timeTaken = System.currentTimeMillis() - startTime;
		return module;
	}
	
	public LinkedHashMap<String, Long> getMetrics() {
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		metrics.put("Module size", (long) module.size());
		metrics.put("Time taken", timeTaken);
		metrics.put("Syntactic Checks", syntacticChecks);
		metrics.put("QBF Checks", qbfChecks);
		return metrics;
	}


	public LinkedHashMap<String, Long> getQBFMetrics() {
		return inseperableChecker.getQBFMetrics();
	}

	
	
	private void applyRules(boolean[] terminology) throws IOException, QBFSolverException{
		applySyntacticRule(terminology);
		
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(axiomStore.getSubsetAsList(terminology),sigUnionSigM,dependT);
		qbfChecks++;
		if(inseperableChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			OWLLogicalAxiom insepAxiom = findSeparableAxiom(terminology);
			module.add(insepAxiom);
			sigUnionSigM.addAll(insepAxiom.getSignature());
			axiomStore.removeAxiom(terminology, insepAxiom);
			applyRules(terminology);
		}
	}


	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {
	
		SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,dependT);
		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		logger.debug("Adding (semantic): {}", insepAxiom);
		qbfChecks += search.getCheckCount();
		return insepAxiom;
	}


	private void applySyntacticRule(boolean[] terminology){
		boolean change = true;
		
		while(change){
			change = false;
			for (int i = 0; i < terminology.length; i++) {
				
				if(terminology[i]){
					
					OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
					syntacticChecks++;
					if(syntacticDependencyChecker.hasSyntacticSigDependency(chosenAxiom, dependT, sigUnionSigM)){
						
						change = true;
						module.add(chosenAxiom);
						terminology[i] = false;
						logger.debug("Adding (syntactic): {}", chosenAxiom);
						sigUnionSigM.addAll(chosenAxiom.getSignature());
						
						
					}
				}
			}
		}
	
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, InterruptedException {
		ToStringRenderer stringRender = ToStringRenderer.getInstance();
		DLSyntaxObjectRenderer renderer;
		renderer =  new DLSyntaxObjectRenderer();
		stringRender.setRenderer(renderer);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		
//		Renal_Pelvis_and_Ureter_Carcinoma ≡ Kidney_and_Ureter_Neoplasm ⊓ Malignant_Urinary_Tract_Neoplasm ⊓ (∃ Disease_May_Have_Finding.Hematuria) ⊓ (∀ Disease_Has_Primary_Anatomic_Site.Renal_Pelvis_and_Ureter)
//		Urinary_Tract_Neoplasm ≡ Neoplasm_by_Site ⊓ Urinary_Tract_Disorder
//		Renal_Pelvis_and_Ureter ⊑ ∃ Anatomic_Structure_Is_Physical_Part_Of.Kidney_and_Ureter
//		Kidney_and_Ureter_Neoplasm ≡ Urinary_Tract_Neoplasm ⊓ (∀ Disease_Has_Associated_Anatomic_Site.Kidney_and_Ureter)
//		Malignant_Urinary_Tract_Neoplasm ≡ Urinary_Tract_Neoplasm ⊓ (∀ Disease_Has_Abnormal_Cell.Malignant_Cell)
//		Benign_Nasal_Cavity_Neoplasm ≡ Nasal_Cavity_Neoplasm ⊓ (∀ Disease_Excludes_Abnormal_Cell.Malignant_Cell)
//		Signature: [Renal_Pelvis_and_Ureter_Carcinoma, Stage_III_Renal_Pelvis_and_Ureter_Carcinoma, Disease_May_Have_Finding]
		
		OWLOntology ont = manager.createOntology();
		
		OWLClass b = factory.getOWLClass(IRI.create("#Kidney_and_Ureter_Neoplasm"));
		OWLClass c = factory.getOWLClass(IRI.create("#Malignant_Urinary_Tract_Neoplasm"));
		OWLClass e = factory.getOWLClass(IRI.create("#Renal_Pelvis_and_Ureter"));
	
		OWLClass f = factory.getOWLClass(IRI.create("#Urinary_Tract_Neoplasm"));
		OWLClass h =  factory.getOWLClass(IRI.create("#Kidney_and_Ureter"));
		OWLClass i = factory.getOWLClass(IRI.create("#Malignant_Cell"));
		

		OWLClass extra1 = factory.getOWLClass(IRI.create("#Benign_Nasal_Cavity_Neoplasm"));
		OWLClass extra2 = factory.getOWLClass(IRI.create("#Nasal_Cavity_Neoplasm"));
		
		OWLObjectProperty t = factory.getOWLObjectProperty(IRI.create("#Anatomic_Structure_Is_Physical_Part_Of"));
		OWLObjectProperty u = factory.getOWLObjectProperty(IRI.create("#Disease_Has_Associated_Anatomic_Site"));
		OWLObjectProperty v = factory.getOWLObjectProperty(IRI.create("#Disease_Has_Abnormal_Cell")); 
		OWLObjectProperty x = factory.getOWLObjectProperty(IRI.create("#Disease_Excludes_Abnormal_Cell")); 
		
		
		OWLClassExpression threerhs = factory.getOWLObjectSomeValuesFrom(t, h);
		
		OWLClassExpression fourrhs = factory.getOWLObjectIntersectionOf(f, factory.getOWLObjectAllValuesFrom(u, h));
		
		OWLClassExpression fiverhs = factory.getOWLObjectIntersectionOf(f, factory.getOWLObjectAllValuesFrom(v, i));
		
		OWLClassExpression extrarhs = factory.getOWLObjectIntersectionOf(extra2, factory.getOWLObjectAllValuesFrom(x, i));
		
		
		OWLLogicalAxiom three = factory.getOWLSubClassOfAxiom(e, threerhs);
		OWLLogicalAxiom four = factory.getOWLEquivalentClassesAxiom(b, fourrhs);
		OWLLogicalAxiom five = factory.getOWLSubClassOfAxiom(c, fiverhs);
		OWLLogicalAxiom extra = factory.getOWLSubClassOfAxiom(extra1, extrarhs);
		
		
		//manager.addAxiom(ont, one);
	//	manager.addAxiom(ont, two);
		manager.addAxiom(ont, three);
		manager.addAxiom(ont, four);
		manager.addAxiom(ont, five);
		manager.addAxiom(ont, extra);
		
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		signature.add(b);
		signature.add(c);
		signature.add(e);
		
		System.out.println("Ontology: ");
		for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
			System.out.println(ax);
		}
		System.out.println();
		
		System.out.println("Signature: " + signature);
		System.out.println();
		
		SemanticRuleExtractor extractor = new SemanticRuleExtractor(ont);
		
		Thread.sleep(1000);
		
		System.out.println("Module: ");
		Set<OWLLogicalAxiom> module = extractor.extractModule(signature);
		System.out.println();
		for(OWLLogicalAxiom ax : module){
			System.out.println(ax);
		}
		

	}



	 


	

}
