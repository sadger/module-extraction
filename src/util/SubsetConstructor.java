package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import axioms.Pair;


import checkers.DefinitorialDependencies;


public class SubsetConstructor {
	private SignatureAnalyser sigAnalyser;
	private static final int MAX_PAIR_SIZE = 10000; 
	
	
	public SubsetConstructor(Set<OWLLogicalAxiom> axioms) {
		sigAnalyser = new SignatureAnalyser(axioms);
	}

	/*
	 * Given a number map integer->Set<owlclass>  produces a set of size setSize such
	 * that the average value in the set is equal to avgSize
	 */
	public void generateSets(Map<Integer, Set<OWLClass>> numberMap, int averageSize, int setSize){
		
		//We want each pair to add up the average so 
		//The sum of each pair needs to be twice average size
		//Also the maximum values of each pair can be that of twice average
		//size i.e if x=averageSize*2 and y=0 then the x+y/2=averageSize
		
		int checkSize = averageSize * 2;
		
		Set<OWLClass> signature = new HashSet<OWLClass>();
		Set<Pair<OWLClass, OWLClass>> averageSizePairs = new HashSet<Pair<OWLClass,OWLClass>>();

		
		for(int x : numberMap.keySet()){
			if(x <= checkSize){
				for(int y : numberMap.keySet()){
					if(y <=  checkSize){
						if(x + y == checkSize){
							for(OWLClass cls1 : numberMap.get(x)){
								for(OWLClass cls2 : numberMap.get(y)){
									if(!cls1.equals(cls2) && averageSizePairs.size() < MAX_PAIR_SIZE){
										averageSizePairs.add(new Pair<OWLClass, OWLClass>(cls1, cls2));
									}

								}
							}

						}
					}
				}
			}
		}
		

		ArrayList<Pair<OWLClass,OWLClass>> pairList = new ArrayList<Pair<OWLClass,OWLClass>>(averageSizePairs);
		averageSizePairs.clear();
		Collections.shuffle(pairList);
		

		while(signature.size() != setSize){
			for(Pair<OWLClass,OWLClass> p : pairList){
				OWLClass first = p.getFirst();
				OWLClass second = p.getSecond();
				if(!signature.contains(first) && !signature.contains(second)){
					signature.add(first);
					signature.add(second);
					break;
				}
			}
		}
		
		System.out.println(signature.size());
		System.out.println(sigAnalyser.averageDependencySize(signature));
		System.out.println(signature);
		
		
	}



	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/nci-08.09d-terminology.owl");
		SubsetConstructor constructor = new SubsetConstructor(ont.getLogicalAxioms());
		DefinitorialDependencies deps = new DefinitorialDependencies(ont.getLogicalAxioms());
		
		for (int i = 0; i < 5 ; i++) {
			constructor.generateSets(deps.getDependenciesByNumber(),0, 10);
		}
	}


}
