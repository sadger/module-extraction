package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.checkers.ELAxiomChainCollector;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.filters.SupportedExpressivenessFilter;
import uk.ac.liv.moduleextraction.qbf.CyclicSeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicOneDepletingModuleExtractor implements Extractor {

	private final DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private final InseperableChecker inseparableChecker;
	private long qbfChecks = 0;
	private final ELAxiomChainCollector chainCollector;
	private AxiomDependencies dependT;
	private List<OWLLogicalAxiom> allAxioms;
	private Set<OWLLogicalAxiom> cycleCausing = new HashSet<OWLLogicalAxiom>();
	private Set<OWLLogicalAxiom> expressive;
	private ArrayList<OWLLogicalAxiom> acyclicAxioms;

	private CyclicOneDepletingModuleExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public CyclicOneDepletingModuleExtractor(Set<OWLLogicalAxiom> ontology){
		this.axiomStore = new DefinitorialAxiomStore(ontology);
		this.inseparableChecker = new InseperableChecker();
		this.chainCollector = new ELAxiomChainCollector();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {

		CyclicSeparabilityAxiomLocator search = 
				new CyclicSeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology), sigUnionSigM, dependT);

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
		if(verifier.isCyclic()){
			cycleCausing = verifier.getCycleCausingAxioms();
			/* All axioms is now the acyclic subset of the ontology 
			after removing unsupported or cycle causing axioms */
			allAxioms.removeAll(cycleCausing);
		}

		dependT = new AxiomDependencies(new HashSet<OWLLogicalAxiom>(allAxioms));
		acyclicAxioms = dependT.getDefinitorialSortedAxioms();


		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);

		try {
			//Apply the rules to everything
			applyRules(terminology);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}

		return module;
	}

	void applyRules(boolean[] terminology) throws IOException, QBFSolverException{
		moveELChainsToModule(acyclicAxioms, terminology, axiomStore);	

		if(inseparableChecker.isSeperableFromEmptySet(axiomStore.getSubsetAsList(terminology),sigUnionSigM)){
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

	void removeAxiom(boolean[] terminology, OWLLogicalAxiom axiom){
		axiomStore.removeAxiom(terminology, axiom);
		allAxioms.remove(axiom);
		cycleCausing.remove(axiom);
		acyclicAxioms.remove(axiom);
		expressive.remove(axiom);
	}


	public static void main(String[] args) {
		
		File[] files = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only").listFiles();
		System.out.println("Total files: " + files.length);
		int i = 1;
//		String name = "07752c0c-5724-4e83-80f3-ba0d58da9373_L_v315.owl-QBF";
//		File f = new File(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/" + name);
//		System.out.println(f.exists());
		for(File f : files){
			System.out.println("Expr: " + i++);
			if(f.exists() && i == 336){
				System.out.print(f.getName() + ": ");
				OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
				SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
				new CyclicOneDepletingModuleExtractor(ont).extractModule(gen.generateRandomSignature(10));
				ont.getOWLOntologyManager().removeOntology(ont);
			}
		}
		

	}





}
