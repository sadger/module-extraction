package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.experiments.SemanticOnlyComparison;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.qbf.SeparabilityAxiomLocator;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.storage.DefinitorialAxiomStore;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticOnlyExtractor implements Extractor {

	private DefinitorialAxiomStore axiomStore;
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;
	private InseperableChecker inseparableChecker;
	private long qbfChecks = 0;

	public SemanticOnlyExtractor(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public SemanticOnlyExtractor(Set<OWLLogicalAxiom> ontology){
		axiomStore = new DefinitorialAxiomStore(ontology);
		inseparableChecker = new InseperableChecker();
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(),signature);
	}

	private OWLLogicalAxiom findSeparableAxiom(boolean[] terminology)
			throws IOException, QBFSolverException {

		SeparabilityAxiomLocator search = new SeparabilityAxiomLocator(axiomStore.getSubsetAsArray(terminology),sigUnionSigM,null);

		OWLLogicalAxiom insepAxiom = search.getInseperableAxiom();
		qbfChecks += search.getCheckCount();

		return insepAxiom;
	}

	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {

		boolean[] terminology = axiomStore.allAxiomsAsBoolean();
		module = existingModule;
		sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
		sigUnionSigM.addAll(signature);

		try{
						
			while(inseparableChecker.isSeperableFromEmptySet(axiomStore.getSubsetAsList(terminology),sigUnionSigM)){
				OWLLogicalAxiom axiom = findSeparableAxiom(terminology);
				module.add(axiom);
				axiomStore.removeAxiom(terminology, axiom);
				sigUnionSigM.addAll(axiom.getSignature());
			}
			qbfChecks += inseparableChecker.getTestCount();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		} catch (QBFSolverException qbfe) {
			qbfe.printStackTrace();
		}


		return module;
	}

	public long getQBFCount(){
		return qbfChecks;
	}

	public static void main(String[] args) throws IOException {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/semantic-only/test.krss");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		SemanticOnlyExtractor extractor = new SemanticOnlyExtractor(ont);
		System.out.println(ont);
		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLClass a = f.getOWLClass(IRI.create("X#A"));
		OWLClass b = f.getOWLClass(IRI.create("X#B"));
		OWLClass c = f.getOWLClass(IRI.create("X#C"));
		OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));
		Set<OWLEntity> sig = new HashSet<OWLEntity>();
		sig.add(a);
// 		sig.add(b);

		System.out.println(sig);
		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont, null);
		compare.performExperiment(sig);
		compare.writeMetrics(null);



//		System.out.println(sig);
//		Set<OWLLogicalAxiom> module = extractor.extractModule(sig);
//		System.out.println("|M|: " + module);
	}




}
