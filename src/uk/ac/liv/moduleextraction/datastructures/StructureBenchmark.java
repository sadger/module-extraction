package uk.ac.liv.moduleextraction.datastructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class StructureBenchmark {

	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "NCI/nci-08.09d-terminology.owl");
		
		Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(), 100000000);
		
		ArrayList<OWLLogicalAxiom> first = new ArrayList<OWLLogicalAxiom>(subset);
		//Collections.sort(first, new AxiomComparator(new DefinitorialDepth(subset).getDefinitorialMap()));
		
		LinkedHashList<OWLLogicalAxiom> second = new LinkedHashList<OWLLogicalAxiom>(first);
		
		System.out.println(first.size() + ":"  + second.size());
		
		
		
		Set<OWLLogicalAxiom> smallerSubset = ModuleUtils.generateRandomAxioms(subset, 10000);
		
		
		Set<OWLLogicalAxiom> m1 = new HashSet<OWLLogicalAxiom>();
		long startTime = System.currentTimeMillis();
		Iterator<OWLLogicalAxiom> axiomIterator = first.iterator();
		while(!(m1.size() == smallerSubset.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();
			
			if(smallerSubset.contains(chosenAxiom)){
				m1.add(chosenAxiom);
				first.remove(chosenAxiom);
				axiomIterator = first.iterator();
			}
		}
		System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS(System.currentTimeMillis() - startTime));
		
		
		Set<OWLLogicalAxiom> m2 = new HashSet<OWLLogicalAxiom>();
		startTime = System.currentTimeMillis();
		axiomIterator = second.iterator();
		while(!(m2.size() == smallerSubset.size())){
			OWLLogicalAxiom chosenAxiom = axiomIterator.next();
			
			if(smallerSubset.contains(chosenAxiom)){
				m2.add(chosenAxiom);
				second.remove(chosenAxiom);
				axiomIterator = second.iterator();
			}
		}
		System.out.println("Time taken: " + ModuleUtils.getTimeAsHMS((System.currentTimeMillis() - startTime)));
		
		System.out.println("Same result?:" + m1.equals(m2));
	}

}
