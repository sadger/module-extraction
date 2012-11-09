package experiments;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class DomainRangeExtraction {
	
	OWLOntology ontology;
	
	SyntacticLocalityModuleExtractor syntaxModExtractor;
	public DomainRangeExtraction(OWLOntology ont) {
		this.ontology = ont;
		syntaxModExtractor = 
				new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.BOT);
	}
	
	public void performTests(){
		HashSet<OWLEntity> signature = new HashSet<OWLEntity>();
		int rangeDomCount = 0;
		
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
			AxiomType<?> type = axiom.getAxiomType();
			if(type == AxiomType.OBJECT_PROPERTY_RANGE || type == AxiomType.OBJECT_PROPERTY_DOMAIN){
				signature.addAll(axiom.getSignature());
				rangeDomCount++;
			}
		}

		System.out.println("Number of range or domain axioms " + rangeDomCount);
		analysisAxioms(extractSyntacticModule(signature));
	}
	
	private Set<OWLAxiom> extractSyntacticModule(Set<OWLEntity> signature){
		return syntaxModExtractor.extract(signature);
	}
	
	private void analysisAxioms(Set<OWLAxiom> axioms){
		HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
		for(OWLAxiom axiom : axioms){
			if(axiom.isLogicalAxiom()){
				String typeName = axiom.getAxiomType().toString();
				if(typeCounts.get(typeName) == null)
					typeCounts.put(typeName, 1);
				else
					typeCounts.put(typeName, typeCounts.get(typeName)+1);
			}
		}
		for(String name : typeCounts.keySet()){
			System.out.println(name + ":" + typeCounts.get(name));
		}
	}

	public static void main(String[] args) {
		OWLOntology nci1 = 
				OntologyLoader.loadOntologyAllAxioms("/users/loco/wgatens/Ontologies/NCI/nci-10.02d.owl");
		DomainRangeExtraction extractor = new DomainRangeExtraction(nci1);
		extractor.performTests();
	}
}
