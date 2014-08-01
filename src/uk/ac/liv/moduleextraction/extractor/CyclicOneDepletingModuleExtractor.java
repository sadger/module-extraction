package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.ExtendedLHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.experiments.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.qbf.CyclicSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.axioms.OneDepletingSupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.caching.AxiomMetricStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class CyclicOneDepletingModuleExtractor implements Extractor {

	private DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private InseperableChecker inseparableChecker;
	private long qbfChecks = 0;
	private ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;
	private ExtendedLHSSigExtractor lhsExtractor;
	List<OWLLogicalAxiom> allAxioms;
	private Set<OWLLogicalAxiom> cycleCausing;
	private Set<OWLLogicalAxiom> expressive;
	private ArrayList<OWLLogicalAxiom> acyclicAxioms;

	public CyclicOneDepletingModuleExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public CyclicOneDepletingModuleExtractor(Set<OWLLogicalAxiom> ontology){
		this.axiomStore = new DefinitorialAxiomStore(ontology);
		this.inseparableChecker = new InseperableChecker();
		this.chainCollector = new ELAxiomChainCollector();
		this.lhsExtractor = new ExtendedLHSSigExtractor();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {

		CyclicSeparabilityAxiomLocator search = 
				new CyclicSeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology), cycleCausing, sigUnionSigM, dependT);

		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		qbfChecks += search.getCheckCount();

		return insepAxiom;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {

		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		allAxioms = axiomStore.getSubsetAsList(terminology);

		SupportedExpressivenessFilter filter = new SupportedExpressivenessFilter();
		expressive = filter.getUnsupportedAxioms(allAxioms);
		allAxioms.removeAll(expressive);

		//Can only determine if there is a cycle after removing any expressive axioms
		OntologyCycleVerifier verifier = new OntologyCycleVerifier(allAxioms);

		cycleCausing = verifier.getCycleCausingAxioms();

		/* All axioms is now the acyclic subset of the ontology 
		after removing unsupported or cycle causing axioms */
		allAxioms.removeAll(cycleCausing);


		dependT = new AxiomDependencies(new HashSet<OWLLogicalAxiom>(allAxioms));
		acyclicAxioms = dependT.getDefinitorialSortedAxioms();

		
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

		return module;
	}

	public void applyRules(boolean[] terminology) throws IOException, QBFSolverException{
		moveELChainsToModule(acyclicAxioms, terminology, axiomStore);	

		
		Set<OWLLogicalAxiom> lhs = lhsExtractor.getLHSSigAxioms(acyclicAxioms, sigUnionSigM, dependT);
		
	
		lhs.addAll(cycleCausing);
		lhs.addAll(expressive);

		if(inseparableChecker.isSeperableFromEmptySet(lhs,sigUnionSigM)){
			OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
			module.add(axiom);
			removeAxiom(terminology, axiom);
			sigUnionSigM.addAll(axiom.getSignature());
			qbfChecks += inseparableChecker.getTestCount();
			applyRules(terminology);
		}
	}

	private void moveELChainsToModule(List<OWLLogicalAxiom> acyclicAxioms, boolean[] terminology, DefinitorialAxiomStore axiomStore){
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < acyclicAxioms.size(); i++) {
				OWLLogicalAxiom chosenAxiom = acyclicAxioms.get(i);
				if(chainCollector.hasELSyntacticDependency(chosenAxiom, dependT, sigUnionSigM)){
					change = true;
					ArrayList<OWLLogicalAxiom> chain = 
							chainCollector.collectELAxiomChain(acyclicAxioms, i, terminology, axiomStore, dependT, sigUnionSigM);

					module.addAll(chain);
					acyclicAxioms.removeAll(chain);
				}
			}

		}
	}



	public long getQBFCount(){
		return qbfChecks;
	}

	public void removeAxiom(boolean[] terminology, OWLLogicalAxiom axiom){
		axiomStore.removeAxiom(terminology, axiom);
		allAxioms.remove(axiom);
		cycleCausing.remove(axiom);
		acyclicAxioms.remove(axiom);
	}


	public static void main(String[] args) {

		File[] files = new File("/LOCAL/wgatens/Ontologies//OWL-Corpus-All/qbf-only").listFiles();
		//			for(File f : files){
		//				//	System.out.println(i++);
		//			if(f.exists()){
		File f = new File(ModulePaths.getOntologyLocation() + "OWL-Corpus-All/qbf-only/5bef3885-eef0-4497-888e-7ff5bef673e5_graphy.owl-QBF");

		System.out.print(f.getName() + ": ");

		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
		OntologyCycleVerifier verifier = new OntologyCycleVerifier(ModuleUtils.getCoreAxioms(ont));
		System.out.println(verifier.isCyclic());
		System.out.println(ont.getLogicalAxiomCount());
		CyclicOneDepletingModuleExtractor mod = new CyclicOneDepletingModuleExtractor(ont);
		Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(), 10);
		for(OWLLogicalAxiom sub : subset){
			System.out.println(mod.extractModule(sub.getSignature()).size());
		}
	}
	//			}


	//	}





}
