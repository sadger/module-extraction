package uk.ac.liv.moduleextraction.cycles;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModuleUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class OntologyCycleVerifier {


	TarjanStronglyConnectedComponents tarj;
	Collection<OWLLogicalAxiom> axioms;
	private GraphBuilder.Graph g;
	private GraphBuilder builder;

	public OntologyCycleVerifier(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}

	public OntologyCycleVerifier(Collection<OWLLogicalAxiom> axioms) {
		this.axioms = axioms;
		this.builder = new GraphBuilder();
		g = builder.buildGraph(axioms);
		tarj = new TarjanStronglyConnectedComponents();
		tarj.performTarjan(g);
	}

	public boolean isCyclic() {
		for(HashSet<OWLClass> components : tarj.getStronglyConnectComponents()){
			// If there is a set of strongly connected components > 1 then there must be a cycle
			if(components.size() > 1){
				return true;
			}
		}
		//Or if there exists an axiom which is defined directly in terms of itself
		for(GraphBuilder.Vertex v : g.values()){
			if(v.joinedToSelf){
				return true;
			}
		}
		return false;
	}



	public Set<OWLLogicalAxiom> canWeDoBetter(){

		//Candidates concept names for causing cycles
		Set<Set<OWLClass>> largeSCCs =
				tarj.getStronglyConnectComponents().
						stream().
						filter(x -> x.size() > 1).
						collect(Collectors.toSet());

		//Add all those axioms which use their own definition on the RHS
		Set<OWLLogicalAxiom> cycleCausing = new HashSet<>();
		cycleCausing.addAll(builder.getSelfDefinedAxioms());

		for(OWLLogicalAxiom axiom : axioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
				for(Set<OWLClass> component : largeSCCs){
					if(component.contains(name)){
						OWLClassExpression def = AxiomSplitter.getDefinitionofAxiom(axiom);
						Set<OWLClass> defCls = ModuleUtils.getNamedClassesInSignature(def);
						Set<OWLClass> inter = Sets.intersection(component, defCls);
						if(!inter.isEmpty()){
							cycleCausing.add(axiom);
						}
					}
				}
		}
		return cycleCausing;
	}

	//Concept names candidates for cycle causing
	public HashSet<OWLClass> getCycleCausingNames(){
		HashSet<OWLClass> cycleCausing = new HashSet<>();
		//Belongs to SCC > 1 in size...
		tarj.getStronglyConnectComponents()
				.stream()
				.filter(components -> components.size() > 1)
				.forEach(cycleCausing::addAll);

		//...or defined in terms of self
		for(GraphBuilder.Vertex v : g.values()){
			if(v.joinedToSelf){
				cycleCausing.add(v.value);
			}
		}
		return cycleCausing;
	}

	public Set<OWLLogicalAxiom> getNaiveCycleCausingAxioms(){
		Set<OWLLogicalAxiom> cycleCausing = new HashSet<>();
		Set<OWLClass> cycleCausingNames = getCycleCausingNames();
		for(OWLLogicalAxiom axiom : axioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(cycleCausingNames.contains(name)){
				cycleCausing.add(axiom);
			}
		}

		return cycleCausing;
	}

	public Set<OWLLogicalAxiom> getCycleCausingAxioms(){
		return canWeDoBetter();
	}

}


