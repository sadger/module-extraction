package uk.ac.liv.moduleextraction.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SyntacticFirstModuleExtraction {

	/* Syntactic Checking */
	private SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	
	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();
	
	/* Data Structures */
	private LinkedHashList<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> signature;
	private HashSet<OWLEntity> sigUnionSigM;
	
	/* For writing sigs that cause inseperability */
	SigManager sigManager = new SigManager(new File(ModulePaths.getSignatureLocation() + "/insepSigs"));

	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> terminology, Set<OWLEntity> signature) {
		this(terminology, null, signature);
	}

	
	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> sig) {
		ArrayList<OWLLogicalAxiom> listOfAxioms = new ArrayList<OWLLogicalAxiom>(term);
		HashMap<OWLClass, Integer> definitorialMap = new DefinitorialDepth(term).getDefinitorialMap();
		Collections.sort(listOfAxioms, new AxiomComparator(definitorialMap));

		this.terminology = new LinkedHashList<OWLLogicalAxiom>(listOfAxioms);
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		
		populateSignature();
	}
	
	
	public LinkedHashList<OWLLogicalAxiom> getTerminology() {
		return terminology;
	}
	
	public Set<OWLLogicalAxiom> getModule() {
		return module;
	}



	private void populateSignature() {
		sigUnionSigM = new HashSet<OWLEntity>();
		sigUnionSigM.addAll(signature);
	}


	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{
		collectSyntacticDependentAxioms();
		
		ChainDependencies tminusMDependencies = new ChainDependencies();
		tminusMDependencies.updateDependenciesWith(terminology);
		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(terminology,sigUnionSigM,tminusMDependencies);

		if(insepChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			sigManager.writeFile(signature, "insep" + Math.abs(signature.hashCode()));
			collectSemanticDependentAxioms();
		}

		return module;
	}

	private void collectSyntacticDependentAxioms() {
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
		ChainDependencies syntaticDependencies = new ChainDependencies();

		int addedCount = 0;
		/* Terminology is the value of T\M as we remove items and add them to the module */
		while(!(terminology.size() == W.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);
		
			syntaticDependencies.updateDependenciesWith(chosenAxiom);

			if(syntaxDepChecker.hasSyntacticSigDependency(W, syntaticDependencies, sigUnionSigM)){
				addedCount++;
				Set<OWLLogicalAxiom> axiomsWithDeps = syntaxDepChecker.getAxiomsWithDependencies();
				terminology.removeAll(axiomsWithDeps);
				//System.out.println("Adding " + axiomsWithDeps);
				
				module.add(chosenAxiom);
				sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(axiomsWithDeps));
				
				
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
		}
		if(addedCount > 0)
			System.out.println("Adding " + addedCount + " axiom(s) to module");
	}

	private void collectSemanticDependentAxioms() throws IOException, QBFSolverException {
		System.out.println("Collecting semantic dependencies");
		ChainDependencies lhsDependencies = new ChainDependencies();
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
			
		boolean axiomFound = false;
		while(!axiomFound){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);
			lhsDependencies.updateDependenciesWith(chosenAxiom);

			Set<OWLLogicalAxiom> lhs = lhsExtractor.getLHSSigAxioms(W, sigUnionSigM,lhsDependencies);
			if(insepChecker.isSeperableFromEmptySet(lhs, sigUnionSigM)){
				System.out.println("Adding: " + chosenAxiom);
				module.add(chosenAxiom);
				sigUnionSigM.addAll(chosenAxiom.getSignature());
				terminology.remove(chosenAxiom);
				extractModule();
				axiomFound = true;
			}
		}
		
	}


	public static void main(String[] args) {
		
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
		System.out.println("Loaded Ontology");

		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());

		for (int i = 0; i < 1; i++) {
			Set<OWLEntity> sig = gen.generateRandomSignature(100);
			
			SyntacticLocalityModuleExtractor syntaxModExtractor = 
					new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ont, ModuleType.STAR);
			Set<OWLLogicalAxiom> starModule = ModuleUtils.getLogicalAxioms(syntaxModExtractor.extract(sig));
			System.out.println("Star module size " + starModule.size());

	
			Set<OWLLogicalAxiom> syntfirstExtracted = null;
			System.out.println("|Signature|: " + sig.size());


			try {
				long startTime = System.currentTimeMillis();
				SyntacticFirstModuleExtraction syntmod = new SyntacticFirstModuleExtraction(starModule, sig);
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

	}

	//	}
}
