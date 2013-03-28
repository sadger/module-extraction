package uk.ac.liv.moduleextraction.qbf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.checkers.InseperableChecker;
import uk.ac.liv.moduleextraction.checkers.LHSSigExtractor;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class SeparabilityAxiomLocator {

	Logger logger = LoggerFactory.getLogger(SeparabilityAxiomLocator.class);

	/* Semantic Checking */
	private LHSSigExtractor lhsExtractor = new LHSSigExtractor();
	private InseperableChecker insepChecker = new InseperableChecker();

	/* Data structures */
	private Set<OWLLogicalAxiom> module;
	private Set<OWLEntity> sigUnionSigM;

	private long checkCount = 0;

	private OWLLogicalAxiom[] axiomList;

	public SeparabilityAxiomLocator(List<OWLLogicalAxiom> term, Set<OWLLogicalAxiom> mod, Set<OWLEntity> sig) throws IOException, QBFSolverException{
		this.module = mod;
		this.axiomList = term.toArray(new OWLLogicalAxiom[0]);

		this.sigUnionSigM = sig;
		sigUnionSigM.addAll(ModuleUtils.getClassAndRoleNamesInSet(module));
	}

	public OWLLogicalAxiom getInseperableAxiom() throws IOException, QBFSolverException{	
		/* Represents the last axioms added or removed from the split test */

		logger.debug("Finding separability causing axiom");
		OWLLogicalAxiom[] lastAdded = getTopHalf(axiomList);
		OWLLogicalAxiom[]lastRemoved = getBottomHalf(axiomList);

		OWLLogicalAxiom[] W = lastAdded;

		while(lastAdded.length > 0){

			ChainDependencies Wdeps = new ChainDependencies();
			Wdeps.updateDependenciesWith(W);

			ArrayList<OWLLogicalAxiom> toCheck = new ArrayList<OWLLogicalAxiom>();
			for (int i = 0; i < W.length; i++) {
				toCheck.add(W[i]);
			}

			Set<OWLLogicalAxiom> lhsW = lhsExtractor.getLHSSigAxioms(toCheck, sigUnionSigM, Wdeps);

			checkCount++;

			/* If inseperable */
			if(!insepChecker.isSeperableFromEmptySet(lhsW, sigUnionSigM)){

				lastAdded = getTopHalf(lastRemoved);

				//W.addAll(lastAdded);
				W = concat(W,lastAdded);

				//lastRemoved.removeAll(lastAdded);
				lastRemoved = Arrays.copyOfRange(lastRemoved,lastAdded.length,lastRemoved.length);

				logger.trace("Adding: {}",lastAdded.length);

			}
			else{
				lastRemoved = getBottomHalf(lastAdded);

				//	W.removeAll(lastRemoved);
				W = Arrays.copyOfRange(W,0,W.length - lastRemoved.length);
				//	lastAdded.removeAll(lastRemoved);
				lastAdded = Arrays.copyOfRange(lastAdded, 0,lastAdded.length -lastRemoved.length);

				logger.trace("Removing: {}",lastRemoved.length);

			}

		}
		return  axiomList[W.length];
	}

	public long getCheckCount(){
		return checkCount;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private OWLLogicalAxiom[] getTopHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = 0;
		int toIndex = (int) Math.floor(axiomList.length/2);

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}

	private OWLLogicalAxiom[] getBottomHalf(OWLLogicalAxiom[] axiomList){
		int fromIndex = (int) Math.floor(axiomList.length/2);
		int toIndex = axiomList.length;

		return Arrays.copyOfRange(axiomList, fromIndex,toIndex);

	}

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "Bioportal/NOTEL/Terminologies/Acyclic/Big/LiPrO-converted");
		System.out.println(ont.getLogicalAxiomCount());
		try {
			SeparabilityAxiomLocator locator = new SeparabilityAxiomLocator(new ArrayList<OWLLogicalAxiom>(ont.getLogicalAxioms()), new HashSet<OWLLogicalAxiom>(), new HashSet<OWLEntity>());
			System.out.println(locator.getInseperableAxiom());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QBFSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
