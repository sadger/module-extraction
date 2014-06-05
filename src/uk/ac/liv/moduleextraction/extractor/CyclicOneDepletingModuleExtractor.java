package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
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
import uk.ac.liv.moduleextraction.qbf.CyclicSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
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
		
		OntologyCycleVerifier verifier = new OntologyCycleVerifier(allAxioms);

		cycleCausing = verifier.getCycleCausingAxioms();

		// All axioms is now the acyclic subset of the ontology
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
		
		if(inseparableChecker.isSeperableFromEmptySet(lhs,sigUnionSigM)){
			OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
			module.add(axiom);
			System.out.println("Added axiom");
			removeAxiom(terminology, axiom);
			sigUnionSigM.addAll(axiom.getSignature());
			qbfChecks += inseparableChecker.getTestCount();
			applyRules(terminology);
		}
	}

	private void moveELChainsToModule(List<OWLLogicalAxiom> allAxioms, boolean[] terminology, DefinitorialAxiomStore axiomStore){
		boolean change = true;
		while(change){
			change = false;
			for (int i = 0; i < allAxioms.size(); i++) {
				OWLLogicalAxiom chosenAxiom = allAxioms.get(i);
				if(chainCollector.hasELSyntacticDependency(chosenAxiom, dependT, sigUnionSigM)){
					change = true;
					ArrayList<OWLLogicalAxiom> chain = 
							chainCollector.collectELAxiomChain(allAxioms, i, terminology, axiomStore, dependT, sigUnionSigM);
					
					module.addAll(chain);
					System.out.println("Added chain");
					allAxioms.removeAll(chain);
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
		//OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/cycles/cycle-test2.krss");
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Profile/Thesaurus_14.05d.owl-core");
		System.out.println("Loaded");
		ModuleUtils.remapIRIs(ontology, "X");
	
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		
		OWLClass a = factory.getOWLClass(IRI.create("X#A"));
		OWLClass b = factory.getOWLClass(IRI.create("X#B"));
		OWLClass bprime = factory.getOWLClass(IRI.create("X#B'"));
		OWLClass c = factory.getOWLClass(IRI.create("X#C"));
		OWLClass d = factory.getOWLClass(IRI.create("X#D"));		
		OWLClass x = factory.getOWLClass(IRI.create("X#X"));
		OWLClass y = factory.getOWLClass(IRI.create("X#Y"));
		
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.add(a);
		sig.add(bprime);
		
		SignatureGenerator gen = new SignatureGenerator(ontology.getLogicalAxioms());
		sig = gen.generateRandomSignature(100);
		System.out.println(sig);
		CyclicOneDepletingModuleExtractor onedep = new CyclicOneDepletingModuleExtractor(ontology);
		HybridModuleExtractor hybrid = new HybridModuleExtractor(ontology);
	
		System.out.println("1-dep:  " + onedep.extractModule(sig).size());
		System.out.println("Hybrid: " + hybrid.extractModule(sig).size());
		
		
	}






}
