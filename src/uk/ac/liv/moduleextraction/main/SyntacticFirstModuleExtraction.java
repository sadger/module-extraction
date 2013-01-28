package uk.ac.liv.moduleextraction.main;

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
import uk.ac.liv.moduleextraction.reloading.DumpExtractionToDisk;
import uk.ac.liv.moduleextraction.reloading.ReloadExperimentFromDisk;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.testing.ImprovedDependencyCalculator;
import uk.ac.liv.moduleextraction.util.AxiomComparator;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class SyntacticFirstModuleExtraction {

	private ChainDependencies dependencies;
	private SyntacticDependencyChecker syntaxDepChecker = new SyntacticDependencyChecker();
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();
	
	private LinkedHashList<OWLLogicalAxiom> terminology;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> signature;
	private HashSet<OWLEntity> sigUnionSigM;

	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> sig) {
		ArrayList<OWLLogicalAxiom> listOfAxioms = new ArrayList<OWLLogicalAxiom>(term);
		HashMap<OWLClass, Integer> definitorialMap = new DefinitorialDepth(term).getDefinitorialMap();
		Collections.sort(listOfAxioms, new AxiomComparator(definitorialMap));

		this.terminology = new LinkedHashList<OWLLogicalAxiom>(listOfAxioms);
		this.signature = sig;
		this.module = (existingModule == null) ? new HashSet<OWLLogicalAxiom>() : existingModule;
		this.dependencies = new ChainDependencies();
		
		sigUnionSigM = new HashSet<OWLEntity>();
		sigUnionSigM.addAll(sig);
	}

	public SyntacticFirstModuleExtraction(Set<OWLLogicalAxiom> terminology, Set<OWLEntity> signature) {
		this(terminology, null, signature);
	}

	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException{

		collectSyntacticDependentAxioms();

		HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(terminology, sigUnionSigM);

		if(insepChecker.isSeperableFromEmptySet(lhsSigT, sigUnionSigM)){
			collectSemanticDependentAxioms();
		}

		return module;
	}

	private void collectSyntacticDependentAxioms() {
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module
		while(!(terminology.size() == W.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);
		
			dependencies.updateDependenciesWith(chosenAxiom);

			if(syntaxDepChecker.hasSyntacticSigDependency(W, dependencies, sigUnionSigM)){
				Set<OWLLogicalAxiom> axiomsWithDeps = syntaxDepChecker.getAxiomsWithDependencies();
				terminology.removeAll(axiomsWithDeps);
				System.out.println("Adding " + axiomsWithDeps);
				
				module.add(chosenAxiom);
				sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(axiomsWithDeps));
				
				
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
		}
	}

	private void collectSemanticDependentAxioms() throws IOException, QBFSolverException {
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();

		//Terminology is the value of T\M as we remove items as we add them to the module

		boolean addedAxiom = false;

		while(!addedAxiom){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);

			Set<OWLEntity> signatureAndSigM = new HashSet<OWLEntity>();
			signatureAndSigM.addAll(signature);
			signatureAndSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));

			HashSet<OWLLogicalAxiom> lhsSigT = lhsExtractor.getLHSSigAxioms(W, signatureAndSigM);

			if(insepChecker.isSeperableFromEmptySet(lhsSigT, signatureAndSigM)){
				HashSet<OWLLogicalAxiom> axiomsToAdd = new HashSet<OWLLogicalAxiom>();
				axiomsToAdd.add(chosenAxiom);
				addedAxiom = true;
				terminology.remove(chosenAxiom);
				System.out.println("Adding " + chosenAxiom);
				module.add(chosenAxiom);
				W.clear();
				collectSyntacticDependentAxioms();
			}
		}
	}


	public static void main(String[] args) {
		
		ReloadExperimentFromDisk reloader = null;
		try {
			reloader = new ReloadExperimentFromDisk("/home/william/PhD/Ontologies/Results/test4-50");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
		OWLOntology ont = OntologyLoader.loadOntology("/home/william/PhD/Ontologies/NCI/nci-08.09d-terminology.owl");
		System.out.println("Loaded Ont");

		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());

		Set<OWLEntity> sig = gen.generateRandomSignature(5000);

		SyntacticLocalityModuleExtractor syntaxModExtractor = 
				new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), ont, ModuleType.STAR);

		Set<OWLLogicalAxiom> starModule = ModuleUtils.getLogicalAxioms(syntaxModExtractor.extract(sig));

		System.out.println("Star module size " + starModule.size());

		SyntacticFirstModuleExtraction syntmod = new SyntacticFirstModuleExtraction(starModule, sig);

		Set<OWLLogicalAxiom> syntfirstExtracted = null;

		System.out.println("Signature: " + sig);


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
		System.out.println("Identical to slow test?: " +  syntfirstExtracted.equals(reloader.getModule()));
	}

	//	}
}
