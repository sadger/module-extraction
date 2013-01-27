package uk.ac.liv.moduleextraction.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.testing.ImprovedDependencyCalculator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SyntacticFirstModuleExtraction {
	
	private ImprovedDependencyCalculator dependencyCalculator;
	private SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();
	
	private Set<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> signature;
	private DumpExtractionToDisk dump;
	
	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> sig) {
		this.terminology = term;
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		this.dependencyCalculator = new ImprovedDependencyCalculator(term);
		this.dump = new  DumpExtractionToDisk("test4-50", terminology, module, signature);
	}
	
	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> terminology, Set<OWLEntity> signature) {
		this(terminology, null, signature);
	}
	
	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{
		new Thread(dump).start();
		collectSyntacticDependentAxioms();
		Set<OWLEntity> sigUnionSigM = getSigUnionSigModule();
		
		HashMap<OWLClass, Set<OWLEntity>> dependterm = dependencyCalculator.getDependenciesFor(terminology);
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(dependterm, terminology, sigUnionSigM);
		
		if(insepChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			collectSemanticDependentAxioms();
		}

		new Thread(dump).start();
		return module;
	}
	
	private Set<OWLEntity> getSigUnionSigModule() {
		Set<OWLEntity> signatureAndSigM = new HashSet<OWLEntity>();
		signatureAndSigM.addAll(signature);
		signatureAndSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
		return signatureAndSigM;
	}

	private void collectSyntacticDependentAxioms() {
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module
		while(!terminology.equals(W)){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);

			Set<OWLEntity> signatureAndSigM = getSigUnionSigModule();
			
			/* We can reuse this in the LHS check and syntactic check so do it only once */
			HashMap<OWLClass, Set<OWLEntity>> dependW = dependencyCalculator.getDependenciesFor(W);

			if(syntaxDepChecker.hasSyntacticSigDependency(dependW, signatureAndSigM)){
				terminology.remove(chosenAxiom);
				System.out.println("Adding " + chosenAxiom);
				module.add(chosenAxiom);
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
			dependW.clear();
		}
	}
	
	private void collectSemanticDependentAxioms() throws IOException, QBFSolverException {
		HashSet<OWLLogicalAxiom> W  = new HashSet<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module
		
		boolean addedAxiom = false;
		
		while(!addedAxiom){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);

			Set<OWLEntity> signatureAndSigM = new HashSet<OWLEntity>();
			signatureAndSigM.addAll(signature);
			signatureAndSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
			
			HashMap<OWLClass, Set<OWLEntity>> dependW = dependencyCalculator.getDependenciesFor(W);
			HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(dependW, W, signatureAndSigM);

			if(insepChecker.isSeperableFromEmptySet(lhsSigT, signatureAndSigM)){
				addedAxiom = true;
				terminology.remove(chosenAxiom);
				System.out.println("Adding " + chosenAxiom);
				module.add(chosenAxiom);
				W.clear();
				collectSyntacticDependentAxioms();
			}
			dependW.clear();
		}
	}

	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/nci-08.09d-terminology.owl");

		
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		

		SigManager sigManager = new SigManager(new File(ModulePaths.getOntologyLocation()));
		
		
		
//		for (int i = 0; i < 20; i++) {
		
			Set<OWLEntity> sig = gen.generateRandomSignature(50);

			
			SyntacticLocalityModuleExtractor syntaxModExtractor = 
					new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ont, ModuleType.STAR);

			
			Set<OWLLogicalAxiom> starModule = ModuleUtils.getLogicalAxioms(syntaxModExtractor.extract(sig));
			
			
			System.out.println("Star module size " + starModule.size());
			
			SyntacticFirstModuleExtraction syntmod = new SyntacticFirstModuleExtraction(starModule, sig);
			
			Set<OWLLogicalAxiom> syntfirstExtracted = null;
			
			System.out.println(sig);
		
			
			try {
				long startTime = System.currentTimeMillis();
				syntfirstExtracted = syntmod.extractModule();
				System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (QBFSolverException e) {
				e.printStackTrace();
			}

			System.out.println("Synsize: " + syntfirstExtracted.size());
			System.out.println();
		}

//	}
}
