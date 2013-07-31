package uk.ac.liv.moduleextraction.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.chaindependencies.DependencySet;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.propositional.convertors.ALCtoPropositionalConvertor;


public class EquivalentToTerminologyExtractor implements Extractor {

	private EquivalentToTerminologyProcessor processor;
	private Set<OWLLogicalAxiom> module;
	private SemanticRuleExtractor extractor;
	
	private long timeTaken = 0;
	
	public EquivalentToTerminologyExtractor(OWLOntology equivalentToTerm) {
		try {
			processor = new EquivalentToTerminologyProcessor(equivalentToTerm);
			OWLOntology newOnt = processor.getConvertedOntology();
			extractor = new SemanticRuleExtractor(newOnt);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (NotEquivalentToTerminologyException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {		
		timeTaken = 0;
		
		long startTime = System.currentTimeMillis();
		module =  extractor.extractModule(existingModule,signature);
		module = processor.postProcessModule(module);
		timeTaken = System.currentTimeMillis() - startTime;
		return module;
	}

	public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
		return extractModule(new HashSet<OWLLogicalAxiom>(), signature);
	}

	
	public LinkedHashMap<String, Long> getMetrics() {
		LinkedHashMap<String, Long> metrics = new LinkedHashMap<String, Long>();
		LinkedHashMap<String, Long> extractorMetrics = extractor.getMetrics();
		
		metrics.put("Module size", (long) module.size());
		metrics.put("Time taken", timeTaken);
		metrics.put("Syntactic Checks", extractorMetrics.get("Syntactic Checks"));
		metrics.put("QBF Checks", extractorMetrics.get("QBF Checks"));
		
		return metrics;
	}

	public LinkedHashMap<String, Long> getQBFMetrics() {
		return extractor.getQBFMetrics();
	}
	

	public static void main(String[] args) throws IOException, NotEquivalentToTerminologyException, OWLOntologyCreationException, QBFSolverException, OWLOntologyStorageException, InterruptedException {
		


		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getSignatureLocation() + "/Paper/examples/othersmallmodule2");
		OWLOntologyManager ontMan = ont.getOWLOntologyManager();
		
		DefinitorialDepth depth = new DefinitorialDepth(ont);
		ArrayList<OWLLogicalAxiom> sorted = depth.getDefinitorialSortedList();
		System.out.println("Ontology (sorted by definitorial depth): ");

		for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){
			System.out.println(ax);
		}
		System.out.println();
//
		System.out.println("As propositional:");
		ALCtoPropositionalConvertor convertor = new ALCtoPropositionalConvertor();
		for(OWLLogicalAxiom ax : sorted){
			System.out.println(convertor.convert(ax));
		}
		System.out.println();
		


		

		
		SigManager man = new SigManager(new File(ModulePaths.getSignatureLocation() + "Paper/examples/"));

		EquivalentToTerminologyExtractor extractor = new EquivalentToTerminologyExtractor(ont);
		Set<OWLEntity> sig = man.readFile("othersignature2");
		
	
	
		System.out.println("Signature: " + sig);
		System.out.println();
		

		Set<OWLLogicalAxiom> module = extractor.extractModule(sig);
		System.out.println();
		
		System.out.println("Module:");
		Thread.sleep(1000);
		for(OWLLogicalAxiom ax : module){
			System.out.println(ax);
		}
//
//		Set<OWLAxiom> modOntology = new HashSet<OWLAxiom>();
//		modOntology.addAll(module);
//		
		//OWLOntology ontformod = ontMan.createOntology(modOntology);
//		ontMan.saveOntology(ont,new OWLXMLOntologyFormat(), IRI.create(new File(ModulePaths.getSignatureLocation() + "/Paper/examples/othersmallmodule2")));
		
		
	
	}




}
