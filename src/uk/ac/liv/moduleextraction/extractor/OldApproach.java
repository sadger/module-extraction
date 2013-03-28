package uk.ac.liv.moduleextraction.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.checkers.SyntacticDependencyChecker;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.qbf.QBFSolverException;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class OldApproach {
	
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
	
	private long checks = 0;

	
	public OldApproach(Set<OWLLogicalAxiom> term, Set<OWLEntity> sig) {
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(term);
		ArrayList<OWLLogicalAxiom> depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();
		this.terminology = new LinkedHashList<OWLLogicalAxiom>(depthSortedAxioms);
		this.signature = sig;
		this.module = new HashSet<OWLLogicalAxiom>();
		
		populateSignature();
	}
	
	public Set<OWLLogicalAxiom> extractModule() throws IOException, QBFSolverException {
		LinkedHashList<OWLLogicalAxiom> W  = new LinkedHashList<OWLLogicalAxiom>();
		Iterator<OWLLogicalAxiom> axiomIterator = terminology.iterator();
		ChainDependencies syntaticDependencies = new ChainDependencies();

		/* Terminology is the value of T\M as we remove items and add them to the module */
		while(!(terminology.size() == W.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();

			W.add(chosenAxiom);
			
			syntaticDependencies.updateDependenciesWith(chosenAxiom);
			
			checks++;
			if(syntaxDepChecker.hasSyntacticSigDependency(W, syntaticDependencies, sigUnionSigM) || 
					insepChecker.isSeperableFromEmptySet(lhsExtractor.getLHSSigAxioms(W, sigUnionSigM, syntaticDependencies), sigUnionSigM)){

				module.add(chosenAxiom);
				terminology.remove(chosenAxiom);
				for(OWLEntity e : chosenAxiom.getSignature()){
					if(!e.isBottomEntity() && !e.isTopEntity()){
						sigUnionSigM.add(e);
					}
				}
				W.clear();
				/* reset the iterator */
				axiomIterator = terminology.iterator();
			}
		}
		
		return module;
	}
	
	private void populateSignature() {
		sigUnionSigM = new HashSet<OWLEntity>();
		sigUnionSigM.addAll(signature);
	}
	
	public long getChecks() {
		return checks;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology("/LOCAL/wgatens/Ontologies/Bioportal/NOTEL/Terminologies/Acyclic/Big/LiPrO-converted");
		SignatureGenerator gen = new SignatureGenerator(ont.getLogicalAxioms());
		Set<OWLEntity> sig = gen.generateRandomSignature(20);
		
		Set<OWLLogicalAxiom> syntfirstExtracted = null;
		Set<OWLLogicalAxiom> oldExtracted = null;
		
		SyntacticFirstModuleExtraction syntmod = null;
		OldApproach oldMod = null;
		try {
			long startTime = System.currentTimeMillis();
			syntmod = new SyntacticFirstModuleExtraction(ont.getLogicalAxioms(), sig);
			syntfirstExtracted = syntmod.extractModule();
			System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));
			
			startTime = System.currentTimeMillis();
			oldMod = new OldApproach(ont.getLogicalAxioms(), sig);
			oldExtracted = oldMod.extractModule();
			System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}
		
		System.out.println("New approach " + syntfirstExtracted.size());
		System.out.println("Old approach " + oldExtracted.size());
		System.out.println("Modules same? " + syntfirstExtracted.equals(oldExtracted));
		System.out.println("Old checks" + oldMod.getChecks());
		System.out.println("New metrics " + syntmod.getMetrics());
	}
}
