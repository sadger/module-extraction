package uk.ac.liv.moduleextraction.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.kerberos.KerberosKey;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.checkers.DefinitorialDependencies;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.axioms.Pair;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;





public class SubsetConstructor {
	private static final int MAX_PAIR_SIZE = 15000; 

	private int averageSize;
	private int setSize;
	private HashSet<OWLClass> uniqueClasses;
	private Map<Integer, Set<OWLClass>> numberMap;
	private Set<Pair<OWLClass, OWLClass>> pairs = null;

	public SubsetConstructor(Map<Integer, Set<OWLClass>> numberMap, int averageSize, int setSize) {
		this.averageSize = averageSize;
		this.setSize = setSize;
		this.uniqueClasses = new HashSet<OWLClass>();
		this.numberMap = numberMap;
	}

	public Set<OWLClass> generateRandomSignature(){
		Set<Pair<OWLClass, OWLClass>> pairs = populateNumberMap(numberMap);
		ArrayList<Pair<OWLClass,OWLClass>> pairList = getShuffledPairsAsList(pairs);
		return buildSignatureFromPairs(pairList);
	}

	private ArrayList<Pair<OWLClass,OWLClass>> getShuffledPairsAsList(Set<Pair<OWLClass, OWLClass>> pairs){
		ArrayList<Pair<OWLClass,OWLClass>> pairList = new ArrayList<Pair<OWLClass,OWLClass>>(pairs);
		Collections.shuffle(pairList);
		return pairList;
	}

	private Set<Pair<OWLClass, OWLClass>> populateNumberMap(Map<Integer, Set<OWLClass>> numberMap) {
		if(pairs == null){
			pairs = new HashSet<Pair<OWLClass,OWLClass>>();
			/* We want each pair to add up the average so 
			The sum of each pair needs to be twice average size
			Also the maximum values of each pair can be that of twice average
			size i.e if x=averageSize*2 and y=0 then the x+y/2=averageSize */
			int checkSize = averageSize * 2;

			for(int x : numberMap.keySet()){
				if(x <= checkSize){
					for(int y : numberMap.keySet()){
						if(y <=  checkSize){
							if(x + y == checkSize){
								for(OWLClass cls1 : numberMap.get(x)){
									for(OWLClass cls2 : numberMap.get(y)){
										if(!cls1.equals(cls2) && pairs.size() < MAX_PAIR_SIZE){
											pairs.add(new Pair<OWLClass, OWLClass>(cls1, cls2));
											uniqueClasses.add(cls2);	
										}
									}
								}

							}
						}
					}
				}
			}
		}
		
		return pairs;
	}

	private Set<OWLClass> buildSignatureFromPairs(ArrayList<Pair<OWLClass, OWLClass>> pairList) {
		Set<OWLClass> signature = new HashSet<OWLClass>();

		/* If there aren't enough concepts to make our desired size, use the most we have */
		int desiredSize = uniqueClasses.size()*2 > setSize ? setSize : uniqueClasses.size()*2;  

		while(signature.size() != desiredSize){
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
		uniqueClasses.clear();
		return signature;
	}


	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/expr/nci-08.09d-terminology.owl");
		DefinitorialDependencies deps = new DefinitorialDependencies(ont.getLogicalAxioms());

		int[] values = {25,65,105,145,228,269,310,340,380,420};
		
		for(int i : values){
			SubsetConstructor constructor = new SubsetConstructor(deps.getDependenciesByNumber(),i,50);
			Set<OWLClass> sig = constructor.generateRandomSignature();
			System.out.println(sig);
			System.out.println(i + ":" + sig.size());
		}


	}


}
