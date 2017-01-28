package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;

public class AxiomStructureInspector {

	private HashMap<OWLClass, Set<OWLLogicalAxiom>> inclusions = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
	private HashMap<OWLClass, Set<OWLLogicalAxiom>> equalities = new HashMap<OWLClass, Set<OWLLogicalAxiom>>();
	private HashSet<OWLClass> definedClasses =  new HashSet<OWLClass>();
	private HashSet<OWLClass> primitiveDefinitions = new HashSet<OWLClass>();

	Collection<OWLLogicalAxiom> axioms;

	public AxiomStructureInspector(OWLOntology ont) {
		this(ont.getLogicalAxioms());

	}

	public AxiomStructureInspector(Collection<OWLLogicalAxiom> axioms){
		this.axioms = axioms;
		inspectAxioms();
	}

	public Set<OWLLogicalAxiom> getPrimitiveDefinitions(OWLClass cls) {
		Set<OWLLogicalAxiom> prim = inclusions.get(cls);
		return (prim == null) ? new HashSet<OWLLogicalAxiom>() : prim;
	}

	public Set<OWLLogicalAxiom> getDefinitions(OWLClass cls) {
		Set<OWLLogicalAxiom> eqs = equalities.get(cls);
		return (eqs == null) ? new HashSet<OWLLogicalAxiom>() : eqs;
	}

	private void inspectAxioms(){

		for(OWLLogicalAxiom axiom : axioms){
			
			AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();
			
			if(verifier.isSupportedAxiom(axiom)){
				AxiomType<?> type = axiom.getAxiomType();

				OWLClass name = null;

				name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);


				if(type == AxiomType.EQUIVALENT_CLASSES){
					Set<OWLLogicalAxiom> equalitySet = equalities.get(name);
					if(equalitySet != null){
						equalities.get(name).add(axiom);
					}
					else{
						equalities.put(name, new HashSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
					}

					definedClasses.add(name);

				}
				else if(type == AxiomType.SUBCLASS_OF){
					Set<OWLLogicalAxiom> subclassSet = inclusions.get(name);
					if(subclassSet != null){
						subclassSet.add(axiom);
					}
					else{
						inclusions.put(name, new HashSet<OWLLogicalAxiom>(Collections.singleton(axiom)));
					}
					primitiveDefinitions.add(name);
				}

			}



		}

	}

	public int countNamesWithRepeatedInclusions(){
		int count = 0;
		for(OWLClass cls : inclusions.keySet()){
			if(inclusions.get(cls).size() > 1){
				count++;
			}
		}
		return count;
	}

	public int countNamesWithRepeatedEqualities(){
		int count = 0;
		for(OWLClass cls : equalities.keySet()){
			if(equalities.get(cls).size() > 1){
				count++;
			}
		}
		return count;
	}
	
	public Set<OWLClass> getNamesWithRepeatedEqualities(){
		Set<OWLClass> names = new HashSet<OWLClass>();
		for(OWLClass cls : equalities.keySet()){
			if(equalities.get(cls).size() > 1){
				names.add(cls);
			}
		}
		return names;
		
	}
	
	public Set<OWLClass> getSharedNamesWithRepeatedEqualities(){
		Set<OWLClass> sharedAndRepeatedEquality = new HashSet<OWLClass>();
		Set<OWLClass> repEqualities = getNamesWithRepeatedEqualities();
		for(OWLClass cls : getSharedNames()){
			if(repEqualities.contains(cls)){
				sharedAndRepeatedEquality.add(cls);
			}
		}
		
		return sharedAndRepeatedEquality;
	}


	public Set<OWLClass> getSharedNames(){
		Set<OWLClass> namesInIntersect = new HashSet<OWLClass>(definedClasses);
		namesInIntersect.retainAll(primitiveDefinitions);

		return namesInIntersect;
	}

	public static void main(String[] args) {
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "Bioportal/LiPrO");
        Set<OWLLogicalAxiom> core = new HashSet<>();
        core.addAll(ModuleUtils.getCoreAxioms(ont));
		AxiomStructureInspector inspector = new AxiomStructureInspector(core);
		core.forEach(System.out::println);
		Set<OWLClass> sharedNames = inspector.getSharedNames();
		for(OWLClass shared : sharedNames){
			System.out.println("Shared: " + shared);
			System.out.println(inspector.getDefinitions(shared));
			System.out.println(inspector.getPrimitiveDefinitions(shared).size());
			System.out.println(inspector.getSharedNamesWithRepeatedEqualities().size());
		}

	}



}
